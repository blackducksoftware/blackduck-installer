package com.synopsys.integration.blackduck.installer.dockerswarm.yaml.output;

import java.io.IOException;

public interface YamlWritable {
    void write(YamlWriter writer) throws IOException;
}
