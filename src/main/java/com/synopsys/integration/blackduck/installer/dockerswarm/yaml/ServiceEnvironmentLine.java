package com.synopsys.integration.blackduck.installer.dockerswarm.yaml;

import org.apache.commons.lang3.StringUtils;

public class ServiceEnvironmentLine extends YamlLine {
    private final String key;
    private String value;

    private ServiceEnvironmentLine(boolean commented, String line, String key, String value) {
        super(commented, line);
        this.key = key;
        this.value = value;
    }

    public static ServiceEnvironmentLine newEnvironmentLine(String key) {
        // create a commented variable.
        return of(String.format("#      - %s=", key));
    }

    public static ServiceEnvironmentLine of(String line) {
        boolean commented = YamlLine.isCommented(line);
        int hyphenIndex = line.indexOf("-");
        int equalsIndex = line.indexOf("=");
        String key = null;
        String value = null;
        if (hyphenIndex >= 0 && equalsIndex > 0) {
            key = line.substring(hyphenIndex + 1, equalsIndex).trim();
        }
        if (equalsIndex > 0) {
            value = line.substring(equalsIndex + 1).trim();
        }
        return new ServiceEnvironmentLine(commented, line, key, value);
    }

    public boolean hasKey() {
        return StringUtils.isNotBlank(key);
    }

    public String getKey() {
        return key;
    }

    public String getValue() {
        return value;
    }

    public void setValue(final String value) {
        this.value = value;
    }

    @Override
    public String createTextLine() {
        if (!hasKey()) {
            return getLine();
        }
        return String.format("      - %s=%s", getKey(), getValue());
    }
}
