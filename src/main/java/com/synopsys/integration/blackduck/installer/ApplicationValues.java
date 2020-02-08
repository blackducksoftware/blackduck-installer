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

import java.util.List;

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

    @Value("${blackduck.install.blackduck.config.env.properties.path}")
    private String blackDuckInstallBlackDuckConfigEnvPropertiesPath;

    @Value("${blackduck.install.additional.orchestration.files}")
    private List<String> blackDuckInstallAdditionalOrchestrationFiles;

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

    @Value("${alert.install.additional.orchestration.files}")
    private List<String> alertInstallAdditionalOrchestrationFiles;

    @Value("${alert.install.use.local.overrides}")
    private boolean alertInstallUseLocalOverrides;

    public String getBaseDirectory() {
        return baseDirectory;
    }

    public boolean isInstallDryRun() {
        return installDryRun;
    }

    public int getTimeoutInSeconds() {
        return timeoutInSeconds;
    }

    public String getProxyHost() {
        return proxyHost;
    }

    public int getProxyPort() {
        return proxyPort;
    }

    public String getProxyUsername() {
        return proxyUsername;
    }

    public String getProxyPassword() {
        return proxyPassword;
    }

    public String getProxyNtlmDomain() {
        return proxyNtlmDomain;
    }

    public String getProxyNtlmWorkstation() {
        return proxyNtlmWorkstation;
    }

    public boolean isAlwaysTrust() {
        return alwaysTrust;
    }

    public boolean isKeyStoreUpdate() {
        return keyStoreUpdate;
    }

    public boolean isKeyStoreUpdateForce() {
        return keyStoreUpdateForce;
    }

    public String getKeyStoreFile() {
        return keyStoreFile;
    }

    public String getKeyStoreType() {
        return keyStoreType;
    }

    public char[] getKeyStorePassword() {
        return keyStorePassword;
    }

    public String getStackName() {
        return stackName;
    }

    public String getWebServerHost() {
        return webServerHost;
    }

    public String getCustomCertificatePath() {
        return customCertificatePath;
    }

    public String getCustomCertificateKeyPath() {
        return customCertificateKeyPath;
    }

    public DeployMethod getBlackDuckDeployMethod() {
        return blackDuckDeployMethod;
    }

    public String getBlackDuckVersion() {
        return blackDuckVersion;
    }

    public DownloadSource getBlackDuckDownloadSource() {
        return blackDuckDownloadSource;
    }

    public boolean isBlackDuckDownloadForce() {
        return blackDuckDownloadForce;
    }

    public String getBlackDuckGithubDownloadUrlPrefix() {
        return blackDuckGithubDownloadUrlPrefix;
    }

    public String getBlackDuckArtifactoryUrl() {
        return blackDuckArtifactoryUrl;
    }

    public String getBlackDuckArtifactoryRepo() {
        return blackDuckArtifactoryRepo;
    }

    public String getBlackDuckArtifactPath() {
        return blackDuckArtifactPath;
    }

    public String getBlackDuckArtifact() {
        return blackDuckArtifact;
    }

    public String getBlackDuckInstallBlackDuckConfigEnvPropertiesPath() {
        return blackDuckInstallBlackDuckConfigEnvPropertiesPath;
    }

    public List<String> getBlackDuckInstallAdditionalOrchestrationFiles() {
        return blackDuckInstallAdditionalOrchestrationFiles;
    }

    public boolean isBlackDuckInstallUseLocalOverrides() {
        return blackDuckInstallUseLocalOverrides;
    }

    public int getBlackDuckInstallTimeoutInSeconds() {
        return blackDuckInstallTimeoutInSeconds;
    }

    public String getBlackDuckUsername() {
        return blackDuckUsername;
    }

    public String getBlackDuckPassword() {
        return blackDuckPassword;
    }

    public String getBlackDuckConfigureRegistrationKey() {
        return blackDuckConfigureRegistrationKey;
    }

    public boolean isBlackDuckConfigureAcceptEula() {
        return blackDuckConfigureAcceptEula;
    }

    public boolean isBlackDuckConfigureApiToken() {
        return blackDuckConfigureApiToken;
    }

    public DeployMethod getAlertDeployMethod() {
        return alertDeployMethod;
    }

    public String getAlertVersion() {
        return alertVersion;
    }

    public DownloadSource getAlertDownloadSource() {
        return alertDownloadSource;
    }

    public boolean isAlertDownloadForce() {
        return alertDownloadForce;
    }

    public String getAlertGithubDownloadUrlPrefix() {
        return alertGithubDownloadUrlPrefix;
    }

    public String getAlertArtifactoryUrl() {
        return alertArtifactoryUrl;
    }

    public String getAlertArtifactoryRepo() {
        return alertArtifactoryRepo;
    }

    public String getAlertArtifactPath() {
        return alertArtifactPath;
    }

    public String getAlertArtifact() {
        return alertArtifact;
    }

    public String getAlertInstallPort() {
        return alertInstallPort;
    }

    public String getAlertInstallEncryptionPasswordPath() {
        return alertInstallEncryptionPasswordPath;
    }

    public String getAlertInstallEncryptionGlobalSaltPath() {
        return alertInstallEncryptionGlobalSaltPath;
    }

    public String getAlertInstallDefaultAdminEmail() {
        return alertInstallDefaultAdminEmail;
    }

    public String getAlertInstallBlackDuckUrl() {
        return alertInstallBlackDuckUrl;
    }

    public String getAlertInstallBlackDuckApiToken() {
        return alertInstallBlackDuckApiToken;
    }

    public int getAlertInstallBlackDuckTimeoutInSeconds() {
        return alertInstallBlackDuckTimeoutInSeconds;
    }

    public boolean isAlertInstallBlackDuckAutoSslImport() {
        return alertInstallBlackDuckAutoSslImport;
    }

    public String getAlertInstallBlackDuckHostForAutoSslImport() {
        return alertInstallBlackDuckHostForAutoSslImport;
    }

    public int getAlertInstallBlackDuckPortForAutoSslImport() {
        return alertInstallBlackDuckPortForAutoSslImport;
    }

    public List<String> getAlertInstallAdditionalOrchestrationFiles() {
        return alertInstallAdditionalOrchestrationFiles;
    }

    public boolean isAlertInstallUseLocalOverrides() {
        return alertInstallUseLocalOverrides;
    }

}
