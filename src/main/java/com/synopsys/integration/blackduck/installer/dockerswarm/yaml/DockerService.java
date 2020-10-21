package com.synopsys.integration.blackduck.installer.dockerswarm.yaml;

import java.io.IOException;

import com.synopsys.integration.blackduck.installer.dockerswarm.yaml.output.YamlWriter;

public class DockerService extends YamlLine implements YamlBlock {
    private final DockerServiceEnvironment dockerServiceEnvironment = new DockerServiceEnvironment();
    private final DockerServiceSecrets dockerServiceSecrets = new DockerServiceSecrets();
    private String name;

    public DockerService(String name) {
        super(name);
        this.name = name;
    }

    public void addEnvironmentVariable(String line) {
        ServiceEnvironmentLine environmentVariable = ServiceEnvironmentLine.of(line);
        dockerServiceEnvironment.addEnvironmentVariable(environmentVariable);
    }

    public void addSecret(String line) {
        ServiceSecretLine secret = ServiceSecretLine.of(line);
        dockerServiceSecrets.addSecret(secret);
    }

    public String getName() {
        return name;
    }

    public DockerServiceEnvironment getDockerServiceEnvironment() {
        return dockerServiceEnvironment;
    }

    public DockerServiceSecrets getDockerServiceSecrets() {
        return dockerServiceSecrets;
    }

    @Override
    public void commentBlock() {
        comment();
        dockerServiceEnvironment.commentBlock();
        dockerServiceSecrets.commentBlock();
    }

    @Override
    public void uncommentBlock() {
        uncomment();
        dockerServiceEnvironment.uncommentBlock();
        dockerServiceSecrets.uncommentBlock();
    }

    @Override
    public String createTextLine() {
        return String.format("%s:", getName());
    }

    @Override
    public void write(final YamlWriter writer) throws IOException {
        super.write(writer);
        dockerServiceEnvironment.write(writer);
        dockerServiceSecrets.write(writer);
    }
}
