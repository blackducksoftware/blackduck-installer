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

import java.io.File;
import java.io.IOException;

import org.apache.commons.compress.archivers.examples.Expander;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.entity.ContentType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;

import com.google.gson.Gson;
import com.synopsys.integration.blackduck.configuration.BlackDuckServerConfig;
import com.synopsys.integration.blackduck.configuration.BlackDuckServerConfigBuilder;
import com.synopsys.integration.blackduck.installer.configure.AlertWait;
import com.synopsys.integration.blackduck.installer.configure.BlackDuckConfigureService;
import com.synopsys.integration.blackduck.installer.configure.BlackDuckWait;
import com.synopsys.integration.blackduck.installer.configure.ConfigureResult;
import com.synopsys.integration.blackduck.installer.configure.UpdateKeyStoreService;
import com.synopsys.integration.blackduck.installer.dockerswarm.DockerCommands;
import com.synopsys.integration.blackduck.installer.dockerswarm.DockerStackDeploy;
import com.synopsys.integration.blackduck.installer.dockerswarm.deploy.AlertDockerManager;
import com.synopsys.integration.blackduck.installer.dockerswarm.install.AlertBlackDuckInstallOptionsBuilder;
import com.synopsys.integration.blackduck.installer.dockerswarm.install.AlertInstaller;
import com.synopsys.integration.blackduck.installer.dockerswarm.install.AlertInstallerCreator;
import com.synopsys.integration.blackduck.installer.dockerswarm.install.BlackDuckInstaller;
import com.synopsys.integration.blackduck.installer.dockerswarm.install.BlackDuckInstallerCreator;
import com.synopsys.integration.blackduck.installer.download.StandardCookieSpecHttpClient;
import com.synopsys.integration.blackduck.installer.exception.BlackDuckInstallerException;
import com.synopsys.integration.blackduck.installer.hash.HashUtility;
import com.synopsys.integration.blackduck.installer.keystore.KeyStoreManager;
import com.synopsys.integration.blackduck.installer.keystore.KeyStoreRequest;
import com.synopsys.integration.blackduck.installer.keystore.OpenSslOutputParser;
import com.synopsys.integration.blackduck.installer.keystore.OpenSslRunner;
import com.synopsys.integration.blackduck.installer.model.AlertBlackDuckInstallOptions;
import com.synopsys.integration.blackduck.installer.model.AlertDatabase;
import com.synopsys.integration.blackduck.installer.model.AlertEncryption;
import com.synopsys.integration.blackduck.installer.model.BlackDuckConfigurationOptions;
import com.synopsys.integration.blackduck.installer.model.BlackDuckDeployResult;
import com.synopsys.integration.blackduck.installer.model.ConfigPropertiesLoader;
import com.synopsys.integration.blackduck.installer.model.CustomCertificate;
import com.synopsys.integration.blackduck.installer.model.DeployMethod;
import com.synopsys.integration.blackduck.installer.model.DockerService;
import com.synopsys.integration.blackduck.installer.model.ExecutableCreator;
import com.synopsys.integration.blackduck.installer.model.ExecutablesRunner;
import com.synopsys.integration.blackduck.installer.model.FilePathTransformer;
import com.synopsys.integration.blackduck.installer.model.InstallResult;
import com.synopsys.integration.blackduck.installer.model.LoadedConfigProperties;
import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.executable.DryRunExecutableRunner;
import com.synopsys.integration.executable.ExecutableRunner;
import com.synopsys.integration.executable.ProcessBuilderRunner;
import com.synopsys.integration.log.IntLogger;
import com.synopsys.integration.log.Slf4jIntLogger;
import com.synopsys.integration.rest.HttpUrl;
import com.synopsys.integration.rest.client.IntHttpClient;
import com.synopsys.integration.rest.credentials.Credentials;
import com.synopsys.integration.rest.credentials.CredentialsBuilder;
import com.synopsys.integration.rest.proxy.ProxyInfo;
import com.synopsys.integration.rest.proxy.ProxyInfoBuilder;
import com.synopsys.integration.rest.request.Request;
import com.synopsys.integration.util.CommonZipExpander;

@SpringBootApplication
public class Application implements ApplicationRunner {
    public static final String DEFAULT_LINE_SEPARATOR = "\n";
    private Logger logger = LoggerFactory.getLogger(Application.class);
    @Autowired
    private ApplicationValues applicationValues;

