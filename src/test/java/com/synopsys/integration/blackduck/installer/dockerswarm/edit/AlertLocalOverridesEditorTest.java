package com.synopsys.integration.blackduck.installer.dockerswarm.edit;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.synopsys.integration.blackduck.installer.Application;
import com.synopsys.integration.blackduck.installer.exception.BlackDuckInstallerException;
import com.synopsys.integration.blackduck.installer.hash.HashUtility;
import com.synopsys.integration.blackduck.installer.model.AlertBlackDuckInstallOptions;
import com.synopsys.integration.blackduck.installer.model.AlertDatabase;
import com.synopsys.integration.blackduck.installer.model.AlertEncryption;
import com.synopsys.integration.blackduck.installer.model.CustomCertificate;
import com.synopsys.integration.log.IntLogger;
import com.synopsys.integration.log.Slf4jIntLogger;

public class AlertLocalOverridesEditorTest {
    private static final String STACK_NAME = "alert_test";
    private static final String ALERT_ADMIN_EMAIL = "noreply@blackducksoftware.com";
    private static final String ALERT_HOST = "alert-host";
    private Logger logger = LoggerFactory.getLogger(AlertLocalOverridesEditorTest.class);

    @Test
    @Disabled
    public void testAlertLocalOverrides() throws IOException, BlackDuckInstallerException, URISyntaxException {
        String input = IOUtils.toString(getClass().getResourceAsStream("/alert-docker-compose.yaml"), StandardCharsets.UTF_8);
        String expectedOutput = IOUtils.toString(getClass().getResourceAsStream("/desired-alert-docker-compose.yaml"), StandardCharsets.UTF_8);
        File tempInstallDirectory = new File("build/temp/");
        tempInstallDirectory.mkdirs();
        File dockerDir = new File(tempInstallDirectory, "docker-swarm");
        dockerDir.mkdirs();
        File testComposeFile = new File(dockerDir, "docker-compose.local-overrides.yml");
        Files.write(testComposeFile.toPath(), input.getBytes());

        IntLogger intLogger = new Slf4jIntLogger(logger);
        HashUtility hashUtility = new HashUtility();
        AlertDatabase alertDatabase = createAlertDatabase();
        AlertEncryption alertEncryption = createAlertEncryption();
        CustomCertificate customCertificate = createCustomCertificate();
        AlertBlackDuckInstallOptions alertBlackDuckInstallOptions = createAlertBlackDuckInstallOptions();
        AlertLocalOverridesEditor editor = new AlertLocalOverridesEditor(intLogger, hashUtility, Application.DEFAULT_LINE_SEPARATOR, STACK_NAME, ALERT_HOST, ALERT_ADMIN_EMAIL, alertEncryption, customCertificate,
            alertBlackDuckInstallOptions, true, alertDatabase);
        editor.edit2(tempInstallDirectory);
        String actualFileContent = Files.readString(testComposeFile.toPath());

        assertEquals(expectedOutput, actualFileContent);
        Files.deleteIfExists(tempInstallDirectory.toPath());
    }

    private AlertDatabase createAlertDatabase() throws BlackDuckInstallerException {
        String databaseName = "alert_database";
        String alertUserName = "alert_default_user_name";
        String alertPassword = "alert_default_password";
        String userSecretPath = "alert-db-user-secret-path";
        String passwordSecretPath = "alert-db-password-secret-path";
        return new AlertDatabase(databaseName, false, alertUserName, alertPassword, userSecretPath, passwordSecretPath);
    }

    private AlertEncryption createAlertEncryption() throws BlackDuckInstallerException {
        String alertEncryptionPassword = "alert-encryption-password-secret-path";
        String alertEncryptionSalt = "alert-encryption-salt-secret-path";
        return new AlertEncryption(alertEncryptionPassword, alertEncryptionSalt);
    }

    private CustomCertificate createCustomCertificate() throws BlackDuckInstallerException {
        return new CustomCertificate(null, null);
    }

    private AlertBlackDuckInstallOptions createAlertBlackDuckInstallOptions() {
        String blackDuckUrl = "black_duck_url";
        String blackduckApi = "black_duck_api_token";
        Integer blackduckTimeout = 500;
        return new AlertBlackDuckInstallOptions(blackDuckUrl, blackduckApi, blackduckTimeout, null, -1);
    }
}
