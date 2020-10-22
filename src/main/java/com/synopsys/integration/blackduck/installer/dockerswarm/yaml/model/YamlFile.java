package com.synopsys.integration.blackduck.installer.dockerswarm.yaml.model;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class YamlFile {
    private String version;
    private List<YamlLine> commentsBeforeVersion = new LinkedList<>();
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

    public void addCommentBeforeVersion(YamlLine line) {
        commentsBeforeVersion.add(line);
    }

    public Optional<DockerService> getService(String serviceName) {
        return Optional.ofNullable(services.get(serviceName));
    }

    public void addDockerSecret(DockerGlobalSecret secret) {
        globalSecrets.addSecret(secret);
    }

    public GlobalSecrets getGlobalSecrets() {
        return globalSecrets;
    }

    public Collection<DockerService> getServices() {
        return services.values();
    }

    public YamlLine getVersionLine() {
        return versionLine;
    }

    public YamlLine getServicesLine() {
        return servicesLine;
    }
}
