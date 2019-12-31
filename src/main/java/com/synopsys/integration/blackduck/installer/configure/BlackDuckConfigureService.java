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
package com.synopsys.integration.blackduck.installer.configure;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.synopsys.integration.blackduck.api.generated.component.EndUserLicenseAgreementAction;
import com.synopsys.integration.blackduck.api.generated.component.RegistrationRequest;
import com.synopsys.integration.blackduck.api.generated.discovery.ApiDiscovery;
import com.synopsys.integration.blackduck.api.generated.view.RegistrationView;
import com.synopsys.integration.blackduck.configuration.BlackDuckServerConfig;
import com.synopsys.integration.blackduck.installer.exception.BlackDuckInstallerException;
import com.synopsys.integration.blackduck.installer.exception.IntegrationKeyStoreException;
import com.synopsys.integration.blackduck.installer.model.BlackDuckConfigurationOptions;
import com.synopsys.integration.blackduck.rest.BlackDuckHttpClient;
import com.synopsys.integration.blackduck.service.*;
import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.log.IntLogger;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;

public class BlackDuckConfigureService {
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
        BlackDuckService blackDuckService = blackDuckServicesFactory.createBlackDuckService();

        BlackDuckJsonTransformer blackDuckJsonTransformer = new BlackDuckJsonTransformer(gson, objectMapper, intLogger);
        BlackDuckResponseTransformer blackDuckResponseTransformer = new BlackDuckResponseTransformer(blackDuckHttpClient, blackDuckJsonTransformer);
        BlackDuckResponsesTransformer blackDuckResponsesTransformer = new BlackDuckResponsesTransformer(blackDuckHttpClient, blackDuckJsonTransformer);

        if (StringUtils.isNotBlank(blackDuckConfigurationOptions.getRegistrationKey())) {
            applyRegistrationId(blackDuckConfigurationOptions.getRegistrationKey(), blackDuckService);
        }

        if (blackDuckConfigurationOptions.isAcceptEula()) {
            acceptEndUserLicenseAgreement(blackDuckService);
        }

        String apiToken = null;
        if (blackDuckConfigurationOptions.isCreateApiToken()) {
            try {
                ApiTokenService apiTokenService = new ApiTokenService(blackDuckHttpClient, gson, blackDuckJsonTransformer, blackDuckResponseTransformer, blackDuckResponsesTransformer);
                apiToken = createApiToken(apiTokenService);
            } catch (MalformedURLException e) {
                throw new BlackDuckInstallerException("Could not configure the apiTokenService because of a badly formed URL." + e.getMessage());
            }
        }

        return new ConfigureResult(true, apiToken);
    }

    public void acceptEndUserLicenseAgreement(BlackDuckService blackDuckService) throws IntegrationException {
        intLogger.info("Attempting to accept the end user license agreement...");
        EndUserLicenseAgreementAction endUserLicenseAgreementAction = new EndUserLicenseAgreementAction();
        endUserLicenseAgreementAction.setAccept(true);
        endUserLicenseAgreementAction.setAcceptEndUserLicense(true);

        blackDuckService.post(ApiDiscovery.ENDUSERLICENSEAGREEMENT_LINK, endUserLicenseAgreementAction);
        intLogger.info("Successfully accepted the end user license agreement.");
    }

    public String createApiToken(ApiTokenService apiTokenService) throws IOException, IntegrationException {
        intLogger.info("Attempting to create an api token...");
        ApiTokenView apiTokenView = apiTokenService.createApiToken("installer_token");
        intLogger.info("Successfully created an api token.");

        return apiTokenView.getToken();
    }

    public void applyRegistrationId(final String registrationId, BlackDuckService blackDuckService) throws IntegrationException {
        intLogger.info("Attempting to update the registration id...");
        try {
            final RegistrationView registrationView = blackDuckService.getResponse(ApiDiscovery.REGISTRATION_LINK_RESPONSE);
            if (!registrationView.getRegistrationId().equals(registrationId)) {
                intLogger.info("Attempting to change the registration id from " + registrationView.getRegistrationId() + " to " + registrationId + "...");
                registrationView.setRegistrationId(registrationId);
                blackDuckService.put(registrationView);
            }
        } catch (IntegrationException e) {
            intLogger.info("No previous registration was found - attempting to create one...");
            // no previous registration could be found
            RegistrationRequest registrationRequest = new RegistrationRequest();
            registrationRequest.setRegistrationId(registrationId);

            blackDuckService.post(ApiDiscovery.REGISTRATION_LINK, registrationRequest);
        }

        RegistrationView registrationView = blackDuckService.getResponse(ApiDiscovery.REGISTRATION_LINK_RESPONSE);
        if (registrationView.getRegistrationId().equals(registrationId)) {
            intLogger.info("Successfully set the registration id to " + registrationId + ".");
        }
    }

}
