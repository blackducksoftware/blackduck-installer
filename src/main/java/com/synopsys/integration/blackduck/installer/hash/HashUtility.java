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
package com.synopsys.integration.blackduck.installer.hash;

import com.synopsys.integration.blackduck.installer.exception.BlackDuckInstallerException;
import org.apache.commons.codec.digest.DigestUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

public class HashUtility {
    /*
    If the files to edit *do* change, this utility hopes to make it simpler to update PreComputedHashes.java.
     */
    private static final String HUB_WEBSERVER_ENV_PATH = "C:\\Users\\ekerwin\\Downloads\\original\\hub-2019.8.1\\docker-swarm\\hub-webserver.env";
    private static final String DOCKER_COMPOSE_LOCAL_OVERRIDES_YML_PATH = "C:\\Users\\ekerwin\\Downloads\\original\\hub-2019.8.1\\docker-swarm\\docker-compose.local-overrides.yml";
    private static final String BLACKDUCK_CONFIG_ENV_PATH = "C:\\Users\\ekerwin\\Downloads\\original\\hub-2019.8.1\\docker-swarm\\blackduck-config.env";
    private static final String ALERT_DOCKER_COMPOSE_LOCAL_OVERRIDES_YML_PATH = "C:\\Users\\ekerwin\\Downloads\\blackduck-alert-5.0.0-deployment\\docker-swarm\\docker-compose.local-overrides.yml";

    public static void main(String[] args) throws Exception {
        HashUtility hashUtility = new HashUtility();

        //hub-webserver.env
        hashUtility.hashFileForComputedHashesDotJava(new File(HUB_WEBSERVER_ENV_PATH), "HUB_WEBSERVER_ENV");

        //docker-compose.local-overrides.yml
        hashUtility.hashFileForComputedHashesDotJava(new File(DOCKER_COMPOSE_LOCAL_OVERRIDES_YML_PATH), "DOCKER_COMPOSE_LOCAL_OVERRIDES_YML");

        //blackduck-config.env
        hashUtility.hashFileForComputedHashesDotJava(new File(BLACKDUCK_CONFIG_ENV_PATH), "BLACKDUCK_CONFIG_ENV");

        //alert docker-compose.local-overrides.yml
        hashUtility.hashFileForComputedHashesDotJava(new File(ALERT_DOCKER_COMPOSE_LOCAL_OVERRIDES_YML_PATH), "ALERT_DOCKER_COMPOSE_LOCAL_OVERRIDES_YML");
    }

    public String computeHash(File fileToHash) throws BlackDuckInstallerException {
        try {
            return DigestUtils.sha256Hex(Files.readAllBytes(fileToHash.toPath()));
        } catch (IOException e) {
            throw new BlackDuckInstallerException(String.format("Could not hash the file (%s): %s", fileToHash.getAbsolutePath(), e.getMessage()));
        }
    }

    private void hashFileForComputedHashesDotJava(File fileToHash, String name) throws BlackDuckInstallerException {
        String hash = computeHash(fileToHash);
        System.out.println("public static final String " + name + " = \"" + hash + "\";");
    }

}
