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
package com.synopsys.integration.blackduck.installer.dockerswarm.install;

import com.synopsys.integration.blackduck.installer.ApplicationValues;
import com.synopsys.integration.blackduck.installer.DeployProductProperties;
import com.synopsys.integration.blackduck.installer.dockerswarm.deploy.BlackDuckDockerManager;
import com.synopsys.integration.blackduck.installer.dockerswarm.edit.BlackDuckConfigEnvEditor;
import com.synopsys.integration.blackduck.installer.dockerswarm.edit.HubWebServerEnvEditor;
import com.synopsys.integration.blackduck.installer.dockerswarm.edit.HubWebServerEnvTokens;
import com.synopsys.integration.blackduck.installer.dockerswarm.edit.LocalOverridesEditor;
import com.synopsys.integration.blackduck.installer.download.ArtifactoryDownloadUrl;
import com.synopsys.integration.blackduck.installer.download.BlackDuckGithubDownloadUrl;
import com.synopsys.integration.blackduck.installer.download.ZipFileDownloader;
import com.synopsys.integration.blackduck.installer.workflow.DownloadUrlDecider;

public class BlackDuckInstallerCreator {
    private ApplicationValues applicationValues;
    private DeployProductProperties deployProductProperties;

    public BlackDuckInstallerCreator(ApplicationValues applicationValues, DeployProductProperties deployProductProperties) {
        this.applicationValues = applicationValues;
        this.deployProductProperties = deployProductProperties;
    }

    public BlackDuckInstaller create() {
        BlackDuckGithubDownloadUrl blackDuckGithubDownloadUrl = new BlackDuckGithubDownloadUrl(applicationValues.getBlackDuckGithubDownloadUrlPrefix(), applicationValues.getBlackDuckVersion());
        ArtifactoryDownloadUrl blackDuckArtifactoryDownloadUrl = new ArtifactoryDownloadUrl(applicationValues.getBlackDuckArtifactoryUrl(), applicationValues.getBlackDuckArtifactoryRepo(), applicationValues.getBlackDuckArtifactPath(), applicationValues.getBlackDuckArtifact(), applicationValues.getBlackDuckVersion());
        DownloadUrlDecider downloadUrlDecider = new DownloadUrlDecider(applicationValues.getBlackDuckDownloadSource(), blackDuckGithubDownloadUrl::getDownloadUrl, blackDuckArtifactoryDownloadUrl::getDownloadUrl);

        HubWebServerEnvTokens hubWebServerEnvTokens = new HubWebServerEnvTokens(applicationValues.getBlackDuckInstallWebServerHost());
        HubWebServerEnvEditor hubWebServerEnvEditor = new HubWebServerEnvEditor(deployProductProperties.getIntLogger(), deployProductProperties.getHashUtility(), deployProductProperties.getLineSeparator(), hubWebServerEnvTokens);
        BlackDuckConfigEnvEditor blackDuckConfigEnvEditor = new BlackDuckConfigEnvEditor(deployProductProperties.getIntLogger(), deployProductProperties.getHashUtility(), deployProductProperties.getLineSeparator(), applicationValues.getBlackDuckInstallProxyHost(), applicationValues.getBlackDuckInstallProxyPort(), applicationValues.getBlackDuckInstallProxyScheme(), applicationValues.getBlackDuckInstallProxyUser(), applicationValues.getBlackDuckInstallCustomKbHost());

        boolean useLocalOverrides = applicationValues.isBlackDuckInstallUseLocalOverrides();
        if (!deployProductProperties.getCustomCertificate().isEmpty()) {
            useLocalOverrides = true;
        }
        LocalOverridesEditor localOverridesEditor = new LocalOverridesEditor(deployProductProperties.getIntLogger(), deployProductProperties.getHashUtility(), deployProductProperties.getLineSeparator(), applicationValues.getStackName(), useLocalOverrides);

        ZipFileDownloader blackDuckDownloader = new ZipFileDownloader(deployProductProperties.getIntLogger(), deployProductProperties.getIntHttpClient(), deployProductProperties.getCommonZipExpander(), downloadUrlDecider, deployProductProperties.getBaseDirectory(), "blackduck", applicationValues.getBlackDuckVersion(), applicationValues.isBlackDuckDownloadForce());

        BlackDuckDockerManager blackDuckDockerManager = new BlackDuckDockerManager(deployProductProperties.getIntLogger(), deployProductProperties.getDockerCommands(), applicationValues.getStackName(), deployProductProperties.getCustomCertificate());
        return new BlackDuckInstaller(blackDuckDownloader, deployProductProperties.getExecutablesRunner(), blackDuckDockerManager, deployProductProperties.getDeployStack(), deployProductProperties.getDockerCommands(), blackDuckConfigEnvEditor, hubWebServerEnvEditor, localOverridesEditor, useLocalOverrides);
    }

}
