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

import com.synopsys.integration.blackduck.installer.model.DockerSecret;
import com.synopsys.integration.blackduck.installer.model.DockerService;
import com.synopsys.integration.blackduck.installer.model.ExecutableCreator;
import com.synopsys.integration.executable.Executable;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;

public class DockerCommands {
    private ExecutableCreator executableCreator;

    public DockerCommands(ExecutableCreator executableCreator) {
        this.executableCreator = executableCreator;
    }

    public Executable stopStack(String stackName) {
        String fullCommand = String.format("docker stack rm %s", stackName);
        return executableCreator.createExecutable(fullCommand);
    }

    public Executable restartDocker() {
        return executableCreator.createExecutable("systemctl restart docker");
    }

    public Executable listStackNames() {
        String fullCommand = "docker stack ls --format \"{{.Name}}\"";
        return executableCreator.createExecutable(fullCommand);
    }

    public Executable listSecretNames() {
        String fullCommand = "docker secret ls --format \"{{.Name}}\"";
        return executableCreator.createExecutable(fullCommand);
    }

    public Executable listServiceNames() {
        String fullCommand = "docker service ls --format \"{{.Name}}\"";
        return executableCreator.createExecutable(fullCommand);
    }

    public Executable removeService(DockerService dockerService) {
        String fullCommand = String.format("docker service rm %s", dockerService.getDockerName());
        return executableCreator.createExecutable(fullCommand);
    }

    public Executable createSecret(String stackName, DockerSecret dockerSecret) {
        String fullCommand = String.format("docker secret create %s_%s %s", stackName, dockerSecret.getLabel(), dockerSecret.getPath());
        return executableCreator.createExecutable(fullCommand);
    }

}
