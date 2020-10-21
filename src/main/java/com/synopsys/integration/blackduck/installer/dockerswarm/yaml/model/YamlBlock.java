package com.synopsys.integration.blackduck.installer.dockerswarm.yaml.model;

public interface YamlBlock {
    void commentBlock();

    void uncommentBlock();

    boolean isBlockCommented();
}
