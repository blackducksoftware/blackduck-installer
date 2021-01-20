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

import com.synopsys.integration.blackduck.installer.exception.BlackDuckInstallerException;
import org.apache.commons.lang3.StringUtils;

import java.util.Optional;

public class CustomCertificate {
    private final DockerSecret certificate;
    private final DockerSecret privateKey;

    public CustomCertificate(String customCertPath, String customKeyPath) throws BlackDuckInstallerException {
        String[] values = new String[]{customCertPath, customKeyPath};
        if (StringUtils.isAllBlank(values)) {
            certificate = null;
            privateKey = null;
        } else if (StringUtils.isAnyBlank(values)) {
            throw new BlackDuckInstallerException("Either both certificate and private key should be set, or neither should be set.");
        } else {
            certificate = DockerSecret.createCert(customCertPath);
            privateKey = DockerSecret.createKey(customKeyPath);
        }
    }

    public boolean isEmpty() {
        return null == certificate;
    }

    public DockerSecret getCertificate() {
        return certificate;
    }

    public DockerSecret getPrivateKey() {
        return privateKey;
    }

}
