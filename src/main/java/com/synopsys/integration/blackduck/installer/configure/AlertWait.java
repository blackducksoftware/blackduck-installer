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

import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.log.IntLogger;
import com.synopsys.integration.rest.client.IntHttpClient;
import com.synopsys.integration.rest.proxy.ProxyInfo;
import com.synopsys.integration.rest.request.Request;
import com.synopsys.integration.wait.WaitJob;

import java.io.File;

public class AlertWait {
    private final IntLogger intLogger;
    private final int timeoutInSeconds;
    private final int httpTimeoutInSeconds;
    private final boolean alwaysTrust;
    private final ProxyInfo proxyInfo;
    private final Request alertRequest;
    private final UpdateKeyStoreService updateKeyStoreService;

    public AlertWait(IntLogger intLogger, int timeoutInSeconds, int httpTimeoutInSeconds, boolean alwaysTrust, ProxyInfo proxyInfo, Request alertRequest, UpdateKeyStoreService updateKeyStoreService) {
        this.intLogger = intLogger;
        this.timeoutInSeconds = timeoutInSeconds;
        this.httpTimeoutInSeconds = httpTimeoutInSeconds;
        this.alwaysTrust = alwaysTrust;
        this.proxyInfo = proxyInfo;
        this.alertRequest = alertRequest;
        this.updateKeyStoreService = updateKeyStoreService;
    }

    public boolean waitForAlert(File installDirectory) throws InterruptedException, IntegrationException {
        AlertWaitJobTask alertWaitJobTask = new AlertWaitJobTask(intLogger, httpTimeoutInSeconds, alwaysTrust, proxyInfo, alertRequest, updateKeyStoreService, installDirectory);
        WaitJob waitJob = WaitJob.createUsingSystemTimeWhenInvoked(intLogger, timeoutInSeconds, 30, alertWaitJobTask);

        intLogger.info("Checking the Alert server...");
        return waitJob.waitFor();
    }

}
