package com.synopsys.integration.blackduck.installer.dockerswarm.yaml;

import org.apache.commons.lang3.StringUtils;

public class ServiceSecretLine extends YamlLine {
    private String key;

    private ServiceSecretLine(final boolean commented, final String line, final String key) {
        super(commented, line);
        this.key = key;
    }

    public static ServiceSecretLine of(String line) {
        boolean commented = YamlLine.isCommented(line);
        int hyphenIndex = line.indexOf("-");
        String key = null;
        if (hyphenIndex >= 0) {
            key = line.substring(hyphenIndex + 1).trim();
        }
        return new ServiceSecretLine(commented, line, key);
    }

    public boolean isCommentOnly() {
        return StringUtils.isNotBlank(key);
    }

    public String getKey() {
        return key;
    }

    @Override
    public String createTextLine() {
        if (isCommentOnly()) {
            return getLine();
        }
        return String.format("      - %s", getKey());
    }
}
