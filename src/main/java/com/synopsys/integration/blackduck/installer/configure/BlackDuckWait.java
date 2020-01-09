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

import com.synopsys.integration.blackduck.configuration.BlackDuckServerConfig;
import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.log.IntLogger;
import com.synopsys.integration.wait.WaitJob;

import java.io.File;

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

    public boolean waitForBlackDuck(File installDirectory) throws InterruptedException, IntegrationException {
        BlackDuckWaitJobTask blackDuckWaitJobTask = new BlackDuckWaitJobTask(intLogger, blackDuckServerConfig, updateKeyStoreService, installDirectory);
        WaitJob waitJob = WaitJob.createUsingSystemTimeWhenInvoked(intLogger, timeoutInSeconds, 30, blackDuckWaitJobTask);

        intLogger.info("Checking the Black Duck server...");
        return waitJob.waitFor();
    }

}