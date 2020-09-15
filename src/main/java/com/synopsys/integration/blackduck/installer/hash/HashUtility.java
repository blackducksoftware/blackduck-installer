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
package com.synopsys.integration.blackduck.installer.hash;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.apache.commons.codec.digest.DigestUtils;

import com.synopsys.integration.blackduck.installer.exception.BlackDuckInstallerException;

public class HashUtility {
    /*
    If the files to edit *do* change, this utility hopes to make it simpler to update PreComputedHashes.java.
     */
    private static final List<String> ZIP_FILE_PATHS = new ArrayList<>();

    private static final Pattern VERSION_PATTERN = Pattern.compile("(\\d+(\\.\\d+)*)");

    public static void main(String[] args) throws Exception {
        String userHome = System.getProperty("user.home");
        ZIP_FILE_PATHS.add(userHome + "/Downloads/hub-2019.8.1.zip");
        ZIP_FILE_PATHS.add(userHome + "/Downloads/hub-2019.10.0.zip");
        ZIP_FILE_PATHS.add(userHome + "/Downloads/hub-2019.10.1.zip");
        ZIP_FILE_PATHS.add(userHome + "/Downloads/hub-2019.12.0.zip");
        ZIP_FILE_PATHS.add(userHome + "/Downloads/blackduck-alert-5.0.0-deployment.zip");
        ZIP_FILE_PATHS.add(userHome + "/Downloads/blackduck-alert-5.0.1-deployment.zip");
        ZIP_FILE_PATHS.add(userHome + "/Downloads/blackduck-alert-5.1.0-deployment.zip");
        ZIP_FILE_PATHS.add(userHome + "/Downloads/blackduck-alert-5.2.0-deployment.zip");
        ZIP_FILE_PATHS.add(userHome + "/Downloads/blackduck-alert-6.0.0-deployment.zip");
        ZIP_FILE_PATHS.add(userHome + "/Downloads/blackduck-alert-6.0.1-deployment.zip");
        ZIP_FILE_PATHS.add(userHome + "/Downloads/blackduck-alert-6.1.0-deployment.zip");

        Set<String> entriesToLookFor = Set.of("blackduck-config.env", "docker-compose.local-overrides.yml", "hub-webserver.env");

        HashUtility hashUtility = new HashUtility();

        for (String zipFilePath : ZIP_FILE_PATHS) {
            File file = new File(zipFilePath);
            if (file.exists()) {
                ZipFile zipFile = new ZipFile(zipFilePath);
                Enumeration<? extends ZipEntry> zipEntries = zipFile.entries();
                String version = hashUtility.getVersion(zipFilePath);
                while (zipEntries.hasMoreElements()) {
                    ZipEntry entry = zipEntries.nextElement();
                    String entryName = entry.getName();
                    String filename = entry.getName().substring(entryName.lastIndexOf('/') + 1);

                    if (entryName.contains("docker-swarm") && !entryName.contains("external-db") && entriesToLookFor.contains(filename)) {
                        hashUtility.hashFileForComputedHashesDotJava(zipFile.getInputStream(entry), String.format("%s_%s", filename, version));
                    }
                }
            }
        }
    }

    public String computeHash(File toHash, String name) throws BlackDuckInstallerException {
        try {
            return computeHash(new FileInputStream(toHash), name);
        } catch (FileNotFoundException e) {
            throw new BlackDuckInstallerException(String.format("Could not hash file %s: %s", name, e.getMessage()));
        }
    }

    public String computeHash(InputStream toHash, String name) throws BlackDuckInstallerException {
        try {
            return DigestUtils.sha256Hex(toHash);
        } catch (IOException e) {
            throw new BlackDuckInstallerException(String.format("Could not hash %s: %s", name, e.getMessage()));
        }
    }

    private void hashFileForComputedHashesDotJava(InputStream toHash, String name) throws BlackDuckInstallerException {
        String hash = computeHash(toHash, name);
        System.out.println("public static final String " + convertNonAlpha(name) + " = \"" + hash + "\";");
    }

    private String convertNonAlpha(String s) {
        return s.toUpperCase().replaceAll("[^A-Z0-9]", "_");
    }

    private String getVersion(String filename) {
        Matcher matcher = VERSION_PATTERN.matcher(filename);
        matcher.find();
        return matcher.group();
    }

}
