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
package com.synopsys.integration.blackduck.installer.model;

import com.google.gson.Gson;
import com.synopsys.integration.function.ThrowingSupplier;

import java.io.*;

public class ConfigPropertiesLoader {
    private final Gson gson;

    public ConfigPropertiesLoader(Gson gson) {
        this.gson = gson;
    }

    public LoadedConfigProperties loadPropertiesFromFile(File jsonFile) throws IOException {
        return loadProperties(() -> new FileReader(jsonFile));
    }

    public LoadedConfigProperties loadPropertiesFromString(String json) throws IOException{
        return loadProperties(() -> new StringReader(json));
    }

    private LoadedConfigProperties loadProperties(ThrowingSupplier<Reader, IOException> readerCreator) throws IOException {
        try (Reader reader = readerCreator.get()) {
            return gson.fromJson(reader, LoadedConfigProperties.class);
        }
    }

}
