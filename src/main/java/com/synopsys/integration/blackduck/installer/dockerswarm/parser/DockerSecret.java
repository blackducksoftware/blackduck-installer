package com.synopsys.integration.blackduck.installer.dockerswarm.parser;

public class DockerSecret implements YamlBlock {
    private String key;
    private String stackName;
    private YamlLine external;
    private YamlLine name;
    private YamlLine yamlKey;

    public DockerSecret(String key, String stackName) {
        this.key = key;
        this.yamlKey = YamlLine.create(key);
        this.stackName = stackName;
    }

    public static final DockerSecret of(String stackName, String line) {
        boolean commented = YamlLine.isCommented(line);
        int colonIndex = line.indexOf(":");
        int startIndex = 0;
        String key;
        if (commented) {
            startIndex = 1;
        }
        key = line.trim().substring(startIndex, colonIndex).trim();

        return new DockerSecret(key, stackName);
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

    public String getKey() {
        return key;
    }

    public String getStackName() {
        return stackName;
    }
}
