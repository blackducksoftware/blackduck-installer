package com.synopsys.integration.blackduck.installer.dockerswarm.yaml.parser;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Set;

import org.apache.commons.io.IOUtils;

import com.synopsys.integration.blackduck.installer.dockerswarm.edit.ConfigFile;
import com.synopsys.integration.blackduck.installer.dockerswarm.yaml.model.DefaultSection;
import com.synopsys.integration.blackduck.installer.dockerswarm.yaml.model.DockerGlobalSecret;
import com.synopsys.integration.blackduck.installer.dockerswarm.yaml.model.ServiceEnvironmentSection;
import com.synopsys.integration.blackduck.installer.dockerswarm.yaml.model.ServiceSecretsSection;
import com.synopsys.integration.blackduck.installer.dockerswarm.yaml.model.YamlFile;
import com.synopsys.integration.blackduck.installer.dockerswarm.yaml.model.YamlLine;
import com.synopsys.integration.blackduck.installer.dockerswarm.yaml.model.YamlSection;
import com.synopsys.integration.blackduck.installer.exception.BlackDuckInstallerException;

public class YamlParser {
    private static final Set<String> DOCKER_RESERVED_KEYS = Set.of(
        "constraints", "deploy", "environment", "external", "limits", "mode", "name", "placement", "replicas", "reservations", "resources",
        "secrets", "services", "source", "target", "version");
    private String stackName;
    private String stackReplacementToken;

    public YamlParser(String stackName, String stackReplacementToken) {
        this.stackName = stackName;
        this.stackReplacementToken = stackReplacementToken;
    }

    protected YamlFile createYamlFileModel(List<String> lines) {
        YamlFile yamlFile = new YamlFile();

        boolean inServices = false;
        boolean inGlobalSecrets = false;
        boolean inServiceSecrets = false;
        DockerGlobalSecret currentGlobalSecret = null;
        YamlSection currentSection = null;
        YamlSection currentService = null;
        YamlSection servicesSection = null;

        int count = lines.size();
        for (int index = 0; index < count; index++) {
            String line = lines.get(index);
            boolean isCommented = YamlLine.isCommented(line);
            YamlLine yamlLine = YamlLine.create(isCommented, index, line);
            yamlFile.addLine(yamlLine);
            if (line.contains("services:")) {
                inServices = true;
                inGlobalSecrets = false;
                currentSection = new DefaultSection("services", yamlLine);
                yamlFile.addModifiableSection(currentSection);
                servicesSection = currentSection;
            } else if (inServices) {
                boolean processingSection = null != currentSection;
                String sectionKey = null;
                boolean isSectionKey = false;
                // check for potential start of service name
                if (line.contains(":")) {
                    int colonIndex = line.indexOf(":");
                    String potentialServiceName = line.substring(0, colonIndex);
                    if (isCommented) {
                        potentialServiceName = potentialServiceName.replaceFirst(YamlLine.YAML_COMMENT_REGEX, "");
                    }
                    potentialServiceName = potentialServiceName.trim();

                    if (!YamlLine.isCommented(potentialServiceName) && !potentialServiceName.startsWith("-") && !DOCKER_RESERVED_KEYS.contains(potentialServiceName)) {
                        sectionKey = potentialServiceName;
                        isSectionKey = true;
                    }
                }

                if (processingSection && inServiceSecrets && line.equals("#secrets:")) {
                    inGlobalSecrets = true;
                    inServices = false;
                    inServiceSecrets = false;
                    yamlFile.createGlobalSecrets(yamlLine);
                } else if (processingSection && line.trim().contains("environment:")) {
                    inServiceSecrets = false;
                    DefaultSection environmentSection = new ServiceEnvironmentSection("environment", yamlLine);
                    environmentSection.setIndentation(DefaultSection.SERVICE_SUB_SECTION_INDENTATION);
                    currentService.addSubSection(environmentSection);
                    currentSection = environmentSection;
                } else if (processingSection && line.trim().contains("secrets:")) {
                    inServiceSecrets = true;
                    DefaultSection secretsSection = new ServiceSecretsSection("secrets", yamlLine);
                    secretsSection.setIndentation(DefaultSection.SERVICE_SUB_SECTION_INDENTATION);
                    currentService.addSubSection(secretsSection);
                    currentSection = secretsSection;
                } else if (isSectionKey) {
                    DefaultSection newSection = new DefaultSection(sectionKey, yamlLine);
                    newSection.setIndentation(DefaultSection.SERVICE_SECTION_INDENTATION);
                    if (processingSection) {
                        servicesSection.addSubSection(newSection);
                    }
                    inServiceSecrets = false;
                    currentSection = newSection;
                    currentService = newSection;
                } else {
                    currentSection.addLine(yamlLine);
                }
            } else if (inGlobalSecrets) {
                if (line.trim().contains("external:")) {
                    currentGlobalSecret.applyExternal(yamlLine);
                } else if (line.trim().contains("name:")) {
                    currentGlobalSecret.applyName(yamlLine, stackReplacementToken);
                } else {
                    currentGlobalSecret = DockerGlobalSecret.of(getStackName(), yamlLine);
                    yamlFile.addDockerSecret(currentGlobalSecret);
                }
            }
        }
        return yamlFile;
    }

    public YamlFile parse(ConfigFile configFile) throws BlackDuckInstallerException {
        try (InputStream inputStream = new FileInputStream(configFile.getOriginalCopy())) {
            List<String> lines = IOUtils.readLines(inputStream, StandardCharsets.UTF_8);
            return createYamlFileModel(lines);
        } catch (IOException e) {
            throw new BlackDuckInstallerException("Error editing local overrides: " + e.getMessage());
        }
    }

    public String getStackName() {
        return stackName;
    }
}
