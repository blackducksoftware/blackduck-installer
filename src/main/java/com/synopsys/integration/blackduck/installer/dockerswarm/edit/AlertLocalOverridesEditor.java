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
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;

import com.synopsys.integration.blackduck.installer.dockerswarm.parser.DockerSecret;
import com.synopsys.integration.blackduck.installer.dockerswarm.parser.DockerService;
import com.synopsys.integration.blackduck.installer.dockerswarm.parser.DockerServiceEnvironment;
import com.synopsys.integration.blackduck.installer.dockerswarm.parser.DockerServiceSecrets;
import com.synopsys.integration.blackduck.installer.dockerswarm.parser.OverridesFile;
import com.synopsys.integration.blackduck.installer.dockerswarm.parser.ServiceEnvironmentLine;
import com.synopsys.integration.blackduck.installer.dockerswarm.parser.YamlBlock;
import com.synopsys.integration.blackduck.installer.dockerswarm.parser.YamlLine;
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
            OverridesFile parsedFile = createOverridesFile(lines);
            updateValues(parsedFile);
            try (Writer writer = new FileWriter(configFile.getFileToEdit())) {
                AlertProcessingState processingState = createProcessingState(writer, lines);
                processAlertDBService(processingState, writer);
                processAlertService(processingState, writer);
                processSecrets(processingState, writer);
            }
        } catch (IOException e) {
            throw new BlackDuckInstallerException("Error editing local overrides: " + e.getMessage());
        }

    }

    private OverridesFile createOverridesFile(List<String> lines) {
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
        Optional<DockerService> alertService = parsedFile.getService("alert");

        updateAlertDbServiceValues(parsedFile);

        if (alertService.isPresent()) {

        }
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
            Optional<ServiceEnvironmentLine> alertDBLine = alertDbEnvironment.getEnvironmentVariable("POSTGRES_DB");
            Optional<ServiceEnvironmentLine> alertDBUser = alertDbEnvironment.getEnvironmentVariable("POSTGRES_USER");
            Optional<ServiceEnvironmentLine> alertDBPassword = alertDbEnvironment.getEnvironmentVariable("POSTGRES_PASSWORD");

            if (alertDBLine.isPresent()) {
                alertDBLine.get().setValue(alertDatabase.getDatabaseName());
            }

            if (alertDatabase.hasSecrets()) {
                alertDbEnvironment.getEnvironmentVariable("POSTGRES_USER").ifPresent(YamlLine::comment);
                alertDbEnvironment.getEnvironmentVariable("POSTGRES_PASSWORD").ifPresent(YamlLine::comment);
                alertDbEnvironment.getEnvironmentVariable("POSTGRES_USER_FILE").ifPresent(YamlLine::uncomment);
                alertDbEnvironment.getEnvironmentVariable("POSTGRES_PASSWORD_FILE").ifPresent(YamlLine::uncomment);
                alertDbSecrets.getSecret("ALERT_DB_USERNAME").ifPresent(YamlLine::uncomment);
                alertDbSecrets.getSecret("ALERT_DB_PASSWORD").ifPresent(YamlLine::uncomment);
                parsedFile.getGlobalSecrets().uncomment();
                parsedFile.getGlobalSecrets().getSecret("ALERT_DB_USERNAME").ifPresent(YamlBlock::uncommentBlock);
                parsedFile.getGlobalSecrets().getSecret("ALERT_DB_PASSWORD").ifPresent(YamlBlock::uncommentBlock);

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

    private void updateAlertServiceValues() {

    }

    private AlertProcessingState createProcessingState(Writer writer, List<String> lines) throws IOException {
        AlertProcessingState processingState = new AlertProcessingState();

        for (String line : lines) {
            if (line.startsWith("services:")) {
                processingState.setInServices(true);
                writeLine(writer, line);
            } else if (processingState.isInServices() && line.trim().equals("alertdb:")) {
                processingState.setInAlertDb(true);
                processingState.addLineToAlertDBService(line);
            } else if (processingState.isInAlertDb() && line.trim().equals("#    secrets:")) {
                processingState.setInAlertDbSecrets(true);
                processingState.addLineToAlertDBService(line);
            } else if (processingState.isInServices() && line.trim().equals("#  alert:")) {
                processingState.setInAlert(true);
                processingState.clearAlertDbServiceState();
                processingState.addLineToAlertService(line);
            } else if (processingState.isInAlert() && line.trim().equals("#    secrets:")) {
                processingState.setInAlertSecrets(true);
                processingState.addLineToAlertService(line);
            } else if (processingState.isInAlertSecrets() && line.equals("#secrets:")) {
                processingState.setInSecrets(true);
                processingState.clearAlertDbServiceState();
                processingState.clearAlertServiceState();
                processingState.setInServices(false);
                processingState.addLineToSecrets(line);
            } else if (processingState.isInAlertDb()) {
                processingState.addLineToAlertDBService(line);
            } else if (processingState.isInAlert()) {
                processingState.addLineToAlertService(line);
            } else if (processingState.isInSecrets()) {
                processingState.addLineToSecrets(line);
            } else {
                writeLine(writer, line);
            }
        }

        return processingState;
    }

    private void processAlertDBService(AlertProcessingState processingState, Writer writer) throws IOException {
        //TODO: A future version of Alert may not have the alertdb service uncommented by default. This if can be removed when tthe service is commented out by default.
        if (alertDatabase.isExternal()) {
            for (String line : processingState.getAlertDbLines()) {
                if (!line.trim().startsWith("#")) {
                    commentLine(writer, line);
                } else {
                    writeLine(writer, line);
                }
            }
        } else {
            boolean hasSecrets = alertDatabase.hasSecrets();
            boolean inEnvironment = false;
            boolean inSecrets = false;
            for (String line : processingState.getAlertDbLines()) {
                if (line.trim().equals("environment:")) {
                    inEnvironment = true;
                    uncommentLine(writer, line);
                } else if (inEnvironment && line.trim().contains("POSTGRES_DB")) {
                    uncommentEnvironmentLine(writer, line, alertDatabase.getDatabaseName());
                } else if (inEnvironment && line.trim().equals("#    secrets:")) {
                    inEnvironment = false;
                    inSecrets = true;
                    uncommentLine(writer, line);
                } else if (hasSecrets && inEnvironment && (line.contains("POSTGRES_USER_FILE") || line.contains("POSTGRES_PASSWORD_FILE"))) {
                    uncommentLine(writer, line);
                } else if (hasSecrets && inEnvironment && (line.contains("POSTGRES_USER") || line.contains("POSTGRES_PASSWORD"))) {
                    commentLine(writer, line);
                } else if (hasSecrets && inSecrets && (line.contains("ALERT_DB_USERNAME") || line.contains("ALERT_DB_PASSWORD"))) {
                    uncommentLine(writer, line);
                } else {
                    writeLine(writer, line);
                }
            }
        }
    }

    private void processAlertService(AlertProcessingState processingState, Writer writer) throws IOException {
        boolean hasSecrets = alertDatabase.hasSecrets() || !alertEncryption.isEmpty() || !customCertificate.isEmpty();
        boolean inEnvironment = false;
        boolean inSecrets = false;
        for (String line : processingState.getAlertLines()) {
            if (line.trim().equals("#  alert:")) {
                uncommentLine(writer, line);
            } else if (line.trim().equals("#    environment:")) {
                inEnvironment = true;
                uncommentLine(writer, line);
            } else if (inEnvironment && line.trim().contains("ALERT_HOSTNAME")) {
                uncommentEnvironmentLine(writer, line, webServerHost);
            } else if (inEnvironment && alertDatabase.isExternal() && line.trim().contains("ALERT_DB_HOST")) {
                uncommentEnvironmentLine(writer, line, alertDatabase.getExternalHost());
            } else if (inEnvironment && alertDatabase.isExternal() && line.trim().contains("ALERT_DB_PORT")) {
                uncommentEnvironmentLine(writer, line, String.valueOf(alertDatabase.getExternalPort()));
            } else if (inEnvironment && line.trim().contains("ALERT_DB_NAME")) {
                uncommentEnvironmentLine(writer, line, alertDatabase.getDatabaseName());
            } else if (inEnvironment && StringUtils.isNotBlank(alertBlackDuckInstallOptions.getBlackDuckUrl()) && line.trim().contains("ALERT_PROVIDER_BLACKDUCK_BLACKDUCK_URL")) {
                uncommentEnvironmentLine(writer, line, alertBlackDuckInstallOptions.getBlackDuckUrl());
            } else if (inEnvironment && StringUtils.isNotBlank(alertBlackDuckInstallOptions.getBlackDuckApiToken()) && line.trim().contains("ALERT_PROVIDER_BLACKDUCK_BLACKDUCK_API_KEY")) {
                uncommentEnvironmentLine(writer, line, alertBlackDuckInstallOptions.getBlackDuckApiToken());
            } else if (inEnvironment && alertBlackDuckInstallOptions.getBlackDuckTimeoutInSeconds() > 0 && line.trim().contains("ALERT_PROVIDER_BLACKDUCK_BLACKDUCK_TIMEOUT")) {
                uncommentEnvironmentLine(writer, line, String.valueOf(alertBlackDuckInstallOptions.getBlackDuckTimeoutInSeconds()));
            } else if (inEnvironment && line.trim().contains("# Component Settings")) {
                writeLine(writer, line);
                addAlertAdminEmailEnvironmentVariables(writer);
            } else if (inEnvironment && line.trim().equals("#    secrets:")) {
                inEnvironment = false;
                inSecrets = true;
                if (hasSecrets) {
                    uncommentLine(writer, line);
                } else {
                    writeLine(writer, line);
                }
            } else if (inSecrets && !alertEncryption.isEmpty() && (line.contains("ALERT_ENCRYPTION_PASSWORD") || line.contains("ALERT_ENCRYPTION_GLOBAL_SALT"))) {
                uncommentLine(writer, line);
            } else if (inSecrets && !customCertificate.isEmpty() && (line.contains("WEBSERVER_CUSTOM_CERT_FILE") || line.contains("WEBSERVER_CUSTOM_KEY_FILE"))) {
                uncommentLine(writer, line);
            } else if (inSecrets && alertDatabase.hasSecrets() && (line.contains("ALERT_DB_USERNAME") || line.contains("ALERT_DB_PASSWORD"))) {
                uncommentLine(writer, line);
            } else {
                writeLine(writer, line);
            }
        }
    }

    private void addAlertAdminEmailEnvironmentVariables(Writer writer) throws IOException {
        StringBuilder yamlBuilder = new StringBuilder(100);
        if (StringUtils.isNotBlank(alertAdminEmail)) {
            addEnvironmentVariable(yamlBuilder, "ALERT_COMPONENT_SETTINGS_SETTINGS_USER_DEFAULT_ADMIN_EMAIL", alertAdminEmail);
        }
        writeLine(writer, yamlBuilder.toString());
    }

    private void processSecrets(AlertProcessingState processingState, Writer writer) throws IOException {
        boolean hasEncryptionSecrets = !alertEncryption.isEmpty();
        boolean hasCertificateSecrets = !customCertificate.isEmpty();
        boolean isEncryptionSecret = false;
        boolean isCertificateSecret = false;
        boolean isDatabaseSecret = false;
        for (String line : processingState.getSecretsLines()) {
            if (line.trim().equals("#secrets:")) {
                uncommentLine(writer, line);
            } else if (hasEncryptionSecrets && (line.trim().contains("ALERT_ENCRYPTION_PASSWORD:") || line.trim().contains("ALERT_ENCRYPTION_GLOBAL_SALT:"))) {
                isEncryptionSecret = true;
                uncommentLine(writer, line);
            } else if (hasCertificateSecrets && (line.trim().contains("WEBSERVER_CUSTOM_CERT_FILE:") || line.trim().contains("WEBSERVER_CUSTOM_KEY_FILE:"))) {
                isCertificateSecret = true;
                uncommentLine(writer, line);
            } else if (alertDatabase.hasSecrets() && (line.trim().contains("ALERT_DB_USERNAME:") || line.trim().contains("ALERT_DB_PASSWORD:"))) {
                isDatabaseSecret = true;
                uncommentLine(writer, line);
            } else if (isEncryptionSecret && line.trim().contains("name:")) {
                isEncryptionSecret = false;
                uncommentSecretValue(writer, line);
            } else if (isCertificateSecret && line.trim().contains("name:")) {
                isCertificateSecret = false;
                uncommentSecretValue(writer, line);
            } else if (isDatabaseSecret && line.trim().contains("name:")) {
                isCertificateSecret = false;
                uncommentSecretValue(writer, line);
            } else if (isEncryptionSecret && hasEncryptionSecrets) {
                uncommentLine(writer, line);
            } else if (isCertificateSecret && hasCertificateSecrets) {
                uncommentLine(writer, line);
            } else if (isDatabaseSecret && alertDatabase.hasSecrets()) {
                uncommentLine(writer, line);
            } else {
                writeLine(writer, line);
            }
        }
    }

    private void uncommentSecretValue(Writer writer, String line) throws IOException {
        String fixedLine = line.replace("<STACK_NAME>_", stackName + "_");
        uncommentLine(writer, fixedLine);
    }

    //TODO create a common utility for this.
    private void writeLine(Writer writer, String line) throws IOException {
        writer.append(line + lineSeparator);
    }

    private void uncommentLine(Writer writer, String line) throws IOException {
        writer.append(line.replace("#", "") + lineSeparator);
    }

    private void uncommentEnvironmentLine(Writer writer, String line, String value) throws IOException {
        int equalsIndex = line.indexOf("=");
        writer.append(line.substring(0, equalsIndex + 1).replace("#", "") + value + lineSeparator);
    }

    private void commentLine(Writer writer, String line) throws IOException {
        writer.append("#" + line + lineSeparator);
    }

    private void addEnvironmentVariable(StringBuilder ymlBuilder, String key, String value) {
        if (StringUtils.isNotBlank(value)) {
            append(ymlBuilder, key, value);
        }
    }

    private void append(StringBuilder ymlBuilder, String key, String value) {
        ymlBuilder.append(String.format("      - %s=%s\n", key, value));
    }

    private class AlertProcessingState {
        private final List<String> alertDbLines = new LinkedList<>();
        private final List<String> alertLines = new LinkedList<>();
        private final List<String> secretsLines = new LinkedList<>();

        private boolean inServices = false;
        private boolean inAlert = false;
        private boolean inAlertDb = false;
        private boolean inAlertDbEnvironment = false;
        private boolean inAlertDbSecrets = false;
        private boolean inAlertEnvironment = false;
        private boolean inAlertSecrets = false;
        private boolean inSecrets = false;

        public AlertProcessingState() {
        }

        public void clearAlertDbServiceState() {
            setInAlertDb(false);
            setInAlertDbSecrets(false);
            setInAlertDbEnvironment(false);
        }

        public void clearAlertServiceState() {
            setInAlert(false);
            setInAlertSecrets(false);
            setInAlertEnvironment(false);
        }

        public void clearSecretsState() {
            setInSecrets(false);
        }

        public List<String> getAlertDbLines() {
            return alertDbLines;
        }

        public void addLineToAlertDBService(String line) {
            alertDbLines.add(line);
        }

        public List<String> getAlertLines() {
            return alertLines;
        }

        public void addLineToAlertService(String line) {
            alertLines.add(line);
        }

        public List<String> getSecretsLines() {
            return secretsLines;
        }

        public void addLineToSecrets(String line) {
            secretsLines.add(line);
        }

        public boolean isInServices() {
            return inServices;
        }

        public void setInServices(final boolean inServices) {
            this.inServices = inServices;
        }

        public boolean isInAlert() {
            return inAlert;
        }

        public void setInAlert(final boolean inAlert) {
            this.inAlert = inAlert;
        }

        public boolean isInAlertDb() {
            return inAlertDb;
        }

        public void setInAlertDb(final boolean inAlertDb) {
            this.inAlertDb = inAlertDb;
        }

        public boolean isInAlertDbEnvironment() {
            return inAlertDbEnvironment;
        }

        public void setInAlertDbEnvironment(final boolean inAlertDbEnvironment) {
            this.inAlertDbEnvironment = inAlertDbEnvironment;
        }

        public boolean isInAlertDbSecrets() {
            return inAlertDbSecrets;
        }

        public void setInAlertDbSecrets(final boolean inAlertDbSecrets) {
            this.inAlertDbSecrets = inAlertDbSecrets;
        }

        public boolean isInAlertEnvironment() {
            return inAlertEnvironment;
        }

        public void setInAlertEnvironment(final boolean inAlertEnvironment) {
            this.inAlertEnvironment = inAlertEnvironment;
        }

        public boolean isInAlertSecrets() {
            return inAlertSecrets;
        }

        public void setInAlertSecrets(final boolean inAlertSecrets) {
            this.inAlertSecrets = inAlertSecrets;
        }

        public boolean isInSecrets() {
            return inSecrets;
        }

        public void setInSecrets(final boolean inSecrets) {
            this.inSecrets = inSecrets;
        }
    }

}
