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
import java.util.Optional;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.synopsys.integration.blackduck.installer.dockerswarm.yaml.model.GlobalSecrets;
import com.synopsys.integration.blackduck.installer.dockerswarm.yaml.model.ServiceEnvironmentLine;
import com.synopsys.integration.blackduck.installer.dockerswarm.yaml.model.ServiceEnvironmentSection;
import com.synopsys.integration.blackduck.installer.dockerswarm.yaml.model.ServiceSecretsSection;
import com.synopsys.integration.blackduck.installer.dockerswarm.yaml.model.YamlBlock;
import com.synopsys.integration.blackduck.installer.dockerswarm.yaml.model.YamlFile;
import com.synopsys.integration.blackduck.installer.dockerswarm.yaml.model.YamlSection;
import com.synopsys.integration.blackduck.installer.dockerswarm.yaml.model.YamlTextLine;
import com.synopsys.integration.blackduck.installer.dockerswarm.yaml.output.YamlFileWriter;
import com.synopsys.integration.blackduck.installer.dockerswarm.yaml.output.YamlWriter;
import com.synopsys.integration.blackduck.installer.dockerswarm.yaml.parser.YamlParser;
import com.synopsys.integration.blackduck.installer.exception.BlackDuckInstallerException;
import com.synopsys.integration.blackduck.installer.hash.HashUtility;
import com.synopsys.integration.blackduck.installer.hash.PreComputedHashes;
import com.synopsys.integration.blackduck.installer.model.AlertBlackDuckInstallOptions;
import com.synopsys.integration.blackduck.installer.model.AlertDatabase;
import com.synopsys.integration.blackduck.installer.model.AlertEncryption;
import com.synopsys.integration.blackduck.installer.model.CustomCertificate;
import com.synopsys.integration.log.IntLogger;

public class AlertLocalOverridesEditor extends ConfigFileEditor {
    private final String webServerHost;
    private final String alertAdminEmail;
    private final AlertEncryption alertEncryption;
    private final CustomCertificate customCertificate;
    private final AlertBlackDuckInstallOptions alertBlackDuckInstallOptions;
    private final boolean shouldEditFile;
    private final AlertDatabase alertDatabase;
    private final YamlParser yamlParser;
    private Logger logger = LoggerFactory.getLogger(AlertLocalOverridesEditor.class);

    public AlertLocalOverridesEditor(IntLogger logger, HashUtility hashUtility, String lineSeparator, String stackName, String webServerHost, String alertAdminEmail, AlertEncryption alertEncryption, CustomCertificate customCertificate,
        AlertBlackDuckInstallOptions alertBlackDuckInstallOptions, boolean useLocalOverrides, AlertDatabase alertDatabase) {
        super(logger, hashUtility, lineSeparator);

        this.webServerHost = webServerHost;
        this.alertAdminEmail = alertAdminEmail;
        this.alertEncryption = alertEncryption;
        this.customCertificate = customCertificate;
        this.alertBlackDuckInstallOptions = alertBlackDuckInstallOptions;
        shouldEditFile = useLocalOverrides;
        this.alertDatabase = alertDatabase;
        this.yamlParser = new YamlParser(stackName, "<STACK_NAME>_");
    }

    public String getFilename() {
        return "docker-compose.local-overrides.yml";
    }

    @Override
    public Set<String> getSupportedComputedHashes() {
        return PreComputedHashes.ALERT_DOCKER_COMPOSE_LOCAL_OVERRIDES_YML;
    }

    @Override
    public void edit(File installDirectory) throws BlackDuckInstallerException {
        ConfigFile configFile = createConfigFile(installDirectory);
        if (!shouldEditFile)
            return;

        YamlFile yamlFileModel = yamlParser.parse(configFile);
        updateValues(yamlFileModel);
        try (Writer writer = new FileWriter(configFile.getFileToEdit())) {
            YamlWriter yamlWriter = new YamlWriter(writer, lineSeparator);
            YamlFileWriter.write(yamlWriter, yamlFileModel);
        } catch (IOException e) {
            throw new BlackDuckInstallerException("Error editing local overrides: " + e.getMessage());
        }
    }

    private void updateValues(YamlFile parsedFile) {
        updateAlertDbServiceValues(parsedFile);
        updateAlertServiceValues(parsedFile);
    }