    public static void main(String[] args) {
        SpringApplicationBuilder builder = new SpringApplicationBuilder(Application.class);
        builder.run(args);
    }

    @Override
    public void run(ApplicationArguments applicationArguments) {
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
            String lineSeparator = DEFAULT_LINE_SEPARATOR;
            Expander expander = new Expander();
            Gson gson = new Gson();
            ConfigPropertiesLoader configPropertiesLoader = new ConfigPropertiesLoader(gson);
            IntLogger intLogger = new Slf4jIntLogger(logger);
            HashUtility hashUtility = new HashUtility();
            FilePathTransformer filePathTransformer = new FilePathTransformer();
            ExecutableCreator executableCreator = new ExecutableCreator();
            DockerCommands dockerCommands = new DockerCommands(executableCreator);
            CommonZipExpander commonZipExpander = new CommonZipExpander(intLogger, expander);
            CustomCertificate customCertificate = new CustomCertificate(applicationValues.getCustomCertificatePath(), applicationValues.getCustomCertificateKeyPath());
            AlertEncryption alertEncryption = new AlertEncryption(applicationValues.getAlertInstallEncryptionPasswordPath(), applicationValues.getAlertInstallEncryptionGlobalSaltPath());
            AlertDatabase alertDatabase = new AlertDatabase(applicationValues.getAlertInstallDatabaseName(), applicationValues.getAlertInstallDatabaseExternalHost(), applicationValues.getAlertInstallDatabaseExternalPort(),
                applicationValues.getAlertInstallDatabaseDefaultUserName(),
                applicationValues.getAlertInstallDatabaseDefaultPassword(), applicationValues.getAlertInstallDatabaseUserNamePath(), applicationValues.getAlertInstallDatabasePasswordPath());

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
            IntHttpClient intHttpClient = new StandardCookieSpecHttpClient(intLogger, applicationValues.getTimeoutInSeconds(), applicationValues.isAlwaysTrust(), proxyInfo);

            ExecutableRunner executableRunner;
            if (applicationValues.isInstallDryRun()) {
                logger.info("Executing a dry run of installer");
                executableRunner = new DryRunExecutableRunner(intLogger::info);
            } else {
                executableRunner = new ProcessBuilderRunner(intLogger, intLogger::info, intLogger::trace);
            }
            ExecutablesRunner executablesRunner = new ExecutablesRunner(executableRunner);
            DockerStackDeploy deployStack = new DockerStackDeploy(applicationValues.getStackName());

            DeployProductProperties deployProductProperties = new DeployProductProperties(baseDirectory, lineSeparator, intLogger, hashUtility, filePathTransformer, dockerCommands, commonZipExpander, customCertificate, intHttpClient,
                executablesRunner, deployStack);

            BlackDuckConfigurationOptions blackDuckConfigurationOptions = new BlackDuckConfigurationOptions(applicationValues.getBlackDuckConfigureRegistrationKey(), applicationValues.isBlackDuckConfigureAcceptEula(),
                applicationValues.isBlackDuckConfigureApiToken(), applicationValues.isInstallDryRun());

            DockerService alertService = new DockerService(applicationValues.getStackName(), AlertDockerManager.ALERT_SERVICE_NAME);
            AlertBlackDuckInstallOptionsBuilder alertBlackDuckInstallOptionsBuilder = new AlertBlackDuckInstallOptionsBuilder(applicationValues);
            DeployAlertProperties deployAlertProperties = new DeployAlertProperties(alertService, alertBlackDuckInstallOptionsBuilder, alertEncryption, alertDatabase);
            AlertBlackDuckInstallOptions alertBlackDuckInstallOptions = deployAlertProperties.getAlertBlackDuckInstallOptions();

            OpenSslOutputParser openSslOutputParser = new OpenSslOutputParser();
            OpenSslRunner openSslRunner = new OpenSslRunner(intLogger, executablesRunner, openSslOutputParser);
            KeyStoreManager keyStoreManager = new KeyStoreManager();
            KeyStoreRequest keyStoreRequest = new KeyStoreRequest(new File(applicationValues.getKeyStoreFile()), applicationValues.getKeyStoreType(), applicationValues.getKeyStorePassword());

            if (DeployMethod.DEPLOY == applicationValues.getBlackDuckDeployMethod()) {
                logger.info("Attempting to deploy Black Duck.");
                UpdateKeyStoreService blackDuckUpdateKeyStoreService = UpdateKeyStoreService.createForBlackDuck(intLogger, keyStoreManager, keyStoreRequest, applicationValues.isKeyStoreUpdate(), applicationValues.isKeyStoreUpdateForce(),
                    applicationValues.getWebServerHost(), 443, openSslRunner);
                BlackDuckServerConfig blackDuckServerConfig = createBlackDuckServerConfig(intLogger);
                BlackDuckWait blackDuckWait = new BlackDuckWait(intLogger, applicationValues.getBlackDuckInstallTimeoutInSeconds(), blackDuckServerConfig, blackDuckUpdateKeyStoreService);
                BlackDuckConfigureService blackDuckConfigureService = new BlackDuckConfigureService(deployProductProperties.getIntLogger(), blackDuckServerConfig, applicationValues.getBlackDuckInstallTimeoutInSeconds(),
                    blackDuckConfigurationOptions);
                LoadedConfigProperties blackDuckConfigEnvLoadedProperties = new LoadedConfigProperties();

                // the string contents override the json file
                String blackDuckConfigEnvProperties = applicationValues.getBlackDuckInstallBlackDuckConfigEnvProperties();
                if (StringUtils.isNotBlank(blackDuckConfigEnvProperties)) {
                    blackDuckConfigEnvLoadedProperties = configPropertiesLoader.loadPropertiesFromString(blackDuckConfigEnvProperties);
                } else {
                    String blackDuckConfigEnvPropertiesFilePath = applicationValues.getBlackDuckInstallBlackDuckConfigEnvPropertiesPath();
                    if (StringUtils.isNotEmpty(blackDuckConfigEnvPropertiesFilePath)) {
                        File file = filePathTransformer.transformFilePath(blackDuckConfigEnvPropertiesFilePath);
                        blackDuckConfigEnvLoadedProperties = configPropertiesLoader.loadPropertiesFromFile(file);
                    }
                }

                //TODO pass in the req'd properties instead of applicationValues
                BlackDuckInstallerCreator blackDuckInstallerCreator = new BlackDuckInstallerCreator(applicationValues, deployProductProperties, blackDuckConfigEnvLoadedProperties);
                BlackDuckInstaller blackDuckInstaller = blackDuckInstallerCreator.create();

                BlackDuckDeployResult blackDuckDeployResult = deployBlackDuck(blackDuckInstaller, blackDuckConfigurationOptions, blackDuckConfigureService, blackDuckWait);

                if (DeployMethod.DEPLOY == applicationValues.getAlertDeployMethod()) {
                    logger.info("Attempting to deploy Alert once Black Duck is healthy.");
                    blackDuckDeployResult.getApiToken().ifPresent(deployAlertProperties::setBlackDuckApiToken);

                    UpdateKeyStoreService alertUpdateKeyStoreService = UpdateKeyStoreService.createForAlert(intLogger, keyStoreManager, keyStoreRequest, applicationValues.isKeyStoreUpdate(), applicationValues.isKeyStoreUpdateForce(),
                        applicationValues.getWebServerHost(), 8443, openSslRunner);
                    AlertWait alertWait = createAlertWait(intLogger, alertUpdateKeyStoreService, alertBlackDuckInstallOptions);
                    //TODO pass in the req'd properties instead of applicationValues
                    AlertInstallerCreator alertInstallerCreator = new AlertInstallerCreator(applicationValues, deployProductProperties, deployAlertProperties);
                    AlertInstaller alertInstaller = alertInstallerCreator.create();
                    deployAlert(alertInstaller, alertWait);
                }
            } else {
                logger.info("Attempting to deploy Alert.");

                UpdateKeyStoreService alertUpdateKeyStoreService = UpdateKeyStoreService.createForAlert(intLogger, keyStoreManager, keyStoreRequest, applicationValues.isKeyStoreUpdate(), applicationValues.isKeyStoreUpdateForce(),
                    applicationValues.getWebServerHost(), 8443, openSslRunner);
                AlertWait alertWait = createAlertWait(intLogger, alertUpdateKeyStoreService, alertBlackDuckInstallOptions);
                //TODO pass in the req'd properties instead of applicationValues
                AlertInstallerCreator alertInstallerCreator = new AlertInstallerCreator(applicationValues, deployProductProperties, deployAlertProperties);
                AlertInstaller alertInstaller = alertInstallerCreator.create();
                deployAlert(alertInstaller, alertWait);
            }
        } catch (InterruptedException | IntegrationException | IOException e) {
            logger.error("The installer could not complete successfully: " + e.getMessage());
        }
    }

    private BlackDuckDeployResult deployBlackDuck(BlackDuckInstaller blackDuckInstaller, BlackDuckConfigurationOptions blackDuckConfigurationOptions, BlackDuckConfigureService blackDuckConfigureService, BlackDuckWait blackDuckWait)
        throws IntegrationException, InterruptedException, IOException {
        InstallResult blackDuckInstallResult = blackDuckInstaller.performInstall();

        if (applicationValues.isInstallDryRun()) {
            logger.info("BlackDuck dry run install.  Do not wait for Black Duck installation.");
            return new BlackDuckDeployResult(blackDuckInstallResult);
        }

        if (blackDuckInstallResult.getReturnCode() != 0) {
            throw new BlackDuckInstallerException("At least one Black Duck install command was not successful, the install can not continue - please check the output for any errors.");
        }

        boolean isBlackDuckRunning = blackDuckWait.waitForBlackDuck(blackDuckInstallResult.getInstallDirectory());
        if (!isBlackDuckRunning) {
            throw new BlackDuckInstallerException("Black Duck did not respond within the configured timeout period, the install can not continue - please check the output for any errors.");
        }

        logger.info("The Black Duck install was successful!");
        if (blackDuckConfigurationOptions.shouldConfigure()) {
            logger.info("Black Duck will now be configured.");
            ConfigureResult configureResult = blackDuckConfigureService.configureBlackDuck();
            if (configureResult.isSuccess() && configureResult.getApiToken().isPresent()) {
                return new BlackDuckDeployResult(configureResult.getApiToken().get(), blackDuckInstallResult);
            }
        }

        return new BlackDuckDeployResult(blackDuckInstallResult);
    }

    private void deployAlert(AlertInstaller alertInstaller, AlertWait alertWait) throws IntegrationException, InterruptedException {
        InstallResult alertInstallResult = alertInstaller.performInstall();

        if (applicationValues.isInstallDryRun()) {
            logger.info("Alert dry run install.  Do not wait for Alert installation.");
            return;
        }

        if (alertInstallResult.getReturnCode() != 0) {
            throw new BlackDuckInstallerException("At least one Alert install command was not successful, the install can not continue - please check the output for any errors.");
        }

        boolean isAlertRunning = alertWait.waitForAlert(alertInstallResult.getInstallDirectory());
        if (!isAlertRunning) {
            throw new BlackDuckInstallerException("Alert did not respond within the configured timeout period, the install can not continue - please check the output for any errors.");
        }

        logger.info("The Alert install was successful!");
    }

    private BlackDuckServerConfig createBlackDuckServerConfig(IntLogger intLogger) {
        BlackDuckServerConfigBuilder builder = BlackDuckServerConfig.newBuilder();
        builder.setLogger(intLogger);

        builder.setUrl("https://" + applicationValues.getWebServerHost());
        builder.setTimeoutInSeconds(applicationValues.getTimeoutInSeconds());
        builder.setTrustCert(applicationValues.isAlwaysTrust());
        builder.setUsername(applicationValues.getBlackDuckUsername());
        builder.setPassword(applicationValues.getBlackDuckPassword());

        return builder.build();
    }

    private AlertWait createAlertWait(IntLogger intLogger, UpdateKeyStoreService alertUpdateKeyStoreService, AlertBlackDuckInstallOptions alertBlackDuckInstallOptions) throws IntegrationException {
        HttpUrl alertUrl = new HttpUrl(String.format("https://%s:%s/alert", applicationValues.getWebServerHost(), applicationValues.getAlertInstallPort()));
        Request.Builder requestBuilder = new Request.Builder(alertUrl);
        requestBuilder.acceptMimeType(ContentType.TEXT_HTML.getMimeType());
        Request alertRequest = requestBuilder.build();
        return new AlertWait(intLogger, applicationValues.getBlackDuckInstallTimeoutInSeconds(), applicationValues.getTimeoutInSeconds(), applicationValues.isAlwaysTrust(), ProxyInfo.NO_PROXY_INFO, alertRequest, alertUpdateKeyStoreService);
    }

}
