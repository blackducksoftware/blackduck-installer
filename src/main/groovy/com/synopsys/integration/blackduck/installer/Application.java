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
import com.synopsys.integration.blackduck.installer.dockerswarm.AlertInstaller;
import com.synopsys.integration.blackduck.installer.dockerswarm.DockerCommands;
import com.synopsys.integration.blackduck.installer.dockerswarm.BlackDuckInstaller;
import com.synopsys.integration.blackduck.installer.dockerswarm.edit.BlackDuckConfigEnvEditor;
import com.synopsys.integration.blackduck.installer.dockerswarm.edit.HubWebServerEnvEditor;
import com.synopsys.integration.blackduck.installer.dockerswarm.edit.LocalOverridesEditor;
import com.synopsys.integration.blackduck.installer.download.ZipFileDownloader;
import com.synopsys.integration.blackduck.installer.exception.BlackDuckInstallerException;
import com.synopsys.integration.blackduck.installer.hash.HashUtility;
import com.synopsys.integration.blackduck.installer.model.BlackDuckConfigurationOptions;
import com.synopsys.integration.blackduck.installer.model.CustomCertificate;
import com.synopsys.integration.blackduck.installer.model.InstallMethod;
import com.synopsys.integration.blackduck.installer.workflow.AlertDownloadUrlDecider;
import com.synopsys.integration.blackduck.installer.workflow.BlackDuckDownloadUrlDecider;
import com.synopsys.integration.blackduck.installer.workflow.InstallMethodDecider;
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

            if (applicationValues.getBlackDuckInstallMethod() == null || applicationValues.getBlackDuckInstallMethod().equals(InstallMethod.NONE)) {
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

            boolean blackDuckInstallUseLocalOverrides = applicationValues.isBlackDuckInstallUseLocalOverrides();
            if (!customCertificate.isEmpty()) {
                blackDuckInstallUseLocalOverrides = true;
            }

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

            AlertDownloadUrlDecider alertDownloadDecider = new AlertDownloadUrlDecider(applicationValues.getAlertDownloadSource(), applicationValues.getAlertVersion(), applicationValues.getAlertGithubDownloadUrlPrefix(), applicationValues.getAlertArtifactoryUrl(), applicationValues.getAlertArtifactoryRepo(), applicationValues.getAlertArtifactPath(), applicationValues.getAlertArtifact());

            ExecutableRunner executableRunner;
            if (applicationValues.isInstallDryRun()) {
                executableRunner = new DryRunExecutableRunner(intLogger::info);
            } else {
                executableRunner = new ProcessBuilderRunner();
            }

            int blackDuckInstallReturnCode = installBlackDuck(baseDirectory, lineSeparator, intLogger, commonZipExpander, customCertificate, blackDuckInstallUseLocalOverrides, hashUtility, intHttpClient, executableRunner, dockerCommands);

            //alert install step
            int alertInstallReturnCode = installAlert(baseDirectory, lineSeparator, intLogger, commonZipExpander, customCertificate, false, intHttpClient, executableRunner, dockerCommands);

            BlackDuckConfigureService blackDuckConfigureService = null;
            BlackDuckConfigurationOptions blackDuckConfigurationOptions = new BlackDuckConfigurationOptions(applicationValues.getBlackDuckConfigureRegistrationKey(), applicationValues.isBlackDuckConfigureAcceptEula());
            if (blackDuckConfigurationOptions.shouldConfigure()) {
                blackDuckConfigureService = createBlackDuckConfigureService(intLogger, blackDuckConfigurationOptions);
            }
            if (blackDuckInstallReturnCode == 0) {
                logger.info("The install was successful!");
                if (blackDuckConfigurationOptions.shouldConfigure() && !applicationValues.isInstallDryRun()) {
                    logger.info("Black Duck will now be configured.");
                    blackDuckConfigureService.configureBlackDuck();
                }
            } else {
                logger.warn("At least one install command was not successful - please check the output for any errors.");
            }
        } catch (BlackDuckInstallerException e) {
            logger.error("The installer could not complete successfully: " + e.getMessage());
        }

    }

    private int installBlackDuck(File baseDirectory, String lineSeparator, IntLogger intLogger, CommonZipExpander commonZipExpander, CustomCertificate customCertificate, boolean blackDuckInstallUseLocalOverrides, HashUtility hashUtility, IntHttpClient intHttpClient, ExecutableRunner executableRunner, DockerCommands dockerCommands) throws BlackDuckInstallerException {
        BlackDuckDownloadUrlDecider blackDuckDownloadUrlDecider = new BlackDuckDownloadUrlDecider(applicationValues.getBlackDuckDownloadSource(), applicationValues.getBlackDuckVersion(), applicationValues.getBlackDuckGithubDownloadUrlPrefix(), applicationValues.getBlackDuckArtifactoryUrl(), applicationValues.getBlackDuckArtifactoryRepo(), applicationValues.getBlackDuckArtifactPath(), applicationValues.getBlackDuckArtifact());
        InstallMethodDecider installMethodDecider = new InstallMethodDecider(applicationValues.getBlackDuckInstallMethod(), dockerCommands, applicationValues.getBlackDuckStackName(), customCertificate, blackDuckInstallUseLocalOverrides);

        HubWebServerEnvEditor hubWebServerEnvEditor = new HubWebServerEnvEditor(intLogger, hashUtility, lineSeparator, applicationValues.getBlackDuckInstallWebServerHost());
        BlackDuckConfigEnvEditor blackDuckConfigEnvEditor = new BlackDuckConfigEnvEditor(intLogger, hashUtility, lineSeparator, applicationValues.getBlackDuckInstallProxyHost(), applicationValues.getBlackDuckInstallProxyPort(), applicationValues.getBlackDuckInstallProxyScheme(), applicationValues.getBlackDuckInstallProxyUser(), applicationValues.getBlackDuckInstallCustomKbHost());
        LocalOverridesEditor localOverridesEditor = new LocalOverridesEditor(intLogger, hashUtility, lineSeparator, applicationValues.getBlackDuckStackName(), blackDuckInstallUseLocalOverrides);
        ZipFileDownloader blackDuckDownloader = new ZipFileDownloader(intLogger, intHttpClient, commonZipExpander, blackDuckDownloadUrlDecider, baseDirectory, "blackduck", applicationValues.getBlackDuckVersion());
        com.synopsys.integration.blackduck.installer.dockerswarm.install.InstallMethod installMethodToUse = installMethodDecider.determineInstallMethod();

        BlackDuckInstaller blackDuckInstaller = new BlackDuckInstaller(blackDuckDownloader, blackDuckConfigEnvEditor, hubWebServerEnvEditor, localOverridesEditor, executableRunner, installMethodToUse);
        return blackDuckInstaller.performInstall();
    }

    private int installAlert(File baseDirectory, String lineSeparator, IntLogger intLogger, CommonZipExpander commonZipExpander, CustomCertificate customCertificate, boolean alertInstallUseLocalOverrides, IntHttpClient intHttpClient, ExecutableRunner executableRunner, DockerCommands dockerCommands) throws BlackDuckInstallerException {
        AlertDownloadUrlDecider alertDownloadUrlDecider = new AlertDownloadUrlDecider(applicationValues.getAlertDownloadSource(), applicationValues.getAlertVersion(), applicationValues.getAlertGithubDownloadUrlPrefix(), applicationValues.getAlertArtifactoryUrl(), applicationValues.getAlertArtifactoryRepo(), applicationValues.getAlertArtifactPath(), applicationValues.getAlertArtifact());
        InstallMethodDecider installMethodDecider = new InstallMethodDecider(applicationValues.getAlertInstallMethod(), dockerCommands, applicationValues.getAlertStackName(), customCertificate, alertInstallUseLocalOverrides);

        ZipFileDownloader alertDownloader = new ZipFileDownloader(intLogger, intHttpClient, commonZipExpander, alertDownloadUrlDecider, baseDirectory, "alert", applicationValues.getAlertVersion());
        com.synopsys.integration.blackduck.installer.dockerswarm.install.InstallMethod installMethodToUse = installMethodDecider.determineInstallMethod();

        AlertInstaller alertInstaller = new AlertInstaller(alertDownloader, executableRunner, installMethodToUse);
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
