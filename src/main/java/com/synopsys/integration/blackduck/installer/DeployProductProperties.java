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
package com.synopsys.integration.blackduck.installer;

import com.synopsys.integration.blackduck.installer.dockerswarm.DockerCommands;
import com.synopsys.integration.blackduck.installer.dockerswarm.DockerStackDeploy;
import com.synopsys.integration.blackduck.installer.hash.HashUtility;
import com.synopsys.integration.blackduck.installer.model.CustomCertificate;
import com.synopsys.integration.blackduck.installer.model.ExecutablesRunner;
import com.synopsys.integration.blackduck.installer.model.FilePathTransformer;
import com.synopsys.integration.log.IntLogger;
import com.synopsys.integration.rest.client.IntHttpClient;
import com.synopsys.integration.util.CommonZipExpander;

import java.io.File;

public class DeployProductProperties {
    private final File baseDirectory;
    private final String lineSeparator;
    private final IntLogger intLogger;
    private final HashUtility hashUtility;
    private final FilePathTransformer filePathTransformer;
    private final DockerCommands dockerCommands;
    private final CommonZipExpander commonZipExpander;
    private final CustomCertificate customCertificate;
    private final IntHttpClient intHttpClient;
    private final ExecutablesRunner executablesRunner;
    private final DockerStackDeploy deployStack;

    public DeployProductProperties(File baseDirectory, String lineSeparator, IntLogger intLogger, HashUtility hashUtility, FilePathTransformer filePathTransformer, DockerCommands dockerCommands, CommonZipExpander commonZipExpander, CustomCertificate customCertificate, IntHttpClient intHttpClient, ExecutablesRunner executablesRunner, DockerStackDeploy deployStack) {
        this.baseDirectory = baseDirectory;
        this.lineSeparator = lineSeparator;
        this.intLogger = intLogger;
        this.hashUtility = hashUtility;
        this.filePathTransformer = filePathTransformer;
        this.dockerCommands = dockerCommands;
        this.commonZipExpander = commonZipExpander;
        this.customCertificate = customCertificate;
        this.intHttpClient = intHttpClient;
        this.executablesRunner = executablesRunner;
        this.deployStack = deployStack;
    }

    public File getBaseDirectory() {
        return baseDirectory;
    }

    public String getLineSeparator() {
        return lineSeparator;
    }

    public IntLogger getIntLogger() {
        return intLogger;
    }

    public HashUtility getHashUtility() {
        return hashUtility;
    }

    public FilePathTransformer getFilePathTransformer() {
        return filePathTransformer;
    }

    public DockerCommands getDockerCommands() {
        return dockerCommands;
    }

    public CommonZipExpander getCommonZipExpander() {
        return commonZipExpander;
    }

    public CustomCertificate getCustomCertificate() {
        return customCertificate;
    }

    public IntHttpClient getIntHttpClient() {
        return intHttpClient;
    }

    public ExecutablesRunner getExecutablesRunner() {
        return executablesRunner;
    }

    public DockerStackDeploy getDeployStack() {
        return deployStack;
    }

}
