package com.synopsys.integration.blackduck.installer.dockerswarm.yaml.model;

import org.apache.commons.lang3.StringUtils;

public class ServiceSecretLine implements YamlTextLine {
    private String key;
    private YamlLine yamlLine;

    private ServiceSecretLine(String key, YamlLine yamlLine) {
        this.key = key;
        this.yamlLine = yamlLine;
    }

    public static ServiceSecretLine of(YamlLine line) {
        boolean commented = YamlLine.isCommented(line.getFormattedText());
        if (commented) {
            line.comment();
        } else {
            line.uncomment();
        }
        String rawText = line.getFormattedText();
        int hyphenIndex = rawText.indexOf("- ");
        String key = null;
        if (hyphenIndex > 0) {
            // prevent the strings "-- " from being considered a valid secret.
            if ('-' != rawText.charAt(hyphenIndex - 1)) {
                key = rawText.substring(hyphenIndex + 1).trim();
            }
        }
        ServiceSecretLine secretLine = new ServiceSecretLine(key, line);
        if (secretLine.hasKey()) {
            secretLine.getYamlLine().setCurrentRawText(String.format("      - %s", secretLine.getKey()));
        }
        return secretLine;
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
}
