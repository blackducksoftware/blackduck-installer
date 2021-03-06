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

import com.synopsys.integration.blackduck.installer.exception.BlackDuckInstallerException;
import com.synopsys.integration.blackduck.installer.hash.HashUtility;
import com.synopsys.integration.blackduck.installer.model.ConfigProperties;
import com.synopsys.integration.blackduck.installer.model.ConfigProperty;
import com.synopsys.integration.log.IntLogger;
import org.apache.commons.lang3.StringUtils;

import java.io.*;
import java.util.List;
import java.util.Map;

public abstract class PropertyFileEditor extends ConfigFileEditor {
    public PropertyFileEditor(IntLogger logger, HashUtility hashUtility, String lineSeparator) {
        super(logger, hashUtility, lineSeparator);
    }

    protected void writeLine(Writer writer, ConfigProperty configProperty) throws BlackDuckInstallerException {
        try {
            writer.append(configProperty.toConfigString() + lineSeparator);
        } catch (IOException e) {
            throw new BlackDuckInstallerException("Error writing line: " + e.getMessage(), e);
        }
    }

    protected void writeBlank(Writer writer) throws BlackDuckInstallerException {
        try {
            writer.append(lineSeparator);
        } catch (IOException e) {
            throw new BlackDuckInstallerException("Could not write blank line: " + e.getMessage(), e);
        }
    }

    protected void writeLinesWithTokenValues(Writer writer, ConfigProperties configProperties, File originalCopy) throws BlackDuckInstallerException {
        try (BufferedReader reader = new BufferedReader(new FileReader(originalCopy))) {
            String line = reader.readLine();
            while (null != line) {
                writeLineWithTokenValues(writer, configProperties, line);
                line = reader.readLine();
            }
        } catch (IOException e) {
            throw new BlackDuckInstallerException(String.format("Error reading original copy %s", originalCopy.getAbsolutePath()), e);
        }
    }

    protected void writeLineWithTokenValues(Writer writer, ConfigProperties configProperties, String line) throws BlackDuckInstallerException {
        String fixedLine = fixLine(configProperties, line);
        try {
            writer.append(fixedLine + lineSeparator);
        } catch (IOException e) {
            throw new BlackDuckInstallerException("Error editing line: " + e.getMessage(), e);
        }
    }

    protected void addTokenIfApplicable(ConfigProperties configProperties, String key, String value) {
        if (StringUtils.isNotBlank(value)) {
            configProperties.put(key, value);
        }
    }

    protected void addTokenIfApplicable(ConfigProperties configProperties, String key, int value) {
        if (value > 0) {
            configProperties.put(key, Integer.toString(value));
        }
    }

    private String fixLine(ConfigProperties configProperties, String line) {
        for (String token : configProperties.keySet()) {
            if (line.startsWith(token)) {
                return configProperties.get(token).toConfigString();
            }
        }

        return line;
    }

}
