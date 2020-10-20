package com.synopsys.integration.blackduck.installer.dockerswarm.parser;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

public class OverridesFile {
    private String version;
    private Map<String, DockerService> services = new LinkedHashMap<>();
    private GlobalSecrets globalSecrets = new GlobalSecrets();

    public String getVersion() {
        return version;
    }

    public void setVersion(final String version) {
        this.version = version;
    }

    public void addService(DockerService service) {
        services.put(service.getName(), service);
    }

    public Optional<DockerService> getService(String serviceName) {
        return Optional.ofNullable(services.get(serviceName));
    }

    public void addDockerSecret(DockerSecret secret) {
        globalSecrets.addSecret(secret);
    }

    public GlobalSecrets getGlobalSecrets() {
        return globalSecrets;
    }
}
