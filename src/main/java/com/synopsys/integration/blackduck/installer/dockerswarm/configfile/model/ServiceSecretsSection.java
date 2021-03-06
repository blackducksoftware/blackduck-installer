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

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class ServiceSecretsSection extends Section {
    private Map<String, ServiceSecretLine> secrets;

    public ServiceSecretsSection(String key, CustomYamlLine line) {
        super(key, line);
        secrets = new HashMap<>();
    }

    public Optional<ServiceSecretLine> getSecret(String key) {
        return Optional.ofNullable(secrets.get(key));
    }

    @Override
    public void addLine(CustomYamlLine customYamlLine) {
        ServiceSecretLine secretLine = ServiceSecretLine.of(customYamlLine);
        if (secretLine.hasKey()) {
            secrets.put(secretLine.getKey(), secretLine);
        }
        super.addLine(customYamlLine);
    }

    public void commentIfPresent(String key) {
        getSecret(key).ifPresent(CustomYamlTextLine::comment);
    }

    public void uncommentIfPresent(String key) {
        getSecret(key).ifPresent(CustomYamlTextLine::uncomment);
    }
}
