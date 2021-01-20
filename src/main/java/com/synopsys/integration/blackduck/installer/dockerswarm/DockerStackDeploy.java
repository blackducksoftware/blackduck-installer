/**
 * blackduck-installer
 *
 * Copyright (c) 2021 Synopsys, Inc.
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

import com.synopsys.integration.executable.Executable;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class DockerStackDeploy {
    private final String stackName;
    private final Set<String> orchestrationFiles = new LinkedHashSet<>();

    public DockerStackDeploy(String stackName) {
        this.stackName = stackName;
    }

    public void addOrchestrationFile(File orchestrationDirectory, String orchestrationFile) {
        addOrchestrationFile(new File(orchestrationDirectory, orchestrationFile));
    }

    public void addOrchestrationFile(File orchestrationFile) {
        orchestrationFiles.add(orchestrationFile.getAbsolutePath());
    }

    public Executable createDeployExecutable() {
        List<String> deployCommand = new ArrayList<>();
        deployCommand.add("docker");
        deployCommand.add("stack");
        deployCommand.add("deploy");

        for (String orchestrationFile : orchestrationFiles) {
            deployCommand.add("-c");
            deployCommand.add(orchestrationFile);
        }

        deployCommand.add(stackName);

        return Executable.create(new File("."), deployCommand);
    }

}
