package com.synopsys.integration.blackduck.installer.dockerswarm.yaml.model;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class GlobalSecrets extends YamlLine implements YamlBlock {
    private Map<String, DockerSecret> secrets = new LinkedHashMap<>();

    public GlobalSecrets() {
        super("");
    }

    public boolean allSecretsCommented() {
        return !secrets.isEmpty() && secrets.values().stream().allMatch(DockerSecret::isCommented);
    }

    public void addSecret(DockerSecret secret) {
        secrets.put(secret.getKey(), secret);
    }

    public Optional<DockerSecret> getSecret(String key) {
        return Optional.ofNullable(secrets.get(key));
    }

    public Collection<DockerSecret> getSecrets() {
        return secrets.values();
    }

    @Override
    public void commentBlock() {
        comment();
        Collection<DockerSecret> dockerSecrets = getSecrets();
        dockerSecrets.forEach(YamlBlock::commentBlock);
    }

    @Override
    public void uncommentBlock() {
        uncomment();
        Collection<DockerSecret> dockerSecrets = getSecrets();
        dockerSecrets.forEach(YamlBlock::commentBlock);
    }

    @Override
    public boolean isBlockCommented() {
        return false;
    }

    @Override
    public Collection<YamlLine> getLinesInBlock() {
        List<YamlLine> linesInBlock = new LinkedList<>();
        linesInBlock.add(this); // add the line containing the secrets name
        Collection<DockerSecret> dockerSecrets = getSecrets();
        for (DockerSecret secret : dockerSecrets) {
            linesInBlock.addAll(secret.getLinesInBlock());
        }
        return linesInBlock;
    }

    @Override
    public String createTextLine() {
        return "secrets:";
    }
}
