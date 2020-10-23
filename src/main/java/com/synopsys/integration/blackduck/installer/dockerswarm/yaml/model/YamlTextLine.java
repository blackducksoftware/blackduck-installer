package com.synopsys.integration.blackduck.installer.dockerswarm.yaml.model;

public interface YamlTextLine {
    boolean isCommented();

    void comment();

    void uncomment();
}
