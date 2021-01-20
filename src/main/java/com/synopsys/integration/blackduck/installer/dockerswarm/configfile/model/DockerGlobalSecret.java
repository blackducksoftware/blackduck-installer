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

public class DockerGlobalSecret implements CustomYamlBlock {
    private String key;
    private String stackName;
    private CustomYamlLine external;
    private CustomYamlLine name;
    private CustomYamlLine yamlKey;

    private DockerGlobalSecret(String key, String stackName, CustomYamlLine yamlKey) {
        this.key = key;
        this.yamlKey = yamlKey;
        this.stackName = stackName;
    }

    public static DockerGlobalSecret of(String stackName, CustomYamlLine line) {
        boolean commented = CustomYamlLine.isCommented(line.getCurrentRawText());
        if (commented) {
            line.comment();
        } else {
            line.uncomment();
        }
        String rawText = line.getCurrentRawText();
        int colonIndex = rawText.indexOf(":");
        int startIndex = 0;
        String key;
        if (commented) {
            startIndex = 1;
        }
        key = rawText.trim().substring(startIndex, colonIndex).trim();
        CustomYamlLine yamlKey = line;
        if (StringUtils.isNotBlank(key)) {
            yamlKey.setCurrentRawText(String.format("  %s:", key));
        }

        return new DockerGlobalSecret(key, stackName, yamlKey);
    }

    public void applyName(CustomYamlLine nameLine, String stackPrefix) {
        String rawText = nameLine.getCurrentRawText();
        int colonIndex = rawText.indexOf(":");

        String nameSuffix = rawText.substring(colonIndex + 1).trim();
        String secretName = nameSuffix.replace("\"", "")
                                .replace(stackPrefix, "")
                                .trim();
        String secretYamlLine = String.format("    name: \"%s_%s\"", stackName, secretName);
        nameLine.setCurrentRawText(secretYamlLine);
        this.name = nameLine;
    }

    public void applyExternal(CustomYamlLine externalLine) {
        String rawText = externalLine.getCurrentRawText();
        int colonIndex = rawText.indexOf(":");

        String externalBoolean = rawText.substring(colonIndex + 1).trim();
        String secretYamlLine = String.format("    external: %s", externalBoolean);
        externalLine.setCurrentRawText(secretYamlLine);
        this.external = externalLine;
    }

    @Override
    public boolean isBlockCommented() {
        return this.isCommented();
    }

    @Override
    public void commentBlock() {
        yamlKey.comment();
        if (null != external) {
            external.comment();
        }
        if (null != name) {
            name.comment();
        }
    }

    @Override
    public void uncommentBlock() {
        yamlKey.uncomment();
        if (null != external) {
            external.uncomment();
        }
        if (null != name) {
            name.uncomment();
        }
    }

    public boolean isCommented() {
        return yamlKey.isCommented();
    }

    public String getKey() {
        return key;
    }

    public String getStackName() {
        return stackName;
    }
}
