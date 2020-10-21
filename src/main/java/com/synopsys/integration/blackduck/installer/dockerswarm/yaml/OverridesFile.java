package com.synopsys.integration.blackduck.installer.dockerswarm.yaml;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

import com.synopsys.integration.blackduck.installer.dockerswarm.yaml.output.YamlWritable;
import com.synopsys.integration.blackduck.installer.dockerswarm.yaml.output.YamlWriter;

public class OverridesFile implements YamlWritable {
    private String version;
    private Map<String, DockerService> services = new LinkedHashMap<>();
    private GlobalSecrets globalSecrets = new GlobalSecrets();
    private YamlLine versionLine;
    private YamlLine servicesLine;

    public boolean allServicesCommented() {
        return !services.isEmpty() && services.values().stream().allMatch(DockerService::isCommented);
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(final String version) {
        this.version = version;
        String versionString = String.format("version: %s", getVersion());
        this.versionLine = YamlLine.create(versionString);
    }

    public void addService(DockerService service) {
        services.put(service.getName(), service);
        servicesLine = YamlLine.create("services:");
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
        boolean allServicesCommented = allServicesCommented();
        boolean allSecretsCommented = globalSecrets.allSecretsCommented();
        versionLine.uncomment();
        servicesLine.uncomment();
        globalSecrets.uncomment();

        if (allServicesCommented) {
            servicesLine.comment();
        }
        if (allSecretsCommented) {
            globalSecrets.comment();
        }

        if (allSecretsCommented && allServicesCommented) {
            versionLine.comment();
        }

        versionLine.write(writer);
        servicesLine.write(writer);
        for (DockerService service : services.values()) {
            service.write(writer);
        }
        globalSecrets.write(writer);
    }

}
