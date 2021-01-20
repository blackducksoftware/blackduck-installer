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
package com.synopsys.integration.blackduck.installer.dockerswarm.install;

import java.io.File;
import java.util.List;

import com.synopsys.integration.blackduck.installer.ApplicationValues;
import com.synopsys.integration.blackduck.installer.DeployProductProperties;
import com.synopsys.integration.blackduck.installer.dockerswarm.deploy.BlackDuckDockerManager;
import com.synopsys.integration.blackduck.installer.dockerswarm.edit.BlackDuckConfigEnvEditor;
import com.synopsys.integration.blackduck.installer.dockerswarm.edit.BlackDuckLocalOverridesEditor;
import com.synopsys.integration.blackduck.installer.dockerswarm.edit.HubWebServerEnvEditor;
import com.synopsys.integration.blackduck.installer.dockerswarm.edit.HubWebServerEnvTokens;
import com.synopsys.integration.blackduck.installer.download.ArtifactoryDownloadUrl;
import com.synopsys.integration.blackduck.installer.download.BlackDuckGithubDownloadUrl;
import com.synopsys.integration.blackduck.installer.download.ZipFileDownloader;
import com.synopsys.integration.blackduck.installer.exception.BlackDuckInstallerException;
import com.synopsys.integration.blackduck.installer.hash.HashUtility;
import com.synopsys.integration.blackduck.installer.model.BlackDuckAdditionalOrchestrationFiles;
import com.synopsys.integration.blackduck.installer.model.CustomCertificate;
import com.synopsys.integration.blackduck.installer.model.LoadedConfigProperties;
import com.synopsys.integration.blackduck.installer.workflow.DownloadUrlDecider;
import com.synopsys.integration.log.IntLogger;

public class BlackDuckInstallerCreator {
    private ApplicationValues applicationValues;
    private DeployProductProperties deployProductProperties;
    private LoadedConfigProperties blackDuckConfigEnvLoadedProperties;

    public BlackDuckInstallerCreator(ApplicationValues applicationValues, DeployProductProperties deployProductProperties, LoadedConfigProperties blackDuckConfigEnvLoadedProperties) {
        this.applicationValues = applicationValues;
        this.deployProductProperties = deployProductProperties;
        this.blackDuckConfigEnvLoadedProperties = blackDuckConfigEnvLoadedProperties;
    }

    public BlackDuckInstaller create() throws BlackDuckInstallerException {
        IntLogger intLogger = deployProductProperties.getIntLogger();
        HashUtility hashUtility = deployProductProperties.getHashUtility();
        String stackName = applicationValues.getStackName();
        CustomCertificate customCertificate = deployProductProperties.getCustomCertificate();

        BlackDuckGithubDownloadUrl blackDuckGithubDownloadUrl = new BlackDuckGithubDownloadUrl(applicationValues.getBlackDuckGithubDownloadUrlPrefix(), applicationValues.getBlackDuckVersion());
        ArtifactoryDownloadUrl blackDuckArtifactoryDownloadUrl = new ArtifactoryDownloadUrl(applicationValues.getBlackDuckArtifactoryUrl(), applicationValues.getBlackDuckArtifactoryRepo(), applicationValues.getBlackDuckArtifactPath(),
            applicationValues.getBlackDuckArtifact(), applicationValues.getBlackDuckVersion());
        DownloadUrlDecider downloadUrlDecider = new DownloadUrlDecider(applicationValues.getBlackDuckDownloadSource(), blackDuckGithubDownloadUrl::getDownloadUrl, blackDuckArtifactoryDownloadUrl::getDownloadUrl);

        HubWebServerEnvTokens hubWebServerEnvTokens = new HubWebServerEnvTokens(applicationValues.getWebServerHost());
        HubWebServerEnvEditor hubWebServerEnvEditor = new HubWebServerEnvEditor(intLogger, hashUtility, deployProductProperties.getLineSeparator(), hubWebServerEnvTokens);

        BlackDuckConfigEnvEditor blackDuckConfigEnvEditor = new BlackDuckConfigEnvEditor(intLogger, hashUtility, deployProductProperties.getLineSeparator(), blackDuckConfigEnvLoadedProperties);

        boolean useLocalOverrides = applicationValues.isBlackDuckInstallUseLocalOverrides();
        if (!deployProductProperties.getCustomCertificate().isEmpty()) {
            useLocalOverrides = true;
        }
        BlackDuckLocalOverridesEditor blackDuckLocalOverridesEditor = new BlackDuckLocalOverridesEditor(intLogger, hashUtility, deployProductProperties.getLineSeparator(), stackName, useLocalOverrides, customCertificate);
        ZipFileDownloader blackDuckDownloader = new ZipFileDownloader(intLogger, deployProductProperties.getIntHttpClient(), deployProductProperties.getCommonZipExpander(), downloadUrlDecider, deployProductProperties.getBaseDirectory(),
            "blackduck", applicationValues.getBlackDuckVersion(), applicationValues.isBlackDuckDownloadForce());

        List<String> additionalOrchestrationFilePaths = applicationValues.getBlackDuckInstallAdditionalOrchestrationFiles();
        List<File> additionalOrchestrationFiles = deployProductProperties.getFilePathTransformer().transformFilePaths(additionalOrchestrationFilePaths);
        BlackDuckDockerManager blackDuckDockerManager = new BlackDuckDockerManager(intLogger, deployProductProperties.getDockerCommands(), stackName, deployProductProperties.getCustomCertificate());

        BlackDuckAdditionalOrchestrationFiles blackDuckAdditionalOrchestrationFiles = new BlackDuckAdditionalOrchestrationFiles();
        if (applicationValues.isBlackDuckInstallUseBdbaOrchestrationFile()) {
            blackDuckAdditionalOrchestrationFiles.addOrchestrationFilePath(BlackDuckAdditionalOrchestrationFiles.BlackDuckOrchestrationFile.BDBA);
        }
        if (applicationValues.isBlackDuckInstallUseExternaldbOrchestrationFile()) {
            blackDuckAdditionalOrchestrationFiles.addOrchestrationFilePath(BlackDuckAdditionalOrchestrationFiles.BlackDuckOrchestrationFile.EXTERNALDB);
        }
        if (applicationValues.isBlackDuckInstallUseDbmigrateOrchestrationFile()) {
            blackDuckAdditionalOrchestrationFiles.addOrchestrationFilePath(BlackDuckAdditionalOrchestrationFiles.BlackDuckOrchestrationFile.DBMIGRATE);
        }

        return new BlackDuckInstaller(intLogger, blackDuckDownloader, deployProductProperties.getExecutablesRunner(), blackDuckDockerManager, deployProductProperties.getDeployStack(), deployProductProperties.getDockerCommands(), stackName,
            additionalOrchestrationFiles, blackDuckConfigEnvEditor, hubWebServerEnvEditor,
            blackDuckLocalOverridesEditor, useLocalOverrides, blackDuckAdditionalOrchestrationFiles);
    }

}
