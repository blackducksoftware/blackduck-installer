package com.synopsys.integration.blackduck.installer.model;

import org.apache.commons.lang3.StringUtils;

public class AlertEncryption {
    private final String passwordPath;
    private final String globalSaltPath;

    public AlertEncryption(String passwordPath, String globalSaltPath) {
        this.passwordPath = passwordPath;
        this.globalSaltPath = globalSaltPath;
    }

    public boolean isEmpty() {
        return StringUtils.isAllBlank(passwordPath, globalSaltPath);
    }

    public String getPasswordPath() {
        return passwordPath;
    }

    public String getGlobalSaltPath() {
        return globalSaltPath;
    }

}
