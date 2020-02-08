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

import java.util.ArrayList;
import java.util.List;

public class FileLoadedProperties {
    private List<ConfigProperty> toAdd = new ArrayList<>();
    private List<ConfigProperty> toEdit = new ArrayList<>();

    public FileLoadedProperties() {

    }

    public FileLoadedProperties(List<ConfigProperty> toAdd, List<ConfigProperty> toEdit) {
        this.toAdd = toAdd;
        this.toEdit = toEdit;
    }

    public List<ConfigProperty> getToAdd() {
        return toAdd;
    }

    public List<ConfigProperty> getToEdit() {
        return toEdit;
    }

}
