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
package com.synopsys.integration.blackduck.installer.download;

import com.synopsys.integration.blackduck.installer.exception.BlackDuckInstallerException;
import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.rest.HttpUrl;

import org.apache.commons.lang3.StringUtils;

public class ArtifactoryDownloadUrl implements DownloadUrl {
    private String artifactoryUrl;
    private String artifactoryRepo;
    private String artifactPath;
    private String artifact;
    private String version;

    public ArtifactoryDownloadUrl(String artifactoryUrl, String artifactoryRepo, String artifactPath, String artifact, String version) {
        this.artifactoryUrl = artifactoryUrl;
        this.artifactoryRepo = artifactoryRepo;
        this.artifactPath = artifactPath;
        this.artifact = artifact;
        this.version = version;
    }

    public HttpUrl getDownloadUrl() throws BlackDuckInstallerException {
        if (StringUtils.isAnyBlank(artifactoryUrl, artifactoryRepo, artifactPath, artifact, version)) {
            throw new BlackDuckInstallerException("To use Artifactory for downloading, the url, repo, path, artifact and version must all be set.");
        }

        try {
            return new HttpUrl(String.format("%s/%s/%s/%s/%s/%s-%s.zip", artifactoryUrl, artifactoryRepo, artifactPath, artifact, version, artifact, version));
        } catch (IntegrationException e) {
            throw new BlackDuckInstallerException("bad artifactory download url");
        }
    }

}
