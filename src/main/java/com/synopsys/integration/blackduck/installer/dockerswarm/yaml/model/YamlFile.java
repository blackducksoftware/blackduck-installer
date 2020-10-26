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

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

// This is a mutable representation of the yaml files.
public class YamlFile {
    private List<YamlLine> allLines = new LinkedList<>();
    private Map<String, YamlSection> modifiableSections = new LinkedHashMap<>();
    private GlobalSecrets globalSecrets = new GlobalSecrets(YamlLine.create(-1, "#secrets:"));

    public void createGlobalSecrets(YamlLine line) {
        globalSecrets = new GlobalSecrets(line);
    }

    public void addLine(YamlLine line) {
        allLines.add(line);
    }

    public void addLine(int index, YamlLine line) {
        allLines.add(index, line);
    }

    public List<YamlLine> getAllLines() {
        return allLines;
    }

    public void addDockerSecret(DockerGlobalSecret secret) {
        globalSecrets.addSecret(secret);
    }

    public GlobalSecrets getGlobalSecrets() {
        return globalSecrets;
    }

    public void addModifiableSection(YamlSection yamlSection) {
        modifiableSections.put(yamlSection.getKey(), yamlSection);
    }

    public Optional<YamlSection> getModifiableSection(String sectionKey) {
        return Optional.ofNullable(modifiableSections.get(sectionKey));
    }
}
