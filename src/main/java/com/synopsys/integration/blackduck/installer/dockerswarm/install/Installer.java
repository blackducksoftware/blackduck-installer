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
import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.executable.Executable;
import com.synopsys.integration.executable.ExecutableOutput;
import com.synopsys.integration.log.IntLogger;

import java.io.File;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

public abstract class Installer {
    protected final IntLogger logger;

    private final ZipFileDownloader zipFileDownloader;
    private final ProductDockerManager productDockerManager;
    private final DockerStackDeploy dockerStackDeploy;
    private final List<File> additionalOrchestrationFiles;

    protected final ExecutablesRunner executablesRunner;
    protected final DockerCommands dockerCommands;

    protected final String stackName;

    public Installer(IntLogger logger, ZipFileDownloader zipFileDownloader, ExecutablesRunner executablesRunner, ProductDockerManager productDockerManager, DockerStackDeploy dockerStackDeploy, DockerCommands dockerCommands, String stackName, List<File> additionalOrchestrationFiles) {
        this.logger = logger;
        this.zipFileDownloader = zipFileDownloader;
        this.executablesRunner = executablesRunner;
        this.productDockerManager = productDockerManager;
        this.dockerStackDeploy = dockerStackDeploy;
        this.dockerCommands = dockerCommands;
        this.stackName = stackName;
        this.additionalOrchestrationFiles = additionalOrchestrationFiles;
    }

    public abstract void postDownloadProcessing(File installDirectory) throws BlackDuckInstallerException;

    public abstract void preDockerStackDeployCleanup(InstallerDockerData installerDockerData) throws IntegrationException, InterruptedException;

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

    public InstallResult performInstall() throws IntegrationException, InterruptedException {
        File installDirectory = zipFileDownloader.download(this::postDownloadProcessing);

        DockerStacks dockerStacks = createDockerOutput(dockerCommands::listStackNames, DockerStacks::create);
        DockerSecrets dockerSecrets = createDockerOutput(dockerCommands::listSecretNames, DockerSecrets::create);
        DockerServices dockerServices = createDockerOutput(dockerCommands::listServiceNames, DockerServices::create);
        InstallerDockerData installerDockerData = new InstallerDockerData(installDirectory, dockerStacks, dockerSecrets, dockerServices);

        List<Executable> executables = productDockerManager.createExecutables(installerDockerData);
        addAdditionalExecutables(executables);

        int overallReturnCode = 0;
        overallReturnCode += executablesRunner.runExecutables(executables);

        populateDockerStackDeploy(installerDockerData);

        preDockerStackDeployCleanup(installerDockerData);

        Executable dockerStackDeployExecutable = dockerStackDeploy.createDeployExecutable();
        overallReturnCode += executablesRunner.runExecutableCode(dockerStackDeployExecutable);

        return new InstallResult(overallReturnCode, installDirectory, dockerStackDeploy);
    }

    protected <T extends Object> T createDockerOutput(Supplier<Executable> executableSupplier, Function<String, T> creator) throws BlackDuckInstallerException {
        Executable executable = executableSupplier.get();
        ExecutableOutput executableOutput = executablesRunner.runExecutable(executable);
        return creator.apply(executableOutput.getStandardOutput());
    }

    protected String createSimpleDockerOutput(Supplier<Executable> executableSupplier) throws BlackDuckInstallerException {
        Executable executable = executableSupplier.get();
        ExecutableOutput executableOutput = executablesRunner.runExecutable(executable);
        return executableOutput.getStandardOutput();
    }

}
