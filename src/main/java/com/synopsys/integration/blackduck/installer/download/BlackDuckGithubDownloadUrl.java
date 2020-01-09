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
import org.apache.commons.lang3.StringUtils;

public class BlackDuckGithubDownloadUrl implements DownloadUrl {
    private final String githubReleasesUrlPrefix;
    private final String version;

    public BlackDuckGithubDownloadUrl(String githubReleasesUrlPrefix, String version) {
        this.githubReleasesUrlPrefix = githubReleasesUrlPrefix;
        this.version = version;
    }

    public String getDownloadUrl() throws BlackDuckInstallerException {
        if (StringUtils.isAnyBlank(githubReleasesUrlPrefix, version)) {
            throw new BlackDuckInstallerException("To use GitHub for downloading Black Duck, the url prefix and the version must be set.");
        }

        return String.format("%s/v%s.zip", githubReleasesUrlPrefix, version);
    }

}
