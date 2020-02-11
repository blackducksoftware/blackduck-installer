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
package com.synopsys.integration.blackduck.installer.dockerswarm.install;

import com.synopsys.integration.blackduck.installer.dockerswarm.DockerCommands;
import com.synopsys.integration.blackduck.installer.dockerswarm.DockerStackDeploy;
import com.synopsys.integration.blackduck.installer.dockerswarm.OrchestrationFiles;
import com.synopsys.integration.blackduck.installer.dockerswarm.deploy.AlertDockerManager;
import com.synopsys.integration.blackduck.installer.dockerswarm.edit.AlertLocalOverridesEditor;
import com.synopsys.integration.blackduck.installer.dockerswarm.edit.ConfigFileEditor;
import com.synopsys.integration.blackduck.installer.dockerswarm.output.DockerServices;
import com.synopsys.integration.blackduck.installer.download.ZipFileDownloader;
import com.synopsys.integration.blackduck.installer.exception.BlackDuckInstallerException;
import com.synopsys.integration.blackduck.installer.model.DockerService;
import com.synopsys.integration.blackduck.installer.model.ExecutablesRunner;
import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.log.IntLogger;
import com.synopsys.integration.wait.WaitJob;
import com.synopsys.integration.wait.WaitJobTask;

import java.io.File;
import java.util.List;

public class AlertInstaller extends Installer {
    private final DockerService alertService;
    private final ConfigFileEditor alertLocalOverridesEditor;
    private final boolean useLocalOverrides;

    public AlertInstaller(IntLogger logger, ZipFileDownloader zipFileDownloader, ExecutablesRunner executablesRunner, AlertDockerManager alertDockerManager, DockerStackDeploy dockerStackDeploy, DockerCommands dockerCommands, String stackName, List<File> additionalOrchestrationFiles, DockerService alertService, AlertLocalOverridesEditor alertLocalOverridesEditor, boolean useLocalOverrides) {
        super(logger, zipFileDownloader, executablesRunner, alertDockerManager, dockerStackDeploy, dockerCommands, stackName, additionalOrchestrationFiles);

        this.alertService = alertService;
        this.alertLocalOverridesEditor = alertLocalOverridesEditor;
        this.useLocalOverrides = useLocalOverrides;
    }

    @Override
    public void postDownloadProcessing(File installDirectory) throws BlackDuckInstallerException {
        alertLocalOverridesEditor.edit(installDirectory);
    }

    @Override
    public void populateDockerStackDeploy(InstallerDockerData installerDockerData) {
        File dockerSwarm = new File(installerDockerData.getInstallDirectory(), "docker-swarm");
        File composeYmlDirectory = new File(dockerSwarm, "standalone");
        if (installerDockerData.getDockerStacks().doesStackExist(stackName)) {
            composeYmlDirectory = new File(dockerSwarm, "hub");
        }
        addOrchestrationFile(composeYmlDirectory, OrchestrationFiles.COMPOSE);

        addAdditionalOrchestrationFiles();

        if (useLocalOverrides) {
            addOrchestrationFile(dockerSwarm, OrchestrationFiles.LOCAL_OVERRIDES);
        }
    }

    @Override
    public void preDockerStackDeployCleanup(InstallerDockerData installerDockerData) throws IntegrationException, InterruptedException {
        if (installerDockerData.getDockerServices().doesServiceExist(alertService)) {
            logger.info(String.format("Removing the service \"%s\".", alertService.getDockerName()));
            executablesRunner.runExecutable(dockerCommands.removeService(alertService));

            WaitJob waitForRemoval = WaitJob.createUsingSystemTimeWhenInvoked(logger, 60, 5, createWaitForNoService());
            waitForRemoval.waitFor();
        }

    }

    private WaitJobTask createWaitForNoService() {
        return new WaitJobTask() {
            @Override
            public boolean isComplete() throws IntegrationException {
                logger.info(String.format("Checking if the service \"%s\" is removed...", alertService.getDockerName()));
                DockerServices currentDockerServices = createDockerOutput(dockerCommands::listServiceNames, DockerServices::create);
                return !currentDockerServices.doesServiceExist(alertService);
            }
        };
    }

}
