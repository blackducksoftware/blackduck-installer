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
package com.synopsys.integration.blackduck.installer.configure;

import com.synopsys.integration.blackduck.installer.exception.BlackDuckInstallerException;
import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.log.IntLogger;
import com.synopsys.integration.rest.client.IntHttpClient;
import com.synopsys.integration.rest.proxy.ProxyInfo;
import com.synopsys.integration.rest.request.Request;
import com.synopsys.integration.rest.response.Response;
import com.synopsys.integration.wait.WaitJobTask;

import javax.net.ssl.SSLHandshakeException;
import java.io.File;
import java.io.IOException;

public class AlertWaitJobTask implements WaitJobTask {
    private final IntLogger intLogger;
    private final Request alertRequest;
    private final int timeoutInSeconds;
    private final boolean alwaysTrust;
    private final ProxyInfo proxyInfo;
    private final UpdateKeyStoreService updateKeyStoreService;
    private final File installDirectory;

    public AlertWaitJobTask(IntLogger intLogger, int timeoutInSeconds, boolean alwaysTrust, ProxyInfo proxyInfo, Request alertRequest, UpdateKeyStoreService updateKeyStoreService, File installDirectory) {
        this.intLogger = intLogger;
        this.alertRequest = alertRequest;
        this.timeoutInSeconds = timeoutInSeconds;
        this.alwaysTrust = alwaysTrust;
        this.proxyInfo = proxyInfo;
        this.updateKeyStoreService = updateKeyStoreService;
        this.installDirectory = installDirectory;
    }

    @Override
    public boolean isComplete() throws BlackDuckInstallerException {
        intLogger.info(String.format("Attempting to connect to %s.", alertRequest.getUrl()));
        IntHttpClient httpClient = new IntHttpClient(intLogger, timeoutInSeconds, alwaysTrust, proxyInfo);
        try (Response response = httpClient.execute(alertRequest)) {
            // at the moment, any valid http response is considered healthy
            intLogger.info(String.format("Alert server responded with %s - this means it is online!", response.getStatusCode()));
            return true;
        } catch (IntegrationException | IOException e) {
            if (e.getCause() instanceof SSLHandshakeException) {
                updateKeyStoreService.handleSSLHandshakeException(installDirectory);
                return false;
            }
            intLogger.info(String.format("Exception trying to verify Alert. This may be okay as Alert may not be available yet: %s", e.getMessage()));
        }

        return false;
    }

}
