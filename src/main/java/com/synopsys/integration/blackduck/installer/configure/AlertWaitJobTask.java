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

import com.synopsys.integration.blackduck.installer.exception.BlackDuckInstallerException;
import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.log.IntLogger;
import com.synopsys.integration.rest.client.IntHttpClient;
import com.synopsys.integration.rest.request.Request;
import com.synopsys.integration.rest.request.Response;
import com.synopsys.integration.wait.WaitJobTask;

import java.io.IOException;

public class AlertWaitJobTask implements WaitJobTask {
    private final IntLogger intLogger;
    private final IntHttpClient intHttpClient;
    private final Request alertRequest;

    public AlertWaitJobTask(IntLogger intLogger, IntHttpClient intHttpClient, Request alertRequest) {
        this.intLogger = intLogger;
        this.intHttpClient = intHttpClient;
        this.alertRequest = alertRequest;
    }

    @Override
    public boolean isComplete() throws BlackDuckInstallerException {
        try (Response response = intHttpClient.execute(alertRequest)) {
            // at the moment, any valid http response is considered healthy
            intLogger.info(String.format("Alert server responded with %s - this means it is online!", response.getStatusCode()));
            return true;
        } catch (IntegrationException | IOException e) {
            intLogger.info(String.format("Exception trying to verify Alert. This may be okay as Alert may not be available yet: ", e.getMessage()));
        }

        return false;
    }
}
