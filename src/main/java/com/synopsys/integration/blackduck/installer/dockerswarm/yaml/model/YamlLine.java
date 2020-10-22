package com.synopsys.integration.blackduck.installer.dockerswarm.yaml.model;

public class YamlLine {
    private final String line;
    private boolean commented;

    protected YamlLine(String line) {
        this.commented = true;
        this.line = line;
    }

    protected YamlLine(boolean commented, String line) {
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
    public String toString() {
        String textLine = createTextLine();
        if (isCommented() && !textLine.trim().startsWith("#")) {
            textLine = "#" + textLine;
        }
        return textLine;
    }
}
