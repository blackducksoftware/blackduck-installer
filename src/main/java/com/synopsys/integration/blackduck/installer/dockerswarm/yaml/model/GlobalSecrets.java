package com.synopsys.integration.blackduck.installer.dockerswarm.yaml.model;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

import com.synopsys.integration.blackduck.installer.dockerswarm.yaml.output.YamlWriter;

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

    @Override
    public String createTextLine() {
        return "secrets:";
    }

    @Override
    public void write(final YamlWriter writer) throws IOException {
        super.write(writer);
        for (DockerSecret secret : secrets.values()) {
            secret.write(writer);
        }
    }
}
