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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.compress.archivers.ArchiveException;

import com.synopsys.integration.blackduck.installer.exception.BlackDuckInstallerException;
import com.synopsys.integration.blackduck.installer.model.ThrowingConsumer;
import com.synopsys.integration.blackduck.installer.workflow.DownloadUrlDecider;
import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.log.IntLogger;
import com.synopsys.integration.rest.HttpUrl;
import com.synopsys.integration.rest.client.IntHttpClient;
import com.synopsys.integration.rest.request.Request;
import com.synopsys.integration.rest.response.Response;
import com.synopsys.integration.util.CommonZipExpander;

public class ZipFileDownloader {
    private final IntLogger logger;
    private final IntHttpClient intHttpClient;
    private final CommonZipExpander commonZipExpander;
    private final DownloadUrlDecider downloadUrlDecider;
    private final File baseDirectory;
    private final String name;
    private final String version;
    private final boolean forceDownload;

    public ZipFileDownloader(IntLogger logger, IntHttpClient intHttpClient, CommonZipExpander commonZipExpander, DownloadUrlDecider downloadUrlDecider, File baseDirectory, String name, String version, boolean forceDownload) {
        this.logger = logger;
        this.intHttpClient = intHttpClient;
        this.commonZipExpander = commonZipExpander;
        this.downloadUrlDecider = downloadUrlDecider;
        this.baseDirectory = baseDirectory;
        this.name = name;
        this.version = version;
        this.forceDownload = forceDownload;
    }

    public File download(ThrowingConsumer<File, BlackDuckInstallerException> postDownloadProcessing) throws BlackDuckInstallerException {
        File downloadDirectory = new File(baseDirectory, name + "-" + version);
        downloadDirectory.mkdirs();
        if (downloadDirectory.listFiles().length != 0) {
            if (!forceDownload) {
                logger.info(String.format("%s %s has already been downloaded - it won't be downloaded or edited again. To force downloading/editing, please use the appropriate download.force property.", name, version));
                return downloadDirectory.listFiles()[0];
            } else {
                logger.info(String.format("%s %s has already been downloaded, but downloading/editing has been forced, so local changes could be lost.", name, version));
            }
        }

        HttpUrl downloadUrl = downloadUrlDecider.determineDownloadUrl().orElseThrow(() -> new BlackDuckInstallerException("No download url could be determined - not enough information provided to use github or artifactory."));

        logger.info("Downloading " + name + " version " + version + " from " + downloadUrl + ".");
        Request downloadRequest = new Request.Builder(downloadUrl).build();
        try (Response response = intHttpClient.execute(downloadRequest)) {
            try (InputStream responseStream = response.getContent()) {
                commonZipExpander.expand(responseStream, downloadDirectory);
            } catch (ArchiveException e) {
                throw new BlackDuckInstallerException("Could not expand the archive.", e);
            }

            logger.info(String.format(name + " downloaded successfully."));
        } catch (IntegrationException | IOException e) {
            throw new BlackDuckInstallerException("Could not download: " + downloadUrl + ". Make sure that the url finder is configured correctly.", e);
        }

        File installDirectory = downloadDirectory.listFiles()[0];
        postDownloadProcessing.accept(installDirectory);

        return installDirectory;
    }

}
