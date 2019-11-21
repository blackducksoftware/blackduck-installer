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

import com.synopsys.integration.blackduck.installer.dockerswarm.DockerCommands;
import com.synopsys.integration.blackduck.installer.dockerswarm.DockerStackDeploy;
import com.synopsys.integration.blackduck.installer.exception.BlackDuckInstallerException;
import com.synopsys.integration.blackduck.installer.model.DockerService;
import com.synopsys.integration.blackduck.installer.model.ExecutablesRunner;
import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.executable.Executable;
import com.synopsys.integration.executable.ExecutableOutput;
import com.synopsys.integration.log.IntLogger;
import com.synopsys.integration.rest.client.IntHttpClient;
import com.synopsys.integration.rest.request.Request;
import com.synopsys.integration.rest.request.Response;
import org.apache.commons.lang3.time.DurationFormatUtils;
import org.apache.http.entity.ContentType;

import java.io.IOException;
import java.time.Duration;

public class AlertWithBlackDuckCertificateService {
    private final IntLogger logger;
    private final BlackDuckWait blackDuckWait;
    private final IntHttpClient intHttpClient;
    private final int installTimeoutInSeconds;
    private final DockerCommands dockerCommands;
    private final DockerService alertService;
    private final ExecutablesRunner executablesRunner;
    private final Request alertRequest;

    public AlertWithBlackDuckCertificateService(IntLogger logger, BlackDuckWait blackDuckWait, IntHttpClient intHttpClient, String alertUrl, int installTimeoutInSeconds, DockerCommands dockerCommands, DockerService alertService, ExecutablesRunner executablesRunner) {
        this.logger = logger;
        this.blackDuckWait = blackDuckWait;
        this.intHttpClient = intHttpClient;
        this.installTimeoutInSeconds = installTimeoutInSeconds;
        this.dockerCommands = dockerCommands;
        this.alertService = alertService;
        this.executablesRunner = executablesRunner;

        Request.Builder requestBuilder = Request.newBuilder();
        requestBuilder.uri(alertUrl);
        requestBuilder.mimeType(ContentType.TEXT_HTML.getMimeType());
        alertRequest = requestBuilder.build();
    }

    /**
     * 1) Verify Alert & Black Duck started successfully
     * 2) Remove the Alert service
     * 3) Redeploy Alert service
     * <p>
     * To successfully configure Alert w/ Black Duck, we must wait until both Black Duck and Alert are healthy, bring
     * Alert down, then restart it. This is to allow Alert to get the certificate from a healthy Black Duck server upon
     * startup. Sometimes, Alert will become healthy before Black Duck, so we have to force Alert to become healthy only
     * AFTER Black Duck is healthy.
     */
    public void configureCertificate(DockerStackDeploy alertStackDeploy) throws BlackDuckInstallerException, InterruptedException {
        waitForAlertHealthy();
        blackDuckWait.waitForBlackDuck();

        logger.info(String.format("Removing the service \"%s\".", alertService.getDockerName()));
        executablesRunner.runExecutable(dockerCommands.removeService(alertService));

        Executable listNames = dockerCommands.listServiceNames();
        boolean notYetRemoved = true;
        int removedCheckAttempts = 0;
        while (notYetRemoved && removedCheckAttempts < 5) {
            ExecutableOutput serviceNamesListOutput = executablesRunner.runExecutable(listNames);
            if (serviceNamesListOutput.getStandardOutput().contains(alertService.getDockerName())) {
                logger.info(String.format("Service not yet removed (try #%s)", removedCheckAttempts));
                removedCheckAttempts++;
                Thread.sleep(5000);
            } else {
                notYetRemoved = false;
            }
        }

        logger.info(String.format("Redeploying \"%s\".", alertService.getDockerName()));
        Executable redeployAlert = alertStackDeploy.createDeployExecutable();
        executablesRunner.runExecutable(redeployAlert);
    }

    private void waitForAlertHealthy() throws InterruptedException {
        int attempts = 0;
        long start = System.currentTimeMillis();

        Duration currentDuration = Duration.ofMillis(0);
        Duration maximumDuration = Duration.ofMillis(installTimeoutInSeconds * 1000);
        boolean complete = false;
        while (!complete && currentDuration.compareTo(maximumDuration) <= 0) {
            logger.info(String.format("Checking the Alert server...(try #%s, elapsed: %s)", attempts, DurationFormatUtils.formatDurationHMS(currentDuration.toMillis())));
            try (Response response = intHttpClient.execute(alertRequest)) {
                // at the moment, any valid http response is considered healthy
                logger.info(String.format("Alert server responded with %s - this means it is online!", response.getStatusCode()));
                complete = true;
            } catch (IntegrationException | IOException e) {
                logger.info(String.format("Exception trying to verify Alert. This may be okay as Alert may not be available yet: ", e.getMessage()));
            }

            logger.info("Could not verify Alert is responding, waiting 30 seconds and trying again.");
            Thread.sleep(30000);
            attempts++;
            currentDuration = Duration.ofMillis(System.currentTimeMillis() - start);
        }
    }

}
