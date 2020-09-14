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

import java.io.File;
import java.util.List;

import com.synopsys.integration.blackduck.installer.ApplicationValues;
import com.synopsys.integration.blackduck.installer.DeployAlertProperties;
import com.synopsys.integration.blackduck.installer.DeployProductProperties;
import com.synopsys.integration.blackduck.installer.dockerswarm.DockerCommands;
import com.synopsys.integration.blackduck.installer.dockerswarm.DockerStackDeploy;
import com.synopsys.integration.blackduck.installer.dockerswarm.deploy.AlertDockerManager;
import com.synopsys.integration.blackduck.installer.dockerswarm.edit.AlertLocalOverridesEditor;
import com.synopsys.integration.blackduck.installer.download.AlertGithubDownloadUrl;
import com.synopsys.integration.blackduck.installer.download.ArtifactoryDownloadUrl;
import com.synopsys.integration.blackduck.installer.download.ZipFileDownloader;
import com.synopsys.integration.blackduck.installer.exception.BlackDuckInstallerException;
import com.synopsys.integration.blackduck.installer.hash.HashUtility;
import com.synopsys.integration.blackduck.installer.model.AlertBlackDuckInstallOptions;
import com.synopsys.integration.blackduck.installer.model.AlertDatabase;
import com.synopsys.integration.blackduck.installer.model.AlertEncryption;
import com.synopsys.integration.blackduck.installer.model.CustomCertificate;
import com.synopsys.integration.blackduck.installer.model.DockerService;
import com.synopsys.integration.blackduck.installer.workflow.DownloadUrlDecider;
import com.synopsys.integration.log.IntLogger;
import com.synopsys.integration.rest.client.IntHttpClient;
import com.synopsys.integration.util.CommonZipExpander;

public class AlertInstallerCreator {
    private ApplicationValues applicationValues;
    private DeployProductProperties deployProductProperties;
    private DeployAlertProperties deployAlertProperties;

    public AlertInstallerCreator(ApplicationValues applicationValues, DeployProductProperties deployProductProperties, DeployAlertProperties deployAlertProperties) {
        this.applicationValues = applicationValues;
        this.deployProductProperties = deployProductProperties;
        this.deployAlertProperties = deployAlertProperties;
    }

    public AlertInstaller create() throws BlackDuckInstallerException {
        IntLogger intLogger = deployProductProperties.getIntLogger();
        HashUtility hashUtility = deployProductProperties.getHashUtility();
        String lineSeparator = deployProductProperties.getLineSeparator();
        DockerCommands dockerCommands = deployProductProperties.getDockerCommands();
        IntHttpClient intHttpClient = deployProductProperties.getIntHttpClient();
        CommonZipExpander commonZipExpander = deployProductProperties.getCommonZipExpander();
        File baseDirectory = deployProductProperties.getBaseDirectory();
        String stackName = applicationValues.getStackName();
        CustomCertificate customCertificate = deployProductProperties.getCustomCertificate();

        DockerService alertService = deployAlertProperties.getAlertService();
        AlertBlackDuckInstallOptions alertBlackDuckInstallOptions = deployAlertProperties.getAlertBlackDuckInstallOptions();
        AlertEncryption alertEncryption = deployAlertProperties.getAlertEncryption();
        AlertDatabase alertDatabase = deployAlertProperties.getAlertDatabase();

        AlertGithubDownloadUrl alertGithubDownloadUrl = new AlertGithubDownloadUrl(applicationValues.getAlertGithubDownloadUrlPrefix(), applicationValues.getAlertVersion());
        ArtifactoryDownloadUrl alertArtifactoryDownloadUrl = new ArtifactoryDownloadUrl(applicationValues.getAlertArtifactoryUrl(), applicationValues.getAlertArtifactoryRepo(), applicationValues.getAlertArtifactPath(),
            applicationValues.getAlertArtifact(), applicationValues.getAlertVersion());
        DownloadUrlDecider downloadUrlDecider = new DownloadUrlDecider(applicationValues.getAlertDownloadSource(), alertGithubDownloadUrl::getDownloadUrl, alertArtifactoryDownloadUrl::getDownloadUrl);

        boolean useLocalOverrides = applicationValues.isAlertInstallUseLocalOverrides();
        if (!deployProductProperties.getCustomCertificate().isEmpty() || !alertEncryption.isEmpty() || !alertBlackDuckInstallOptions.isEmpty() || !alertDatabase.isEmpty()) {
            useLocalOverrides = true;
        }
        AlertLocalOverridesEditor alertLocalOverridesEditor = new AlertLocalOverridesEditor(intLogger, hashUtility, lineSeparator, stackName, applicationValues.getWebServerHost(), applicationValues.getAlertInstallDefaultAdminEmail(),
            alertEncryption, customCertificate, alertBlackDuckInstallOptions, useLocalOverrides, alertDatabase);
        ZipFileDownloader alertDownloader = new ZipFileDownloader(intLogger, intHttpClient, commonZipExpander, downloadUrlDecider, baseDirectory, "blackduck-alert", applicationValues.getAlertVersion(),
            applicationValues.isAlertDownloadForce());
        DockerStackDeploy dockerStackDeploy = new DockerStackDeploy(stackName);

        List<String> additionalOrchestrationFilePaths = applicationValues.getAlertInstallAdditionalOrchestrationFiles();
        List<File> additionalOrchestrationFiles = deployProductProperties.getFilePathTransformer().transformFilePaths(additionalOrchestrationFilePaths);
        AlertDockerManager alertDockerManager = new AlertDockerManager(intLogger, dockerCommands, stackName, customCertificate, alertEncryption);

        return new AlertInstaller(intLogger, alertDownloader, deployProductProperties.getExecutablesRunner(), alertDockerManager, dockerStackDeploy, dockerCommands, stackName, additionalOrchestrationFiles, alertService,
            alertLocalOverridesEditor, useLocalOverrides);
    }

}
