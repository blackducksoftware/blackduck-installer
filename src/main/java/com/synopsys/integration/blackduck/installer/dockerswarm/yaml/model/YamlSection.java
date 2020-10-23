package com.synopsys.integration.blackduck.installer.dockerswarm.yaml.model;

import java.util.Optional;

public abstract class YamlSection implements YamlBlock, YamlTextLine {

    public abstract String getIndentation();

    public abstract void setIndentation(String indentation);

    public abstract String getKey();

    public abstract int getStartLine();

    public abstract void addSubSection(YamlSection subSection);

    public abstract <T extends YamlSection> Optional<T> getSubSection(String subSectionKey);

    public abstract void addLine(YamlLine yamlLine);

    public abstract void addLine(int index, YamlLine yamlLine);
}
