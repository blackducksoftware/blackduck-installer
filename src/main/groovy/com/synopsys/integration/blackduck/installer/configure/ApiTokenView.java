/**
 * blackduck-installer
 *
 * Copyright (c) 2019 Synopsys, Inc.
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
package com.synopsys.integration.blackduck.installer.configure;

import com.synopsys.integration.blackduck.api.core.BlackDuckView;

import java.util.List;

public class ApiTokenView extends BlackDuckView {
    private String name;
    private String description;
    private List<String> scopes;
    private String token;

    public ApiTokenView() {
        //TODO check if gson really needs this
    }

    public ApiTokenView(String name, String description, List<String> scopes, String token) {
        this.name = name;
        this.description = description;
        this.scopes = scopes;
        this.token = token;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public List<String> getScopes() {
        return scopes;
    }

    public String getToken() {
        return token;
    }

}
