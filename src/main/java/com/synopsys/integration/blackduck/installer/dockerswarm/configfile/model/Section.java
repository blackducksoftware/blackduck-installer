/**
 * blackduck-installer
 *
 * Copyright (c) 2020 Synopsys, Inc.
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

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class Section implements CustomYamlBlock, CustomYamlTextLine {
    public static final String SERVICE_SECTION_INDENTATION = "  ";
    public static final String SERVICE_SUB_SECTION_INDENTATION = "    ";
    private List<CustomYamlLine> lines;
    private Map<String, Section> subSections;
    private String key;
    private CustomYamlLine sectionHeading;
    private int startLine;
    private String indentation;

    public Section(String key, CustomYamlLine line) {
        this.key = key;
        this.lines = new LinkedList<>();
        this.subSections = new LinkedHashMap<>();
        this.sectionHeading = line;
        this.startLine = line.getLineNumber();
        this.indentation = "";
    }

    public String getIndentation() {
        return indentation;
    }

    public void setIndentation(final String indentation) {
        this.indentation = indentation;
    }

    public String getKey() {
        return key;
    }

    public int getStartLine() {
        return startLine;
    }

    public void addSubSection(Section subSection) {
        subSections.put(subSection.getKey(), subSection);
    }

    public <T extends Section> Optional<T> getSubSection(String subSectionKey) {
        return Optional.ofNullable((T) subSections.get(subSectionKey));
    }

    public void addLine(CustomYamlLine customYamlLine) {
        lines.add(customYamlLine);
    }

    public void addLine(int index, CustomYamlLine customYamlLine) {
        lines.add(index, customYamlLine);
    }

    @Override
    public String toString() {
        return getKey();
    }

    @Override
    public boolean isCommented() {
        return sectionHeading.isCommented() && isBlockCommented();
    }

    @Override
    public void comment() {
        sectionHeading.comment();
        CustomYamlLine.fixLineIndentation(sectionHeading, getIndentation());
    }

    @Override
    public void uncomment() {
        sectionHeading.uncomment();
        CustomYamlLine.fixLineIndentation(sectionHeading, getIndentation());
    }

    @Override
    public boolean isBlockCommented() {
        return lines.stream()
                   .allMatch(CustomYamlLine::isCommented);
    }

    @Override
    public void commentBlock() {
        sectionHeading.comment();
        subSections.values().stream()
            .forEach(Section::commentBlock);
        lines.stream()
            .forEach(CustomYamlLine::comment);
    }

    @Override
    public void uncommentBlock() {
        sectionHeading.uncomment();
        subSections.values().stream()
            .forEach(Section::uncommentBlock);
        lines.stream()
            .forEach(CustomYamlLine::uncomment);
    }
}
