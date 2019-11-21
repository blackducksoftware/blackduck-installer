/*
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
package com.synopsys.integration.blackduck.installer.configure

import com.synopsys.integration.blackduck.api.generated.component.EndUserLicenseAgreementAction
import com.synopsys.integration.blackduck.api.generated.component.RegistrationRequest
import com.synopsys.integration.blackduck.api.generated.discovery.ApiDiscovery
import com.synopsys.integration.blackduck.api.generated.response.CurrentVersionView
import com.synopsys.integration.blackduck.api.generated.view.RegistrationView
import com.synopsys.integration.blackduck.installer.model.BlackDuckConfigurationOptions
import com.synopsys.integration.blackduck.service.BlackDuckService
import com.synopsys.integration.exception.IntegrationException
import com.synopsys.integration.log.IntLogger
import org.apache.commons.lang3.StringUtils
import org.apache.commons.lang3.time.DurationFormatUtils

import java.time.Duration

class BlackDuckConfigureService {
    IntLogger intLogger
    BlackDuckService blackDuckService
    ApiTokenService apiTokenService
    int installTimeoutInSeconds
    BlackDuckConfigurationOptions blackDuckConfigurationOptions
    BlackDuckWait blackDuckWait

    BlackDuckConfigureService(IntLogger intLogger, BlackDuckService blackDuckService, ApiTokenService apiTokenService, int installTimeoutInSeconds, BlackDuckConfigurationOptions blackDuckConfigurationOptions, BlackDuckWait blackDuckWait) {
        this.intLogger = intLogger
        this.blackDuckService = blackDuckService
        this.apiTokenService = apiTokenService
        this.installTimeoutInSeconds = installTimeoutInSeconds
        this.blackDuckConfigurationOptions = blackDuckConfigurationOptions
        this.blackDuckWait = blackDuckWait
    }

    ConfigureResult configureBlackDuck() {
        blackDuckWait.waitForBlackDuck()

        if (StringUtils.isNotBlank(blackDuckConfigurationOptions.registrationKey)) {
            applyRegistrationId(blackDuckConfigurationOptions.registrationKey)
        }

        if (blackDuckConfigurationOptions.acceptEula) {
            acceptEndUserLicenseAgreement()
        }

        String apiToken
        if (blackDuckConfigurationOptions.createApiToken) {
            apiToken = createApiToken()
        }

        return new ConfigureResult(true, apiToken)
    }

    void acceptEndUserLicenseAgreement() {
        intLogger.info('Attempting to accept the end user license agreement...')
        def endUserLicenseAgreementAction = new EndUserLicenseAgreementAction()
        endUserLicenseAgreementAction.accept = true
        endUserLicenseAgreementAction.acceptEndUserLicense = true

        blackDuckService.post(ApiDiscovery.ENDUSERLICENSEAGREEMENT_LINK, endUserLicenseAgreementAction)
        intLogger.info('Successfully accepted the end user license agreement.')
    }

    String createApiToken() {
        intLogger.info('Attempting to create an api token...')
        ApiTokenView apiTokenView = apiTokenService.createApiToken("installer_token")
        intLogger.info('Successfully created an api token.')

        apiTokenView.token
    }

    void applyRegistrationId(String registrationId) {
        intLogger.info('Attempting to update the registration id...')
        try {
            RegistrationView registrationView = blackDuckService.getResponse(ApiDiscovery.REGISTRATION_LINK_RESPONSE)
            if (!registrationView.registrationId.equals(registrationId)) {
                intLogger.info("Attempting to change the registration id from ${registrationView.registrationId} to ${registrationId}...")
                registrationView.registrationId = registrationId
                blackDuckService.put(registrationView)
            }
        } catch (IntegrationException e) {
            intLogger.info('No previous registration was found - attempting to create one...')
            // no previous registration could be found
            def registrationRequest = new RegistrationRequest()
            registrationRequest.registrationId = registrationId

            blackDuckService.post(ApiDiscovery.REGISTRATION_LINK, registrationRequest)
        }

        RegistrationView registrationView = blackDuckService.getResponse(ApiDiscovery.REGISTRATION_LINK_RESPONSE)
        if (registrationView.registrationId.equals(registrationId)) {
            intLogger.info("Successfully set the registration id to ${registrationId}.")
        }

    }

}
