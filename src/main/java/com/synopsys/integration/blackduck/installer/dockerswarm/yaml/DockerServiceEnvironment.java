package com.synopsys.integration.blackduck.installer.dockerswarm.yaml;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.synopsys.integration.blackduck.installer.dockerswarm.yaml.output.YamlWriter;

public class DockerServiceEnvironment extends YamlLine implements YamlBlock {
    // The list contains comments and environmentVariables
    private List<ServiceEnvironmentLine> lines = new LinkedList<>();
    private Map<String, ServiceEnvironmentLine> environmentVariables = new LinkedHashMap<>();

    public DockerServiceEnvironment() {
        super("");
    }

    public void addEnvironmentVariableAtBeginning(ServiceEnvironmentLine environmentVariable) {
        lines.add(0, environmentVariable);
        if (environmentVariable.hasKey()) {
            environmentVariables.put(environmentVariable.getKey(), environmentVariable);
        }
    }

    public void addEnvironmentVariable(ServiceEnvironmentLine environmentVariable) {
        lines.add(environmentVariable);
        if (environmentVariable.hasKey()) {
            environmentVariables.put(environmentVariable.getKey(), environmentVariable);
        }
    }

    public Optional<ServiceEnvironmentLine> getEnvironmentVariable(String key) {
        return Optional.ofNullable(environmentVariables.get(key));
    }

    @Override
    public boolean isCommented() {
        return super.isCommented() && isBlockCommented();
    }

    @Override
    public boolean isBlockCommented() {
        return lines.stream().allMatch(ServiceEnvironmentLine::isCommented);
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

    @Override
    public void write(final YamlWriter writer) throws IOException {
        super.write(writer);
        for (ServiceEnvironmentLine environmentLine : lines) {
            environmentLine.write(writer);
        }
    }
}
