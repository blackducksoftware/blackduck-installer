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

import com.synopsys.integration.blackduck.installer.dockerswarm.*;
import com.synopsys.integration.blackduck.installer.dockerswarm.deploy.ProductDockerManager;
import com.synopsys.integration.blackduck.installer.dockerswarm.output.DockerSecrets;
import com.synopsys.integration.blackduck.installer.dockerswarm.output.DockerServices;
import com.synopsys.integration.blackduck.installer.dockerswarm.output.DockerStacks;
import com.synopsys.integration.blackduck.installer.download.ZipFileDownloader;
import com.synopsys.integration.blackduck.installer.exception.BlackDuckInstallerException;
import com.synopsys.integration.blackduck.installer.model.ExecutablesRunner;
import com.synopsys.integration.blackduck.installer.model.InstallResult;
import com.synopsys.integration.executable.Executable;
import com.synopsys.integration.executable.ExecutableOutput;

import java.io.File;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

public abstract class Installer {
    private final ZipFileDownloader zipFileDownloader;
    private final ExecutablesRunner executablesRunner;
    private final ProductDockerManager productDockerManager;
    private final DockerStackDeploy dockerStackDeploy;
    private final DockerCommands dockerCommands;
    private final List<File> additionalOrchestrationFiles;

    public Installer(ZipFileDownloader zipFileDownloader, ExecutablesRunner executablesRunner, ProductDockerManager productDockerManager, DockerStackDeploy dockerStackDeploy, DockerCommands dockerCommands, List<File> additionalOrchestrationFiles) {
        this.zipFileDownloader = zipFileDownloader;
        this.executablesRunner = executablesRunner;
        this.productDockerManager = productDockerManager;
        this.dockerStackDeploy = dockerStackDeploy;
        this.dockerCommands = dockerCommands;
        this.additionalOrchestrationFiles = additionalOrchestrationFiles;
    }

    public abstract void postDownloadProcessing(File installDirectory) throws BlackDuckInstallerException;

    public abstract void populateDockerStackDeploy(InstallerDockerData installerDockerData);

    public void addAdditionalExecutables(List<Executable> executables) {
    }

    public void addOrchestrationFile(File orchestrationDirectory, String orchestrationFile) {
        dockerStackDeploy.addOrchestrationFile(orchestrationDirectory, orchestrationFile);
    }

    public void addAdditionalOrchestrationFiles() {
        if (!additionalOrchestrationFiles.isEmpty()) {
            additionalOrchestrationFiles
                    .stream()
                    .forEach(dockerStackDeploy::addOrchestrationFile);
        }
    }

    public InstallResult performInstall() throws BlackDuckInstallerException {
        File installDirectory = zipFileDownloader.download();

        postDownloadProcessing(installDirectory);

        DockerStacks dockerStacks = createDockerOutput(dockerCommands::listStackNames, DockerStacks::create);
        DockerSecrets dockerSecrets = createDockerOutput(dockerCommands::listSecretNames, DockerSecrets::create);
        DockerServices dockerServices = createDockerOutput(dockerCommands::listServiceNames, DockerServices::create);

        List<Executable> executables = productDockerManager.createExecutables(installDirectory, dockerStacks, dockerSecrets, dockerServices);
        addAdditionalExecutables(executables);

        int overallReturnCode = 0;
        overallReturnCode += executablesRunner.runExecutables(executables);

        InstallerDockerData installerDockerData = new InstallerDockerData(installDirectory, dockerStacks, dockerSecrets, dockerServices);
        populateDockerStackDeploy(installerDockerData);

        Executable dockerStackDeployExecutable = dockerStackDeploy.createDeployExecutable();
        overallReturnCode += executablesRunner.runExecutableCode(dockerStackDeployExecutable);

        return new InstallResult(overallReturnCode, installDirectory, dockerStackDeploy);
    }

    private <T extends Object> T createDockerOutput(Supplier<Executable> executableSupplier, Function<String, T> creator) throws BlackDuckInstallerException {
        Executable executable = executableSupplier.get();
        ExecutableOutput executableOutput = executablesRunner.runExecutable(executable);
        return creator.apply(executableOutput.getStandardOutput());
    }

}
