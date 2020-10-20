package com.synopsys.integration.blackduck.installer.dockerswarm.parser;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class DockerServiceEnvironment extends YamlLine implements YamlBlock {
    // The list contains comments and environmentVariables
    private List<ServiceEnvironmentLine> lines = new LinkedList<>();
    private Map<String, ServiceEnvironmentLine> environmentVariables = new LinkedHashMap<>();

    public DockerServiceEnvironment() {
        super("");
    }

    public void addEnvironmentVariable(ServiceEnvironmentLine environmentVariable) {
        lines.add(environmentVariable);
        if (environmentVariable.isCommentOnly()) {
            environmentVariables.put(environmentVariable.getKey(), environmentVariable);
        }
    }

    public Optional<ServiceEnvironmentLine> getEnvironmentVariable(String key) {
        return Optional.ofNullable(environmentVariables.get(key));
    }

    @Override
    public void commentBlock() {
        comment();
        lines.forEach(YamlLine::comment);
    }

    @Override
    public void uncommentBlock() {
        uncomment();
        lines.forEach(YamlLine::uncomment);
    }

    @Override
    public String createTextLine() {
        return "    environment:";
    }
}
