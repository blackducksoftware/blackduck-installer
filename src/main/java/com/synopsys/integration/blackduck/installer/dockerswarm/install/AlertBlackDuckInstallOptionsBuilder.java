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
package com.synopsys.integration.blackduck.installer.dockerswarm.install;

import com.synopsys.integration.blackduck.installer.ApplicationValues;
import com.synopsys.integration.blackduck.installer.model.AlertBlackDuckInstallOptions;
import org.apache.commons.lang3.StringUtils;

public class AlertBlackDuckInstallOptionsBuilder {
    private ApplicationValues applicationValues;
    private String blackDuckApiToken;

    public AlertBlackDuckInstallOptionsBuilder(ApplicationValues applicationValues) {
        this.applicationValues = applicationValues;
    }

    public void setBlackDuckApiToken(String blackDuckApiToken) {
        this.blackDuckApiToken = blackDuckApiToken;
    }

    public AlertBlackDuckInstallOptions build() {
        String blackDuckUrl = null;
        int blackDuckTimeoutInSeconds = 0;
        String blackDuckHostForAutoSslImport = null;
        int blackDuckPortForAutoSslImport = 0;

        if (StringUtils.isNotBlank(applicationValues.getWebServerHost())) {
            blackDuckUrl = "https://" + applicationValues.getWebServerHost();
            if (applicationValues.isAlertInstallBlackDuckAutoSslImport()) {
                blackDuckHostForAutoSslImport = applicationValues.getWebServerHost();
            }
        }

        blackDuckTimeoutInSeconds = applicationValues.getTimeoutInSeconds();

        if (StringUtils.isNotBlank(applicationValues.getAlertInstallBlackDuckUrl())) {
            blackDuckUrl = applicationValues.getAlertInstallBlackDuckUrl();
        }

        if (StringUtils.isNotBlank(applicationValues.getAlertInstallBlackDuckApiToken())) {
            blackDuckApiToken = applicationValues.getAlertInstallBlackDuckApiToken();
        }

        if (applicationValues.getAlertInstallBlackDuckTimeoutInSeconds() > 0) {
            blackDuckTimeoutInSeconds = applicationValues.getAlertInstallBlackDuckTimeoutInSeconds();
        }

        if (applicationValues.isAlertInstallBlackDuckAutoSslImport()) {
            if (StringUtils.isNotBlank(applicationValues.getAlertInstallBlackDuckHostForAutoSslImport())) {
                blackDuckHostForAutoSslImport = applicationValues.getAlertInstallBlackDuckHostForAutoSslImport();
            }
            if (applicationValues.getAlertInstallBlackDuckPortForAutoSslImport() > 0) {
                blackDuckPortForAutoSslImport = applicationValues.getAlertInstallBlackDuckPortForAutoSslImport();
            }
        }

        return new AlertBlackDuckInstallOptions(blackDuckUrl, blackDuckApiToken, blackDuckTimeoutInSeconds, blackDuckHostForAutoSslImport, blackDuckPortForAutoSslImport);
    }

}
