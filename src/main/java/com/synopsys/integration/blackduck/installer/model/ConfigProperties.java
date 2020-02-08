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
package com.synopsys.integration.blackduck.installer.model;

import java.util.*;

public class ConfigProperties implements Iterable<ConfigProperty> {
    private Map<String, ConfigProperty> configProperties = new HashMap<>();

    public void add(ConfigProperty configProperty) {
        configProperties.put(configProperty.getKey(), configProperty);
    }

    public Set<String> keySet() {
        return configProperties.keySet();
    }

    public ConfigProperty get(String key) {
        return new ConfigProperty(key, getValue(key));
    }

    public String getValue(String key) {
        return configProperties.get(key).getValue();
    }

    public void put(String key, String value) {
        configProperties.put(key, new ConfigProperty(key, value));
    }

    @Override
    public Iterator<ConfigProperty> iterator() {
        return configProperties.values().iterator();
    }

}
