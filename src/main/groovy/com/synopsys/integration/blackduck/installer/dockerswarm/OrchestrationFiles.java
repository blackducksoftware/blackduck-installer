package com.synopsys.integration.blackduck.installer.dockerswarm;

import java.io.File;
import java.util.Set;

public class OrchestrationFiles {
    public static final String COMPOSE = "docker-compose.yml";
    public static final String BDBA = "docker-compose.bdba.yml";
    public static final String DBMIGRATE = "docker-compose.dbmigrate.yml";
    public static final String EXTERNALDB = "docker-compose.externaldb.yml";
    public static final String LOCAL_OVERRIDES = "docker-compose.local-overrides.yml";

    public File dockerSwarmDirectory(File installDirectory) {
        return new File(installDirectory, "docker-swarm");
    }

    public void addOrchestrationFile(Set<String> orchestrationFiles, File installDirectory, String orchestrationFile) {
        orchestrationFiles.add(String.format("%s/docker-swarm/%s", installDirectory.getAbsolutePath(), orchestrationFile));
    }

}
