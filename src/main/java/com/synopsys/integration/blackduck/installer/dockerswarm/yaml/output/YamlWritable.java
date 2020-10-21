package com.synopsys.integration.blackduck.installer.dockerswarm.yaml.output;

import java.io.IOException;

// FIXME: Classes the implement this should have separate classes created for it.
// TODO: YamlFileWriter
// TODO: - YamlServiceWriter
// TODO:   - YamlServiceEnvironmentWriter
// TODO:   - YamlServiceSecretsWriter
// TODO:     - YamlLineWriter
// TODO: - YamlGlobalSecretsWriter
public interface YamlWritable {
    void write(YamlWriter writer) throws IOException;
}
