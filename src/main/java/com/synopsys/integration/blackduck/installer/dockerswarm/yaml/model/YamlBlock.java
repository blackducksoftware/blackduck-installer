package com.synopsys.integration.blackduck.installer.dockerswarm.yaml.model;

import java.util.Collection;

public interface YamlBlock {
    void commentBlock();

    void uncommentBlock();

    boolean isBlockCommented();

    Collection<YamlLine> getLinesInBlock();
}
