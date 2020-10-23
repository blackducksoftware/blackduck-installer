package com.synopsys.integration.blackduck.installer.dockerswarm.yaml.output;

import java.io.IOException;

import com.synopsys.integration.blackduck.installer.dockerswarm.yaml.model.YamlFile;
import com.synopsys.integration.blackduck.installer.dockerswarm.yaml.model.YamlLine;

public class YamlFileWriter {

    public static final void write(YamlWriter writer, YamlFile yamlFile) throws IOException {
        for (YamlLine yamlLine : yamlFile.getAllLines()) {
            writer.writeLine(yamlLine.getFormattedText());
        }
    }
}
