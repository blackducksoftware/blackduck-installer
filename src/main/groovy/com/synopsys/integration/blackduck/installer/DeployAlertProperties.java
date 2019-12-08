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
package com.synopsys.integration.blackduck.installer;

import com.synopsys.integration.blackduck.installer.model.AlertBlackDuckInstallOptions;
import com.synopsys.integration.blackduck.installer.model.AlertEncryption;
import com.synopsys.integration.blackduck.installer.model.DockerService;
import com.synopsys.integration.blackduck.installer.dockerswarm.install.AlertBlackDuckInstallOptionsBuilder;

public class DeployAlertProperties {
    private final DockerService alertService;
    private final AlertBlackDuckInstallOptionsBuilder alertBlackDuckInstallOptionsBuilder;
    private final AlertEncryption alertEncryption;

    public DeployAlertProperties(DockerService alertService, AlertBlackDuckInstallOptionsBuilder alertBlackDuckInstallOptionsBuilder, AlertEncryption alertEncryption) {
        this.alertService = alertService;
        this.alertBlackDuckInstallOptionsBuilder = alertBlackDuckInstallOptionsBuilder;
        this.alertEncryption = alertEncryption;
    }

    public DockerService getAlertService() {
        return alertService;
    }

    public void setBlackDuckApiToken(String blackDuckApiToken) {
        alertBlackDuckInstallOptionsBuilder.setBlackDuckApiToken(blackDuckApiToken);
    }

    public AlertBlackDuckInstallOptions getAlertBlackDuckInstallOptions() {
        return alertBlackDuckInstallOptionsBuilder.build();
    }

    public AlertEncryption getAlertEncryption() {
        return alertEncryption;
    }

}
