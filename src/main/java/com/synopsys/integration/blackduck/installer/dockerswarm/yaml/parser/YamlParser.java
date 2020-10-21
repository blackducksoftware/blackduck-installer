package com.synopsys.integration.blackduck.installer.dockerswarm.yaml.parser;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

import org.apache.commons.io.IOUtils;

import com.synopsys.integration.blackduck.installer.dockerswarm.edit.ConfigFile;
import com.synopsys.integration.blackduck.installer.dockerswarm.yaml.model.DockerSecret;
import com.synopsys.integration.blackduck.installer.dockerswarm.yaml.model.DockerService;
import com.synopsys.integration.blackduck.installer.dockerswarm.yaml.model.YamlFile;
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
        DockerSecret currentGlobalSecret = null;

        for (String line : lines) {
            boolean processingService = null != currentService;
            // FIXME: Preserve any comments before the version.
            if (!inServices && line.startsWith("version:")) {
                yamlFile.setVersion(line.replace("#", "")
                                        .replace("version:", "")
                                        .trim());
            } else if (line.startsWith("services:")) {
                inServices = true;
            } // FIXME: Determine if we have a service name
            else if (inServices && line.trim().equals("alertdb:")) {
                if (processingService) {
                    yamlFile.addService(currentService);
                }
                inServiceEnvironment = false;
                inServiceSecrets = false;
                currentService = new DockerService("alertdb");
            } else if (processingService && line.trim().contains("environment:")) {
                inServiceEnvironment = true;
                inServiceSecrets = false;
            } else if (processingService && line.trim().equals("#    secrets:")) {
                inServiceEnvironment = false;
                inServiceSecrets = true;
            } // FIXME: Remove this specific case if we are already in a service section.
            else if (inServices && line.trim().equals("#  alert:")) {
                if (processingService) {
                    yamlFile.addService(currentService);
                }
                inServiceEnvironment = false;
                inServiceSecrets = false;
                currentService = new DockerService("alert");
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
            } else if (inServices) {
                currentService.addCommentBeforeSection(line);
            } else if (inGlobalSecrets) {
                if (line.trim().contains("external:")) {
                    currentGlobalSecret.applyExternal(line);
                } else if (line.trim().contains("name:")) {
                    currentGlobalSecret.applyName(line, "<STACK_NAME>_");
                } else {
                    currentGlobalSecret = DockerSecret.of(stackName, line);
                    yamlFile.addDockerSecret(currentGlobalSecret);
                }
            }
        }
        return yamlFile;
    }
}
