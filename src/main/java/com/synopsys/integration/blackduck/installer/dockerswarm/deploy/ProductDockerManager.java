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
package com.synopsys.integration.blackduck.installer.dockerswarm.deploy;

import com.synopsys.integration.blackduck.installer.dockerswarm.DockerCommands;
import com.synopsys.integration.blackduck.installer.dockerswarm.output.DockerSecrets;
import com.synopsys.integration.blackduck.installer.dockerswarm.output.DockerServices;
import com.synopsys.integration.blackduck.installer.dockerswarm.output.DockerStacks;
import com.synopsys.integration.blackduck.installer.model.DockerSecret;
import com.synopsys.integration.executable.Executable;
import com.synopsys.integration.log.IntLogger;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public abstract class ProductDockerManager {
    protected IntLogger logger;
    protected final DockerCommands dockerCommands;
    protected final String stackName;

    private List<File> additionalOrchestrationFiles = new ArrayList<>();

    public ProductDockerManager(IntLogger logger, DockerCommands dockerCommands, String stackName, List<File> additionalOrchestrationFiles) {
        this.logger = logger;
        this.dockerCommands = dockerCommands;
        this.stackName = stackName;
        this.additionalOrchestrationFiles.addAll(additionalOrchestrationFiles);
    }

    public abstract List<Executable> createExecutables(File installDirectory, DockerStacks dockerStacks, DockerSecrets dockerSecrets, DockerServices dockerServices);

    protected void addSecret(List<Executable> executables, DockerSecrets dockerSecrets, DockerSecret dockerSecret) {
        if (!dockerSecrets.doesSecretExist(dockerSecret)) {
            executables.add(dockerCommands.createSecret(stackName, dockerSecret));
        } else {
            logger.info(String.format("The secret \"%s\" already existed - it will not be changed.", dockerSecret.getLabel()));
        }
    }

    public List<File> getAdditionalOrchestrationFiles() {
        return additionalOrchestrationFiles;
    }

}
