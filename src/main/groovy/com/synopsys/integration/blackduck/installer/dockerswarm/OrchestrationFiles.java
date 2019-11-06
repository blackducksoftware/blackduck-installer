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

import java.io.File;
import java.util.Set;

public class OrchestrationFiles {
    public static final String COMPOSE = "docker-compose.yml";
    public static final String BDBA = "docker-compose.bdba.yml";
    public static final String DBMIGRATE = "docker-compose.dbmigrate.yml";
    public static final String EXTERNALDB = "docker-compose.externaldb.yml";
    public static final String LOCAL_OVERRIDES = "docker-compose.local-overrides.yml";

    public File dockerSwarmDirectory(File installDirectory) {
        return new File(installDirectory, "docker-swarm");
    }

    public void addOrchestrationFile(Set<String> orchestrationFiles, File installDirectory, String orchestrationFile) {
        orchestrationFiles.add(String.format("%s/docker-swarm/%s", installDirectory.getAbsolutePath(), orchestrationFile));
    }

}
