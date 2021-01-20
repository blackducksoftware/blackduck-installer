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
package com.synopsys.integration.blackduck.installer.dockerswarm.configfile.model;

import org.apache.commons.lang3.StringUtils;

public class ServiceEnvironmentLine implements CustomYamlTextLine {
    private final String key;
    private String value;
    private CustomYamlLine customYamlLine;

    private ServiceEnvironmentLine(String key, String value, CustomYamlLine customYamlLine) {
        this.key = key;
        this.value = value;
        this.customYamlLine = customYamlLine;
    }

    public static ServiceEnvironmentLine newEnvironmentLine(int lineNumber, String key) {
        // create a commented variable.
        return of(CustomYamlLine.create(lineNumber, String.format("#      - %s=", key)));
    }

    public static ServiceEnvironmentLine of(CustomYamlLine line) {
        boolean commented = CustomYamlLine.isCommented(line.getFormattedText());
        if (commented) {
            line.comment();
        } else {
            line.uncomment();
        }
        String rawText = line.getFormattedText();
        int hyphenIndex = rawText.indexOf("-");
        int equalsIndex = rawText.indexOf("=");
        String key = null;
        String value = null;
        if (hyphenIndex >= 0 && equalsIndex > 0) {
            key = rawText.substring(hyphenIndex + 1, equalsIndex).trim();
        }
        if (equalsIndex > 0) {
            value = rawText.substring(equalsIndex + 1).trim();
        }
        return new ServiceEnvironmentLine(key, value, line);
    }

    public CustomYamlLine getYamlLine() {
        return customYamlLine;
    }

    @Override
    public boolean isCommented() {
        return customYamlLine.isCommented();
    }

    @Override
    public void comment() {
        customYamlLine.comment();
    }

    @Override
    public void uncomment() {
        customYamlLine.uncomment();
    }

    public boolean hasKey() {
        return StringUtils.isNotBlank(key);
    }

    public String getKey() {
        return key;
    }

    public String getValue() {
        return value;
    }

    public void setValue(final String value) {
        this.value = value;
        if (hasKey()) {
            customYamlLine.setCurrentRawText(String.format("      - %s=%s", getKey(), getValue()));
        }
    }
}
