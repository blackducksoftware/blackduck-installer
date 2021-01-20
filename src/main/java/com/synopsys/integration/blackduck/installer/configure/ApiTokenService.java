/**
 * blackduck-installer
 *
 * Copyright (c) 2021 Synopsys, Inc.
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
import java.util.List;
import java.util.Optional;

import com.google.gson.Gson;
import com.synopsys.integration.blackduck.api.core.BlackDuckPath;
import com.synopsys.integration.blackduck.api.core.response.BlackDuckPathMultipleResponses;
import com.synopsys.integration.blackduck.http.BlackDuckRequestFactory;
import com.synopsys.integration.blackduck.http.client.BlackDuckHttpClient;
import com.synopsys.integration.blackduck.http.transform.BlackDuckJsonTransformer;
import com.synopsys.integration.blackduck.http.transform.BlackDuckResponseTransformer;
import com.synopsys.integration.blackduck.http.transform.BlackDuckResponsesTransformer;
import com.synopsys.integration.blackduck.service.BlackDuckApiClient;
import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.rest.HttpUrl;
import com.synopsys.integration.rest.request.Request;
import com.synopsys.integration.rest.response.Response;

public class ApiTokenService extends BlackDuckApiClient {
    public static final BlackDuckPath API_TOKEN_LINK = new BlackDuckPath("/api/current-user/tokens");
    public static final BlackDuckPathMultipleResponses<ApiTokenView> API_TOKEN_LINK_RESPONSE = new BlackDuckPathMultipleResponses<>(API_TOKEN_LINK, ApiTokenView.class);

    private final BlackDuckRequestFactory blackDuckRequestFactory;
    private final HttpUrl tokensUrl;

    public ApiTokenService(BlackDuckHttpClient blackDuckHttpClient, Gson gson, BlackDuckJsonTransformer blackDuckJsonTransformer, BlackDuckResponseTransformer blackDuckResponseTransformer,
        BlackDuckResponsesTransformer blackDuckResponsesTransformer, BlackDuckRequestFactory blackDuckRequestFactory) throws IntegrationException {
        super(blackDuckHttpClient, gson, blackDuckJsonTransformer, blackDuckResponseTransformer, blackDuckResponsesTransformer, blackDuckRequestFactory);

        this.blackDuckRequestFactory = blackDuckRequestFactory;
        tokensUrl = API_TOKEN_LINK.getFullBlackDuckUrl(blackDuckHttpClient.getBaseUrl());
    }

    public Optional<ApiTokenView> getExistingApiToken(String tokenName) throws IntegrationException {
        List<ApiTokenView> allApiTokens = getAllResponses(tokensUrl, ApiTokenView.class);
        return allApiTokens
                   .stream()
                   .filter(apiTokenView -> apiTokenView.getName().equals(tokenName))
                   .findAny();
    }

    public ApiTokenView createApiToken(String tokenName) throws IOException, IntegrationException {
        ApiTokenRequest apiTokenRequest = ApiTokenRequest.CREATE_READ_WRITE(tokenName);

        ApiTokenView apiTokenView;
        String json = convertToJson(apiTokenRequest);
        Request request = blackDuckRequestFactory.createCommonPostRequestBuilder(tokensUrl, json).build();
        try (Response response = execute(request)) {
            // ekerwin 2019-11-15 We have to get the token from the initial response from the POST, otherwise it is null.
            apiTokenView = transformResponse(response, ApiTokenView.class);
        }

        return apiTokenView;
    }

}
