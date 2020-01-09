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

import com.synopsys.integration.blackduck.installer.exception.BlackDuckInstallerException;
import org.apache.commons.lang3.StringUtils;

public class AlertEncryption {
    private final DockerSecret password;
    private final DockerSecret salt;

    public AlertEncryption(String passwordPath, String saltPath) throws BlackDuckInstallerException {
        String[] values = new String[]{passwordPath, saltPath};
        if (StringUtils.isAllBlank(values)) {
            password = null;
            salt = null;
        } else if (StringUtils.isAnyBlank(values)) {
            throw new BlackDuckInstallerException("Either both password and salt should be set, or neither should be set.");
        } else {
            password = DockerSecret.createAlertPassword(passwordPath);
            salt = DockerSecret.createAlertSalt(saltPath);
        }
    }

    public boolean isEmpty() {
        return null == password;
    }

    public DockerSecret getPassword() {
        return password;
    }

    public DockerSecret getSalt() {
        return salt;
    }

}
