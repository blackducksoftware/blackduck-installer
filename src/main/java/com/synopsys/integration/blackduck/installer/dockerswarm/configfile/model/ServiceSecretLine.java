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

public class ServiceSecretLine implements CustomYamlTextLine {
    private String key;
    private CustomYamlLine customYamlLine;

    private ServiceSecretLine(String key, CustomYamlLine customYamlLine) {
        this.key = key;
        this.customYamlLine = customYamlLine;
    }

    public static ServiceSecretLine of(CustomYamlLine line) {
        boolean commented = CustomYamlLine.isCommented(line.getFormattedText());
        if (commented) {
            line.comment();
        } else {
            line.uncomment();
        }
        String rawText = line.getFormattedText();
        int hyphenIndex = rawText.indexOf("- ");
        String key = null;
        if (hyphenIndex > 0) {
            // prevent the strings "-- " from being considered a valid secret.
            if ('-' != rawText.charAt(hyphenIndex - 1)) {
                key = rawText.substring(hyphenIndex + 1).trim();
            }
        }
        ServiceSecretLine secretLine = new ServiceSecretLine(key, line);
        if (secretLine.hasKey()) {
            secretLine.getYamlLine().setCurrentRawText(String.format("      - %s", secretLine.getKey()));
        }
        return secretLine;
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
}
