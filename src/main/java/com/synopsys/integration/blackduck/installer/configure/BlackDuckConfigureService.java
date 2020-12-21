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
package com.synopsys.integration.blackduck.installer.configure;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.synopsys.integration.blackduck.api.core.BlackDuckPath;
import com.synopsys.integration.blackduck.api.generated.discovery.ApiDiscovery;
import com.synopsys.integration.blackduck.api.generated.view.RegistrationView;
import com.synopsys.integration.blackduck.api.manual.temporary.component.EndUserLicenseAgreementAction;
import com.synopsys.integration.blackduck.api.manual.temporary.component.RegistrationRequest;
import com.synopsys.integration.blackduck.configuration.BlackDuckServerConfig;
import com.synopsys.integration.blackduck.http.BlackDuckRequestBuilder;
import com.synopsys.integration.blackduck.http.BlackDuckRequestFactory;
import com.synopsys.integration.blackduck.http.client.BlackDuckHttpClient;
import com.synopsys.integration.blackduck.http.transform.BlackDuckJsonTransformer;
import com.synopsys.integration.blackduck.http.transform.BlackDuckResponseTransformer;
import com.synopsys.integration.blackduck.http.transform.BlackDuckResponsesTransformer;
import com.synopsys.integration.blackduck.installer.exception.BlackDuckInstallerException;
import com.synopsys.integration.blackduck.installer.model.BlackDuckConfigurationOptions;
import com.synopsys.integration.blackduck.service.BlackDuckApiClient;
import com.synopsys.integration.blackduck.service.BlackDuckServicesFactory;
import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.log.IntLogger;
import com.synopsys.integration.rest.HttpMethod;
import com.synopsys.integration.rest.body.StringBodyContent;
import com.synopsys.integration.rest.request.Request;

public class BlackDuckConfigureService {
    public static final BlackDuckPath ENDUSERLICENSEAGREEMENT_PATH = new BlackDuckPath("/api/enduserlicenseagreement");
    public static final String TOKEN_NAME = "installer_token";

    private final IntLogger intLogger;
    private final BlackDuckServerConfig blackDuckServerConfig;
    private final int installTimeoutInSeconds;
    private final BlackDuckConfigurationOptions blackDuckConfigurationOptions;

    public BlackDuckConfigureService(IntLogger intLogger, BlackDuckServerConfig blackDuckServerConfig, int installTimeoutInSeconds, BlackDuckConfigurationOptions blackDuckConfigurationOptions) {
        this.intLogger = intLogger;
        this.blackDuckServerConfig = blackDuckServerConfig;
        this.installTimeoutInSeconds = installTimeoutInSeconds;
        this.blackDuckConfigurationOptions = blackDuckConfigurationOptions;
    }

    public ConfigureResult configureBlackDuck() throws IntegrationException, IOException {
        BlackDuckServicesFactory blackDuckServicesFactory = blackDuckServerConfig.createBlackDuckServicesFactory(intLogger);
        Gson gson = blackDuckServicesFactory.getGson();
        ObjectMapper objectMapper = blackDuckServicesFactory.getObjectMapper();
        BlackDuckHttpClient blackDuckHttpClient = blackDuckServicesFactory.getBlackDuckHttpClient();
        BlackDuckApiClient blackDuckApiClient = blackDuckServicesFactory.getBlackDuckApiClient();

        BlackDuckJsonTransformer blackDuckJsonTransformer = new BlackDuckJsonTransformer(gson, objectMapper, intLogger);
        BlackDuckResponseTransformer blackDuckResponseTransformer = new BlackDuckResponseTransformer(blackDuckHttpClient, blackDuckJsonTransformer);
        BlackDuckResponsesTransformer blackDuckResponsesTransformer = new BlackDuckResponsesTransformer(blackDuckHttpClient, blackDuckJsonTransformer);
        BlackDuckRequestFactory blackDuckRequestFactory = new BlackDuckRequestFactory();

        if (StringUtils.isNotBlank(blackDuckConfigurationOptions.getRegistrationKey())) {
            applyRegistrationId(blackDuckConfigurationOptions.getRegistrationKey(), blackDuckApiClient);
        }

        if (blackDuckConfigurationOptions.isAcceptEula()) {
            acceptEndUserLicenseAgreement(blackDuckApiClient);
        }

        ConfigureResult configureResult = new ConfigureResult(true);
        if (blackDuckConfigurationOptions.isCreateApiToken()) {
            try {
                ApiTokenService apiTokenService = new ApiTokenService(blackDuckHttpClient, gson, blackDuckJsonTransformer, blackDuckResponseTransformer, blackDuckResponsesTransformer, blackDuckRequestFactory);
                Optional<ApiTokenView> apiTokenView = apiTokenService.getExistingApiToken(TOKEN_NAME);
                if (apiTokenView.isPresent()) {
                    intLogger.warn(String.format("A token named %s already exists - no new token will be created.", TOKEN_NAME));
                } else {
                    ApiTokenView createdApiTokenView = apiTokenService.createApiToken(TOKEN_NAME);
                    configureResult = new ConfigureResult(true, createdApiTokenView.getToken());
                }
            } catch (MalformedURLException e) {
                throw new BlackDuckInstallerException("Could not configure the apiTokenService because of a badly formed URL." + e.getMessage());
            }
        }

        return configureResult;
    }

