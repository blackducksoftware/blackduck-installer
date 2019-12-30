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
package com.synopsys.integration.blackduck.installer.download;

import com.synopsys.integration.log.IntLogger;
import com.synopsys.integration.rest.client.IntHttpClient;
import com.synopsys.integration.rest.proxy.ProxyInfo;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.impl.client.HttpClientBuilder;

// this will avoid the Invalid cookie header warning in the logs
// https://stackoverflow.com/questions/36473478/fixing-httpclient-warning-invalid-expires-attribute-using-fluent-api/40697322
// https://issues.apache.org/jira/browse/HTTPCLIENT-1763
public class StandardCookieSpecHttpClient extends IntHttpClient {
    public StandardCookieSpecHttpClient(IntLogger logger, int timeoutInSeconds, boolean alwaysTrustServerCertificate, ProxyInfo proxyInfo) {
        super(logger, timeoutInSeconds, alwaysTrustServerCertificate, proxyInfo);
    }

    @Override
    public void populateHttpClientBuilder(HttpClientBuilder httpClientBuilder, RequestConfig.Builder defaultRequestConfigBuilder) {
        super.populateHttpClientBuilder(httpClientBuilder, defaultRequestConfigBuilder);

        defaultRequestConfigBuilder.setCookieSpec(CookieSpecs.STANDARD);
    }

}
