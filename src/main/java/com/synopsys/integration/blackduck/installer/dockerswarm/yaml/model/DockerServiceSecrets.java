package com.synopsys.integration.blackduck.installer.dockerswarm.yaml.model;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.synopsys.integration.blackduck.installer.dockerswarm.yaml.output.YamlWriter;

public class DockerServiceSecrets extends YamlLine implements YamlBlock {
    private List<ServiceSecretLine> lines = new LinkedList<>();
    private Map<String, ServiceSecretLine> secrets = new LinkedHashMap<>();

    public DockerServiceSecrets() {
        super("");
    }

    public void addSecret(ServiceSecretLine secret) {
        lines.add(secret);
        if (secret.hasKey()) {
            secrets.put(secret.getKey(), secret);
        }
    }

    public Optional<ServiceSecretLine> getSecret(String key) {
        return Optional.ofNullable(secrets.get(key));
    }

    @Override
    public boolean isCommented() {
        return super.isCommented() && isBlockCommented();
    }

    @Override
    public boolean isBlockCommented() {
        return lines.stream().allMatch(ServiceSecretLine::isCommented);
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

    @Override
    public void write(final YamlWriter writer) throws IOException {
        super.write(writer);
        for (ServiceSecretLine secretLine : lines) {
            secretLine.write(writer);
        }
    }
}
