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
package com.synopsys.integration.blackduck.installer.dockerswarm;

import com.synopsys.integration.blackduck.installer.download.ZipFileDownloader;
import com.synopsys.integration.blackduck.installer.exception.BlackDuckInstallerException;
import com.synopsys.integration.blackduck.installer.model.InstallResult;
import com.synopsys.integration.executable.Executable;
import com.synopsys.integration.executable.ExecutableOutput;
import com.synopsys.integration.executable.ExecutableRunner;
import com.synopsys.integration.executable.ExecutableRunnerException;

import java.io.File;
import java.util.List;

public abstract class Installer {
    private final ZipFileDownloader zipFileDownloader;
    private final ExecutableRunner executableRunner;
    private final InstallMethod installMethod;
    private final DockerStackDeploy dockerStackDeploy;

    public Installer(ZipFileDownloader zipFileDownloader, ExecutableRunner executableRunner, InstallMethod installMethod, DockerStackDeploy dockerStackDeploy) {
        this.zipFileDownloader = zipFileDownloader;
        this.executableRunner = executableRunner;
        this.installMethod = installMethod;
        this.dockerStackDeploy = dockerStackDeploy;
    }

    public abstract void postDownloadProcessing(File installDirectory) throws BlackDuckInstallerException;

    public abstract void populateDockerStackDeploy(File installDirectory);

    public void addAdditionalExecutables(List<Executable> executables) {
    }

    public void addOrchestrationFile(File orchestrationDirectory, String orchestrationFile) {
        dockerStackDeploy.addOrchestrationFile(orchestrationDirectory, orchestrationFile);
    }

    public InstallResult performInstall() throws BlackDuckInstallerException {
        if (NoInstall.DO_NOT_INSTALL == installMethod) {
            return new InstallResult(0, new File("."));
        }

        File installDirectory = zipFileDownloader.download();

        postDownloadProcessing(installDirectory);

        List<Executable> executables = installMethod.createInitialExecutables(installDirectory);
        addAdditionalExecutables(executables);

        int overallReturnCode = 0;
        for (Executable executable : executables) {
            overallReturnCode += runExecutable(executable);
        }

        populateDockerStackDeploy(installDirectory);

        Executable dockerStackDeployExecutable = dockerStackDeploy.createDeployExecutable();
        overallReturnCode += runExecutable(dockerStackDeployExecutable);

        return new InstallResult(overallReturnCode, installDirectory);
    }

    private int runExecutable(Executable executable) throws BlackDuckInstallerException {
        try {
            ExecutableOutput executableOutput = executableRunner.execute(executable);
            return Math.abs(executableOutput.getReturnCode());
        } catch (ExecutableRunnerException e) {
            throw new BlackDuckInstallerException("Exception running executable: " + executable.getExecutableDescription(), e);
        }
    }

}
