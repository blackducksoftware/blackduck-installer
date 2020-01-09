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
import com.synopsys.integration.log.IntLogger;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;

public abstract class ConfigFileEditor {
    private IntLogger logger;
    private HashUtility hashUtility;

    protected String lineSeparator;

    public ConfigFileEditor(IntLogger logger, HashUtility hashUtility, String lineSeparator) {
        this.logger = logger;
        this.hashUtility = hashUtility;
        this.lineSeparator = lineSeparator;
    }

    public abstract String getFilename();

    public abstract String getComputedHash();

    public abstract void edit(File installDirectory) throws BlackDuckInstallerException;

    protected ConfigFile createConfigFile(File installDirectory) throws BlackDuckInstallerException {
        File dockerSwarm = new File(installDirectory, "docker-swarm");
        File original = new File(dockerSwarm, getFilename());
        File originalCopy = copyOriginalIfNeeded(original);

        String currentHash = hashUtility.computeHash(originalCopy);
        if (!currentHash.equals(getComputedHash())) {
            logger.warn(String.format("The file '%s' is different than expected - it may not have been automatically edited correctly. Please double-check this file for any errors.", original.getAbsolutePath()));
        }

        return new ConfigFile(original, originalCopy);
    }

    private File copyOriginalIfNeeded(File original) throws BlackDuckInstallerException {
        File originalCopy = new File(original.getParent(), original.getName() + ".orig");
        if (!originalCopy.exists()) {
            try {
                FileUtils.copyFile(original, originalCopy);
            } catch (IOException e) {
                throw new BlackDuckInstallerException("Could not copy the file to edit: " + e.getMessage());
            }
        }

        return originalCopy;
    }

}
