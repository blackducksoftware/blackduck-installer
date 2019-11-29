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
import com.synopsys.integration.blackduck.installer.configure.*;
import com.synopsys.integration.blackduck.installer.dockerswarm.DockerCommands;
import com.synopsys.integration.blackduck.installer.dockerswarm.DockerStackDeploy;
import com.synopsys.integration.blackduck.installer.dockerswarm.deploy.AlertDockerManager;
import com.synopsys.integration.blackduck.installer.dockerswarm.deploy.BlackDuckDockerManager;
import com.synopsys.integration.blackduck.installer.dockerswarm.edit.BlackDuckConfigEnvEditor;
import com.synopsys.integration.blackduck.installer.dockerswarm.edit.HubWebServerEnvEditor;
import com.synopsys.integration.blackduck.installer.dockerswarm.edit.HubWebServerEnvTokens;
import com.synopsys.integration.blackduck.installer.dockerswarm.edit.LocalOverridesEditor;
import com.synopsys.integration.blackduck.installer.dockerswarm.install.AlertInstaller;
import com.synopsys.integration.blackduck.installer.dockerswarm.install.BlackDuckInstaller;
import com.synopsys.integration.blackduck.installer.download.ArtifactoryDownloadUrl;
import com.synopsys.integration.blackduck.installer.download.BlackDuckGithubDownloadUrl;
import com.synopsys.integration.blackduck.installer.download.ZipFileDownloader;
import com.synopsys.integration.blackduck.installer.exception.BlackDuckInstallerException;
import com.synopsys.integration.blackduck.installer.hash.HashUtility;
import com.synopsys.integration.blackduck.installer.keystore.KeyStoreManager;
import com.synopsys.integration.blackduck.installer.keystore.KeyStoreRequest;
import com.synopsys.integration.blackduck.installer.keystore.OpenSslOutputParser;
import com.synopsys.integration.blackduck.installer.keystore.OpenSslRunner;
import com.synopsys.integration.blackduck.installer.model.*;
import com.synopsys.integration.blackduck.installer.workflow.AlertBlackDuckInstallOptionsBuilder;
import com.synopsys.integration.blackduck.installer.workflow.AlertInstallerBuilder;
import com.synopsys.integration.blackduck.installer.workflow.DownloadUrlDecider;
import com.synopsys.integration.exception.IntegrationException;
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
import com.synopsys.integration.rest.request.Request;
import com.synopsys.integration.util.CommonZipExpander;
import org.apache.commons.compress.archivers.examples.Expander;
import org.apache.http.entity.ContentType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;

