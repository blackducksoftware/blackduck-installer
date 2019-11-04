package com.synopsys.integration.blackduck.installer.dockerswarm.edit;

import com.synopsys.integration.blackduck.installer.exception.BlackDuckInstallerException;
import com.synopsys.integration.blackduck.installer.hash.HashUtility;
import com.synopsys.integration.blackduck.installer.hash.PreComputedHashes;
import com.synopsys.integration.blackduck.installer.model.AlertEncryption;
import com.synopsys.integration.blackduck.installer.model.CustomCertificate;
import com.synopsys.integration.log.IntLogger;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;

import static com.synopsys.integration.blackduck.installer.dockerswarm.DockerCommands.*;

public class AlertLocalOverridesEditor extends ConfigFileEditor {
    private final String stackName;
    private final String webServerHost;
    private final AlertEncryption alertEncryption;
    private final CustomCertificate customCertificate;
    private final boolean shouldEditFile;

    public AlertLocalOverridesEditor(IntLogger logger, HashUtility hashUtility, String lineSeparator, String stackName, String webServerHost, AlertEncryption alertEncryption, CustomCertificate customCertificate, boolean useLocalOverrides) {
        super(logger, hashUtility, lineSeparator);

        this.stackName = stackName;
        this.webServerHost = webServerHost;
        this.alertEncryption = alertEncryption;
        this.customCertificate = customCertificate;
        shouldEditFile = useLocalOverrides;
    }

    public String getFilename() {
        return "docker-compose.local-overrides.yml";
    }

    public String getComputedHash() {
        return PreComputedHashes.ALERT_DOCKER_COMPOSE_LOCAL_OVERRIDES_YML;
    }

    public void edit(File installDirectory) throws BlackDuckInstallerException {
        ConfigFile configFile = createConfigFile(installDirectory);
        if (!shouldEditFile)
            return;

        StringBuilder ymlBuilder = new StringBuilder();
        ymlBuilder.append("  alert:\n");
        ymlBuilder.append("    environment:\n");
        ymlBuilder.append("      - ALERT_HOSTNAME=");
        ymlBuilder.append(webServerHost);
        ymlBuilder.append("\n");

        if (!alertEncryption.isEmpty() || !customCertificate.isEmpty()) {
            appendAlertSecrets(ymlBuilder, alertEncryption, customCertificate);
            appendSecrets(ymlBuilder, alertEncryption, customCertificate);
        }

        try (Writer writer = new FileWriter(configFile.getFileToEdit(), true)) {
            writer.append(ymlBuilder.toString());
        } catch (IOException e) {
            throw new BlackDuckInstallerException("Error editing alert local overrides: " + e.getMessage());
        }
    }

    private void appendAlertSecrets(StringBuilder ymlBuilder, AlertEncryption alertEncryption, CustomCertificate customCertificate) {
        ymlBuilder.append("    secrets:\n");
        if (!alertEncryption.isEmpty()) {
            ymlBuilder.append("      - " + SECRET_ALERT_ENCRYPTION_PASSWORD + "\n");
            ymlBuilder.append("      - " + SECRET_ALERT_ENCRYPTION_GLOBAL_SALT + "\n");
        }
        if (!customCertificate.isEmpty()) {
            ymlBuilder.append("      - " + SECRET_CERT + "\n");
            ymlBuilder.append("      - " + SECRET_KEY + "\n");
        }
    }

    private void appendSecrets(StringBuilder ymlBuilder, AlertEncryption alertEncryption, CustomCertificate customCertificate) {
        ymlBuilder.append("secrets:\n");
        if (!alertEncryption.isEmpty()) {
            appendSecret(ymlBuilder, SECRET_ALERT_ENCRYPTION_PASSWORD);
            appendSecret(ymlBuilder, SECRET_ALERT_ENCRYPTION_GLOBAL_SALT);
        }
        if (!customCertificate.isEmpty()) {
            appendSecret(ymlBuilder, SECRET_CERT);
            appendSecret(ymlBuilder, SECRET_KEY);
        }
    }

    private void appendSecret(StringBuilder ymlBuilder, String secretName) {
        ymlBuilder.append("  ");
        ymlBuilder.append(secretName);
        ymlBuilder.append(":\n");
        ymlBuilder.append("    external: true\n");
        ymlBuilder.append("    name: \"");
        ymlBuilder.append(stackName);
        ymlBuilder.append("_");
        ymlBuilder.append(secretName);
        ymlBuilder.append("\"\n");
    }

}
