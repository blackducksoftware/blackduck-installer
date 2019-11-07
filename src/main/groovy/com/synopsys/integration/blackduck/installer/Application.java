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

import com.synopsys.integration.blackduck.configuration.BlackDuckServerConfig;
import com.synopsys.integration.blackduck.configuration.BlackDuckServerConfigBuilder;
import com.synopsys.integration.blackduck.installer.configure.BlackDuckConfigureService;
import com.synopsys.integration.blackduck.installer.dockerswarm.*;
import com.synopsys.integration.blackduck.installer.dockerswarm.edit.*;
import com.synopsys.integration.blackduck.installer.download.AlertGithubDownloadUrl;
import com.synopsys.integration.blackduck.installer.download.ArtifactoryDownloadUrl;
import com.synopsys.integration.blackduck.installer.download.BlackDuckGithubDownloadUrl;
import com.synopsys.integration.blackduck.installer.download.ZipFileDownloader;
import com.synopsys.integration.blackduck.installer.exception.BlackDuckInstallerException;
import com.synopsys.integration.blackduck.installer.hash.HashUtility;
import com.synopsys.integration.blackduck.installer.model.AlertEncryption;
import com.synopsys.integration.blackduck.installer.model.BlackDuckConfigurationOptions;
import com.synopsys.integration.blackduck.installer.model.CustomCertificate;
import com.synopsys.integration.blackduck.installer.model.InstallResult;
import com.synopsys.integration.blackduck.installer.workflow.AlertInstallMethodDecider;
import com.synopsys.integration.blackduck.installer.workflow.BlackDuckInstallMethodDecider;
import com.synopsys.integration.blackduck.installer.workflow.DownloadUrlDecider;
import com.synopsys.integration.blackduck.service.BlackDuckService;
import com.synopsys.integration.blackduck.service.BlackDuckServicesFactory;
import com.synopsys.integration.executable.DryRunExecutableRunner;
import com.synopsys.integration.executable.ExecutableRunner;
import com.synopsys.integration.executable.ProcessBuilderRunner;
import com.synopsys.integration.log.IntLogger;
import com.synopsys.integration.log.Slf4jIntLogger;
import com.synopsys.integration.rest.client.IntHttpClient;
import com.synopsys.integration.rest.credentials.Credentials;
import com.synopsys.integration.rest.credentials.CredentialsBuilder;
import com.synopsys.integration.rest.proxy.ProxyInfo;
import com.synopsys.integration.rest.proxy.ProxyInfoBuilder;
import com.synopsys.integration.util.CommonZipExpander;
import org.apache.commons.compress.archivers.examples.Expander;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;

import java.io.File;

@SpringBootApplication
public class Application implements ApplicationRunner {
    private Logger logger = LoggerFactory.getLogger(Application.class);

    @Autowired
    private ApplicationValues applicationValues;

    public static void main(final String[] args) {
        SpringApplicationBuilder builder = new SpringApplicationBuilder(Application.class);
        builder.run(args);
    }

