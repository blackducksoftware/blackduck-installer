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

import org.apache.commons.lang3.StringUtils;

import com.synopsys.integration.blackduck.installer.exception.BlackDuckInstallerException;

public class AlertDatabase {
    private final String databaseName;
    private final boolean external;
    private final String defaultUserName;
    private final String defaultPassword;
    private final DockerSecret userNameSecret;
    private final DockerSecret passwordSecret;

    public AlertDatabase(final String databaseName, boolean external, final String defaultUserName, final String defaultPassword, final String userPath, final String passwordPath) throws BlackDuckInstallerException {
        this.databaseName = databaseName;
        this.external = external;
        this.defaultUserName = defaultUserName;
        this.defaultPassword = defaultPassword;
        String[] values = new String[] { userPath, passwordPath };
        if (StringUtils.isAllBlank(values)) {
            passwordSecret = null;
            userNameSecret = null;
        } else if (StringUtils.isAnyBlank(values)) {
            throw new BlackDuckInstallerException("Either both database userName and password should be set, or neither should be set.");
        } else {
            userNameSecret = DockerSecret.createAlertDBUser(userPath);
            passwordSecret = DockerSecret.createAlertDBPassword(passwordPath);
        }
    }

    public boolean isEmpty() {
        return StringUtils.isBlank(databaseName) && (!hasSecrets() && (StringUtils.isBlank(defaultUserName) && StringUtils.isBlank(defaultPassword)));
    }

    public boolean hasSecrets() {
        return null != passwordSecret;
    }

    public String getDatabaseName() {
        return databaseName;
    }

    public boolean isExternal() {
        return external;
    }

    public String getDefaultUserName() {
        return defaultUserName;
    }

    public String getDefaultPassword() {
        return defaultPassword;
    }

    public DockerSecret getUserNameSecret() {
        return userNameSecret;
    }

    public DockerSecret getPasswordSecret() {
        return passwordSecret;
    }

    public String getPostgresUserNameSecretEnvironmentValue() {
        return "/run/secrets/" + getUserNameSecret().getLabel();
    }

    public String getPostgresPasswordSecretEnvironmentValue() {
        return "/run/secrets/" + getPasswordSecret().getLabel();
    }

}
