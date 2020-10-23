package com.synopsys.integration.blackduck.installer.dockerswarm.edit;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

import org.apache.commons.io.IOUtils;

public class ComposeFileUtility {

    public static File createLocalOverridesFile(File installDirectory, String testFileName) throws IOException {
        String input = IOUtils.toString(ComposeFileUtility.class.getResourceAsStream(testFileName), StandardCharsets.UTF_8);
        File dockerDir = new File(installDirectory, "docker-swarm");
        dockerDir.mkdirs();
        File testComposeFile = new File(dockerDir, "docker-compose.local-overrides.yml");
        Files.write(testComposeFile.toPath(), input.getBytes());
        return testComposeFile;
    }
}
