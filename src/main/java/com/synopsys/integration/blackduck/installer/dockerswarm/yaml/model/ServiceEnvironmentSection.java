package com.synopsys.integration.blackduck.installer.dockerswarm.yaml.model;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;

public class ServiceEnvironmentSection extends DefaultSection {
    private Map<String, ServiceEnvironmentLine> environmentVariables;

    public ServiceEnvironmentSection(final String key, final YamlLine line) {
        super(key, line);
        environmentVariables = new HashMap<>();
    }

    @Override
    public void addLine(final YamlLine yamlLine) {
        ServiceEnvironmentLine environmentLine = ServiceEnvironmentLine.of(yamlLine);
        if (environmentLine.hasKey()) {
            environmentVariables.put(environmentLine.getKey(), environmentLine);
        }
        super.addLine(yamlLine);
    }

    @Override
    public void addLine(int index, YamlLine yamlLine) {
        ServiceEnvironmentLine environmentLine = ServiceEnvironmentLine.of(yamlLine);
        if (environmentLine.hasKey()) {
            environmentVariables.put(environmentLine.getKey(), environmentLine);
        }
        super.addLine(index, yamlLine);
    }

    public Optional<ServiceEnvironmentLine> getVariableLine(String key) {
        return Optional.ofNullable(environmentVariables.get(key));
    }

    public void setEnvironmentVariableValue(String key, int value) {
        setEnvironmentVariableValue(key, String.valueOf(value));
    }

    public void setEnvironmentVariableValue(String key, boolean value) {
        setEnvironmentVariableValue(key, String.valueOf(value));
    }

    public void setEnvironmentVariableValue(String key, String value) {
        Optional<ServiceEnvironmentLine> environmentLine = getVariableLine(key);
        if (environmentLine.isPresent() && StringUtils.isNotBlank(value)) {
            ServiceEnvironmentLine environmentVariable = environmentLine.get();
            environmentVariable.getYamlLine().uncomment();
            environmentVariable.setValue(value);
        }
    }
}
