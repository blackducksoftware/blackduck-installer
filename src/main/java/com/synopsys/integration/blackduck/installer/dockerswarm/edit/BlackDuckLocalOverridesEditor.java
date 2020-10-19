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
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Set;

import org.apache.commons.io.IOUtils;

import com.synopsys.integration.blackduck.installer.exception.BlackDuckInstallerException;
import com.synopsys.integration.blackduck.installer.hash.HashUtility;
import com.synopsys.integration.blackduck.installer.hash.PreComputedHashes;
import com.synopsys.integration.log.IntLogger;

public class BlackDuckLocalOverridesEditor extends ConfigFileEditor {
    private final String stackName;
    private final boolean shouldEditFile;

    public BlackDuckLocalOverridesEditor(IntLogger logger, HashUtility hashUtility, String lineSeparator, String stackName, boolean useLocalOverrides) {
        super(logger, hashUtility, lineSeparator);

        this.stackName = stackName;
        shouldEditFile = useLocalOverrides;
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

        try (InputStream inputStream = new FileInputStream(configFile.getOriginalCopy())) {
            List<String> lines = IOUtils.readLines(inputStream, StandardCharsets.UTF_8);

            try (Writer writer = new FileWriter(configFile.getFileToEdit())) {
                boolean inServices = false;
                boolean inWebServer = false;
                boolean inWebServerSecrets = false;
                int secretsCount = 0;
                boolean inSecrets = false;
                boolean inCustom = false;

                for (String line : lines) {
                    if (line.startsWith("services:")) {
                        inServices = true;
                        writeLine(writer, line);
                    } else if (inServices && line.trim().equals("#webserver:")) {
                        inWebServer = true;
                        uncommentLine(writer, line);
                    } else if (inWebServer && line.trim().equals("#secrets:")) {
                        inWebServerSecrets = true;
                        uncommentLine(writer, line);
                    } else if (inWebServerSecrets && (line.trim().endsWith("WEBSERVER_CUSTOM_CERT_FILE") || line.trim().endsWith("WEBSERVER_CUSTOM_KEY_FILE"))) {
                        uncommentLine(writer, line);
                        secretsCount++;
                        if (secretsCount >= 2) {
                            inWebServerSecrets = false;
                            inWebServer = false;
                            inServices = false;
                        }
                    } else if (!inServices && line.equals("#secrets:")) {
                        inSecrets = true;
                        uncommentLine(writer, line);
                    } else if (inSecrets && line.trim().endsWith("WEBSERVER_CUSTOM_CERT_FILE:")) {
                        inCustom = true;
                        uncommentLine(writer, line);
                    } else if (inSecrets && line.trim().endsWith("WEBSERVER_CUSTOM_KEY_FILE:")) {
                        inCustom = true;
                        uncommentLine(writer, line);
                    } else if (inCustom && line.contains("name: ")) {
                        inCustom = false;
                        String fixedLine = line.replace("hub_", stackName + "_");
                        uncommentLine(writer, fixedLine);
                    } else if (inCustom) {
                        uncommentLine(writer, line);
                    } else {
                        writeLine(writer, line);
                    }
                }
            }
        } catch (IOException e) {
            throw new BlackDuckInstallerException("Error editing local overrides: " + e.getMessage());
        }
    }

    private void writeLine(Writer writer, String line) throws IOException {
        writer.append(line + lineSeparator);
    }

    private void uncommentLine(Writer writer, String line) throws IOException {
        writer.append(line.replace("#", "") + lineSeparator);
    }

}
