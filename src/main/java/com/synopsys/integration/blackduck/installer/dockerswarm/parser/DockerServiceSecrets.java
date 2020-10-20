package com.synopsys.integration.blackduck.installer.dockerswarm.parser;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class DockerServiceSecrets extends YamlLine implements YamlBlock {
    private List<ServiceSecretLine> lines = new LinkedList<>();
    private Map<String, ServiceSecretLine> secrets = new LinkedHashMap<>();

    public DockerServiceSecrets() {
        super("");
    }

    public void addSecret(ServiceSecretLine secret) {
        lines.add(secret);
        if (!secret.isCommentOnly()) {
            secrets.put(secret.getKey(), secret);
        }
    }

    public Optional<ServiceSecretLine> getSecret(String key) {
        return Optional.ofNullable(secrets.get(key));
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
        return "    secrets:";
    }
}
