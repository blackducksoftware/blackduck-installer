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
import com.synopsys.integration.wait.WaitJobTask;
import org.apache.commons.lang3.StringUtils;

import javax.net.ssl.SSLHandshakeException;
import java.io.File;

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
        BlackDuckServicesFactory blackDuckServicesFactory = blackDuckServerConfig.createBlackDuckServicesFactory(intLogger);
        BlackDuckService blackDuckService = blackDuckServicesFactory.createBlackDuckService();
        try {
            CurrentVersionView currentVersionView = blackDuckService.getResponse(ApiDiscovery.CURRENT_VERSION_LINK_RESPONSE);
            if (null != currentVersionView) {
                String currentVersion = currentVersionView.getVersion();
                if (StringUtils.isNotBlank(currentVersion)) {
                    intLogger.info(String.format("Black Duck server found running version %s.", currentVersion));
                    return true;
                }
            }
        } catch (IntegrationException e) {
            if (e.getCause() instanceof SSLHandshakeException) {
                intLogger.info("The Black Duck server is responding, but its certificate is not in the java keystore.");
                if (!updateKeyStoreService.canAttemptKeyStoreUpdate()) {
                    intLogger.error("Since keystore.update=false, no automatic update of the keystore will be attempted.");
                    throw new BlackDuckInstallerException("The keystore is not setup properly (either add the certificate manually, or set keystore.update=true) - Black Duck can not be configured.");
                } else {
                    intLogger.info("Since keystore.update=true, an automatic update of the keystore will be attempted.");
                    try {
                        updateKeyStoreService.updateKeyStoreWithBlackDuckCertificate(installDirectory);
                    } catch (BlackDuckInstallerException | IntegrationKeyStoreException ex) {
                        throw new BlackDuckInstallerException("The keystore could not be updated successfully - Black Duck can not be configured.", ex);
                    }
                }

                intLogger.info("Couldn't check the version because of a missing certificate - the next check should work.");
                return false;
            }
        }

        intLogger.info("Couldn't check the version because the Black Duck server is not responding successfully yet.");
        return false;
    }

}
