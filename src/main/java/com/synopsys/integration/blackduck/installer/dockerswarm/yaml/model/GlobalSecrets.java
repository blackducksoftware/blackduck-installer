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

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

public class GlobalSecrets implements YamlTextLine, YamlBlock {
    private Map<String, DockerGlobalSecret> secrets = new LinkedHashMap<>();
    private YamlLine yamlLine;

    public GlobalSecrets(final YamlLine yamlLine) {
        this.yamlLine = yamlLine;
    }

    public boolean allSecretsCommented() {
        return !secrets.isEmpty() && secrets.values().stream().allMatch(DockerGlobalSecret::isCommented);
    }

    public void addSecret(DockerGlobalSecret secret) {
        secrets.put(secret.getKey(), secret);
    }

    public Optional<DockerGlobalSecret> getSecret(String key) {
        return Optional.ofNullable(secrets.get(key));
    }

    public Collection<DockerGlobalSecret> getSecrets() {
        return secrets.values();
    }

    @Override
    public boolean isCommented() {
        return yamlLine.isCommented();
    }

    @Override
    public void comment() {
        yamlLine.comment();
    }

    @Override
    public void uncomment() {
        yamlLine.uncomment();
    }

    @Override
    public void commentBlock() {
        yamlLine.comment();
        Collection<DockerGlobalSecret> dockerSecrets = getSecrets();
        dockerSecrets.forEach(YamlBlock::commentBlock);
    }

    @Override
    public void uncommentBlock() {
        yamlLine.uncomment();
        Collection<DockerGlobalSecret> dockerSecrets = getSecrets();
        dockerSecrets.forEach(YamlBlock::commentBlock);
    }

    @Override
    public boolean isBlockCommented() {
        return getSecrets().stream()
                   .allMatch(DockerGlobalSecret::isCommented);
    }
}
