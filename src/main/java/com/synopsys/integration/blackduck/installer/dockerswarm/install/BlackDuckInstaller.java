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
package com.synopsys.integration.blackduck.installer.dockerswarm.install;

import java.io.File;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import com.synopsys.integration.blackduck.installer.dockerswarm.DockerCommands;
import com.synopsys.integration.blackduck.installer.dockerswarm.DockerStackDeploy;
import com.synopsys.integration.blackduck.installer.dockerswarm.OrchestrationFiles;
import com.synopsys.integration.blackduck.installer.dockerswarm.deploy.BlackDuckDockerManager;
import com.synopsys.integration.blackduck.installer.dockerswarm.edit.BlackDuckConfigEnvEditor;
import com.synopsys.integration.blackduck.installer.dockerswarm.edit.BlackDuckLocalOverridesEditor;
import com.synopsys.integration.blackduck.installer.dockerswarm.edit.ConfigFileEditor;
import com.synopsys.integration.blackduck.installer.dockerswarm.edit.HubWebServerEnvEditor;
import com.synopsys.integration.blackduck.installer.download.ZipFileDownloader;
import com.synopsys.integration.blackduck.installer.exception.BlackDuckInstallerException;
import com.synopsys.integration.blackduck.installer.model.BlackDuckAdditionalOrchestrationFiles;
import com.synopsys.integration.blackduck.installer.model.ExecutablesRunner;
import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.log.IntLogger;
import com.synopsys.integration.wait.WaitJob;
import com.synopsys.integration.wait.WaitJobTask;

public class BlackDuckInstaller extends Installer {
    private final ConfigFileEditor blackDuckConfigEnvEditor;
    private final ConfigFileEditor hubWebServerEnvEditor;
    private final ConfigFileEditor localOverridesEditor;
    private final boolean useLocalOverrides;
    private final BlackDuckAdditionalOrchestrationFiles blackDuckAdditionalOrchestrationFiles;

    public BlackDuckInstaller(IntLogger logger, ZipFileDownloader zipFileDownloader, ExecutablesRunner executablesRunner, BlackDuckDockerManager blackDuckDockerManager, DockerStackDeploy dockerStackDeploy, DockerCommands dockerCommands,
        String stackName, List<File> additionalOrchestrationFiles, BlackDuckConfigEnvEditor blackDuckConfigEnvEditor, HubWebServerEnvEditor hubWebServerEnvEditor, BlackDuckLocalOverridesEditor blackDuckLocalOverridesEditor,
        boolean useLocalOverrides, BlackDuckAdditionalOrchestrationFiles blackDuckAdditionalOrchestrationFiles) {
        super(logger, zipFileDownloader, executablesRunner, blackDuckDockerManager, dockerStackDeploy, dockerCommands, stackName, additionalOrchestrationFiles);

        this.blackDuckConfigEnvEditor = blackDuckConfigEnvEditor;
        this.hubWebServerEnvEditor = hubWebServerEnvEditor;
        this.localOverridesEditor = blackDuckLocalOverridesEditor;
        this.useLocalOverrides = useLocalOverrides;
        this.blackDuckAdditionalOrchestrationFiles = blackDuckAdditionalOrchestrationFiles;
    }

    @Override
    public void postDownloadProcessing(File installDirectory) throws BlackDuckInstallerException {
        blackDuckConfigEnvEditor.edit(installDirectory);
        hubWebServerEnvEditor.edit(installDirectory);
        localOverridesEditor.edit(installDirectory);
    }

    @Override
    public void populateDockerStackDeploy(InstallerDockerData installerDockerData) {
        File dockerSwarm = new File(installerDockerData.getInstallDirectory(), "docker-swarm");
        addOrchestrationFile(dockerSwarm, OrchestrationFiles.COMPOSE);

        addAdditionalOrchestrationFiles();

        blackDuckAdditionalOrchestrationFiles.getBlackDuckAdditionalOrchestrationFilePaths()
            .stream()
            .map(blackDuckOrchestrationFile -> blackDuckOrchestrationFile.filePath)
            .forEach(filePath -> addOrchestrationFile(dockerSwarm, filePath));

        if (useLocalOverrides) {
            addOrchestrationFile(dockerSwarm, OrchestrationFiles.LOCAL_OVERRIDES);
        }
    }

    @Override
    public void preDockerStackDeployCleanup(InstallerDockerData installerDockerData) throws IntegrationException, InterruptedException {
        if (installerDockerData.getDockerStacks().doesStackExist(stackName)) {
            logger.info(String.format("The stack \"%s\" already existed - removing it.", stackName));
            executablesRunner.runExecutable(dockerCommands.stopStack(stackName));

            WaitJob waitForRemoval = WaitJob.createUsingSystemTimeWhenInvoked(logger, 60, 5, createWaitForNoStack());
            waitForRemoval.waitFor();
        }
    }

    private WaitJobTask createWaitForNoStack() {
        return new WaitJobTask() {
            @Override
            public boolean isComplete() throws IntegrationException {
                logger.info(String.format("Checking if any services or networks remain for stack \"%s\"...", stackName));
                String simpleServiceOutput = createSimpleDockerOutput(() -> dockerCommands.listServiceNamesUsingStack(stackName));
                String simpleNetworkOutput = createSimpleDockerOutput(() -> dockerCommands.listNetworkNamesUsingStack(stackName));

                return StringUtils.isAllBlank(simpleServiceOutput, simpleNetworkOutput);
            }
        };
    }

}