    @Override
    public void run(final ApplicationArguments applicationArguments) {
        try {
            File baseDirectory = new File(applicationValues.getBaseDirectory());
            baseDirectory.mkdirs();
            if (!baseDirectory.exists()) {
                throw new BlackDuckInstallerException("The base directory (" + applicationValues.getBaseDirectory() + ") must exist or be creatable.");
            }

            if (applicationValues.getBlackDuckInstallMethod() == null) {
                throw new BlackDuckInstallerException("The Black Duck install method was not set - it must be CLEAN, NEW, or UPGRADE.");
            }

            // use only unix style endings for now
            String lineSeparator = "\n";
            Expander expander = new Expander();
            IntLogger intLogger = new Slf4jIntLogger(logger);
            HashUtility hashUtility = new HashUtility();
            DockerCommands dockerCommands = new DockerCommands();
            CommonZipExpander commonZipExpander = new CommonZipExpander(intLogger, expander);
            CustomCertificate customCertificate = new CustomCertificate(applicationValues.getBlackDuckInstallCustomCertPath(), applicationValues.getBlackDuckInstallCustomKeyPath());

            CredentialsBuilder credentialsBuilder = Credentials.newBuilder();
            credentialsBuilder.setUsername(applicationValues.getProxyUsername());
            credentialsBuilder.setPassword(applicationValues.getProxyPassword());
            Credentials proxyCredentials = credentialsBuilder.build();

            ProxyInfoBuilder proxyInfoBuilder = ProxyInfo.newBuilder();
            proxyInfoBuilder.setHost(applicationValues.getProxyHost());
            proxyInfoBuilder.setPort(applicationValues.getProxyPort());
            proxyInfoBuilder.setCredentials(proxyCredentials);
            proxyInfoBuilder.setNtlmDomain(applicationValues.getProxyNtlmDomain());
            proxyInfoBuilder.setNtlmWorkstation(applicationValues.getProxyNtlmWorkstation());

            ProxyInfo proxyInfo = proxyInfoBuilder.build();
            IntHttpClient intHttpClient = new IntHttpClient(intLogger, applicationValues.getTimeoutInSeconds(), applicationValues.isAlwaysTrust(), proxyInfo);

            ExecutableRunner executableRunner;
            if (applicationValues.isInstallDryRun()) {
                executableRunner = new DryRunExecutableRunner(intLogger::info);
            } else {
                executableRunner = new ProcessBuilderRunner();
            }

            DockerStackDeploy deployBlackDuck = new DockerStackDeploy(applicationValues.getBlackDuckStackName());
            InstallResult blackDuckInstallResult = installBlackDuck(baseDirectory, lineSeparator, intLogger, commonZipExpander, customCertificate, hashUtility, intHttpClient, executableRunner, dockerCommands, deployBlackDuck);

            BlackDuckConfigureService blackDuckConfigureService = null;
            BlackDuckConfigurationOptions blackDuckConfigurationOptions = new BlackDuckConfigurationOptions(applicationValues.getBlackDuckConfigureRegistrationKey(), applicationValues.isBlackDuckConfigureAcceptEula());
            if (blackDuckConfigurationOptions.shouldConfigure()) {
                blackDuckConfigureService = createBlackDuckConfigureService(intLogger, blackDuckConfigurationOptions);
            }
            if (blackDuckInstallResult.getReturnCode() == 0) {
                logger.info("The Black Duck install was successful!");
                if (blackDuckConfigurationOptions.shouldConfigure() && !applicationValues.isInstallDryRun()) {
                    logger.info("Black Duck will now be configured.");
                    blackDuckConfigureService.configureBlackDuck();
                }
                if (com.synopsys.integration.blackduck.installer.model.InstallMethod.NONE != applicationValues.getAlertInstallMethod()) {
                    logger.info("The Alert install will now be attempted...");
                    AlertEncryption alertEncryption = new AlertEncryption(applicationValues.getAlertInstallEncryptionPasswordPath(), applicationValues.getAlertInstallEncryptionGlobalSaltPath());
                    InstallResult alertInstallResult = installAlert(baseDirectory, lineSeparator, intLogger, commonZipExpander, blackDuckInstallResult.getInstallDirectory(), customCertificate, alertEncryption, hashUtility, intHttpClient, executableRunner, dockerCommands, deployBlackDuck);
                    if (alertInstallResult.getReturnCode() == 0) {
                        logger.info("The Alert install was successful!");
                    } else {
                        logger.warn("At least one Alert install command was not successful - please check the output for any errors.");
                    }
                }
            } else {
                logger.warn("At least one Black Duck install command was not successful - please check the output for any errors.");
            }
        } catch (BlackDuckInstallerException e) {
            logger.error("The installer could not complete successfully: " + e.getMessage());
        }

    }

