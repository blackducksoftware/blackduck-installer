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

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

public class GlobalSecrets implements CustomYamlTextLine, CustomYamlBlock {
    private Map<String, DockerGlobalSecret> secrets = new LinkedHashMap<>();
    private CustomYamlLine customYamlLine;

    public GlobalSecrets(final CustomYamlLine customYamlLine) {
        this.customYamlLine = customYamlLine;
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

    @Override
    public void commentBlock() {
        customYamlLine.comment();
        Collection<DockerGlobalSecret> dockerSecrets = getSecrets();
        dockerSecrets.forEach(CustomYamlBlock::commentBlock);
    }

    @Override
    public void uncommentBlock() {
        customYamlLine.uncomment();
        Collection<DockerGlobalSecret> dockerSecrets = getSecrets();
        dockerSecrets.forEach(CustomYamlBlock::commentBlock);
    }

    @Override
    public boolean isBlockCommented() {
        return getSecrets().stream()
                   .allMatch(DockerGlobalSecret::isCommented);
    }

    public void commentIfPresent(String key) {
        getSecret(key).ifPresent(CustomYamlBlock::commentBlock);
    }

    public void uncommentIfPresent(String key) {
        getSecret(key).ifPresent(CustomYamlBlock::uncommentBlock);
    }
}
