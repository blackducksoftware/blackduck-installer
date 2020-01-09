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
package com.synopsys.integration.blackduck.installer;

import com.synopsys.integration.blackduck.installer.download.DownloadSource;
import com.synopsys.integration.blackduck.installer.model.DeployMethod;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class ApplicationValues {
    @Value("${base.directory}")
    private String baseDirectory;

    @Value("${install.dry.run}")
    private boolean installDryRun;

    @Value("${timeout.in.seconds}")
    private int timeoutInSeconds;

    @Value("${proxy.host}")
    private String proxyHost;

    @Value("${proxy.port}")
    private int proxyPort;

    @Value("${proxy.username}")
    private String proxyUsername;

    @Value("${proxy.password}")
    private String proxyPassword;

    @Value("${proxy.ntlm.domain}")
    private String proxyNtlmDomain;

    @Value("${proxy.ntlm.workstation}")
    private String proxyNtlmWorkstation;

    @Value("${always.trust}")
    private boolean alwaysTrust;

    @Value("${keystore.update}")
    private boolean keyStoreUpdate;

    @Value("${keystore.update.force}")
    private boolean keyStoreUpdateForce;

    @Value("${keystore.file}")
    private String keyStoreFile;

    @Value("${keystore.type}")
    private String keyStoreType;

    @Value("${keystore.password}")
    private char[] keyStorePassword;

    @Value("${stack.name}")
    private String stackName;

    @Value("${web.server.host}")
    private String webServerHost;

    @Value("${custom.certificate.path}")
    private String customCertificatePath;

    @Value("${custom.certificate.key.path}")
    private String customCertificateKeyPath;

    @Value("${blackduck.deploy.method}")
    private DeployMethod blackDuckDeployMethod;

    @Value("${blackduck.version}")
    private String blackDuckVersion;

    @Value("${blackduck.download.source}")
    private DownloadSource blackDuckDownloadSource;

    @Value("${blackduck.download.force}")
    private boolean blackDuckDownloadForce;

    @Value("${blackduck.github.download.url.prefix}")
    private String blackDuckGithubDownloadUrlPrefix;

    @Value("${blackduck.artifactory.url}")
    private String blackDuckArtifactoryUrl;

    @Value("${blackduck.artifactory.repo}")
    private String blackDuckArtifactoryRepo;

    @Value("${blackduck.artifact.path}")
    private String blackDuckArtifactPath;

    @Value("${blackduck.artifact}")
    private String blackDuckArtifact;

    @Value("${blackduck.install.proxy.host}")
    private String blackDuckInstallProxyHost;

    @Value("${blackduck.install.proxy.port}")
    private int blackDuckInstallProxyPort;

    @Value("${blackduck.install.proxy.scheme}")
    private String blackDuckInstallProxyScheme;

    @Value("${blackduck.install.proxy.user}")
    private String blackDuckInstallProxyUser;

    @Value("${blackduck.install.custom.kb.host}")
    private String blackDuckInstallCustomKbHost;

    @Value("${blackduck.install.use.local.overrides}")
    private boolean blackDuckInstallUseLocalOverrides;

    @Value("${blackduck.install.timeout.in.seconds}")
    private int blackDuckInstallTimeoutInSeconds;

    @Value("${blackduck.username}")
    private String blackDuckUsername;

    @Value("${blackduck.password}")
    private String blackDuckPassword;

    @Value("${blackduck.configure.registration.key}")
    private String blackDuckConfigureRegistrationKey;

    @Value("${blackduck.configure.accept.eula}")
    private boolean blackDuckConfigureAcceptEula;

    @Value("${blackduck.configure.api.token}")
    private boolean blackDuckConfigureApiToken;

    @Value("${alert.deploy.method}")
    private DeployMethod alertDeployMethod;

    @Value("${alert.version}")
    private String alertVersion;

    @Value("${alert.download.source}")
    private DownloadSource alertDownloadSource;

    @Value("${alert.download.force}")
    private boolean alertDownloadForce;

    @Value("${alert.github.download.url.prefix}")
    private String alertGithubDownloadUrlPrefix;

    @Value("${alert.artifactory.url}")
    private String alertArtifactoryUrl;

    @Value("${alert.artifactory.repo}")
    private String alertArtifactoryRepo;

    @Value("${alert.artifact.path}")
    private String alertArtifactPath;

    @Value("${alert.artifact}")
    private String alertArtifact;

    @Value("${alert.install.port}")
    private String alertInstallPort;

    @Value("${alert.install.encryption.password.path}")
    private String alertInstallEncryptionPasswordPath;

    @Value("${alert.install.encryption.global.salt.path}")
    private String alertInstallEncryptionGlobalSaltPath;

    @Value("${alert.install.default.admin.email}")
    private String alertInstallDefaultAdminEmail;

    @Value("${alert.install.blackduck.url}")
    private String alertInstallBlackDuckUrl;

    @Value("${alert.install.blackduck.api.token}")
    private String alertInstallBlackDuckApiToken;

    @Value("${alert.install.blackduck.timeout.in.seconds}")
    private int alertInstallBlackDuckTimeoutInSeconds;

    @Value("${alert.install.blackduck.auto.ssl.import}")
    private boolean alertInstallBlackDuckAutoSslImport;

    @Value("${alert.install.blackduck.host.for.auto.ssl.import}")
    private String alertInstallBlackDuckHostForAutoSslImport;

    @Value("${alert.install.blackduck.port.for.auto.ssl.import}")
    private int alertInstallBlackDuckPortForAutoSslImport;

    @Value("${alert.install.use.local.overrides}")
    private boolean alertInstallUseLocalOverrides;

    public String getBaseDirectory() {
        return baseDirectory;
    }

    public void setBaseDirectory(String baseDirectory) {
        this.baseDirectory = baseDirectory;
    }

    public boolean isInstallDryRun() {
        return installDryRun;
    }

    public void setInstallDryRun(boolean installDryRun) {
        this.installDryRun = installDryRun;
    }

    public int getTimeoutInSeconds() {
        return timeoutInSeconds;
    }

    public void setTimeoutInSeconds(int timeoutInSeconds) {
        this.timeoutInSeconds = timeoutInSeconds;
    }

    public String getProxyHost() {
        return proxyHost;
    }

    public void setProxyHost(String proxyHost) {
        this.proxyHost = proxyHost;
    }

    public int getProxyPort() {
        return proxyPort;
    }

    public void setProxyPort(int proxyPort) {
        this.proxyPort = proxyPort;
    }

    public String getProxyUsername() {
        return proxyUsername;
    }

    public void setProxyUsername(String proxyUsername) {
        this.proxyUsername = proxyUsername;
    }

    public String getProxyPassword() {
        return proxyPassword;
    }

    public void setProxyPassword(String proxyPassword) {
        this.proxyPassword = proxyPassword;
    }

    public String getProxyNtlmDomain() {
        return proxyNtlmDomain;
    }

    public void setProxyNtlmDomain(String proxyNtlmDomain) {
        this.proxyNtlmDomain = proxyNtlmDomain;
    }

    public String getProxyNtlmWorkstation() {
        return proxyNtlmWorkstation;
    }

    public void setProxyNtlmWorkstation(String proxyNtlmWorkstation) {
        this.proxyNtlmWorkstation = proxyNtlmWorkstation;
    }

    public boolean isAlwaysTrust() {
        return alwaysTrust;
    }

    public void setAlwaysTrust(boolean alwaysTrust) {
        this.alwaysTrust = alwaysTrust;
    }

    public boolean isKeyStoreUpdate() {
        return keyStoreUpdate;
    }

    public void setKeyStoreUpdate(boolean keyStoreUpdate) {
        this.keyStoreUpdate = keyStoreUpdate;
    }

    public boolean isKeyStoreUpdateForce() {
        return keyStoreUpdateForce;
    }

    public void setKeyStoreUpdateForce(boolean keyStoreUpdateForce) {
        this.keyStoreUpdateForce = keyStoreUpdateForce;
    }

    public String getKeyStoreFile() {
        return keyStoreFile;
    }

    public void setKeyStoreFile(String keyStoreFile) {
        this.keyStoreFile = keyStoreFile;
    }

    public String getKeyStoreType() {
        return keyStoreType;
    }

    public void setKeyStoreType(String keyStoreType) {
        this.keyStoreType = keyStoreType;
    }

    public char[] getKeyStorePassword() {
        return keyStorePassword;
    }

    public void setKeyStorePassword(char[] keyStorePassword) {
        this.keyStorePassword = keyStorePassword;
    }

    public String getStackName() {
        return stackName;
    }

    public void setStackName(String stackName) {
        this.stackName = stackName;
    }

    public String getWebServerHost() {
        return webServerHost;
    }

    public void setWebServerHost(String webServerHost) {
        this.webServerHost = webServerHost;
    }

    public String getCustomCertificatePath() {
        return customCertificatePath;
    }

    public void setCustomCertificatePath(String customCertificatePath) {
        this.customCertificatePath = customCertificatePath;
    }

    public String getCustomCertificateKeyPath() {
        return customCertificateKeyPath;
    }

    public void setCustomCertificateKeyPath(String customCertificateKeyPath) {
        this.customCertificateKeyPath = customCertificateKeyPath;
    }

    public DeployMethod getBlackDuckDeployMethod() {
        return blackDuckDeployMethod;
    }

    public void setBlackDuckDeployMethod(DeployMethod blackDuckDeployMethod) {
        this.blackDuckDeployMethod = blackDuckDeployMethod;
    }

    public String getBlackDuckVersion() {
        return blackDuckVersion;
    }

    public void setBlackDuckVersion(String blackDuckVersion) {
        this.blackDuckVersion = blackDuckVersion;
    }

    public DownloadSource getBlackDuckDownloadSource() {
        return blackDuckDownloadSource;
    }

    public void setBlackDuckDownloadSource(DownloadSource blackDuckDownloadSource) {
        this.blackDuckDownloadSource = blackDuckDownloadSource;
    }

    public boolean isBlackDuckDownloadForce() {
        return blackDuckDownloadForce;
    }

    public void setBlackDuckDownloadForce(boolean blackDuckDownloadForce) {
        this.blackDuckDownloadForce = blackDuckDownloadForce;
    }

    public String getBlackDuckGithubDownloadUrlPrefix() {
        return blackDuckGithubDownloadUrlPrefix;
    }

    public void setBlackDuckGithubDownloadUrlPrefix(String blackDuckGithubDownloadUrlPrefix) {
        this.blackDuckGithubDownloadUrlPrefix = blackDuckGithubDownloadUrlPrefix;
    }

    public String getBlackDuckArtifactoryUrl() {
        return blackDuckArtifactoryUrl;
    }

    public void setBlackDuckArtifactoryUrl(String blackDuckArtifactoryUrl) {
        this.blackDuckArtifactoryUrl = blackDuckArtifactoryUrl;
    }

    public String getBlackDuckArtifactoryRepo() {
        return blackDuckArtifactoryRepo;
    }

    public void setBlackDuckArtifactoryRepo(String blackDuckArtifactoryRepo) {
        this.blackDuckArtifactoryRepo = blackDuckArtifactoryRepo;
    }

    public String getBlackDuckArtifactPath() {
        return blackDuckArtifactPath;
    }

    public void setBlackDuckArtifactPath(String blackDuckArtifactPath) {
        this.blackDuckArtifactPath = blackDuckArtifactPath;
    }

    public String getBlackDuckArtifact() {
        return blackDuckArtifact;
    }

    public void setBlackDuckArtifact(String blackDuckArtifact) {
        this.blackDuckArtifact = blackDuckArtifact;
    }

    public String getBlackDuckInstallProxyHost() {
        return blackDuckInstallProxyHost;
    }

    public void setBlackDuckInstallProxyHost(String blackDuckInstallProxyHost) {
        this.blackDuckInstallProxyHost = blackDuckInstallProxyHost;
    }

    public int getBlackDuckInstallProxyPort() {
        return blackDuckInstallProxyPort;
    }

    public void setBlackDuckInstallProxyPort(int blackDuckInstallProxyPort) {
        this.blackDuckInstallProxyPort = blackDuckInstallProxyPort;
    }

    public String getBlackDuckInstallProxyScheme() {
        return blackDuckInstallProxyScheme;
    }

    public void setBlackDuckInstallProxyScheme(String blackDuckInstallProxyScheme) {
        this.blackDuckInstallProxyScheme = blackDuckInstallProxyScheme;
    }

    public String getBlackDuckInstallProxyUser() {
        return blackDuckInstallProxyUser;
    }

    public void setBlackDuckInstallProxyUser(String blackDuckInstallProxyUser) {
        this.blackDuckInstallProxyUser = blackDuckInstallProxyUser;
    }

    public String getBlackDuckInstallCustomKbHost() {
        return blackDuckInstallCustomKbHost;
    }

    public void setBlackDuckInstallCustomKbHost(String blackDuckInstallCustomKbHost) {
        this.blackDuckInstallCustomKbHost = blackDuckInstallCustomKbHost;
    }

    public boolean isBlackDuckInstallUseLocalOverrides() {
        return blackDuckInstallUseLocalOverrides;
    }

    public void setBlackDuckInstallUseLocalOverrides(boolean blackDuckInstallUseLocalOverrides) {
        this.blackDuckInstallUseLocalOverrides = blackDuckInstallUseLocalOverrides;
    }

    public int getBlackDuckInstallTimeoutInSeconds() {
        return blackDuckInstallTimeoutInSeconds;
    }

    public void setBlackDuckInstallTimeoutInSeconds(int blackDuckInstallTimeoutInSeconds) {
        this.blackDuckInstallTimeoutInSeconds = blackDuckInstallTimeoutInSeconds;
    }

    public String getBlackDuckUsername() {
        return blackDuckUsername;
    }

    public void setBlackDuckUsername(String blackDuckUsername) {
        this.blackDuckUsername = blackDuckUsername;
    }

    public String getBlackDuckPassword() {
        return blackDuckPassword;
    }

    public void setBlackDuckPassword(String blackDuckPassword) {
        this.blackDuckPassword = blackDuckPassword;
    }

    public String getBlackDuckConfigureRegistrationKey() {
        return blackDuckConfigureRegistrationKey;
    }

    public void setBlackDuckConfigureRegistrationKey(String blackDuckConfigureRegistrationKey) {
        this.blackDuckConfigureRegistrationKey = blackDuckConfigureRegistrationKey;
    }

    public boolean isBlackDuckConfigureAcceptEula() {
        return blackDuckConfigureAcceptEula;
    }

    public void setBlackDuckConfigureAcceptEula(boolean blackDuckConfigureAcceptEula) {
        this.blackDuckConfigureAcceptEula = blackDuckConfigureAcceptEula;
    }

    public boolean isBlackDuckConfigureApiToken() {
        return blackDuckConfigureApiToken;
    }

    public void setBlackDuckConfigureApiToken(boolean blackDuckConfigureApiToken) {
        this.blackDuckConfigureApiToken = blackDuckConfigureApiToken;
    }

    public DeployMethod getAlertDeployMethod() {
        return alertDeployMethod;
    }

    public void setAlertDeployMethod(DeployMethod alertDeployMethod) {
        this.alertDeployMethod = alertDeployMethod;
    }

    public String getAlertVersion() {
        return alertVersion;
    }

    public void setAlertVersion(String alertVersion) {
        this.alertVersion = alertVersion;
    }

    public DownloadSource getAlertDownloadSource() {
        return alertDownloadSource;
    }

    public void setAlertDownloadSource(DownloadSource alertDownloadSource) {
        this.alertDownloadSource = alertDownloadSource;
    }

    public boolean isAlertDownloadForce() {
        return alertDownloadForce;
    }

    public void setAlertDownloadForce(boolean alertDownloadForce) {
        this.alertDownloadForce = alertDownloadForce;
    }

    public String getAlertGithubDownloadUrlPrefix() {
        return alertGithubDownloadUrlPrefix;
    }

    public void setAlertGithubDownloadUrlPrefix(String alertGithubDownloadUrlPrefix) {
        this.alertGithubDownloadUrlPrefix = alertGithubDownloadUrlPrefix;
    }

    public String getAlertArtifactoryUrl() {
        return alertArtifactoryUrl;
    }

    public void setAlertArtifactoryUrl(String alertArtifactoryUrl) {
        this.alertArtifactoryUrl = alertArtifactoryUrl;
    }

    public String getAlertArtifactoryRepo() {
        return alertArtifactoryRepo;
    }

    public void setAlertArtifactoryRepo(String alertArtifactoryRepo) {
        this.alertArtifactoryRepo = alertArtifactoryRepo;
    }

    public String getAlertArtifactPath() {
        return alertArtifactPath;
    }

    public void setAlertArtifactPath(String alertArtifactPath) {
        this.alertArtifactPath = alertArtifactPath;
    }

    public String getAlertArtifact() {
        return alertArtifact;
    }

    public void setAlertArtifact(String alertArtifact) {
        this.alertArtifact = alertArtifact;
    }

    public String getAlertInstallPort() {
        return alertInstallPort;
    }

    public void setAlertInstallPort(String alertInstallPort) {
        this.alertInstallPort = alertInstallPort;
    }

    public String getAlertInstallEncryptionPasswordPath() {
        return alertInstallEncryptionPasswordPath;
    }

    public void setAlertInstallEncryptionPasswordPath(String alertInstallEncryptionPasswordPath) {
        this.alertInstallEncryptionPasswordPath = alertInstallEncryptionPasswordPath;
    }

    public String getAlertInstallEncryptionGlobalSaltPath() {
        return alertInstallEncryptionGlobalSaltPath;
    }

    public void setAlertInstallEncryptionGlobalSaltPath(String alertInstallEncryptionGlobalSaltPath) {
        this.alertInstallEncryptionGlobalSaltPath = alertInstallEncryptionGlobalSaltPath;
    }

    public String getAlertInstallDefaultAdminEmail() {
        return alertInstallDefaultAdminEmail;
    }

    public void setAlertInstallDefaultAdminEmail(String alertInstallDefaultAdminEmail) {
        this.alertInstallDefaultAdminEmail = alertInstallDefaultAdminEmail;
    }

    public String getAlertInstallBlackDuckUrl() {
        return alertInstallBlackDuckUrl;
    }

    public void setAlertInstallBlackDuckUrl(String alertInstallBlackDuckUrl) {
        this.alertInstallBlackDuckUrl = alertInstallBlackDuckUrl;
    }

    public String getAlertInstallBlackDuckApiToken() {
        return alertInstallBlackDuckApiToken;
    }

    public void setAlertInstallBlackDuckApiToken(String alertInstallBlackDuckApiToken) {
        this.alertInstallBlackDuckApiToken = alertInstallBlackDuckApiToken;
    }

    public int getAlertInstallBlackDuckTimeoutInSeconds() {
        return alertInstallBlackDuckTimeoutInSeconds;
    }

    public void setAlertInstallBlackDuckTimeoutInSeconds(int alertInstallBlackDuckTimeoutInSeconds) {
        this.alertInstallBlackDuckTimeoutInSeconds = alertInstallBlackDuckTimeoutInSeconds;
    }

    public boolean isAlertInstallBlackDuckAutoSslImport() {
        return alertInstallBlackDuckAutoSslImport;
    }

    public void setAlertInstallBlackDuckAutoSslImport(boolean alertInstallBlackDuckAutoSslImport) {
        this.alertInstallBlackDuckAutoSslImport = alertInstallBlackDuckAutoSslImport;
    }

    public String getAlertInstallBlackDuckHostForAutoSslImport() {
        return alertInstallBlackDuckHostForAutoSslImport;
    }

    public void setAlertInstallBlackDuckHostForAutoSslImport(String alertInstallBlackDuckHostForAutoSslImport) {
        this.alertInstallBlackDuckHostForAutoSslImport = alertInstallBlackDuckHostForAutoSslImport;
    }

    public int getAlertInstallBlackDuckPortForAutoSslImport() {
        return alertInstallBlackDuckPortForAutoSslImport;
    }

    public void setAlertInstallBlackDuckPortForAutoSslImport(int alertInstallBlackDuckPortForAutoSslImport) {
        this.alertInstallBlackDuckPortForAutoSslImport = alertInstallBlackDuckPortForAutoSslImport;
    }

    public boolean isAlertInstallUseLocalOverrides() {
        return alertInstallUseLocalOverrides;
    }

    public void setAlertInstallUseLocalOverrides(boolean alertInstallUseLocalOverrides) {
        this.alertInstallUseLocalOverrides = alertInstallUseLocalOverrides;
    }

}
