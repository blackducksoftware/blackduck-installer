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
package com.synopsys.integration.blackduck.installer.dockerswarm.yaml.model;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class DefaultSection extends YamlSection {
    public static final String SERVICE_SECTION_INDENTATION = "  ";
    public static final String SERVICE_SUB_SECTION_INDENTATION = "    ";
    private List<YamlLine> lines;
    private Map<String, YamlSection> subSections;
    private String key;
    private YamlLine sectionLine;
    private int startLine;
    private String indentation;

    public DefaultSection(String key, YamlLine line) {
        this.key = key;
        this.lines = new LinkedList<>();
        this.subSections = new LinkedHashMap<>();
        this.sectionLine = line;
        this.startLine = line.getLineNumber();
        this.indentation = "";
    }

    @Override
    public String getIndentation() {
        return indentation;
    }

    @Override
    public void setIndentation(final String indentation) {
        this.indentation = indentation;
    }

    @Override
    public String getKey() {
        return key;
    }

    @Override
    public int getStartLine() {
        return startLine;
    }

    @Override
    public void addSubSection(YamlSection subSection) {
        subSections.put(subSection.getKey(), subSection);
    }

    @Override
    public <T extends YamlSection> Optional<T> getSubSection(String subSectionKey) {
        return Optional.ofNullable((T) subSections.get(subSectionKey));
    }

    @Override
    public void addLine(YamlLine yamlLine) {
        lines.add(yamlLine);
    }

    @Override
    public void addLine(int index, YamlLine yamlLine) {
        lines.add(index, yamlLine);
    }

    @Override
    public String toString() {
        return getKey();
    }

    @Override
    public boolean isCommented() {
        return sectionLine.isCommented() && isBlockCommented();
    }

    @Override
    public void comment() {
        sectionLine.comment();
        YamlLine.fixLineIndentation(sectionLine, getIndentation());
    }

    @Override
    public void uncomment() {
        sectionLine.uncomment();
        YamlLine.fixLineIndentation(sectionLine, getIndentation());
    }

    @Override
    public boolean isBlockCommented() {
        return lines.stream()
                   .allMatch(YamlLine::isCommented);
    }

    @Override
    public void commentBlock() {
        sectionLine.comment();
        subSections.values().stream()
            .forEach(YamlSection::commentBlock);
        lines.stream()
            .forEach(YamlLine::comment);
    }

    @Override
    public void uncommentBlock() {
        sectionLine.uncomment();
        subSections.values().stream()
            .forEach(YamlSection::uncommentBlock);
        lines.stream()
            .forEach(YamlLine::uncomment);
    }
}