import java.io.File;
import java.io.IOException;

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

            if (null == applicationValues.getBlackDuckDeployMethod() || null == applicationValues.getAlertDeployMethod()) {
                throw new BlackDuckInstallerException("The deploy methods must be set to either DEPLOY or NONE.");
            }

            if (DeployMethod.NONE == applicationValues.getBlackDuckDeployMethod() && DeployMethod.NONE == applicationValues.getAlertDeployMethod()) {
                throw new BlackDuckInstallerException("No product was configured for deploy.");
            }

            // use only unix style endings for now
            String lineSeparator = "\n";
            Expander expander = new Expander();
            IntLogger intLogger = new Slf4jIntLogger(logger);
            HashUtility hashUtility = new HashUtility();
            ExecutableCreator executableCreator = new ExecutableCreator();
            DockerCommands dockerCommands = new DockerCommands(executableCreator);
            CommonZipExpander commonZipExpander = new CommonZipExpander(intLogger, expander);
            CustomCertificate customCertificate = new CustomCertificate(applicationValues.getBlackDuckInstallCustomCertPath(), applicationValues.getBlackDuckInstallCustomKeyPath());
            AlertEncryption alertEncryption = new AlertEncryption(applicationValues.getAlertInstallEncryptionPasswordPath(), applicationValues.getAlertInstallEncryptionGlobalSaltPath());

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
            ExecutablesRunner executablesRunner = new ExecutablesRunner(executableRunner);
            DockerStackDeploy deployStack = new DockerStackDeploy(applicationValues.getStackName());

            DeployProductProperties deployProductProperties = new DeployProductProperties(baseDirectory, lineSeparator, intLogger, hashUtility, dockerCommands, commonZipExpander, customCertificate, intHttpClient, executablesRunner, deployStack);

            BlackDuckConfigurationOptions blackDuckConfigurationOptions = new BlackDuckConfigurationOptions(applicationValues.getBlackDuckConfigureRegistrationKey(), applicationValues.isBlackDuckConfigureAcceptEula(), applicationValues.isBlackDuckConfigureApiToken(), applicationValues.isInstallDryRun());

            DockerService alertService = new DockerService(applicationValues.getStackName(), AlertDockerManager.ALERT_SERVICE_NAME);
            AlertBlackDuckInstallOptionsBuilder alertBlackDuckInstallOptionsBuilder = new AlertBlackDuckInstallOptionsBuilder(applicationValues);
            DeployAlertProperties deployAlertProperties = new DeployAlertProperties(alertService, alertBlackDuckInstallOptionsBuilder, alertEncryption);

            if (DeployMethod.DEPLOY == applicationValues.getBlackDuckDeployMethod()) {
                logger.info("Attempting to deploy Black Duck.");
                OpenSslOutputParser openSslOutputParser = new OpenSslOutputParser();
                OpenSslRunner openSslRunner = new OpenSslRunner(intLogger, executablesRunner, openSslOutputParser);
                KeyStoreManager keyStoreManager = new KeyStoreManager();
                KeyStoreRequest keyStoreRequest = new KeyStoreRequest(new File(applicationValues.getKeyStoreFile()), applicationValues.getKeyStoreType(), applicationValues.getKeyStorePassword());
                UpdateKeyStoreService updateKeyStoreService = new UpdateKeyStoreService(intLogger, keyStoreManager, keyStoreRequest, applicationValues.isKeyStoreUpdate(), applicationValues.isKeyStoreUpdateForce(), applicationValues.getBlackDuckInstallWebServerHost(), 443, openSslRunner);
                BlackDuckServerConfig blackDuckServerConfig = createBlackDuckServerConfig(intLogger);
                BlackDuckWait blackDuckWait = new BlackDuckWait(intLogger, applicationValues.getBlackDuckInstallTimeoutInSeconds(), blackDuckServerConfig, updateKeyStoreService);
                BlackDuckConfigureService blackDuckConfigureService = new BlackDuckConfigureService(deployProductProperties.getIntLogger(), blackDuckServerConfig, applicationValues.getBlackDuckInstallTimeoutInSeconds(), blackDuckConfigurationOptions);
                BlackDuckDeployResult blackDuckDeployResult = deployBlackDuck(deployProductProperties, blackDuckConfigurationOptions, blackDuckConfigureService, blackDuckWait);

                if (DeployMethod.DEPLOY == applicationValues.getAlertDeployMethod()) {
                    logger.info("Attempting to deploy Alert once Black Duck is healthy.");
                    AlertWait alertWait = createAlertWait(intLogger);
                    blackDuckDeployResult.getApiToken().ifPresent(deployAlertProperties::setBlackDuckApiToken);
                    deployAlert(deployProductProperties, deployAlertProperties, alertWait);
                }
            } else {
                logger.info("Attempting to deploy Alert.");
                AlertWait alertWait = createAlertWait(intLogger);
                deployAlert(deployProductProperties, deployAlertProperties, alertWait);
            }
        } catch (InterruptedException | IntegrationException | IOException e) {
            logger.error("The installer could not complete successfully: " + e.getMessage());
        }
    }

    private BlackDuckDeployResult deployBlackDuck(DeployProductProperties deployProductProperties, BlackDuckConfigurationOptions blackDuckConfigurationOptions, BlackDuckConfigureService blackDuckConfigureService, BlackDuckWait blackDuckWait) throws IntegrationException, InterruptedException, IOException {
        InstallResult blackDuckInstallResult = installBlackDuck(deployProductProperties);

        if (blackDuckInstallResult.getReturnCode() == 0) {
            blackDuckWait.waitForBlackDuck(blackDuckInstallResult.getInstallDirectory());
            logger.info("The Black Duck install was successful!");
            if (blackDuckConfigurationOptions.shouldConfigure()) {
                logger.info("Black Duck will now be configured.");
                ConfigureResult configureResult = blackDuckConfigureService.configureBlackDuck();
                if (configureResult.isSuccess() && configureResult.getApiToken().isPresent()) {
                    return new BlackDuckDeployResult(configureResult.getApiToken().get(), blackDuckInstallResult);
                }
            }

            return new BlackDuckDeployResult(blackDuckInstallResult);
        } else {
            throw new BlackDuckInstallerException("At least one Black Duck install command was not successful, the install can not continue - please check the output for any errors.");
        }
    }

    private InstallResult installBlackDuck(DeployProductProperties deployProductProperties) throws BlackDuckInstallerException {
        BlackDuckGithubDownloadUrl blackDuckGithubDownloadUrl = new BlackDuckGithubDownloadUrl(applicationValues.getBlackDuckGithubDownloadUrlPrefix(), applicationValues.getBlackDuckVersion());
        ArtifactoryDownloadUrl blackDuckArtifactoryDownloadUrl = new ArtifactoryDownloadUrl(applicationValues.getBlackDuckArtifactoryUrl(), applicationValues.getBlackDuckArtifactoryRepo(), applicationValues.getBlackDuckArtifactPath(), applicationValues.getBlackDuckArtifact(), applicationValues.getBlackDuckVersion());
        DownloadUrlDecider downloadUrlDecider = new DownloadUrlDecider(applicationValues.getBlackDuckDownloadSource(), blackDuckGithubDownloadUrl::getDownloadUrl, blackDuckArtifactoryDownloadUrl::getDownloadUrl);

        HubWebServerEnvTokens hubWebServerEnvTokens = new HubWebServerEnvTokens(applicationValues.getBlackDuckInstallWebServerHost());
        HubWebServerEnvEditor hubWebServerEnvEditor = new HubWebServerEnvEditor(deployProductProperties.getIntLogger(), deployProductProperties.getHashUtility(), deployProductProperties.getLineSeparator(), hubWebServerEnvTokens);
        BlackDuckConfigEnvEditor blackDuckConfigEnvEditor = new BlackDuckConfigEnvEditor(deployProductProperties.getIntLogger(), deployProductProperties.getHashUtility(), deployProductProperties.getLineSeparator(), applicationValues.getBlackDuckInstallProxyHost(), applicationValues.getBlackDuckInstallProxyPort(), applicationValues.getBlackDuckInstallProxyScheme(), applicationValues.getBlackDuckInstallProxyUser(), applicationValues.getBlackDuckInstallCustomKbHost());

        boolean useLocalOverrides = applicationValues.isBlackDuckInstallUseLocalOverrides();
        if (!deployProductProperties.getCustomCertificate().isEmpty()) {
            useLocalOverrides = true;
        }
        LocalOverridesEditor localOverridesEditor = new LocalOverridesEditor(deployProductProperties.getIntLogger(), deployProductProperties.getHashUtility(), deployProductProperties.getLineSeparator(), applicationValues.getStackName(), useLocalOverrides);

        ZipFileDownloader blackDuckDownloader = new ZipFileDownloader(deployProductProperties.getIntLogger(), deployProductProperties.getIntHttpClient(), deployProductProperties.getCommonZipExpander(), downloadUrlDecider, deployProductProperties.getBaseDirectory(), "blackduck", applicationValues.getBlackDuckVersion(), applicationValues.isBlackDuckDownloadForce());

        BlackDuckDockerManager blackDuckDockerManager = new BlackDuckDockerManager(deployProductProperties.getIntLogger(), deployProductProperties.getDockerCommands(), applicationValues.getStackName(), deployProductProperties.getCustomCertificate());
        BlackDuckInstaller blackDuckInstaller = new BlackDuckInstaller(blackDuckDownloader, deployProductProperties.getExecutablesRunner(), blackDuckDockerManager, deployProductProperties.getDeployStack(), deployProductProperties.getDockerCommands(), blackDuckConfigEnvEditor, hubWebServerEnvEditor, localOverridesEditor, useLocalOverrides);
        return blackDuckInstaller.performInstall();
    }

    private void deployAlert(DeployProductProperties deployProductProperties, DeployAlertProperties deployAlertProperties, AlertWait alertWait) throws BlackDuckInstallerException, InterruptedException {
        alertWait.waitForAlert();
        logger.info("The Alert install will now be attempted...");
        InstallResult alertInstallResult = installAlert(deployProductProperties, deployAlertProperties);
        if (alertInstallResult.getReturnCode() == 0) {
            logger.info("The Alert install was successful!");
        } else {
            logger.warn("At least one Alert install command was not successful - please check the output for any errors.");
        }
    }

    private InstallResult installAlert(DeployProductProperties deployProductProperties, DeployAlertProperties deployAlertProperties) throws BlackDuckInstallerException {
        AlertInstallerBuilder alertInstallerBuilder = new AlertInstallerBuilder();
        alertInstallerBuilder.setRequiredProperties(deployProductProperties, deployAlertProperties, applicationValues);

        AlertInstaller alertInstaller = alertInstallerBuilder.build();
        return alertInstaller.performInstall();
    }

    private BlackDuckServerConfig createBlackDuckServerConfig(IntLogger intLogger) {
        BlackDuckServerConfigBuilder builder = BlackDuckServerConfig.newBuilder();
        builder.setLogger(intLogger);

        builder.setUrl("https://" + applicationValues.getBlackDuckInstallWebServerHost());
        builder.setTimeoutInSeconds(applicationValues.getTimeoutInSeconds());
        builder.setTrustCert(applicationValues.isAlwaysTrust());
        builder.setUsername(applicationValues.getBlackDuckUsername());
        builder.setPassword(applicationValues.getBlackDuckPassword());

        return builder.build();
    }

    private AlertWait createAlertWait(IntLogger intLogger) {
        String alertUrl = String.format("https://%s:%s/alert", applicationValues.getBlackDuckInstallWebServerHost(), applicationValues.getAlertInstallPort());
        Request.Builder requestBuilder = Request.newBuilder();
        requestBuilder.uri(alertUrl);
        requestBuilder.mimeType(ContentType.TEXT_HTML.getMimeType());
        Request alertRequest = requestBuilder.build();
        IntHttpClient httpClient = new IntHttpClient(intLogger, applicationValues.getTimeoutInSeconds(), applicationValues.isAlwaysTrust(), ProxyInfo.NO_PROXY_INFO);
        return new AlertWait(intLogger, applicationValues.getBlackDuckInstallTimeoutInSeconds(), httpClient, alertRequest);
    }

}
