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
import com.synopsys.integration.blackduck.installer.dockerswarm.deploy.BlackDuckDockerManager;
import com.synopsys.integration.blackduck.installer.dockerswarm.edit.BlackDuckConfigEnvEditor;
import com.synopsys.integration.blackduck.installer.dockerswarm.edit.ConfigFileEditor;
import com.synopsys.integration.blackduck.installer.dockerswarm.edit.HubWebServerEnvEditor;
import com.synopsys.integration.blackduck.installer.dockerswarm.edit.LocalOverridesEditor;
import com.synopsys.integration.blackduck.installer.download.ZipFileDownloader;
import com.synopsys.integration.blackduck.installer.exception.BlackDuckInstallerException;
import com.synopsys.integration.blackduck.installer.model.ExecutablesRunner;

import java.io.File;

public class BlackDuckInstaller extends Installer {
    private final ConfigFileEditor blackDuckConfigEnvEditor;
    private final ConfigFileEditor hubWebServerEnvEditor;
    private final ConfigFileEditor localOverridesEditor;
    private final boolean useLocalOverrides;

    public BlackDuckInstaller(ZipFileDownloader zipFileDownloader, ExecutablesRunner executablesRunner, BlackDuckDockerManager blackDuckDockerManager, DockerStackDeploy dockerStackDeploy, DockerCommands dockerCommands, BlackDuckConfigEnvEditor blackDuckConfigEnvEditor, HubWebServerEnvEditor hubWebServerEnvEditor, LocalOverridesEditor localOverridesEditor, boolean useLocalOverrides) {
        super(zipFileDownloader, executablesRunner, blackDuckDockerManager, dockerStackDeploy, dockerCommands);

        this.blackDuckConfigEnvEditor = blackDuckConfigEnvEditor;
        this.hubWebServerEnvEditor = hubWebServerEnvEditor;
        this.localOverridesEditor = localOverridesEditor;
        this.useLocalOverrides = useLocalOverrides;
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

        if (useLocalOverrides) {
            addOrchestrationFile(dockerSwarm, OrchestrationFiles.LOCAL_OVERRIDES);
        }
    }


}
