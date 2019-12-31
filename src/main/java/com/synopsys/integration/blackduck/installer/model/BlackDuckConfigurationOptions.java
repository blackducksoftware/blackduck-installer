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
package com.synopsys.integration.blackduck.installer.model;

import org.apache.commons.lang3.StringUtils;

public class BlackDuckConfigurationOptions {
    private String registrationKey;
    private boolean acceptEula;
    private boolean createApiToken;
    private boolean isDryRun;

    public BlackDuckConfigurationOptions(String registrationKey, boolean acceptEula, boolean createApiToken, boolean isDryRun) {
        this.registrationKey = registrationKey;
        this.acceptEula = acceptEula;
        this.createApiToken = createApiToken;
        this.isDryRun = isDryRun;
    }

    public boolean shouldConfigure() {
        return !isDryRun && (StringUtils.isNotBlank(registrationKey) || acceptEula || createApiToken);
    }

    public String getRegistrationKey() {
        return registrationKey;
    }

    public boolean isAcceptEula() {
        return acceptEula;
    }

    public boolean isCreateApiToken() {
        return createApiToken;
    }

    public boolean isDryRun() {
        return isDryRun;
    }

}
