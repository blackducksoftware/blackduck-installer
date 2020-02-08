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
