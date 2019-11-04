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
package com.synopsys.integration.blackduck.installer.download;

import com.synopsys.integration.blackduck.installer.exception.BlackDuckInstallerException;
import com.synopsys.integration.blackduck.installer.workflow.DownloadUrlDecider;
import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.log.IntLogger;
import com.synopsys.integration.rest.client.IntHttpClient;
import com.synopsys.integration.rest.request.Request;
import com.synopsys.integration.rest.request.Response;
import com.synopsys.integration.util.CommonZipExpander;
import org.apache.commons.compress.archivers.ArchiveException;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

public class ZipFileDownloader {
    private final IntLogger logger;
    private final IntHttpClient intHttpClient;
    private final CommonZipExpander commonZipExpander;
    private final DownloadUrlDecider downloadUrlDecider;
    private final File baseDirectory;
    private final String name;
    private final String version;

    public ZipFileDownloader(IntLogger logger, IntHttpClient intHttpClient, CommonZipExpander commonZipExpander, DownloadUrlDecider downloadUrlDecider, File baseDirectory, String name, String version) {
        this.logger = logger;
        this.intHttpClient = intHttpClient;
        this.commonZipExpander = commonZipExpander;
        this.downloadUrlDecider = downloadUrlDecider;
        this.baseDirectory = baseDirectory;
        this.name = name;
        this.version = version;
    }

    public File download() throws BlackDuckInstallerException {
        File downloadDirectory = new File(baseDirectory, name + "-" + version);
        downloadDirectory.mkdirs();

        String downloadUrl = downloadUrlDecider.determineDownloadUrl().orElseThrow(() -> new BlackDuckInstallerException("No download url could be determined - not enough information provided to use github or artifactory."));

        logger.info("Downloading " + name + " version " + version + " from " + downloadUrl + ".");
        Request downloadRequest = new Request.Builder(downloadUrl).build();
        try (Response response = intHttpClient.execute(downloadRequest)) {
            try (InputStream responseStream = response.getContent()) {
                commonZipExpander.expand(responseStream, downloadDirectory);
            } catch (ArchiveException e) {
                throw new BlackDuckInstallerException("Could not expand the archive.", e);
            }

            logger.info(String.format(name + " downloaded successfully."));
        } catch (IntegrationException| IOException e) {
            throw new BlackDuckInstallerException("Could not download: " + downloadUrl + ". Make sure that the url finder is configured correctly.", e);
        }

        return downloadDirectory.listFiles()[0];
    }

}
