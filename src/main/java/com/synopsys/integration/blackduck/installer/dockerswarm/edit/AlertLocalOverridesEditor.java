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
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;

import com.synopsys.integration.blackduck.installer.dockerswarm.yaml.DockerSecret;
import com.synopsys.integration.blackduck.installer.dockerswarm.yaml.DockerService;
import com.synopsys.integration.blackduck.installer.dockerswarm.yaml.DockerServiceEnvironment;
import com.synopsys.integration.blackduck.installer.dockerswarm.yaml.DockerServiceSecrets;
import com.synopsys.integration.blackduck.installer.dockerswarm.yaml.GlobalSecrets;
import com.synopsys.integration.blackduck.installer.dockerswarm.yaml.OverridesFile;
import com.synopsys.integration.blackduck.installer.dockerswarm.yaml.ServiceEnvironmentLine;
import com.synopsys.integration.blackduck.installer.dockerswarm.yaml.YamlBlock;
import com.synopsys.integration.blackduck.installer.dockerswarm.yaml.YamlLine;
import com.synopsys.integration.blackduck.installer.dockerswarm.yaml.output.YamlWriter;
import com.synopsys.integration.blackduck.installer.exception.BlackDuckInstallerException;
import com.synopsys.integration.blackduck.installer.hash.HashUtility;
import com.synopsys.integration.blackduck.installer.hash.PreComputedHashes;
import com.synopsys.integration.blackduck.installer.model.AlertBlackDuckInstallOptions;
import com.synopsys.integration.blackduck.installer.model.AlertDatabase;
import com.synopsys.integration.blackduck.installer.model.AlertEncryption;
import com.synopsys.integration.blackduck.installer.model.CustomCertificate;
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

    @Override
    public void edit(File installDirectory) throws BlackDuckInstallerException {
        ConfigFile configFile = createConfigFile(installDirectory);
        if (!shouldEditFile)
            return;

        try (InputStream inputStream = new FileInputStream(configFile.getOriginalCopy())) {
            List<String> lines = IOUtils.readLines(inputStream, StandardCharsets.UTF_8);
            OverridesFile yamlFileModel = createYamlFileModel(lines);
            updateValues(yamlFileModel);
            try (Writer writer = new FileWriter(configFile.getFileToEdit())) {
                YamlWriter yamlWriter = new YamlWriter(writer, lineSeparator);
                yamlFileModel.write(yamlWriter);
            }
        } catch (IOException e) {
            throw new BlackDuckInstallerException("Error editing local overrides: " + e.getMessage());
        }
    }

    private OverridesFile createYamlFileModel(List<String> lines) {
        OverridesFile overridesFile = new OverridesFile();

        boolean inServices = false;
        boolean inServiceEnvironment = false;
        boolean inServiceSecrets = false;
        boolean inGlobalSecrets = false;
        DockerService currentService = null;
        DockerSecret currentGlobalSecret = null;

        for (String line : lines) {
            boolean processingService = null != currentService;
            if (!inServices && line.startsWith("version:")) {
                overridesFile.setVersion(line.replace("#", "")
                                             .replace("version:", "")
                                             .trim());
            } else if (line.startsWith("services:")) {
                inServices = true;
            } else if (inServices && line.trim().equals("alertdb:")) {
                if (processingService) {
                    overridesFile.addService(currentService);
                }
                inServiceEnvironment = false;
                inServiceSecrets = false;
                currentService = new DockerService("alertdb");
            } else if (processingService && line.trim().contains("environment:")) {
                inServiceEnvironment = true;
                inServiceSecrets = false;
            } else if (processingService && line.trim().equals("#    secrets:")) {
                inServiceEnvironment = false;
                inServiceSecrets = true;
            } else if (inServices && line.trim().equals("#  alert:")) {
                if (processingService) {
                    overridesFile.addService(currentService);
                }
                inServiceEnvironment = false;
                inServiceSecrets = false;
                currentService = new DockerService("alert");
            } else if (inServices && line.equals("#secrets:")) {
                inGlobalSecrets = true;
                inServices = false;
                inServiceEnvironment = false;
                inServiceSecrets = false;
                overridesFile.addService(currentService);
            } else if (processingService && inServiceEnvironment) {
                currentService.addEnvironmentVariable(line);
            } else if (processingService && inServiceSecrets) {
                currentService.addSecret(line);
            } else if (inServices) {
                currentService.addCommentBeforeSection(line);
            } else if (inGlobalSecrets) {
                if (line.trim().contains("external:")) {
                    currentGlobalSecret.applyExternal(line);
                } else if (line.trim().contains("name:")) {
                    currentGlobalSecret.applyName(line, "<STACK_NAME>_");
                } else {
                    currentGlobalSecret = DockerSecret.of(stackName, line);
                    overridesFile.addDockerSecret(currentGlobalSecret);
                }
            }
        }
        return overridesFile;
    }

    private void updateValues(OverridesFile parsedFile) throws BlackDuckInstallerException {
        updateAlertDbServiceValues(parsedFile);
        updateAlertServiceValues(parsedFile);
    }

    private void updateAlertDbServiceValues(OverridesFile parsedFile) throws BlackDuckInstallerException {
        Optional<DockerService> alertDbService = parsedFile.getService("alertdb");
        if (alertDbService.isEmpty()) {
            throw new BlackDuckInstallerException("alertDb service missing from overrides file.");
        }
        DockerService alertDb = alertDbService.get();
        DockerServiceEnvironment alertDbEnvironment = alertDb.getDockerServiceEnvironment();
        DockerServiceSecrets alertDbSecrets = alertDb.getDockerServiceSecrets();
        if (alertDatabase.isExternal()) {
            alertDb.commentBlock();
        } else {
            alertDb.uncomment();
            Optional<ServiceEnvironmentLine> alertDBLine = alertDbEnvironment.getEnvironmentVariable("POSTGRES_DB");
            Optional<ServiceEnvironmentLine> alertDBUser = alertDbEnvironment.getEnvironmentVariable("POSTGRES_USER");
            Optional<ServiceEnvironmentLine> alertDBPassword = alertDbEnvironment.getEnvironmentVariable("POSTGRES_PASSWORD");

            if (alertDBLine.isPresent() && StringUtils.isNotBlank(alertDatabase.getDatabaseName())) {
                alertDBLine.get().uncomment();
                alertDBLine.get().setValue(alertDatabase.getDatabaseName());
            }

            if (alertDatabase.hasSecrets()) {
                alertDbEnvironment.getEnvironmentVariable("POSTGRES_USER").ifPresent(YamlLine::comment);
                alertDbEnvironment.getEnvironmentVariable("POSTGRES_PASSWORD").ifPresent(YamlLine::comment);
                alertDbEnvironment.getEnvironmentVariable("POSTGRES_USER_FILE").ifPresent(YamlLine::uncomment);
                alertDbEnvironment.getEnvironmentVariable("POSTGRES_PASSWORD_FILE").ifPresent(YamlLine::uncomment);
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
    }

    private void updateAlertServiceValues(OverridesFile parsedFile) throws BlackDuckInstallerException {
        Optional<DockerService> alertService = parsedFile.getService("alert");
        if (alertService.isEmpty()) {
            throw new BlackDuckInstallerException("alert service missing from overrides file.");
        }
        DockerService alert = alertService.get();
        DockerServiceEnvironment alertEnvironment = alert.getDockerServiceEnvironment();
        DockerServiceSecrets alertSecrets = alert.getDockerServiceSecrets();
        GlobalSecrets globalSecrets = parsedFile.getGlobalSecrets();
        setEnvironmentValue(alertEnvironment, "ALERT_DB_NAME", alertDatabase.getDatabaseName());

        // check if the alert service should be uncommented because there are settings.
        if (StringUtils.isNotBlank(alertAdminEmail) || StringUtils.isNotBlank(webServerHost) || !alertDatabase.isEmpty() || !alertEncryption.isEmpty() || !customCertificate.isEmpty() || !alertBlackDuckInstallOptions.isEmpty()) {
            alert.uncomment();
        }

        if (alertDatabase.hasSecrets()) {
            enableDatabaseSecrets(alertSecrets, globalSecrets);
        }

        if (alertDatabase.isExternal()) {
            alertEnvironment.getEnvironmentVariable("ALERT_DB_HOST").ifPresent(YamlLine::uncomment);
            alertEnvironment.getEnvironmentVariable("ALERT_DB_PORT").ifPresent(YamlLine::uncomment);
            setEnvironmentValue(alertEnvironment, "ALERT_DB_HOST", alertDatabase.getExternalHost());
            setEnvironmentValue(alertEnvironment, "ALERT_DB_PORT", alertDatabase.getExternalPort());
        }

        setEnvironmentValue(alertEnvironment, "ALERT_HOSTNAME", webServerHost);
        setEnvironmentValue(alertEnvironment, "ALERT_PROVIDER_BLACKDUCK_BLACKDUCK_URL", alertBlackDuckInstallOptions.getBlackDuckUrl());
        setEnvironmentValue(alertEnvironment, "ALERT_PROVIDER_BLACKDUCK_BLACKDUCK_API_KEY", alertBlackDuckInstallOptions.getBlackDuckApiToken());
        setEnvironmentValue(alertEnvironment, "ALERT_PROVIDER_BLACKDUCK_BLACKDUCK_TIMEOUT", alertBlackDuckInstallOptions.getBlackDuckTimeoutInSeconds());

        // add these environment variables to the beginning of the environment block:
        ServiceEnvironmentLine publicWebserverHost = ServiceEnvironmentLine.newEnvironmentLine("PUBLIC_HUB_WEBSERVER_HOST");
        ServiceEnvironmentLine publicWebserverPort = ServiceEnvironmentLine.newEnvironmentLine("PUBLIC_HUB_WEBSERVER_PORT");
        ServiceEnvironmentLine alertImportCert = ServiceEnvironmentLine.newEnvironmentLine("ALERT_IMPORT_CERT");
        ServiceEnvironmentLine defaultAdminEmail = ServiceEnvironmentLine.newEnvironmentLine("ALERT_COMPONENT_SETTINGS_SETTINGS_USER_DEFAULT_ADMIN_EMAIL");

        alertEnvironment.addEnvironmentVariableAtBeginning(publicWebserverHost);
        alertEnvironment.addEnvironmentVariableAtBeginning(publicWebserverPort);
        alertEnvironment.addEnvironmentVariableAtBeginning(alertImportCert);
        alertEnvironment.addEnvironmentVariableAtBeginning(defaultAdminEmail);

        setEnvironmentValue(alertEnvironment, "PUBLIC_HUB_WEBSERVER_HOST", alertBlackDuckInstallOptions.getBlackDuckHostForAutoSslImport());
        setEnvironmentValue(alertEnvironment, "PUBLIC_HUB_WEBSERVER_PORT", alertBlackDuckInstallOptions.getBlackDuckPortForAutoSslImport());
        setEnvironmentValue(alertEnvironment, "ALERT_IMPORT_CERT", StringUtils.isNotBlank(alertBlackDuckInstallOptions.getBlackDuckHostForAutoSslImport()));
        setEnvironmentValue(alertEnvironment, "ALERT_COMPONENT_SETTINGS_SETTINGS_USER_DEFAULT_ADMIN_EMAIL", alertAdminEmail);

        //secrets
        if (!alertEncryption.isEmpty()) {
            alertSecrets.getSecret("ALERT_ENCRYPTION_PASSWORD").ifPresent(YamlLine::uncomment);
            alertSecrets.getSecret("ALERT_ENCRYPTION_GLOBAL_SALT").ifPresent(YamlLine::uncomment);
            globalSecrets.uncomment();
            globalSecrets.getSecret("ALERT_ENCRYPTION_PASSWORD").ifPresent(YamlBlock::uncommentBlock);
            globalSecrets.getSecret("ALERT_ENCRYPTION_GLOBAL_SALT").ifPresent(YamlBlock::uncommentBlock);
        }

        if (!customCertificate.isEmpty()) {
            alertSecrets.getSecret("WEBSERVER_CUSTOM_CERT_FILE").ifPresent(YamlLine::uncomment);
            alertSecrets.getSecret("WEBSERVER_CUSTOM_KEY_FILE").ifPresent(YamlLine::uncomment);
            globalSecrets.uncomment();
            globalSecrets.getSecret("WEBSERVER_CUSTOM_CERT_FILE").ifPresent(YamlBlock::uncommentBlock);
            globalSecrets.getSecret("WEBSERVER_CUSTOM_KEY_FILE").ifPresent(YamlBlock::uncommentBlock);
        }
    }

    private void enableDatabaseSecrets(DockerServiceSecrets secrets, GlobalSecrets globalSecrets) {
        secrets.getSecret("ALERT_DB_USERNAME").ifPresent(YamlLine::uncomment);
        secrets.getSecret("ALERT_DB_PASSWORD").ifPresent(YamlLine::uncomment);
        globalSecrets.uncomment();
        globalSecrets.getSecret("ALERT_DB_USERNAME").ifPresent(YamlBlock::uncommentBlock);
        globalSecrets.getSecret("ALERT_DB_PASSWORD").ifPresent(YamlBlock::uncommentBlock);
    }

    private void setEnvironmentValue(DockerServiceEnvironment environmentBlock, String key, int value) {
        setEnvironmentValue(environmentBlock, key, String.valueOf(value));
    }

    private void setEnvironmentValue(DockerServiceEnvironment environmentBlock, String key, boolean value) {
        setEnvironmentValue(environmentBlock, key, String.valueOf(value));
    }

    private void setEnvironmentValue(DockerServiceEnvironment environmentBlock, String key, String value) {
        Optional<ServiceEnvironmentLine> environmentLine = environmentBlock.getEnvironmentVariable(key);
        if (environmentLine.isPresent() && StringUtils.isNotBlank(value)) {
            ServiceEnvironmentLine environmentVariable = environmentLine.get();
            environmentVariable.uncomment();
            environmentVariable.setValue(value);
        }
    }
}
