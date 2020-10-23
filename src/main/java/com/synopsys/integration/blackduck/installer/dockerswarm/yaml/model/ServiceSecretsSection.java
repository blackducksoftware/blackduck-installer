package com.synopsys.integration.blackduck.installer.dockerswarm.yaml.model;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class ServiceSecretsSection extends DefaultSection {
    private Map<String, ServiceSecretLine> secrets;

    public ServiceSecretsSection(String key, YamlLine line) {
        super(key, line);
        secrets = new HashMap<>();
    }

    public Optional<ServiceSecretLine> getSecret(String key) {
        return Optional.ofNullable(secrets.get(key));
    }

    @Override
    public void addLine(YamlLine yamlLine) {
        ServiceSecretLine secretLine = ServiceSecretLine.of(yamlLine);
        if (secretLine.hasKey()) {
            secrets.put(secretLine.getKey(), secretLine);
        }
        super.addLine(yamlLine);
    }
}
