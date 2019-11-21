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
package com.synopsys.integration.blackduck.installer.dockerswarm.deploy;

import com.synopsys.integration.blackduck.installer.dockerswarm.DockerCommands;
import com.synopsys.integration.blackduck.installer.dockerswarm.DockerSecrets;
import com.synopsys.integration.blackduck.installer.dockerswarm.DockerServices;
import com.synopsys.integration.blackduck.installer.dockerswarm.DockerStacks;
import com.synopsys.integration.blackduck.installer.model.CustomCertificate;
import com.synopsys.integration.executable.Executable;
import com.synopsys.integration.log.IntLogger;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class BlackDuckDockerManager extends ProductDockerManager {
    private final CustomCertificate customCertificate;

    public BlackDuckDockerManager(IntLogger logger, DockerCommands dockerCommands, String stackName, CustomCertificate customCertificate) {
        super(logger, dockerCommands, stackName);
        this.customCertificate = customCertificate;
    }

    public List<Executable> createExecutables(File installDirectory, DockerStacks dockerStacks, DockerSecrets dockerSecrets, DockerServices dockerServices) {
        List<Executable> executables = new ArrayList<>();

        if (dockerStacks.doesStackExist(stackName)) {
            logger.info(String.format("The stack \"%s\" already existed - removing it and restarting docker.", stackName));
            executables.add(dockerCommands.stopStack(stackName));
            //TODO it would be better to list services and wait for nothing stackName_, as the permissions for restarting Docker could be more restrictive
            executables.add(dockerCommands.restartDocker());
        }

        if (!customCertificate.isEmpty()) {
            addSecret(executables, dockerSecrets, customCertificate.getCertificate());
            addSecret(executables, dockerSecrets, customCertificate.getPrivateKey());
        }

        return executables;
    }

}
