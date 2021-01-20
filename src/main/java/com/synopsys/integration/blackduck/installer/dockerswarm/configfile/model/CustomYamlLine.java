/**
 * blackduck-installer
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package com.synopsys.integration.blackduck.installer.dockerswarm.configfile.model;

public class CustomYamlLine implements CustomYamlTextLine {
    public static final String YAML_COMMENT_REGEX = "\\#";
    private boolean commented;
    private String currentRawText;
    private int lineNumber;

    protected CustomYamlLine(int lineNumber, String line) {
        this.commented = true;
        this.currentRawText = line;
        this.lineNumber = lineNumber;
    }

    protected CustomYamlLine(boolean commented, int lineNumber, String line) {
        this.commented = commented;
        this.currentRawText = line;
        this.lineNumber = lineNumber;
    }

    public static CustomYamlLine create(int lineNumber, String line) {
        return new CustomYamlLine(lineNumber, line);
    }

    public static CustomYamlLine create(boolean commented, int lineNumber, String line) {
        return new CustomYamlLine(commented, lineNumber, line);
    }

    public static boolean isCommented(String line) {
        return line.trim().startsWith("#");
    }

    public static void fixLineIndentation(CustomYamlLine line, String requiredIndentation) {
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
