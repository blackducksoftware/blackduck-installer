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

public class AlertBlackDuckInstallOptions {
    private final String blackDuckUrl;
    private final String blackDuckApiToken;
    private final int blackDuckTimeoutInSeconds;
    private final String blackDuckHostForAutoSslImport;
    private final int blackDuckPortForAutoSslImport;

    public AlertBlackDuckInstallOptions(String blackDuckUrl, String blackDuckApiToken, int blackDuckTimeoutInSeconds, String blackDuckHostForAutoSslImport, int blackDuckPortForAutoSslImport) {
        this.blackDuckUrl = blackDuckUrl;
        this.blackDuckApiToken = blackDuckApiToken;
        this.blackDuckTimeoutInSeconds = blackDuckTimeoutInSeconds;
        this.blackDuckHostForAutoSslImport = blackDuckHostForAutoSslImport;
        this.blackDuckPortForAutoSslImport = blackDuckPortForAutoSslImport;
    }

    public boolean isEmpty() {
        return StringUtils.isBlank(blackDuckUrl) && StringUtils.isBlank(blackDuckApiToken) && StringUtils.isBlank(blackDuckHostForAutoSslImport) && blackDuckPortForAutoSslImport == 0 && blackDuckTimeoutInSeconds == 0;
    }

    public String getBlackDuckUrl() {
        return blackDuckUrl;
    }

    public String getBlackDuckApiToken() {
        return blackDuckApiToken;
    }

    public int getBlackDuckTimeoutInSeconds() {
        return blackDuckTimeoutInSeconds;
    }

    public String getBlackDuckHostForAutoSslImport() {
        return blackDuckHostForAutoSslImport;
    }

    public int getBlackDuckPortForAutoSslImport() {
        return blackDuckPortForAutoSslImport;
    }

}
