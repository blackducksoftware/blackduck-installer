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
package com.synopsys.integration.blackduck.installer.dockerswarm.blackduckinstall;

import com.synopsys.integration.blackduck.installer.dockerswarm.DockerCommands;
import com.synopsys.integration.blackduck.installer.dockerswarm.InstallMethod;
import com.synopsys.integration.blackduck.installer.model.CustomCertificate;
import com.synopsys.integration.executable.Executable;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class CleanInstall implements InstallMethod {
    private final DockerCommands dockerCommands;
    private final String stackName;
    private final CustomCertificate customCertificate;
    private final NewInstall newInstall;

    public CleanInstall(DockerCommands dockerCommands, String stackName, CustomCertificate customCertificate, NewInstall newInstall) {
        this.dockerCommands = dockerCommands;
        this.stackName = stackName;
        this.customCertificate = customCertificate;
        this.newInstall = newInstall;
    }

    @Override
    public boolean shouldPerformInstall() {
        return true;
    }

    public List<Executable> createInitialExecutables(File installDirectory) {
        List<Executable> executables = new ArrayList<>();

        executables.add(dockerCommands.stopStack(stackName));
        executables.add(dockerCommands.restartDocker());
        executables.add(dockerCommands.pruneSystem());

        if (!customCertificate.isEmpty()) {
            executables.add(dockerCommands.deleteSecret(stackName, customCertificate.getCertificate()));
            executables.add(dockerCommands.deleteSecret(stackName, customCertificate.getPrivateKey()));
        }

        executables.addAll(newInstall.createInitialExecutables(installDirectory));

        return executables;
    }

}
