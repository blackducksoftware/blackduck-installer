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

import com.synopsys.integration.executable.Executable;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class DockerCommands {
    public static final String SECRET_CERT = "WEBSERVER_CUSTOM_CERT_FILE";
    public static final String SECRET_KEY = "WEBSERVER_CUSTOM_KEY_FILE";
    public static final String SECRET_ALERT_ENCRYPTION_PASSWORD = "ALERT_ENCRYPTION_PASSWORD";
    public static final String SECRET_ALERT_ENCRYPTION_GLOBAL_SALT = "ALERT_ENCRYPTION_GLOBAL_SALT";

    public Executable startStack(File installDirectory, String stackName) {
        return startStack(installDirectory, stackName, Collections.emptyList());
    }

    public Executable startStack(File installDirectory, String stackName, List<String> additionalFiles) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("docker stack deploy -c docker-compose.yml");
        for (String additionalFile : additionalFiles) {
            stringBuilder.append(" -c ");
            stringBuilder.append(additionalFile);
        }
        stringBuilder.append(" ");
        stringBuilder.append(stackName);

        String fullCommand = stringBuilder.toString();
        return createExecutable(installDirectory, fullCommand);
    }

    public Executable stopStack(String stackName) {
        String fullCommand = String.format("docker stack rm %s", stackName);
        return createExecutable(fullCommand);
    }

    public Executable restartDocker() {
        return createExecutable("systemctl restart docker");
    }

    public Executable pruneSystem() {
        return createExecutable("docker system prune --all --volumes --force");
    }

    public Executable createSecretCert(String stackName, String certPath) {
        String fullCommand = String.format("docker secret create %s%s %s", stackName, SECRET_CERT, certPath);
        return createExecutable(fullCommand);
    }

    public Executable createSecretKey(String stackName, String keyPath) {
        String fullCommand = String.format("docker secret create %s%s %s", stackName, SECRET_KEY, keyPath);
        return createExecutable(fullCommand);
    }

    public Executable createSecretAlertPassword(String stackName, String passwordPath) {
        String fullCommand = String.format("docker secret create %s%s %s", stackName, SECRET_ALERT_ENCRYPTION_PASSWORD, passwordPath);
        return createExecutable(fullCommand);
    }

    public Executable createSecretAlertSalt(String stackName, String saltPath) {
        String fullCommand = String.format("docker secret create %s%s %s", stackName, SECRET_KEY, saltPath);
        return createExecutable(fullCommand);
    }

    public Executable deleteSecretCert(String stackName) {
        String fullCommand = String.format("docker secret rm %s%s", stackName, SECRET_CERT);
        return createExecutable(fullCommand);
    }

    public Executable deleteSecretKey(String stackName) {
        String fullCommand = String.format("docker secret rm %s%s", stackName, SECRET_KEY);
        return createExecutable(fullCommand);
    }

    public Executable deleteSecretAlertPassword(String stackName) {
        String fullCommand = String.format("docker secret rm %s%s", stackName, SECRET_ALERT_ENCRYPTION_PASSWORD);
        return createExecutable(fullCommand);
    }

    public Executable deleteSecretAlertSalt(String stackName) {
        String fullCommand = String.format("docker secret rm %s%s", stackName, SECRET_ALERT_ENCRYPTION_GLOBAL_SALT);
        return createExecutable(fullCommand);
    }

    private File getWorkingDirectory(File installDirectory) {
        return new File(installDirectory, "docker-swarm");
    }

    private Executable createExecutable(String fullCommand) {
        List<String> commandPieces = Arrays.asList(fullCommand.split(" "));
        return Executable.create(new File("."), commandPieces);
    }

    private Executable createExecutable(File installDirectory, String fullCommand) {
        List<String> commandPieces = Arrays.asList(fullCommand.split(" "));
        return Executable.create(getWorkingDirectory(installDirectory), commandPieces);
    }

}
