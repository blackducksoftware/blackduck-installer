/**
 * blackduck-installer
 *
 * Copyright (c) 2021 Synopsys, Inc.
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
import java.io.IOException;
import java.io.Writer;
import java.util.Optional;
import java.util.Set;

import com.synopsys.integration.blackduck.installer.dockerswarm.configfile.model.CustomYamlFile;
import com.synopsys.integration.blackduck.installer.dockerswarm.configfile.model.GlobalSecrets;
import com.synopsys.integration.blackduck.installer.dockerswarm.configfile.model.Section;
import com.synopsys.integration.blackduck.installer.dockerswarm.configfile.model.ServiceSecretsSection;
import com.synopsys.integration.blackduck.installer.dockerswarm.configfile.output.FileWriter;
import com.synopsys.integration.blackduck.installer.dockerswarm.configfile.output.LineWriter;
import com.synopsys.integration.blackduck.installer.dockerswarm.configfile.parser.FileParser;
import com.synopsys.integration.blackduck.installer.exception.BlackDuckInstallerException;
import com.synopsys.integration.blackduck.installer.hash.HashUtility;
import com.synopsys.integration.blackduck.installer.hash.PreComputedHashes;
import com.synopsys.integration.blackduck.installer.model.CustomCertificate;
import com.synopsys.integration.log.IntLogger;

public class BlackDuckLocalOverridesEditor extends ConfigFileEditor {
    private final String stackName;
    private final boolean shouldEditFile;
    private FileParser fileParser;
    private CustomCertificate customCertificate;

    public BlackDuckLocalOverridesEditor(IntLogger logger, HashUtility hashUtility, String lineSeparator, String stackName, boolean useLocalOverrides, CustomCertificate customCertificate) {
        super(logger, hashUtility, lineSeparator);

        this.stackName = stackName;
        shouldEditFile = useLocalOverrides;
        fileParser = new FileParser(stackName, "hub_");
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

        CustomYamlFile yamlFileModel = fileParser.parse(configFile);
        updateValues(yamlFileModel);
        try (Writer writer = new java.io.FileWriter(configFile.getFileToEdit())) {
            LineWriter lineWriter = new LineWriter(writer, lineSeparator);
            FileWriter.write(lineWriter, yamlFileModel);
        } catch (IOException e) {
            throw new BlackDuckInstallerException("Error editing local overrides: " + e.getMessage());
        }
    }

    private void updateValues(CustomYamlFile parsedFile) throws BlackDuckInstallerException {

        Optional<Section> webServerSection = parsedFile.getModifiableSection("services")
                                                 .flatMap(servicesSection -> servicesSection.getSubSection("webserver"));
        if (webServerSection.isEmpty()) {
            throw new BlackDuckInstallerException("webserver service missing from overrides file.");
        }

        Section webserver = webServerSection.get();
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
            webServerSecrets.uncommentIfPresent("WEBSERVER_CUSTOM_CERT_FILE");
            webServerSecrets.uncommentIfPresent("WEBSERVER_CUSTOM_KEY_FILE");
            globalSecrets.uncomment();
            globalSecrets.uncommentIfPresent("WEBSERVER_CUSTOM_CERT_FILE");
            globalSecrets.uncommentIfPresent("WEBSERVER_CUSTOM_KEY_FILE");
        }
    }
}
