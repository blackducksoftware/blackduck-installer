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
package com.synopsys.integration.blackduck.installer.dockerswarm.edit;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

import com.synopsys.integration.blackduck.installer.exception.BlackDuckInstallerException;
import com.synopsys.integration.blackduck.installer.hash.HashUtility;
import com.synopsys.integration.blackduck.installer.hash.PreComputedHashes;
import com.synopsys.integration.blackduck.installer.model.AlertBlackDuckInstallOptions;
import com.synopsys.integration.blackduck.installer.model.AlertDatabase;
import com.synopsys.integration.blackduck.installer.model.AlertEncryption;
import com.synopsys.integration.blackduck.installer.model.CustomCertificate;
import com.synopsys.integration.blackduck.installer.model.DockerSecret;
import com.synopsys.integration.log.IntLogger;

public class AlertLocalOverridesEditor extends ConfigFileEditor {
    private final String stackName;
    private final String webServerHost;
    private final String alertAdminEmail;
    private final AlertEncryption alertEncryption;
    private final CustomCertificate customCertificate;
    private final AlertBlackDuckInstallOptions alertBlackDuckInstallOptions;
    private final boolean shouldEditFile;
    private final AlertDatabase alertDatabase;

    public AlertLocalOverridesEditor(IntLogger logger, HashUtility hashUtility, String lineSeparator, String stackName, String webServerHost, String alertAdminEmail, AlertEncryption alertEncryption, CustomCertificate customCertificate,
        AlertBlackDuckInstallOptions alertBlackDuckInstallOptions, boolean useLocalOverrides, AlertDatabase alertDatabase) {
        super(logger, hashUtility, lineSeparator);

        this.stackName = stackName;
        this.webServerHost = webServerHost;
        this.alertAdminEmail = alertAdminEmail;
        this.alertEncryption = alertEncryption;
        this.customCertificate = customCertificate;
        this.alertBlackDuckInstallOptions = alertBlackDuckInstallOptions;
        shouldEditFile = useLocalOverrides;
        this.alertDatabase = alertDatabase;
    }

    public String getFilename() {
        return "docker-compose.local-overrides.yml";
    }

    @Override
    public Set<String> getSupportedComputedHashes() {
        return PreComputedHashes.ALERT_DOCKER_COMPOSE_LOCAL_OVERRIDES_YML;
    }

    public void edit(File installDirectory) throws BlackDuckInstallerException {
        ConfigFile configFile = createConfigFile(installDirectory);
        if (!shouldEditFile)
            return;

        StringBuilder ymlBuilder = new StringBuilder();
        createServicesSection(ymlBuilder);
        if (!alertDatabase.isExternal()) {
            ymlBuilder.append("  alertdb:\n");
            ymlBuilder.append("    environment:\n");
            addEnvironmentVariable(ymlBuilder, "POSTGRES_DB", alertDatabase.getDatabaseName());
            if (alertDatabase.hasSecrets()) {
                addEnvironmentVariable(ymlBuilder, "POSTGRES_USER_FILE", alertDatabase.getPostgresUserNameSecretEnvironmentValue());
                addEnvironmentVariable(ymlBuilder, "POSTGRES_PASSWORD_FILE", alertDatabase.getPostgresPasswordSecretEnvironmentValue());

                List<DockerSecret> secrets = Arrays.asList(alertDatabase.getUserNameSecret(), alertDatabase.getPasswordSecret());
                appendContainerSecrets(ymlBuilder, secrets);
            } else {
                addEnvironmentVariable(ymlBuilder, "POSTGRES_USER", alertDatabase.getDefaultUserName());
                addEnvironmentVariable(ymlBuilder, "POSTGRES_PASSWORD", alertDatabase.getDefaultPassword());
            }
        }

        ymlBuilder.append("  alert:\n");
        ymlBuilder.append("    environment:\n");

        addEnvironmentVariable(ymlBuilder, "ALERT_HOSTNAME", webServerHost);
        addEnvironmentVariable(ymlBuilder, "ALERT_COMPONENT_SETTINGS_SETTINGS_USER_DEFAULT_ADMIN_EMAIL", alertAdminEmail);
        addEnvironmentVariable(ymlBuilder, "ALERT_IMPORT_CERT", StringUtils.isNotBlank(alertBlackDuckInstallOptions.getBlackDuckHostForAutoSslImport()));
        addEnvironmentVariable(ymlBuilder, "PUBLIC_HUB_WEBSERVER_HOST", alertBlackDuckInstallOptions.getBlackDuckHostForAutoSslImport());
        addEnvironmentVariable(ymlBuilder, "PUBLIC_HUB_WEBSERVER_PORT", alertBlackDuckInstallOptions.getBlackDuckPortForAutoSslImport());
        addEnvironmentVariable(ymlBuilder, "ALERT_PROVIDER_BLACKDUCK_BLACKDUCK_URL", alertBlackDuckInstallOptions.getBlackDuckUrl());
        addEnvironmentVariable(ymlBuilder, "ALERT_PROVIDER_BLACKDUCK_BLACKDUCK_API_KEY", alertBlackDuckInstallOptions.getBlackDuckApiToken());
        addEnvironmentVariable(ymlBuilder, "ALERT_PROVIDER_BLACKDUCK_BLACKDUCK_TIMEOUT", alertBlackDuckInstallOptions.getBlackDuckTimeoutInSeconds());

        if (!alertEncryption.isEmpty() || !customCertificate.isEmpty() || alertDatabase.hasSecrets()) {
            appendAlertSecrets(ymlBuilder, alertEncryption, customCertificate, alertDatabase);
            appendSecrets(ymlBuilder, alertEncryption, customCertificate, alertDatabase);
        }

        // write the file clean because the original has an alertdb service which may not be there if it's an external deployment.
        // Also the original file has environment variables set for postgres but if secrets are used those environment variables cannot be present.
        // To cover all the deployment options the safest thing to do is write the file clean.
        try (Writer writer = new FileWriter(configFile.getFileToEdit(), false)) {
            writer.append(ymlBuilder.toString());
        } catch (IOException e) {
            throw new BlackDuckInstallerException("Error editing alert local overrides: " + e.getMessage());
        }
    }

