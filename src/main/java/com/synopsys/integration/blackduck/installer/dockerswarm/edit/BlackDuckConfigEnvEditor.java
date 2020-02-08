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

import com.synopsys.integration.blackduck.installer.exception.BlackDuckInstallerException;
import com.synopsys.integration.blackduck.installer.hash.HashUtility;
import com.synopsys.integration.blackduck.installer.hash.PreComputedHashes;
import com.synopsys.integration.blackduck.installer.model.ConfigProperties;
import com.synopsys.integration.blackduck.installer.model.ConfigProperty;
import com.synopsys.integration.blackduck.installer.model.FileLoadedProperties;
import com.synopsys.integration.log.IntLogger;

import java.io.*;
import java.util.Set;

public class BlackDuckConfigEnvEditor extends PropertyFileEditor {
    public static final String FILENAME = "blackduck-config.env";

    private ConfigProperties toAdd = new ConfigProperties();
    private ConfigProperties toEdit = new ConfigProperties();

    public BlackDuckConfigEnvEditor(IntLogger logger, HashUtility hashUtility, String lineSeparator, FileLoadedProperties blackDuckConfigEnvLoadedProperties) {
        super(logger, hashUtility, lineSeparator);

        blackDuckConfigEnvLoadedProperties.getToAdd().stream().forEach(toAdd::add);

        blackDuckConfigEnvLoadedProperties.getToEdit().stream().forEach(toEdit::add);
    }

    public String getFilename() {
        return FILENAME;
    }

    @Override
    public Set<String> getSupportedComputedHashes() {
        return PreComputedHashes.BLACKDUCK_CONFIG_ENV;
    }

    public void edit(File installDirectory) throws BlackDuckInstallerException {
        ConfigFile configFile = createConfigFile(installDirectory);

        try (Writer writer = new BufferedWriter(new FileWriter(configFile.getFileToEdit()))) {
            for (ConfigProperty configProperty : toAdd) {
                writeLine(writer, configProperty);
            }
            writeBlank(writer);

            writeLinesWithTokenValues(writer, toEdit, configFile.getOriginalCopy());
        } catch (IOException e) {
            throw new BlackDuckInstallerException(String.format("Error writing file %s", configFile.getFileToEdit().getAbsolutePath()), e);
        }
    }

}
