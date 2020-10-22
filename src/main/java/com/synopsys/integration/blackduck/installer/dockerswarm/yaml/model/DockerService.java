package com.synopsys.integration.blackduck.installer.dockerswarm.yaml.model;

import java.util.LinkedList;
import java.util.List;

public class DockerService extends YamlLine implements YamlBlock {
    private final List<YamlLine> commentsBeforeSections = new LinkedList<>();
    private final DockerServiceEnvironment dockerServiceEnvironment = new DockerServiceEnvironment();
    private final DockerServiceSecrets dockerServiceSecrets = new DockerServiceSecrets();
    private String name;

    public DockerService(String name) {
        super(name);
        this.name = name;
    }

    public void addCommentBeforeSection(String line) {
        YamlLine yamlLine = YamlLine.create(line);
        commentsBeforeSections.add(yamlLine);
    }

    public void addEnvironmentVariable(String line) {
        ServiceEnvironmentLine environmentVariable = ServiceEnvironmentLine.of(line);
        dockerServiceEnvironment.addVariableLine(environmentVariable);
    }

    public void addSecret(String line) {
        ServiceSecretLine secret = ServiceSecretLine.of(line);
        dockerServiceSecrets.addSecret(secret);
    }

    public String getName() {
        return name;
    }

    public List<YamlLine> getCommentsBeforeSections() {
        return commentsBeforeSections;
    }

    public DockerServiceEnvironment getDockerServiceEnvironment() {
        return dockerServiceEnvironment;
    }

    public DockerServiceSecrets getDockerServiceSecrets() {
        return dockerServiceSecrets;
    }

    @Override
    public boolean isCommented() {
        return super.isCommented() && isBlockCommented();
    }

    @Override
    public boolean isBlockCommented() {
        return dockerServiceEnvironment.isBlockCommented() && dockerServiceSecrets.isBlockCommented();
    }

    @Override
    public void commentBlock() {
        comment();
        // should be a comment already
        commentsBeforeSections.stream().forEach(YamlLine::comment);
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
        return String.format("  %s:", getName());
    }
}
