package com.synopsys.integration.blackduck.installer.dockerswarm.yaml;

import java.io.IOException;

import com.synopsys.integration.blackduck.installer.dockerswarm.yaml.output.YamlWritable;
import com.synopsys.integration.blackduck.installer.dockerswarm.yaml.output.YamlWriter;

public class YamlLine implements YamlWritable {
    private final String line;
    private boolean commented;

    public YamlLine(String line) {
        this.commented = true;
        this.line = line;
    }

    public YamlLine(boolean commented, String line) {
        this.commented = commented;
        this.line = line;
    }

    public static YamlLine create(String line) {
        return new YamlLine(line);
    }

    public static YamlLine create(boolean commented, String line) {
        return new YamlLine(commented, line);
    }

    public static boolean isCommented(String line) {
        return line.trim().startsWith("#");
    }

    public String createTextLine() {
        return getLine();
    }

    public boolean isCommented() {
        return commented;
    }

    public void comment() {
        this.commented = true;
    }

    public void uncomment() {
        this.commented = false;
    }

    public String getLine() {
        return line;
    }

    @Override
    public void write(YamlWriter writer) throws IOException {
        String line = this.toString();
        writer.writeLine(line);
    }

    @Override
    public String toString() {
        return (isCommented() ? "#" : "") + createTextLine();
    }
}
