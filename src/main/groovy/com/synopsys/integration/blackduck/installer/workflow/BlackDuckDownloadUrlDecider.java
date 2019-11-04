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
package com.synopsys.integration.blackduck.installer.workflow;

import com.synopsys.integration.blackduck.installer.download.ArtifactoryDownloadUrl;
import com.synopsys.integration.blackduck.installer.download.BlackDuckGithubDownloadUrl;
import com.synopsys.integration.blackduck.installer.download.DownloadSource;
import com.synopsys.integration.blackduck.installer.download.DownloadUrl;
import com.synopsys.integration.blackduck.installer.exception.BlackDuckInstallerException;

import java.util.Optional;

public class BlackDuckDownloadUrlDecider implements DownloadUrlDecider {
    private final DownloadSource downloadSource;
    private final String version;
    private final String githubDownloadUrlPrefix;
    private final String artifactoryUrl;
    private final String artifactoryRepo;
    private final String artifactoryPath;
    private final String artifact;

    public BlackDuckDownloadUrlDecider(DownloadSource downloadSource, String version, String githubDownloadUrlPrefix, String artifactoryUrl, String artifactoryRepo, String artifactoryPath, String artifact) {
        this.downloadSource = downloadSource;
        this.version = version;
        this.githubDownloadUrlPrefix = githubDownloadUrlPrefix;
        this.artifactoryUrl = artifactoryUrl;
        this.artifactoryRepo = artifactoryRepo;
        this.artifactoryPath = artifactoryPath;
        this.artifact = artifact;
    }

    public Optional<String> determineDownloadUrl() throws BlackDuckInstallerException {
        if (DownloadSource.GITHUB == downloadSource) {
            return Optional.of(new BlackDuckGithubDownloadUrl(githubDownloadUrlPrefix, version).getDownloadUrl());
        } else if (DownloadSource.ARTIFACTORY == downloadSource) {
            return Optional.of(new ArtifactoryDownloadUrl(artifactoryUrl, artifactoryRepo, artifactoryPath, artifact, version).getDownloadUrl());
        } else {
            return Optional.empty();
        }
    }

}
