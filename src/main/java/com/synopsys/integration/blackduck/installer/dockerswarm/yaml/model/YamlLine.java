package com.synopsys.integration.blackduck.installer.dockerswarm.yaml.model;

public class YamlLine implements YamlTextLine {
    public static final String YAML_COMMENT_REGEX = "\\#";
    private boolean commented;
    private String currentRawText;
    private int lineNumber;

    protected YamlLine(int lineNumber, String line) {
        this.commented = true;
        this.currentRawText = line;
        this.lineNumber = lineNumber;
    }

    protected YamlLine(boolean commented, int lineNumber, String line) {
        this.commented = commented;
        this.currentRawText = line;
        this.lineNumber = lineNumber;
    }

    public static YamlLine create(int lineNumber, String line) {
        return new YamlLine(lineNumber, line);
    }

    public static YamlLine create(boolean commented, int lineNumber, String line) {
        return new YamlLine(commented, lineNumber, line);
    }

    public static boolean isCommented(String line) {
        return line.trim().startsWith("#");
    }

    public static void fixLineIndentation(YamlLine line, String requiredIndentation) {
        // get the text including the comment
        String currentText = line.getFormattedText();
        // remove the comment character and indent the line accordingly
        String indentedText = currentText.replaceFirst(YAML_COMMENT_REGEX, "");
        if (!indentedText.startsWith(requiredIndentation)) {
            indentedText = requiredIndentation + indentedText;
        }
        line.setCurrentRawText(indentedText);
    }

    public String getCurrentRawText() {
        return currentRawText;
    }

    public void setCurrentRawText(final String currentRawText) {
        this.currentRawText = currentRawText;
    }

    public int getLineNumber() {
        return lineNumber;
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

    public String getFormattedText() {
        String textLine = getCurrentRawText();
        if (isCommented()) {
            if (!textLine.trim().startsWith("#")) {
                textLine = "#" + textLine;
            }
        } else {
            textLine = textLine.replaceFirst(YAML_COMMENT_REGEX, "");
        }
        return textLine;
    }

    @Override
    public String toString() {
        return getFormattedText();
    }
}
