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

import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.log.IntLogger;
import com.synopsys.integration.rest.client.IntHttpClient;
import com.synopsys.integration.rest.request.Request;
import com.synopsys.integration.rest.request.Response;
import org.apache.commons.lang3.time.DurationFormatUtils;

import java.io.IOException;
import java.time.Duration;

public class AlertWait {
    private final IntLogger intLogger;
    private final int timeoutInSeconds;
    private final IntHttpClient intHttpClient;
    private final Request alertRequest;

    public AlertWait(IntLogger intLogger, int timeoutInSeconds, IntHttpClient intHttpClient, Request alertRequest) {
        this.intLogger = intLogger;
        this.timeoutInSeconds = timeoutInSeconds;
        this.intHttpClient = intHttpClient;
        this.alertRequest = alertRequest;
    }

    public boolean waitForAlert() throws InterruptedException {
        int attempts = 0;
        long start = System.currentTimeMillis();

        Duration currentDuration = Duration.ofMillis(0);
        Duration maximumDuration = Duration.ofMillis(timeoutInSeconds * 1000);
        while (currentDuration.compareTo(maximumDuration) <= 0) {
            intLogger.info(String.format("Checking the Alert server...(try #%s, elapsed: %s)", attempts, DurationFormatUtils.formatDurationHMS(currentDuration.toMillis())));
            try (Response response = intHttpClient.execute(alertRequest)) {
                // at the moment, any valid http response is considered healthy
                intLogger.info(String.format("Alert server responded with %s - this means it is online!", response.getStatusCode()));
                return true;
            } catch (IntegrationException | IOException e) {
                intLogger.info(String.format("Exception trying to verify Alert. This may be okay as Alert may not be available yet: ", e.getMessage()));
            }

            intLogger.info(String.format("The Alert server is not responding successfully yet, waiting 30 seconds and trying again."));
            Thread.sleep(30000);
            attempts++;
            currentDuration = Duration.ofMillis(System.currentTimeMillis() - start);
        }

        return false;
    }
}
