package com.synopsys.integration.blackduck.installer.dockerswarm.yaml.output;

import java.io.IOException;
import java.io.Writer;

public class YamlWriter {
    private final Writer writer;
    private final String lineSeparator;

    public YamlWriter(final Writer writer, final String lineSeparator) {
        this.writer = writer;
        this.lineSeparator = lineSeparator;
    }

    public void writeLine(String line) throws IOException {
        writer.write(line + lineSeparator);
    }
}
