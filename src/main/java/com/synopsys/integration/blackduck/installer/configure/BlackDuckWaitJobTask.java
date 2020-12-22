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
package com.synopsys.integration.blackduck.installer.configure;

import java.io.File;

import javax.net.ssl.SSLHandshakeException;

import org.apache.commons.lang3.StringUtils;

import com.synopsys.integration.blackduck.api.generated.discovery.ApiDiscovery;
import com.synopsys.integration.blackduck.api.generated.response.CurrentVersionView;
import com.synopsys.integration.blackduck.configuration.BlackDuckServerConfig;
import com.synopsys.integration.blackduck.installer.exception.BlackDuckInstallerException;
import com.synopsys.integration.blackduck.service.BlackDuckApiClient;
import com.synopsys.integration.blackduck.service.BlackDuckServicesFactory;
import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.log.IntLogger;
import com.synopsys.integration.wait.WaitJobTask;

public class BlackDuckWaitJobTask implements WaitJobTask {
    private final IntLogger intLogger;
    private final BlackDuckServerConfig blackDuckServerConfig;
    private final UpdateKeyStoreService updateKeyStoreService;
    private final File installDirectory;

    public BlackDuckWaitJobTask(IntLogger intLogger, BlackDuckServerConfig blackDuckServerConfig, UpdateKeyStoreService updateKeyStoreService, File installDirectory) {
        this.intLogger = intLogger;
        this.blackDuckServerConfig = blackDuckServerConfig;
        this.updateKeyStoreService = updateKeyStoreService;
        this.installDirectory = installDirectory;
    }

    @Override
    public boolean isComplete() throws BlackDuckInstallerException {
        intLogger.info(String.format("Attempting to connect to %s.", blackDuckServerConfig.getBlackDuckUrl()));
        BlackDuckServicesFactory blackDuckServicesFactory = blackDuckServerConfig.createBlackDuckServicesFactory(intLogger);
        BlackDuckApiClient blackDuckApiClient = blackDuckServicesFactory.getBlackDuckApiClient();
        try {
            CurrentVersionView currentVersionView = blackDuckApiClient.getResponse(ApiDiscovery.CURRENT_VERSION_LINK_RESPONSE);
            if (null != currentVersionView) {
                String currentVersion = currentVersionView.getVersion();
                if (StringUtils.isNotBlank(currentVersion)) {
                    intLogger.info(String.format("Black Duck server found running version %s.", currentVersion));
                    return true;
                }
            }
        } catch (IntegrationException e) {
            if (e.getCause() instanceof SSLHandshakeException) {
                updateKeyStoreService.handleSSLHandshakeException(installDirectory);
                return false;
            }
        }

        intLogger.info("Couldn't check the version because the Black Duck server is not responding successfully yet.");
        return false;
    }

}
