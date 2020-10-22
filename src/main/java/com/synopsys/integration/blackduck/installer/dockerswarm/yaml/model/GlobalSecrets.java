package com.synopsys.integration.blackduck.installer.dockerswarm.yaml.model;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

public class GlobalSecrets extends YamlLine {
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
    public String createTextLine() {
        return "secrets:";
    }
}
