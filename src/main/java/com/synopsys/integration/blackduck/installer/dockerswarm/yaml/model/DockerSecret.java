package com.synopsys.integration.blackduck.installer.dockerswarm.yaml.model;

import java.util.Collection;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

public class DockerSecret implements YamlBlock {
    private String key;
    private String stackName;
    private YamlLine external;
    private YamlLine name;
    private YamlLine yamlKey;

    private DockerSecret(String key, String stackName, YamlLine yamlKey) {
        this.key = key;
        this.yamlKey = yamlKey;
        this.stackName = stackName;
    }

    public static DockerSecret of(String stackName, String line) {
        boolean commented = YamlLine.isCommented(line);
        int colonIndex = line.indexOf(":");
        int startIndex = 0;
        String key;
        if (commented) {
            startIndex = 1;
        }
        key = line.trim().substring(startIndex, colonIndex).trim();
        YamlLine yamlKey = YamlLine.create(line);
        if (StringUtils.isNotBlank(key)) {
            yamlKey = YamlLine.create(String.format("  %s:", key));
        }

        return new DockerSecret(key, stackName, yamlKey);
    }

    public void applyName(String nameLine, String stackPrefix) {
        int colonIndex = nameLine.indexOf(":");

        String nameSuffix = nameLine.substring(colonIndex + 1).trim();
        String secretName = nameSuffix.replace("\"", "")
                                .replace(stackPrefix, "")
                                .trim();
        String secretYamlLine = String.format("    name: \"%s_%s\"", stackName, secretName);
        this.name = YamlLine.create(secretYamlLine);
    }

    public void applyExternal(String externalLine) {
        int colonIndex = externalLine.indexOf(":");

        String externalBoolean = externalLine.substring(colonIndex + 1).trim();
        String secretYamlLine = String.format("    external: %s", externalBoolean);
        this.external = YamlLine.create(secretYamlLine);
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

    public Collection<YamlLine> getLinesInBlock() {
        return List.of(yamlKey, external, name);
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
