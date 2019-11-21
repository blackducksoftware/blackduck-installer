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

import com.google.gson.Gson;
import com.synopsys.integration.blackduck.api.core.BlackDuckPath;
import com.synopsys.integration.blackduck.api.core.BlackDuckPathMultipleResponses;
import com.synopsys.integration.blackduck.rest.BlackDuckHttpClient;
import com.synopsys.integration.blackduck.service.BlackDuckJsonTransformer;
import com.synopsys.integration.blackduck.service.BlackDuckResponseTransformer;
import com.synopsys.integration.blackduck.service.BlackDuckResponsesTransformer;
import com.synopsys.integration.blackduck.service.BlackDuckService;
import com.synopsys.integration.blackduck.service.model.RequestFactory;
import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.rest.request.Request;
import com.synopsys.integration.rest.request.Response;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

public class ApiTokenService extends BlackDuckService {
    public static final BlackDuckPath API_TOKEN_LINK = new BlackDuckPath("/api/current-user/tokens");
    public static final BlackDuckPathMultipleResponses<ApiTokenView> API_TOKEN_LINK_RESPONSE = new BlackDuckPathMultipleResponses<>(API_TOKEN_LINK, ApiTokenView.class);

    private final URL baseUrl;

    public ApiTokenService(BlackDuckHttpClient blackDuckHttpClient, Gson gson, BlackDuckJsonTransformer blackDuckJsonTransformer, BlackDuckResponseTransformer blackDuckResponseTransformer, BlackDuckResponsesTransformer blackDuckResponsesTransformer) throws MalformedURLException {
        super(blackDuckHttpClient, gson, blackDuckJsonTransformer, blackDuckResponseTransformer, blackDuckResponsesTransformer);

        baseUrl = new URL(blackDuckHttpClient.getBaseUrl());
    }

    public ApiTokenView createApiToken(String tokenName) throws IOException, IntegrationException {
        String createApiTokenUrl = new URL(baseUrl, API_TOKEN_LINK.getPath()).toString();

        ApiTokenRequest apiTokenRequest = ApiTokenRequest.CREATE_READ_WRITE(tokenName);

        ApiTokenView apiTokenView;
        String json = convertToJson(apiTokenRequest);
        Request request = RequestFactory.createCommonPostRequestBuilder(json).uri(createApiTokenUrl).build();
        try (Response response = execute(request)) {
            // ekerwin 2019-11-15 We have to get the token from the initial response from the POST, otherwise it is null.
            apiTokenView = transformResponse(response, ApiTokenView.class);
        }

        return apiTokenView;
    }

}
