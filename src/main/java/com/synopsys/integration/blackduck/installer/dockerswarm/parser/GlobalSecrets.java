package com.synopsys.integration.blackduck.installer.dockerswarm.parser;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

public class GlobalSecrets extends YamlLine {
    private Map<String, DockerSecret> secrets = new LinkedHashMap<>();

    public GlobalSecrets() {
        super("");
    }

    public void addSecret(DockerSecret secret) {
        secrets.put(secret.getKey(), secret);
    }

    public Optional<DockerSecret> getSecret(String key) {
        return Optional.ofNullable(secrets.get(key));
    }

    @Override
    public String createTextLine() {
        return "secrets:";
    }
}
