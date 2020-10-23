package com.synopsys.integration.blackduck.installer.dockerswarm.yaml.model;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

// This is a mutable representation of the yaml files.
public class YamlFile {
    private List<YamlLine> allLines = new LinkedList<>();
    private Map<String, YamlSection> modifiableSections = new LinkedHashMap<>();
    private GlobalSecrets globalSecrets = new GlobalSecrets(YamlLine.create(-1, "#secrets:"));

    public void createGlobalSecrets(YamlLine line) {
        globalSecrets = new GlobalSecrets(line);
    }

    public void addLine(YamlLine line) {
        allLines.add(line);
    }

    public void addLine(int index, YamlLine line) {
        allLines.add(index, line);
    }

    public List<YamlLine> getAllLines() {
        return allLines;
    }

    public void addDockerSecret(DockerGlobalSecret secret) {
        globalSecrets.addSecret(secret);
    }

    public GlobalSecrets getGlobalSecrets() {
        return globalSecrets;
    }

    public void addModifiableSection(YamlSection yamlSection) {
        modifiableSections.put(yamlSection.getKey(), yamlSection);
    }

    public Optional<YamlSection> getModifiableSection(String sectionKey) {
        return Optional.ofNullable(modifiableSections.get(sectionKey));
    }
}
