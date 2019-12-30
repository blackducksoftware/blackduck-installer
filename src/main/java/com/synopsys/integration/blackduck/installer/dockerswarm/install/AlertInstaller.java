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
import com.synopsys.integration.blackduck.installer.dockerswarm.deploy.AlertDockerManager;
import com.synopsys.integration.blackduck.installer.dockerswarm.edit.AlertLocalOverridesEditor;
import com.synopsys.integration.blackduck.installer.dockerswarm.edit.ConfigFileEditor;
import com.synopsys.integration.blackduck.installer.download.ZipFileDownloader;
import com.synopsys.integration.blackduck.installer.exception.BlackDuckInstallerException;
import com.synopsys.integration.blackduck.installer.model.ExecutablesRunner;

import java.io.File;

public class AlertInstaller extends Installer {
    private final ConfigFileEditor alertLocalOverridesEditor;
    private final boolean useLocalOverrides;

    public AlertInstaller(ZipFileDownloader zipFileDownloader, ExecutablesRunner executablesRunner, AlertDockerManager alertDockerManager, DockerStackDeploy dockerStackDeploy, DockerCommands dockerCommands, AlertLocalOverridesEditor alertLocalOverridesEditor, boolean useLocalOverrides) {
        super(zipFileDownloader, executablesRunner, alertDockerManager, dockerStackDeploy, dockerCommands);

        this.alertLocalOverridesEditor = alertLocalOverridesEditor;
        this.useLocalOverrides = useLocalOverrides;
    }

    @Override
    public void postDownloadProcessing(File installDirectory) throws BlackDuckInstallerException {
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

}
