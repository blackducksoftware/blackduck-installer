package com.synopsys.integration.blackduck.installer.dockerswarm.yaml.model;

import org.apache.commons.lang3.StringUtils;

public class ServiceEnvironmentLine implements YamlTextLine {
    private final String key;
    private String value;
    private YamlLine yamlLine;

    private ServiceEnvironmentLine(String key, String value, YamlLine yamlLine) {
        this.key = key;
        this.value = value;
        this.yamlLine = yamlLine;
    }

    public static ServiceEnvironmentLine newEnvironmentLine(int lineNumber, String key) {
        // create a commented variable.
        return of(YamlLine.create(lineNumber, String.format("#      - %s=", key)));
    }

    public static ServiceEnvironmentLine of(YamlLine line) {
        boolean commented = YamlLine.isCommented(line.getFormattedText());
        if (commented) {
            line.comment();
        } else {
            line.uncomment();
        }
        String rawText = line.getFormattedText();
        int hyphenIndex = rawText.indexOf("-");
        int equalsIndex = rawText.indexOf("=");
        String key = null;
        String value = null;
        if (hyphenIndex >= 0 && equalsIndex > 0) {
            key = rawText.substring(hyphenIndex + 1, equalsIndex).trim();
        }
        if (equalsIndex > 0) {
            value = rawText.substring(equalsIndex + 1).trim();
        }
        return new ServiceEnvironmentLine(key, value, line);
    }

    public YamlLine getYamlLine() {
        return yamlLine;
    }

    @Override
    public boolean isCommented() {
        return yamlLine.isCommented();
    }

    @Override
    public void comment() {
        yamlLine.comment();
    }

    @Override
    public void uncomment() {
        yamlLine.uncomment();
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
        if (hasKey()) {
            yamlLine.setCurrentRawText(String.format("      - %s=%s", getKey(), getValue()));
        }
    }
}
