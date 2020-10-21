package com.synopsys.integration.blackduck.installer.dockerswarm.yaml;

public interface YamlBlock {
    void commentBlock();

    void uncommentBlock();

    boolean isBlockCommented();
}
