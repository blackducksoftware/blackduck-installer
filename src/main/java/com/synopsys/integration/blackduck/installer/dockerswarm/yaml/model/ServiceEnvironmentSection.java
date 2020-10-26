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
package com.synopsys.integration.blackduck.installer.dockerswarm.yaml.model;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;

public class ServiceEnvironmentSection extends DefaultSection {
    private Map<String, ServiceEnvironmentLine> environmentVariables;

    public ServiceEnvironmentSection(final String key, final YamlLine line) {
        super(key, line);
        environmentVariables = new HashMap<>();
    }

    @Override
    public void addLine(final YamlLine yamlLine) {
        ServiceEnvironmentLine environmentLine = ServiceEnvironmentLine.of(yamlLine);
        if (environmentLine.hasKey()) {
            environmentVariables.put(environmentLine.getKey(), environmentLine);
        }
        super.addLine(yamlLine);
    }

    @Override
    public void addLine(int index, YamlLine yamlLine) {
        ServiceEnvironmentLine environmentLine = ServiceEnvironmentLine.of(yamlLine);
        if (environmentLine.hasKey()) {
            environmentVariables.put(environmentLine.getKey(), environmentLine);
        }
        super.addLine(index, yamlLine);
    }

    public Optional<ServiceEnvironmentLine> getVariableLine(String key) {
        return Optional.ofNullable(environmentVariables.get(key));
    }

    public void setEnvironmentVariableValue(String key, int value) {
        setEnvironmentVariableValue(key, String.valueOf(value));
    }

    public void setEnvironmentVariableValue(String key, boolean value) {
        setEnvironmentVariableValue(key, String.valueOf(value));
    }

    public void setEnvironmentVariableValue(String key, String value) {
        Optional<ServiceEnvironmentLine> environmentLine = getVariableLine(key);
        if (environmentLine.isPresent() && StringUtils.isNotBlank(value)) {
            ServiceEnvironmentLine environmentVariable = environmentLine.get();
            environmentVariable.getYamlLine().uncomment();
            environmentVariable.setValue(value);
        }
    }
}