    private void updateAlertDbServiceValues(YamlFile parsedFile) {
        Optional<YamlSection> alertDbSection = parsedFile.getModifiableSection("services")
                                                   .flatMap(servicesSection -> servicesSection.getSubSection("alertdb"));
        if (alertDbSection.isEmpty()) {
            logger.error("alertdb service missing from overrides file.");
            return;
        }

        YamlSection alertDb = alertDbSection.get();
        if (alertDatabase.isExternal()) {
            alertDb.commentBlock();
            return;
        }
        Optional<ServiceEnvironmentSection> alertDbEnvironmentSection = alertDb.getSubSection("environment");
        Optional<ServiceSecretsSection> alertDbSecretsSection = alertDb.getSubSection("secrets");
        if (alertDbEnvironmentSection.isEmpty()) {
            logger.error("alertdb -> environment section missing from overrides file.");
            return;
        }

        if (alertDbSecretsSection.isEmpty()) {
            logger.error("alertdb -> secrets section missing from overrides file.");
            return;
        }

        ServiceEnvironmentSection alertDbEnvironment = alertDbEnvironmentSection.get();
        ServiceSecretsSection alertDbSecrets = alertDbSecretsSection.get();
        alertDb.uncomment();
        Optional<ServiceEnvironmentLine> alertDBLine = alertDbEnvironment.getVariableLine("POSTGRES_DB");
        Optional<ServiceEnvironmentLine> alertDBUser = alertDbEnvironment.getVariableLine("POSTGRES_USER");
        Optional<ServiceEnvironmentLine> alertDBPassword = alertDbEnvironment.getVariableLine("POSTGRES_PASSWORD");

        if (alertDBLine.isPresent() && StringUtils.isNotBlank(alertDatabase.getDatabaseName())) {
            alertDbEnvironment.uncomment();
            alertDBLine.get().uncomment();
            alertDBLine.get().setValue(alertDatabase.getDatabaseName());
        }

        if (alertDatabase.hasSecrets()) {
            alertDbSecrets.uncomment();
            alertDbEnvironment.getVariableLine("POSTGRES_USER").ifPresent(YamlTextLine::comment);
            alertDbEnvironment.getVariableLine("POSTGRES_PASSWORD").ifPresent(YamlTextLine::comment);
            alertDbEnvironment.getVariableLine("POSTGRES_USER_FILE").ifPresent(YamlTextLine::uncomment);
            alertDbEnvironment.getVariableLine("POSTGRES_PASSWORD_FILE").ifPresent(YamlTextLine::uncomment);
            enableDatabaseSecrets(alertDbSecrets, parsedFile.getGlobalSecrets());

        } else {
            if (alertDBUser.isPresent()) {
                ServiceEnvironmentLine dbUser = alertDBUser.get();
                dbUser.uncomment();
                dbUser.setValue(alertDatabase.getDefaultUserName());
            }
            if (alertDBPassword.isPresent()) {
                ServiceEnvironmentLine dbPassword = alertDBPassword.get();
                dbPassword.uncomment();
                dbPassword.setValue(alertDatabase.getDefaultPassword());
            }
        }
    }

