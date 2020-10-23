package com.synopsys.integration.blackduck.installer.dockerswarm.yaml.model;

import org.apache.commons.lang3.StringUtils;

public class DockerGlobalSecret implements YamlBlock {
    private String key;
    private String stackName;
    private YamlLine external;
    private YamlLine name;
    private YamlLine yamlKey;

    private DockerGlobalSecret(String key, String stackName, YamlLine yamlKey) {
        this.key = key;
        this.yamlKey = yamlKey;
        this.stackName = stackName;
    }

    public static DockerGlobalSecret of(String stackName, YamlLine line) {
        boolean commented = YamlLine.isCommented(line.getCurrentRawText());
        if (commented) {
            line.comment();
        } else {
            line.uncomment();
        }
        String rawText = line.getCurrentRawText();
        int colonIndex = rawText.indexOf(":");
        int startIndex = 0;
        String key;
        if (commented) {
            startIndex = 1;
        }
        key = rawText.trim().substring(startIndex, colonIndex).trim();
        YamlLine yamlKey = line;
        if (StringUtils.isNotBlank(key)) {
            yamlKey.setCurrentRawText(String.format("  %s:", key));
        }

        return new DockerGlobalSecret(key, stackName, yamlKey);
    }

    public void applyName(YamlLine nameLine, String stackPrefix) {
        String rawText = nameLine.getCurrentRawText();
        int colonIndex = rawText.indexOf(":");

        String nameSuffix = rawText.substring(colonIndex + 1).trim();
        String secretName = nameSuffix.replace("\"", "")
                                .replace(stackPrefix, "")
                                .trim();
        String secretYamlLine = String.format("    name: \"%s_%s\"", stackName, secretName);
        nameLine.setCurrentRawText(secretYamlLine);
        this.name = nameLine;
    }

    public void applyExternal(YamlLine externalLine) {
        String rawText = externalLine.getCurrentRawText();
        int colonIndex = rawText.indexOf(":");

        String externalBoolean = rawText.substring(colonIndex + 1).trim();
        String secretYamlLine = String.format("    external: %s", externalBoolean);
        externalLine.setCurrentRawText(secretYamlLine);
        this.external = externalLine;
    }

    @Override
    public boolean isBlockCommented() {
        return this.isCommented();
    }

    @Override
    public void commentBlock() {
        yamlKey.comment();
        if (null != external) {
            external.comment();
        }
        if (null != name) {
            name.comment();
        }
    }

    @Override
    public void uncommentBlock() {
        yamlKey.uncomment();
        if (null != external) {
            external.uncomment();
        }
        if (null != name) {
            name.uncomment();
        }
    }

    public boolean isCommented() {
        return yamlKey.isCommented();
    }

    public String getKey() {
        return key;
    }

    public String getStackName() {
        return stackName;
    }
}
