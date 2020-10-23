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
package com.synopsys.integration.blackduck.installer.dockerswarm.edit;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.Optional;
import java.util.Set;

import com.synopsys.integration.blackduck.installer.dockerswarm.yaml.model.GlobalSecrets;
import com.synopsys.integration.blackduck.installer.dockerswarm.yaml.model.ServiceSecretsSection;
import com.synopsys.integration.blackduck.installer.dockerswarm.yaml.model.YamlBlock;
import com.synopsys.integration.blackduck.installer.dockerswarm.yaml.model.YamlFile;
import com.synopsys.integration.blackduck.installer.dockerswarm.yaml.model.YamlSection;
import com.synopsys.integration.blackduck.installer.dockerswarm.yaml.model.YamlTextLine;
import com.synopsys.integration.blackduck.installer.dockerswarm.yaml.output.YamlFileWriter;
import com.synopsys.integration.blackduck.installer.dockerswarm.yaml.output.YamlWriter;
import com.synopsys.integration.blackduck.installer.dockerswarm.yaml.parser.YamlParser;
import com.synopsys.integration.blackduck.installer.exception.BlackDuckInstallerException;
import com.synopsys.integration.blackduck.installer.hash.HashUtility;
import com.synopsys.integration.blackduck.installer.hash.PreComputedHashes;
import com.synopsys.integration.blackduck.installer.model.CustomCertificate;
import com.synopsys.integration.log.IntLogger;

public class BlackDuckLocalOverridesEditor extends ConfigFileEditor {
    private final String stackName;
    private final boolean shouldEditFile;
    private YamlParser yamlParser;
    private CustomCertificate customCertificate;

    public BlackDuckLocalOverridesEditor(IntLogger logger, HashUtility hashUtility, String lineSeparator, String stackName, boolean useLocalOverrides, CustomCertificate customCertificate) {
        super(logger, hashUtility, lineSeparator);

        this.stackName = stackName;
        shouldEditFile = useLocalOverrides;
        yamlParser = new YamlParser(stackName, "hub_");
        this.customCertificate = customCertificate;
    }

    public String getFilename() {
        return "docker-compose.local-overrides.yml";
    }

    @Override
    public Set<String> getSupportedComputedHashes() {
        return PreComputedHashes.DOCKER_COMPOSE_LOCAL_OVERRIDES_YML;
    }

    public void edit(File installDirectory) throws BlackDuckInstallerException {
        ConfigFile configFile = createConfigFile(installDirectory);
        if (!shouldEditFile)
            return;

        YamlFile yamlFileModel = yamlParser.parse(configFile);
        updateValues(yamlFileModel);
        try (Writer writer = new FileWriter(configFile.getFileToEdit())) {
            YamlWriter yamlWriter = new YamlWriter(writer, lineSeparator);
            YamlFileWriter.write(yamlWriter, yamlFileModel);
        } catch (IOException e) {
            throw new BlackDuckInstallerException("Error editing local overrides: " + e.getMessage());
        }
    }

    private void updateValues(YamlFile parsedFile) throws BlackDuckInstallerException {

        Optional<YamlSection> webServerSection = parsedFile.getModifiableSection("services")
                                                     .flatMap(servicesSection -> servicesSection.getSubSection("webserver"));
        if (webServerSection.isEmpty()) {
            throw new BlackDuckInstallerException("webserver service missing from overrides file.");
        }

        YamlSection webserver = webServerSection.get();
        Optional<ServiceSecretsSection> webserverSecretsSection = webserver.getSubSection("secrets");
        GlobalSecrets globalSecrets = parsedFile.getGlobalSecrets();
        // exit if we don't have the section we need. Write the file content as is.
        if (webserverSecretsSection.isEmpty()) {
            return;
        }

        ServiceSecretsSection webServerSecrets = webserverSecretsSection.get();

        if (!customCertificate.isEmpty()) {
            webserver.uncomment();
            webServerSecrets.uncomment();
            webServerSecrets.getSecret("WEBSERVER_CUSTOM_CERT_FILE").ifPresent(YamlTextLine::uncomment);
            webServerSecrets.getSecret("WEBSERVER_CUSTOM_KEY_FILE").ifPresent(YamlTextLine::uncomment);
            globalSecrets.uncomment();
            globalSecrets.getSecret("WEBSERVER_CUSTOM_CERT_FILE").ifPresent(YamlBlock::uncommentBlock);
            globalSecrets.getSecret("WEBSERVER_CUSTOM_KEY_FILE").ifPresent(YamlBlock::uncommentBlock);
        }
    }
}
