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
package com.synopsys.integration.blackduck.installer.workflow;

import com.synopsys.integration.blackduck.installer.download.DownloadSource;
import com.synopsys.integration.blackduck.installer.exception.BlackDuckInstallerException;
import com.synopsys.integration.function.ThrowingSupplier;
import com.synopsys.integration.rest.HttpUrl;

import java.util.Optional;
import java.util.function.Supplier;

public class DownloadUrlDecider {
    private final DownloadSource downloadSource;
    private final ThrowingSupplier<HttpUrl, BlackDuckInstallerException> githubDownloadUrl;
    private final ThrowingSupplier<HttpUrl, BlackDuckInstallerException> artifactoryDownloadUrl;

    public DownloadUrlDecider(DownloadSource downloadSource, ThrowingSupplier<HttpUrl, BlackDuckInstallerException> githubDownloadUrl, ThrowingSupplier<HttpUrl, BlackDuckInstallerException> artifactoryDownloadUrl) {
        this.downloadSource = downloadSource;
        this.githubDownloadUrl = githubDownloadUrl;
        this.artifactoryDownloadUrl = artifactoryDownloadUrl;
    }

    public Optional<HttpUrl> determineDownloadUrl() throws BlackDuckInstallerException {
        if (DownloadSource.GITHUB == downloadSource) {
            return Optional.of(githubDownloadUrl.get());
        } else if (DownloadSource.ARTIFACTORY == downloadSource) {
            return Optional.of(artifactoryDownloadUrl.get());
        } else {
            return Optional.empty();
        }
    }

}
