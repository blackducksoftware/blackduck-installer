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
import com.synopsys.integration.blackduck.installer.dockerswarm.deploy.AlertDockerManager;
import com.synopsys.integration.blackduck.installer.dockerswarm.edit.AlertLocalOverridesEditor;
import com.synopsys.integration.blackduck.installer.dockerswarm.edit.ConfigFileEditor;
import com.synopsys.integration.blackduck.installer.dockerswarm.edit.HubWebServerEnvEditor;
import com.synopsys.integration.blackduck.installer.download.ZipFileDownloader;
import com.synopsys.integration.blackduck.installer.exception.BlackDuckInstallerException;
import com.synopsys.integration.blackduck.installer.model.ExecutablesRunner;
import com.synopsys.integration.executable.Executable;

import java.io.File;
import java.util.List;

public class AlertWithBlackDuckInstaller extends AlertInstaller {
    private final DockerStackDeploy deployBlackDuck;
    private final File blackDuckInstallDirectory;
    private final ConfigFileEditor hubWebServerEnvEditor;

    public AlertWithBlackDuckInstaller(ZipFileDownloader zipFileDownloader, ExecutablesRunner executablesRunner, AlertDockerManager alertDockerManager, DockerStackDeploy dockerStackDeploy, DockerCommands dockerCommands, DockerStackDeploy deployBlackDuck, File blackDuckInstallDirectory, HubWebServerEnvEditor hubWebServerEnvEditor, AlertLocalOverridesEditor alertLocalOverridesEditor, boolean useLocalOverrides) {
        super(zipFileDownloader, executablesRunner, alertDockerManager, dockerStackDeploy, dockerCommands, alertLocalOverridesEditor, useLocalOverrides);

        this.deployBlackDuck = deployBlackDuck;
        this.blackDuckInstallDirectory = blackDuckInstallDirectory;
        this.hubWebServerEnvEditor = hubWebServerEnvEditor;
    }

    @Override
    public void postDownloadProcessing(File installDirectory) throws BlackDuckInstallerException {
        super.postDownloadProcessing(installDirectory);
        hubWebServerEnvEditor.edit(blackDuckInstallDirectory);
    }

    @Override
    public void addAdditionalExecutables(List<Executable> executables) {
        super.addAdditionalExecutables(executables);
        executables.add(deployBlackDuck.createDeployExecutable());
    }

}
