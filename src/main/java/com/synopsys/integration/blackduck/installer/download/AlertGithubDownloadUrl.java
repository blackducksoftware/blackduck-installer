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
package com.synopsys.integration.blackduck.installer.download;

import com.synopsys.integration.blackduck.installer.exception.BlackDuckInstallerException;
import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.rest.HttpUrl;

import org.apache.commons.lang3.StringUtils;

public class AlertGithubDownloadUrl implements DownloadUrl {
    private final String githubReleasesUrlPrefix;
    private final String version;

    public AlertGithubDownloadUrl(String githubReleasesUrlPrefix, String version) {
        this.githubReleasesUrlPrefix = githubReleasesUrlPrefix;
        this.version = version;
    }

    public HttpUrl getDownloadUrl() throws BlackDuckInstallerException {
        if (StringUtils.isAnyBlank(githubReleasesUrlPrefix, version)) {
            throw new BlackDuckInstallerException("To use GitHub for downloading Alert, the url prefix and version must be set.");
        }

        try {
            return new HttpUrl(String.format("%s/%s/blackduck-alert-%s-deployment.zip", githubReleasesUrlPrefix, version, version));
        } catch (IntegrationException e) {
            throw new BlackDuckInstallerException("bad alert github download url");
        }
    }

}