    private InstallResult installBlackDuck(File baseDirectory, String lineSeparator, IntLogger intLogger, CommonZipExpander commonZipExpander, CustomCertificate customCertificate, HashUtility hashUtility, IntHttpClient intHttpClient, ExecutableRunner executableRunner, DockerCommands dockerCommands, DockerStackDeploy deployBlackDuck) throws BlackDuckInstallerException {
        BlackDuckGithubDownloadUrl blackDuckGithubDownloadUrl = new BlackDuckGithubDownloadUrl(applicationValues.getBlackDuckGithubDownloadUrlPrefix(), applicationValues.getBlackDuckVersion());
        ArtifactoryDownloadUrl blackDuckArtifactoryDownloadUrl = new ArtifactoryDownloadUrl(applicationValues.getBlackDuckArtifactoryUrl(), applicationValues.getBlackDuckArtifactoryRepo(), applicationValues.getBlackDuckArtifactPath(), applicationValues.getBlackDuckArtifact(), applicationValues.getBlackDuckVersion());
        DownloadUrlDecider downloadUrlDecider = new DownloadUrlDecider(applicationValues.getBlackDuckDownloadSource(), blackDuckGithubDownloadUrl::getDownloadUrl, blackDuckArtifactoryDownloadUrl::getDownloadUrl);
        BlackDuckInstallMethodDecider blackDuckInstallMethodDecider = new BlackDuckInstallMethodDecider(applicationValues.getBlackDuckInstallMethod(), dockerCommands, applicationValues.getBlackDuckStackName(), customCertificate);
        InstallMethod installMethodToUse = blackDuckInstallMethodDecider.determineInstallMethod();

        HubWebServerEnvTokens hubWebServerEnvTokens = new HubWebServerEnvTokens(applicationValues.getBlackDuckInstallWebServerHost());
        HubWebServerEnvEditor hubWebServerEnvEditor = new HubWebServerEnvEditor(intLogger, hashUtility, lineSeparator, hubWebServerEnvTokens);
        BlackDuckConfigEnvEditor blackDuckConfigEnvEditor = new BlackDuckConfigEnvEditor(intLogger, hashUtility, lineSeparator, applicationValues.getBlackDuckInstallProxyHost(), applicationValues.getBlackDuckInstallProxyPort(), applicationValues.getBlackDuckInstallProxyScheme(), applicationValues.getBlackDuckInstallProxyUser(), applicationValues.getBlackDuckInstallCustomKbHost());

        boolean useLocalOverrides = applicationValues.isBlackDuckInstallUseLocalOverrides();
        if (!customCertificate.isEmpty()) {
            useLocalOverrides = true;
        }
        LocalOverridesEditor localOverridesEditor = new LocalOverridesEditor(intLogger, hashUtility, lineSeparator, applicationValues.getBlackDuckStackName(), useLocalOverrides);

        ZipFileDownloader blackDuckDownloader = new ZipFileDownloader(intLogger, intHttpClient, commonZipExpander, downloadUrlDecider, baseDirectory, "blackduck", applicationValues.getBlackDuckVersion(), applicationValues.isBlackDuckDownloadForce());

        BlackDuckInstaller blackDuckInstaller = new BlackDuckInstaller(blackDuckDownloader, executableRunner, installMethodToUse, deployBlackDuck, blackDuckConfigEnvEditor, hubWebServerEnvEditor, localOverridesEditor, useLocalOverrides);
        return blackDuckInstaller.performInstall();
    }

