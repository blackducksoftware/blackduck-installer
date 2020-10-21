package com.synopsys.integration.blackduck.installer.dockerswarm.yaml;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;

import com.synopsys.integration.blackduck.installer.dockerswarm.yaml.output.YamlWritable;
import com.synopsys.integration.blackduck.installer.dockerswarm.yaml.output.YamlWriter;

public class OverridesFile implements YamlWritable {
    private String version;
    private Map<String, DockerService> services = new LinkedHashMap<>();
    private GlobalSecrets globalSecrets = new GlobalSecrets();

    public boolean hasServices() {
        Predicate<DockerService> serviceUncommented = Predicate.not(DockerService::isCommented);
        return !services.isEmpty() && services.values().stream().anyMatch(serviceUncommented);
    }

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

    @Override
    public void write(YamlWriter writer) throws IOException {
        if (hasServices()) {
            String version = String.format("version: %s", getVersion());
            writer.writeLine(version);
            writer.writeLine("services:");
            for (DockerService service : services.values()) {
                service.write(writer);
            }
            globalSecrets.write(writer);
        }
    }
}