    public void acceptEndUserLicenseAgreement(BlackDuckApiClient blackDuckApiClient) throws IntegrationException {
        intLogger.info("Attempting to accept the end user license agreement...");
        EndUserLicenseAgreementAction endUserLicenseAgreementAction = new EndUserLicenseAgreementAction();
        endUserLicenseAgreementAction.setAccept(true);
        endUserLicenseAgreementAction.setAcceptEndUserLicense(true);
        String json = blackDuckApiClient.convertToJson(endUserLicenseAgreementAction);

        BlackDuckRequestBuilder blackDuckRequestBuilder = new BlackDuckRequestBuilder(new Request.Builder());
        blackDuckRequestBuilder.bodyContent(new StringBodyContent(json));
        blackDuckRequestBuilder.method(HttpMethod.POST);

        blackDuckApiClient.execute(ENDUSERLICENSEAGREEMENT_PATH, blackDuckRequestBuilder);
        intLogger.info("Successfully accepted the end user license agreement.");
    }

    public Optional<String> createApiToken(ApiTokenService apiTokenService) throws IOException, IntegrationException {
        intLogger.info("Attempting to create an api token...");
        Optional<ApiTokenView> existingToken = apiTokenService.getExistingApiToken(TOKEN_NAME);
        if (existingToken.isPresent()) {
            intLogger.warn(String.format("An api token named %s already exists - no new token will be created.", TOKEN_NAME));
            return Optional.empty();
        } else {
            ApiTokenView apiTokenView = apiTokenService.createApiToken(TOKEN_NAME);
            intLogger.info("Successfully created an api token.");
            return Optional.of(apiTokenView.getToken());
        }
    }

    public void applyRegistrationId(String registrationId, BlackDuckApiClient blackDuckApiClient) throws IntegrationException {
        intLogger.info("Attempting to update the registration id...");
        try {
            RegistrationView registrationView = blackDuckApiClient.getResponse(ApiDiscovery.REGISTRATION_LINK_RESPONSE);
            if (!registrationView.getRegistrationId().equals(registrationId)) {
                intLogger.info("Attempting to change the registration id from " + registrationView.getRegistrationId() + " to " + registrationId + "...");
                registrationView.setRegistrationId(registrationId);
                blackDuckApiClient.put(registrationView);
            }
        } catch (IntegrationException e) {
            intLogger.info("No previous registration was found - attempting to create one...");
            // no previous registration could be found
            RegistrationRequest registrationRequest = new RegistrationRequest();
            registrationRequest.setRegistrationId(registrationId);

            blackDuckApiClient.post(ApiDiscovery.REGISTRATION_LINK, registrationRequest);
        }

        RegistrationView registrationView = blackDuckApiClient.getResponse(ApiDiscovery.REGISTRATION_LINK_RESPONSE);
        if (registrationView.getRegistrationId().equals(registrationId)) {
            intLogger.info("Successfully set the registration id to " + registrationId + ".");
        }
    }

}
