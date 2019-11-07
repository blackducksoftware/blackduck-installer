/**
 * blackduck-installer
 *
 * Copyright (c) 2019 Synopsys, Inc.
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
import com.synopsys.integration.blackduck.installer.model.InstallMethod;
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

    @Value("${blackduck.stack.name}")
    private String blackDuckStackName;

    @Value("${blackduck.install.method}")
    private InstallMethod blackDuckInstallMethod;

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

    @Value("${blackduck.install.web.server.host}")
    private String blackDuckInstallWebServerHost;

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

    @Value("${blackduck.install.custom.key.path}")
    private String blackDuckInstallCustomKeyPath;

    @Value("${blackduck.install.custom.cert.path}")
    private String blackDuckInstallCustomCertPath;

    @Value("${blackduck.install.use.local.overrides}")
    private boolean blackDuckInstallUseLocalOverrides;

    @Value("${blackduck.install.timeout.in.seconds}")
    private int blackDuckInstallTimeoutInSeconds;

    @Value("${blackduck.username}")
    private String blackDuckUsername;

    @Value("${blackduck.password}")
    private String blackDuckPassword;

    @Value("${blackduck.api.token}")
    private String blackDuckApiToken;

    @Value("${blackduck.configure.registration.key}")
    private String blackDuckConfigureRegistrationKey;

    @Value("${blackduck.configure.accept.eula}")
    private boolean blackDuckConfigureAcceptEula;

    @Value("${alert.install.method}")
    private InstallMethod alertInstallMethod;

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

    @Value("${alert.install.encryption.password.path}")
    private String alertInstallEncryptionPasswordPath;

    @Value("${alert.install.encryption.global.salt.path}")
    private String alertInstallEncryptionGlobalSaltPath;

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

    public String getBlackDuckStackName() {
        return blackDuckStackName;
    }

    public InstallMethod getBlackDuckInstallMethod() {
        return blackDuckInstallMethod;
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

    public String getBlackDuckInstallWebServerHost() {
        return blackDuckInstallWebServerHost;
    }

    public String getBlackDuckInstallProxyHost() {
        return blackDuckInstallProxyHost;
    }

    public int getBlackDuckInstallProxyPort() {
        return blackDuckInstallProxyPort;
    }

    public String getBlackDuckInstallProxyScheme() {
        return blackDuckInstallProxyScheme;
    }

    public String getBlackDuckInstallProxyUser() {
        return blackDuckInstallProxyUser;
    }

    public String getBlackDuckInstallCustomKbHost() {
        return blackDuckInstallCustomKbHost;
    }

    public String getBlackDuckInstallCustomKeyPath() {
        return blackDuckInstallCustomKeyPath;
    }

    public String getBlackDuckInstallCustomCertPath() {
        return blackDuckInstallCustomCertPath;
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

    public String getBlackDuckApiToken() {
        return blackDuckApiToken;
    }

    public String getBlackDuckConfigureRegistrationKey() {
        return blackDuckConfigureRegistrationKey;
    }

    public boolean isBlackDuckConfigureAcceptEula() {
        return blackDuckConfigureAcceptEula;
    }

    public InstallMethod getAlertInstallMethod() {
        return alertInstallMethod;
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

    public String getAlertInstallEncryptionPasswordPath() {
        return alertInstallEncryptionPasswordPath;
    }

    public String getAlertInstallEncryptionGlobalSaltPath() {
        return alertInstallEncryptionGlobalSaltPath;
    }

    public boolean isAlertInstallUseLocalOverrides() {
        return alertInstallUseLocalOverrides;
    }

}
