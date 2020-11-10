package com.synopsys.integration.blackduck.installer.dockerswarm.edit;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.synopsys.integration.blackduck.installer.Application;
import com.synopsys.integration.blackduck.installer.exception.BlackDuckInstallerException;
import com.synopsys.integration.blackduck.installer.hash.HashUtility;
import com.synopsys.integration.blackduck.installer.model.CustomCertificate;
import com.synopsys.integration.log.IntLogger;
import com.synopsys.integration.log.Slf4jIntLogger;

public class BlackDuckLocalOverridesEditorTest {
    private static final String STACK_NAME = "hub_test";
    private final Logger logger = LoggerFactory.getLogger(BlackDuckLocalOverridesEditor.class);
    private File tempInstallDirectory = new File("build/temp/");

    @BeforeEach
    public void cleanDirectory() {
        FileUtils.deleteQuietly(tempInstallDirectory);
    }

    @Test
    public void testOverrides() throws IOException, BlackDuckInstallerException {
        String expectedOutput = IOUtils.toString(getClass().getResourceAsStream("/blackduck/desired-blackduck-compose.yaml"), StandardCharsets.UTF_8);
        File testComposeFile = ComposeFileUtility.createLocalOverridesFile(tempInstallDirectory, "/blackduck/blackduck-docker-compose.yaml");

        IntLogger intLogger = new Slf4jIntLogger(logger);
        HashUtility hashUtility = new HashUtility();
        CustomCertificate customCertificate = new CustomCertificate("certPath", "certKeyPath");

        BlackDuckLocalOverridesEditor editor = new BlackDuckLocalOverridesEditor(intLogger, hashUtility, Application.DEFAULT_LINE_SEPARATOR, STACK_NAME, true, customCertificate);
        editor.edit(tempInstallDirectory);
        String actualFileContent = Files.readString(testComposeFile.toPath());
        assertEquals(expectedOutput, actualFileContent);
        FileUtils.deleteQuietly(tempInstallDirectory);
    }

    @Test
    public void testOverridesMissingCerts() throws IOException, BlackDuckInstallerException {
        String expectedOutput = IOUtils.toString(getClass().getResourceAsStream("/blackduck/desired-blackduck-no-certs-docker-compose.yaml"), StandardCharsets.UTF_8);
        File testComposeFile = ComposeFileUtility.createLocalOverridesFile(tempInstallDirectory, "/blackduck/blackduck-docker-compose.yaml");

        IntLogger intLogger = new Slf4jIntLogger(logger);
        HashUtility hashUtility = new HashUtility();
        CustomCertificate customCertificate = new CustomCertificate(null, null);

        BlackDuckLocalOverridesEditor editor = new BlackDuckLocalOverridesEditor(intLogger, hashUtility, Application.DEFAULT_LINE_SEPARATOR, STACK_NAME, true, customCertificate);
        editor.edit(tempInstallDirectory);
        String actualFileContent = Files.readString(testComposeFile.toPath());
        assertEquals(expectedOutput, actualFileContent);
        FileUtils.deleteQuietly(tempInstallDirectory);
    }

}
