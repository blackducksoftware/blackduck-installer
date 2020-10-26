/**
 * blackduck-installer
 *
 * Copyright (c) 2020 Synopsys, Inc.
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package com.synopsys.integration.blackduck.installer.dockerswarm.configfile.parser;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.apache.commons.io.IOUtils;

import com.synopsys.integration.blackduck.installer.dockerswarm.configfile.model.CustomYamlFile;
import com.synopsys.integration.blackduck.installer.dockerswarm.configfile.model.CustomYamlLine;
import com.synopsys.integration.blackduck.installer.dockerswarm.configfile.model.DockerGlobalSecret;
import com.synopsys.integration.blackduck.installer.dockerswarm.configfile.model.Section;
import com.synopsys.integration.blackduck.installer.dockerswarm.configfile.model.ServiceEnvironmentSection;
import com.synopsys.integration.blackduck.installer.dockerswarm.configfile.model.ServiceSecretsSection;
import com.synopsys.integration.blackduck.installer.dockerswarm.edit.ConfigFile;
import com.synopsys.integration.blackduck.installer.exception.BlackDuckInstallerException;

public class FileParser {
    private static final Set<String> DOCKER_RESERVED_KEYS = Set.of(
        "constraints", "deploy", "environment", "external", "limits", "mode", "name", "placement", "replicas", "reservations", "resources",
        "secrets", "services", "source", "target", "version");
    private String stackName;
    private String stackReplacementToken;

    public FileParser(String stackName, String stackReplacementToken) {
        this.stackName = stackName;
        this.stackReplacementToken = stackReplacementToken;
    }

    protected CustomYamlFile createYamlFileModel(List<String> lines) {
        CustomYamlFile yamlFile = new CustomYamlFile();

        boolean inServices = false;
        boolean inGlobalSecrets = false;
        boolean inServiceSecrets = false;
        DockerGlobalSecret currentGlobalSecret = null;
        Section currentSection = null;
        Section currentService = null;
        Section servicesSection = null;

        int count = lines.size();
        for (int index = 0; index < count; index++) {
            String line = lines.get(index);
            boolean isCommented = CustomYamlLine.isCommented(line);
            CustomYamlLine customYamlLine = CustomYamlLine.create(isCommented, index, line);
            yamlFile.addLine(customYamlLine);
            if (line.contains("services:")) {
                inServices = true;
                inGlobalSecrets = false;
                currentSection = new Section("services", customYamlLine);
                yamlFile.addModifiableSection(currentSection);
                servicesSection = currentSection;
            } else if (inServices) {
                boolean processingSection = null != currentSection;
                Optional<String> sectionKey = calculateSectionKey(isCommented, line);

                if (processingSection && inServiceSecrets && line.equals("#secrets:")) {
                    inGlobalSecrets = true;
                    inServices = false;
                    inServiceSecrets = false;
                    yamlFile.createGlobalSecrets(customYamlLine);
                } else if (processingSection && line.trim().contains("environment:")) {
                    inServiceSecrets = false;
                    Section environmentSection = new ServiceEnvironmentSection("environment", customYamlLine);
                    environmentSection.setIndentation(Section.SERVICE_SUB_SECTION_INDENTATION);
                    currentService.addSubSection(environmentSection);
                    currentSection = environmentSection;
                } else if (processingSection && line.trim().contains("secrets:")) {
                    inServiceSecrets = true;
                    Section secretsSection = new ServiceSecretsSection("secrets", customYamlLine);
                    secretsSection.setIndentation(Section.SERVICE_SUB_SECTION_INDENTATION);
                    currentService.addSubSection(secretsSection);
                    currentSection = secretsSection;
                } else if (sectionKey.isPresent()) {
                    Section newSection = new Section(sectionKey.get(), customYamlLine);
                    newSection.setIndentation(Section.SERVICE_SECTION_INDENTATION);
                    if (processingSection) {
                        servicesSection.addSubSection(newSection);
                    }
                    inServiceSecrets = false;
                    currentSection = newSection;
                    currentService = newSection;
                } else {
                    currentSection.addLine(customYamlLine);
                }
            } else if (inGlobalSecrets) {
                if (line.trim().contains("external:")) {
                    currentGlobalSecret.applyExternal(customYamlLine);
                } else if (line.trim().contains("name:")) {
                    currentGlobalSecret.applyName(customYamlLine, stackReplacementToken);
                } else {
                    currentGlobalSecret = DockerGlobalSecret.of(getStackName(), customYamlLine);
                    yamlFile.addDockerSecret(currentGlobalSecret);
                }
            }
        }
        return yamlFile;
    }

    private Optional<String> calculateSectionKey(boolean isCommented, String line) {
        // check for potential start of service name
        if (line.contains(":")) {
            int colonIndex = line.indexOf(":");
            String potentialServiceName = line.substring(0, colonIndex);
            if (isCommented) {
                potentialServiceName = potentialServiceName.replaceFirst(CustomYamlLine.YAML_COMMENT_REGEX, "");
            }
            potentialServiceName = potentialServiceName.trim();

            if (!CustomYamlLine.isCommented(potentialServiceName) && !potentialServiceName.startsWith("-") && !DOCKER_RESERVED_KEYS.contains(potentialServiceName)) {
                return Optional.ofNullable(potentialServiceName);
            }
        }
        return Optional.empty();
    }

    public CustomYamlFile parse(ConfigFile configFile) throws BlackDuckInstallerException {
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
