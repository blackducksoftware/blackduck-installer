package com.synopsys.integration.blackduck.installer.dockerswarm.yaml.model;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class GlobalSecrets extends YamlLine implements YamlBlock {
    private Map<String, DockerGlobalSecret> secrets = new LinkedHashMap<>();

    public GlobalSecrets() {
        super("");
    }

    public boolean allSecretsCommented() {
        return !secrets.isEmpty() && secrets.values().stream().allMatch(DockerGlobalSecret::isCommented);
    }

    public void addSecret(DockerGlobalSecret secret) {
        secrets.put(secret.getKey(), secret);
    }

    public Optional<DockerGlobalSecret> getSecret(String key) {
        return Optional.ofNullable(secrets.get(key));
    }

    public Collection<DockerGlobalSecret> getSecrets() {
        return secrets.values();
    }

    @Override
    public void commentBlock() {
        comment();
        Collection<DockerGlobalSecret> dockerSecrets = getSecrets();
        dockerSecrets.forEach(YamlBlock::commentBlock);
    }

    @Override
    public void uncommentBlock() {
        uncomment();
        Collection<DockerGlobalSecret> dockerSecrets = getSecrets();
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
        Collection<DockerGlobalSecret> dockerSecrets = getSecrets();
        for (DockerGlobalSecret secret : dockerSecrets) {
            linesInBlock.addAll(secret.getLinesInBlock());
        }
        return linesInBlock;
    }

    @Override
    public String createTextLine() {
        return "secrets:";
    }
}
