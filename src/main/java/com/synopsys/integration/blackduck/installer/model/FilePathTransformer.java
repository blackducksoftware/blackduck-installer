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
package com.synopsys.integration.blackduck.installer.model;

import com.synopsys.integration.blackduck.installer.exception.BlackDuckInstallerException;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class FilePathTransformer {
    public File transformFilePath(String filePath) throws BlackDuckInstallerException {
        File file = new File(filePath);
        if (file == null || !file.exists() || !file.isFile() || file.length() <= 0) {
            throw new BlackDuckInstallerException(String.format("The provided file path %s does not appear to be valid.", filePath));
        }

        return file;
    }

    public List<File> transformFilePaths(List<String> filePaths) throws BlackDuckInstallerException {
        if (null != filePaths || !filePaths.isEmpty()) {
            List<File> files = new ArrayList<>();
            for (String filePath : filePaths) {
                files.add(transformFilePath(filePath));
            }

            return files;
        }

        return Collections.emptyList();
    }

}
