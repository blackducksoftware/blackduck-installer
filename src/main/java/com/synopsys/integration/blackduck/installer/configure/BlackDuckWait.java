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

import com.synopsys.integration.blackduck.api.generated.discovery.ApiDiscovery;
import com.synopsys.integration.blackduck.api.generated.response.CurrentVersionView;
import com.synopsys.integration.blackduck.configuration.BlackDuckServerConfig;
import com.synopsys.integration.blackduck.installer.exception.BlackDuckInstallerException;
import com.synopsys.integration.blackduck.installer.exception.IntegrationKeyStoreException;
import com.synopsys.integration.blackduck.service.BlackDuckService;
import com.synopsys.integration.blackduck.service.BlackDuckServicesFactory;
import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.log.IntLogger;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DurationFormatUtils;

import javax.net.ssl.SSLHandshakeException;
import java.io.File;
import java.time.Duration;

public class BlackDuckWait {
    private final IntLogger intLogger;
    private final int timeoutInSeconds;
    private final BlackDuckServerConfig blackDuckServerConfig;
    private final UpdateKeyStoreService updateKeyStoreService;

    public BlackDuckWait(IntLogger intLogger, int timeoutInSeconds, BlackDuckServerConfig blackDuckServerConfig, UpdateKeyStoreService updateKeyStoreService) {
        this.intLogger = intLogger;
        this.timeoutInSeconds = timeoutInSeconds;
        this.blackDuckServerConfig = blackDuckServerConfig;
        this.updateKeyStoreService = updateKeyStoreService;
    }

    public boolean waitForBlackDuck(File installDirectory) throws InterruptedException, BlackDuckInstallerException, IntegrationKeyStoreException {
        int attempts = 0;
        long start = System.currentTimeMillis();

        String currentVersion= null;
        Duration currentDuration = Duration.ofMillis(0);
        Duration maximumDuration = Duration.ofMillis(timeoutInSeconds * 1000);
        while (null == currentVersion && currentDuration.compareTo(maximumDuration) <= 0) {
            try {
                intLogger.info(String.format("Checking the Black Duck server...(try #%s, elapsed: %s)", attempts, DurationFormatUtils.formatDurationHMS(currentDuration.toMillis())));
                currentVersion = retrieveCurrentVersion();
                if (StringUtils.isNotBlank(currentVersion)) {
                    intLogger.info(String.format("Black Duck server found running version %s.", currentVersion));
                    return true;
                }
            } catch (IntegrationException e) {
                if (e.getCause() instanceof SSLHandshakeException) {
                    intLogger.info("The Black Duck server is responding, but its certificate is not in the java keystore.");
                    if (updateKeyStoreService.canAttemptKeyStoreUpdate()) {
                        intLogger.info("Since keystore.update=true, an automatic update of the keystore will be attempted.");
                        try {
                            updateKeyStoreService.updateKeyStoreWithBlackDuckCertificate(installDirectory);
                        } catch (BlackDuckInstallerException | IntegrationKeyStoreException ex) {
                            intLogger.error("The keystore could not be updated successfully - Black Duck can not be configured.");
                            throw ex;
                        }
                    }
                } else {
                    intLogger.info(String.format("The Black Duck server is not responding successfully yet, waiting 30 seconds and trying again. (%s)", e.getMessage()));
                    Thread.sleep(30000);
                    attempts++;
                }
            }

            currentDuration = Duration.ofMillis(System.currentTimeMillis() - start);
        }

        return false;
    }

    private String retrieveCurrentVersion() throws IntegrationException {
        BlackDuckServicesFactory blackDuckServicesFactory = blackDuckServerConfig.createBlackDuckServicesFactory(intLogger);
        BlackDuckService blackDuckService = blackDuckServicesFactory.createBlackDuckService();
        CurrentVersionView currentVersionView = blackDuckService.getResponse(ApiDiscovery.CURRENT_VERSION_LINK_RESPONSE);

        return currentVersionView.getVersion();
    }

}
