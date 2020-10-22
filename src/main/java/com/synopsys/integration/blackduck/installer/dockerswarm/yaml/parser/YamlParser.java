package com.synopsys.integration.blackduck.installer.dockerswarm.yaml.parser;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

import org.apache.commons.io.IOUtils;

import com.synopsys.integration.blackduck.installer.dockerswarm.edit.ConfigFile;
import com.synopsys.integration.blackduck.installer.dockerswarm.yaml.model.DockerGlobalSecret;
import com.synopsys.integration.blackduck.installer.dockerswarm.yaml.model.DockerService;
import com.synopsys.integration.blackduck.installer.dockerswarm.yaml.model.YamlFile;
import com.synopsys.integration.blackduck.installer.dockerswarm.yaml.model.YamlLine;
import com.synopsys.integration.blackduck.installer.exception.BlackDuckInstallerException;

public class YamlParser {
    private String stackName;
    private ConfigFile configFile;

    public YamlParser(final String stackName, final ConfigFile configFile) {
        this.stackName = stackName;
        this.configFile = configFile;
    }

    public YamlFile parse() throws BlackDuckInstallerException {
        try (InputStream inputStream = new FileInputStream(configFile.getOriginalCopy())) {
            List<String> lines = IOUtils.readLines(inputStream, StandardCharsets.UTF_8);
            return createYamlFileModel(lines);
        } catch (IOException e) {
            throw new BlackDuckInstallerException("Error editing local overrides: " + e.getMessage());
        }
    }

    private YamlFile createYamlFileModel(List<String> lines) {
        YamlFile yamlFile = new YamlFile();

        boolean inServices = false;
        boolean inServiceEnvironment = false;
        boolean inServiceSecrets = false;
        boolean inGlobalSecrets = false;
        DockerService currentService = null;
        DockerGlobalSecret currentGlobalSecret = null;

        for (String line : lines) {
            if (!inServices && line.startsWith("version:")) {
                yamlFile.setVersion(line.replace("#", "")
                                        .replace("version:", "")
                                        .trim());
            } else if (line.startsWith("services:")) {
                inServices = true;
                inGlobalSecrets = false;
            } else if (inServices) {
                boolean processingService = null != currentService;
                String serviceName = null;
                boolean isServiceName = false;
                // check for potential start of service name
                if (line.contains(":")) {
                    if (line.trim().equals("#  alert:")) {
                        isServiceName = true;
                        serviceName = "alert";
                    } else if (line.trim().equals("alertdb:")) {
                        isServiceName = true;
                        serviceName = "alertdb";
                    }
                }

                if (processingService && line.trim().contains("environment:")) {
                    inServiceEnvironment = true;
                    inServiceSecrets = false;
                } else if (processingService && line.trim().equals("#    secrets:")) {
                    inServiceEnvironment = false;
                    inServiceSecrets = true;
                } else if (isServiceName) {
                    if (processingService) {
                        yamlFile.addService(currentService);
                    }
                    inServiceEnvironment = false;
                    inServiceSecrets = false;
                    currentService = new DockerService(serviceName);
                } else if (inServices && line.equals("#secrets:")) {
                    inGlobalSecrets = true;
                    inServices = false;
                    inServiceEnvironment = false;
                    inServiceSecrets = false;
                    yamlFile.addService(currentService);
                } else if (processingService && inServiceEnvironment) {
                    currentService.addEnvironmentVariable(line);
                } else if (processingService && inServiceSecrets) {
                    currentService.addSecret(line);
                } else {
                    currentService.addCommentBeforeSection(line);
                }
            } else if (inGlobalSecrets) {
                if (line.trim().contains("external:")) {
                    currentGlobalSecret.applyExternal(line);
                } else if (line.trim().contains("name:")) {
                    currentGlobalSecret.applyName(line, "<STACK_NAME>_");
                } else {
                    currentGlobalSecret = DockerGlobalSecret.of(stackName, line);
                    yamlFile.addDockerSecret(currentGlobalSecret);
                }
            } else {
                yamlFile.addCommentBeforeVersion(YamlLine.create(line));
            }
        }
        return yamlFile;
    }
}