    private InstallResult installAlert(File baseDirectory, String lineSeparator, IntLogger intLogger, CommonZipExpander commonZipExpander, File blackDuckInstallDirectory, CustomCertificate customCertificate, AlertEncryption alertEncryption, HashUtility hashUtility, IntHttpClient intHttpClient, ExecutableRunner executableRunner, DockerCommands dockerCommands, DockerStackDeploy deployBlackDuck) throws BlackDuckInstallerException {
        AlertGithubDownloadUrl alertGithubDownloadUrl = new AlertGithubDownloadUrl(applicationValues.getAlertGithubDownloadUrlPrefix(), applicationValues.getAlertVersion());
        ArtifactoryDownloadUrl alertArtifactoryDownloadUrl = new ArtifactoryDownloadUrl(applicationValues.getAlertArtifactoryUrl(), applicationValues.getAlertArtifactoryRepo(), applicationValues.getAlertArtifactPath(), applicationValues.getAlertArtifact(), applicationValues.getAlertVersion());
        DownloadUrlDecider downloadUrlDecider = new DownloadUrlDecider(applicationValues.getAlertDownloadSource(), alertGithubDownloadUrl::getDownloadUrl, alertArtifactoryDownloadUrl::getDownloadUrl);
        AlertInstallMethodDecider alertInstallMethodDecider = new AlertInstallMethodDecider(applicationValues.getAlertInstallMethod(), dockerCommands, applicationValues.getBlackDuckStackName(), alertEncryption);
        InstallMethod installMethodToUse = alertInstallMethodDecider.determineInstallMethod();
        HubWebServerEnvTokens hubWebServerEnvTokens = new HubWebServerEnvTokens(applicationValues.getBlackDuckInstallWebServerHost(), true);
        HubWebServerEnvEditor hubWebServerEnvEditor = new HubWebServerEnvEditor(intLogger, hashUtility, lineSeparator, hubWebServerEnvTokens);

        boolean useLocalOverrides = applicationValues.isAlertInstallUseLocalOverrides();
        if (!customCertificate.isEmpty() || !alertEncryption.isEmpty()) {
            useLocalOverrides = true;
        }
        AlertLocalOverridesEditor alertLocalOverridesEditor = new AlertLocalOverridesEditor(intLogger, hashUtility, lineSeparator, applicationValues.getBlackDuckStackName(), applicationValues.getBlackDuckInstallWebServerHost(), alertEncryption, customCertificate, useLocalOverrides);

        ZipFileDownloader alertDownloader = new ZipFileDownloader(intLogger, intHttpClient, commonZipExpander, downloadUrlDecider, baseDirectory, "blackduck-alert", applicationValues.getAlertVersion(), applicationValues.isAlertDownloadForce());

        DockerStackDeploy dockerStackDeploy = new DockerStackDeploy(applicationValues.getBlackDuckStackName());
        AlertInstaller alertInstaller = new AlertInstaller(alertDownloader, executableRunner, installMethodToUse, dockerStackDeploy, deployBlackDuck, blackDuckInstallDirectory, hubWebServerEnvEditor, alertLocalOverridesEditor, useLocalOverrides);
        return alertInstaller.performInstall();
    }

    private BlackDuckConfigureService createBlackDuckConfigureService(IntLogger intLogger, BlackDuckConfigurationOptions blackDuckConfigurationOptions) {
        BlackDuckServerConfigBuilder builder = BlackDuckServerConfig.newBuilder();
        builder.setLogger(intLogger);

        builder.setUrl("https://" + applicationValues.getBlackDuckInstallWebServerHost());
        builder.setTimeoutInSeconds(applicationValues.getTimeoutInSeconds());
        builder.setTrustCert(applicationValues.isAlwaysTrust());
        builder.setUsername(applicationValues.getBlackDuckUsername());
        builder.setPassword(applicationValues.getBlackDuckPassword());
        builder.setApiToken(applicationValues.getBlackDuckApiToken());

        BlackDuckServerConfig blackDuckServerConfig = builder.build();
        BlackDuckServicesFactory blackDuckServicesFactory = blackDuckServerConfig.createBlackDuckServicesFactory(intLogger);
        BlackDuckService blackDuckService = blackDuckServicesFactory.createBlackDuckService();

        return new BlackDuckConfigureService(intLogger, blackDuckService, applicationValues.getBlackDuckInstallTimeoutInSeconds(), blackDuckConfigurationOptions);
    }

}