    private void updateAlertServiceValues(YamlFile parsedFile) {
        Optional<YamlSection> alertSection = parsedFile.getModifiableSection("services")
                                                 .flatMap(servicesSection -> servicesSection.getSubSection("alert"));
        if (alertSection.isEmpty()) {
            logger.error("alert service missing from overrides file.");
            return;
        }
        YamlSection alert = alertSection.get();
        Optional<ServiceEnvironmentSection> alertDbEnvironmentSection = alert.getSubSection("environment");
        Optional<ServiceSecretsSection> alertDbSecretsSection = alert.getSubSection("secrets");
        GlobalSecrets globalSecrets = parsedFile.getGlobalSecrets();

        if (alertDbEnvironmentSection.isEmpty()) {
            logger.error("alertdb -> environment section missing from overrides file.");
            return;
        }

        if (alertDbSecretsSection.isEmpty()) {
            logger.error("alertdb -> secrets section missing from overrides file.");
            return;
        }

        ServiceEnvironmentSection alertEnvironment = alertDbEnvironmentSection.get();
        ServiceSecretsSection alertSecrets = alertDbSecretsSection.get();
        alertEnvironment.setEnvironmentVariableValue("ALERT_DB_NAME", alertDatabase.getDatabaseName());

        boolean hasEnvironmentSettings = StringUtils.isNotBlank(alertAdminEmail) || StringUtils.isNotBlank(webServerHost) || alertDatabase.isExternal() || !alertBlackDuckInstallOptions.isEmpty();
        boolean hasSecretsSettings = alertDatabase.hasSecrets() || !alertEncryption.isEmpty() || !customCertificate.isEmpty();
        // check if the alert service should be uncommented because there are settings.
        if (hasEnvironmentSettings || hasSecretsSettings) {
            alert.uncomment();
        }

        if (hasEnvironmentSettings) {
            alertEnvironment.uncomment();
        }

        if (hasSecretsSettings) {
            alertSecrets.uncomment();
        }

        if (alertDatabase.hasSecrets()) {
            enableDatabaseSecrets(alertSecrets, globalSecrets);
        }

        if (alertDatabase.isExternal()) {
            alertEnvironment.getVariableLine("ALERT_DB_HOST").ifPresent(YamlTextLine::uncomment);
            alertEnvironment.getVariableLine("ALERT_DB_PORT").ifPresent(YamlTextLine::uncomment);
            alertEnvironment.setEnvironmentVariableValue("ALERT_DB_HOST", alertDatabase.getExternalHost());
            alertEnvironment.setEnvironmentVariableValue("ALERT_DB_PORT", alertDatabase.getExternalPort());
        }

        alertEnvironment.setEnvironmentVariableValue("ALERT_HOSTNAME", webServerHost);
        alertEnvironment.setEnvironmentVariableValue("ALERT_PROVIDER_BLACKDUCK_BLACKDUCK_URL", alertBlackDuckInstallOptions.getBlackDuckUrl());
        alertEnvironment.setEnvironmentVariableValue("ALERT_PROVIDER_BLACKDUCK_BLACKDUCK_API_KEY", alertBlackDuckInstallOptions.getBlackDuckApiToken());
        alertEnvironment.setEnvironmentVariableValue("ALERT_PROVIDER_BLACKDUCK_BLACKDUCK_TIMEOUT", alertBlackDuckInstallOptions.getBlackDuckTimeoutInSeconds());

        // add these environment variables to the beginning of the environment block:
        // TODO: to be safe can check if these exist first.
        addEnvironmentVariable(parsedFile, alertEnvironment, 1, "PUBLIC_HUB_WEBSERVER_HOST");
        addEnvironmentVariable(parsedFile, alertEnvironment, 2, "PUBLIC_HUB_WEBSERVER_PORT");
        addEnvironmentVariable(parsedFile, alertEnvironment, 3, "ALERT_IMPORT_CERT");
        addEnvironmentVariable(parsedFile, alertEnvironment, 4, "ALERT_COMPONENT_SETTINGS_SETTINGS_USER_DEFAULT_ADMIN_EMAIL");

        alertEnvironment.setEnvironmentVariableValue("PUBLIC_HUB_WEBSERVER_HOST", alertBlackDuckInstallOptions.getBlackDuckHostForAutoSslImport());
        alertEnvironment.setEnvironmentVariableValue("PUBLIC_HUB_WEBSERVER_PORT", alertBlackDuckInstallOptions.getBlackDuckPortForAutoSslImport());
        alertEnvironment.setEnvironmentVariableValue("ALERT_IMPORT_CERT", StringUtils.isNotBlank(alertBlackDuckInstallOptions.getBlackDuckHostForAutoSslImport()));
        alertEnvironment.setEnvironmentVariableValue("ALERT_COMPONENT_SETTINGS_SETTINGS_USER_DEFAULT_ADMIN_EMAIL", alertAdminEmail);

        //secrets
        if (!alertEncryption.isEmpty()) {
            alertSecrets.getSecret("ALERT_ENCRYPTION_PASSWORD").ifPresent(YamlTextLine::uncomment);
            alertSecrets.getSecret("ALERT_ENCRYPTION_GLOBAL_SALT").ifPresent(YamlTextLine::uncomment);
            globalSecrets.uncomment();
            globalSecrets.getSecret("ALERT_ENCRYPTION_PASSWORD").ifPresent(YamlBlock::uncommentBlock);
            globalSecrets.getSecret("ALERT_ENCRYPTION_GLOBAL_SALT").ifPresent(YamlBlock::uncommentBlock);
        }

        if (!customCertificate.isEmpty()) {
            alertSecrets.getSecret("WEBSERVER_CUSTOM_CERT_FILE").ifPresent(YamlTextLine::uncomment);
            alertSecrets.getSecret("WEBSERVER_CUSTOM_KEY_FILE").ifPresent(YamlTextLine::uncomment);
            globalSecrets.uncomment();
            globalSecrets.getSecret("WEBSERVER_CUSTOM_CERT_FILE").ifPresent(YamlBlock::uncommentBlock);
            globalSecrets.getSecret("WEBSERVER_CUSTOM_KEY_FILE").ifPresent(YamlBlock::uncommentBlock);
        }
    }

    private void addEnvironmentVariable(YamlFile yamlFile, ServiceEnvironmentSection environmentSection, int offsetFromBeginning, String key) {
        ServiceEnvironmentLine newVariable = ServiceEnvironmentLine.newEnvironmentLine(-1, key);
        int enivronmentStart = environmentSection.getStartLine();
        environmentSection.addLine(offsetFromBeginning, newVariable.getYamlLine());
        yamlFile.addLine(enivronmentStart + offsetFromBeginning, newVariable.getYamlLine());
    }

    private void enableDatabaseSecrets(ServiceSecretsSection secrets, GlobalSecrets globalSecrets) {
        secrets.getSecret("ALERT_DB_USERNAME").ifPresent(YamlTextLine::uncomment);
        secrets.getSecret("ALERT_DB_PASSWORD").ifPresent(YamlTextLine::uncomment);
        globalSecrets.uncomment();
        globalSecrets.getSecret("ALERT_DB_USERNAME").ifPresent(YamlBlock::uncommentBlock);
        globalSecrets.getSecret("ALERT_DB_PASSWORD").ifPresent(YamlBlock::uncommentBlock);
    }
}