    private void createServicesSection(StringBuilder ymlBuilder) {
        ymlBuilder.append("version: '3.6'\n");
        ymlBuilder.append("services:\n");
    }

    private void addEnvironmentVariable(StringBuilder ymlBuilder, String key, String value) {
        if (StringUtils.isNotBlank(value)) {
            append(ymlBuilder, key, value);
        }
    }

    private void addEnvironmentVariable(StringBuilder ymlBuilder, String key, int value) {
        if (value > 0) {
            append(ymlBuilder, key, Integer.toString(value));
        }
    }

    private void addEnvironmentVariable(StringBuilder ymlBuilder, String key, boolean value) {
        if (value) {
            append(ymlBuilder, key, "true");
        }
    }

    private void append(StringBuilder ymlBuilder, String key, String value) {
        ymlBuilder.append(String.format("      - %s=%s\n", key, value));
    }

    private void appendAlertSecrets(StringBuilder ymlBuilder, AlertEncryption alertEncryption, CustomCertificate customCertificate, AlertDatabase alertDatabase) {
        List<DockerSecret> secrets = new ArrayList<>();

        ymlBuilder.append("    secrets:\n");
        if (!alertEncryption.isEmpty()) {
            secrets.add(alertEncryption.getPassword());
            secrets.add(alertEncryption.getSalt());
        }
        if (!customCertificate.isEmpty()) {
            secrets.add(customCertificate.getCertificate());
            secrets.add(customCertificate.getPrivateKey());
        }

        if (alertDatabase.hasSecrets()) {
            secrets.add(alertDatabase.getUserNameSecret());
            secrets.add(alertDatabase.getPasswordSecret());
        }

        appendContainerSecrets(ymlBuilder, secrets);
    }

    private void appendContainerSecrets(StringBuilder ymlBuilder, List<DockerSecret> secrets) {
        if (!secrets.isEmpty()) {
            ymlBuilder.append("    secrets:\n");
            for (DockerSecret secret : secrets) {
                ymlBuilder.append("      - " + secret.getLabel() + "\n");
            }
        }
    }

    private void appendSecrets(StringBuilder ymlBuilder, AlertEncryption alertEncryption, CustomCertificate customCertificate, AlertDatabase alertDatabase) {
        ymlBuilder.append("secrets:\n");
        if (!alertEncryption.isEmpty()) {
            appendSecret(ymlBuilder, alertEncryption.getPassword().getLabel());
            appendSecret(ymlBuilder, alertEncryption.getSalt().getLabel());
        }
        if (!customCertificate.isEmpty()) {
            appendSecret(ymlBuilder, customCertificate.getCertificate().getLabel());
            appendSecret(ymlBuilder, customCertificate.getPrivateKey().getLabel());
        }
        if (alertDatabase.hasSecrets()) {
            appendSecret(ymlBuilder, alertDatabase.getUserNameSecret().getLabel());
            appendSecret(ymlBuilder, alertDatabase.getPasswordSecret().getLabel());
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
