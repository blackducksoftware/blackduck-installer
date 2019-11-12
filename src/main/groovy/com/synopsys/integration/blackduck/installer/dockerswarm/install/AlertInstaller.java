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
package com.synopsys.integration.blackduck.installer.dockerswarm.install;

import com.synopsys.integration.blackduck.installer.dockerswarm.DockerCommands;
import com.synopsys.integration.blackduck.installer.dockerswarm.DockerStackDeploy;
import com.synopsys.integration.blackduck.installer.dockerswarm.OrchestrationFiles;
import com.synopsys.integration.blackduck.installer.dockerswarm.deploy.AlertDeployer;
import com.synopsys.integration.blackduck.installer.dockerswarm.edit.*;
import com.synopsys.integration.blackduck.installer.download.ZipFileDownloader;
import com.synopsys.integration.blackduck.installer.exception.BlackDuckInstallerException;
import com.synopsys.integration.executable.Executable;
import com.synopsys.integration.executable.ExecutableRunner;

import java.io.File;
import java.util.List;

public class AlertInstaller extends Installer {
    private final DockerStackDeploy deployBlackDuck;
    private final File blackDuckInstallDirectory;
    private final ConfigFileEditor hubWebServerEnvEditor;
    private final ConfigFileEditor alertLocalOverridesEditor;
    private final boolean useLocalOverrides;

    public AlertInstaller(ZipFileDownloader zipFileDownloader, ExecutableRunner executableRunner, AlertDeployer alertDeployer, DockerStackDeploy dockerStackDeploy, DockerCommands dockerCommands, DockerStackDeploy deployBlackDuck, File blackDuckInstallDirectory, HubWebServerEnvEditor hubWebServerEnvEditor, AlertLocalOverridesEditor alertLocalOverridesEditor, boolean useLocalOverrides) {
        super(zipFileDownloader, executableRunner, alertDeployer, dockerStackDeploy, dockerCommands);

        this.deployBlackDuck = deployBlackDuck;
        this.blackDuckInstallDirectory = blackDuckInstallDirectory;
        this.hubWebServerEnvEditor = hubWebServerEnvEditor;
        this.alertLocalOverridesEditor = alertLocalOverridesEditor;
        this.useLocalOverrides = useLocalOverrides;
    }

    @Override
    public void postDownloadProcessing(File installDirectory) throws BlackDuckInstallerException {
        hubWebServerEnvEditor.edit(blackDuckInstallDirectory);
        alertLocalOverridesEditor.edit(installDirectory);
    }

    @Override
    public void populateDockerStackDeploy(File installDirectory) {
        File dockerSwarm = new File(installDirectory, "docker-swarm");
        File hub = new File(dockerSwarm, "hub");
        addOrchestrationFile(hub, OrchestrationFiles.COMPOSE);

        if (useLocalOverrides) {
            addOrchestrationFile(dockerSwarm, OrchestrationFiles.LOCAL_OVERRIDES);
        }
    }

    @Override
    public void addAdditionalExecutables(List<Executable> executables) {
        super.addAdditionalExecutables(executables);
        executables.add(deployBlackDuck.createDeployExecutable());
    }

}
