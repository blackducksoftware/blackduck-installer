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
package com.synopsys.integration.blackduck.installer.hash;

import java.util.HashSet;
import java.util.Set;

public class PreComputedHashes {
    //alert 5.0.0
    public static final String ALERT_DOCKER_COMPOSE_LOCAL_OVERRIDES_YML_5_0_0 = "57c73ab627cdf6e9e791390626bda0b8fe5588262f455564e7a15c8e0b1fed20";

    //alert 5.0.1
    public static final String ALERT_DOCKER_COMPOSE_LOCAL_OVERRIDES_YML_5_0_1 = "57c73ab627cdf6e9e791390626bda0b8fe5588262f455564e7a15c8e0b1fed20";

    //alert 5.1.0
    public static final String ALERT_DOCKER_COMPOSE_LOCAL_OVERRIDES_YML_5_1_0 = "2850f36c29f36b75ef68b0b760500a00c7d88bd9b0b42d67ed2c0f03c4ed3754";

    //alert 5.2.0
    public static final String ALERT_DOCKER_COMPOSE_LOCAL_OVERRIDES_YML_5_2_0 = "17b30f5a66b7e048b9f8227d2b5aea3d636587f795c66f33a65bfe3cd26a88a4";

    //blackduck 2019.8.1
    public static final String BLACKDUCK_CONFIG_ENV_2019_8_1 = "65bd2a53f058b2822990f71162dfb91d3e5a0afb70f15dbcf4497f80cf55c513";
    public static final String DOCKER_COMPOSE_LOCAL_OVERRIDES_YML_2019_8_1 = "cc8ee040db78cfa3416bc1f1d5ba8e10a0377446f0cc7ff4a9e5ea8b21182359";
    public static final String HUB_WEBSERVER_ENV_2019_8_1 = "dc0f2bde00cc96a5f5cb796875a21d17c25b9aff5ff62b48637a2c70bebe9f70";

    //blackduck 2019.10.0
    public static final String BLACKDUCK_CONFIG_ENV_2019_10_1 = "c3ed57ee811aa41159ef5db147767072894f92bab5f3130974881f3cdae212a0";
    public static final String DOCKER_COMPOSE_LOCAL_OVERRIDES_YML_2019_10_1 = "cc8ee040db78cfa3416bc1f1d5ba8e10a0377446f0cc7ff4a9e5ea8b21182359";
    public static final String HUB_WEBSERVER_ENV_2019_10_1 = "dc0f2bde00cc96a5f5cb796875a21d17c25b9aff5ff62b48637a2c70bebe9f70";

    //blackduck 2019.10.1
    public static final String BLACKDUCK_CONFIG_ENV_2019_10_0 = "312f3b10c1851582ab3c6bcd67a5402ac958ffce52b7cd0fc7ed79c18b67d032";
    public static final String DOCKER_COMPOSE_LOCAL_OVERRIDES_YML_2019_10_0 = "cc8ee040db78cfa3416bc1f1d5ba8e10a0377446f0cc7ff4a9e5ea8b21182359";
    public static final String HUB_WEBSERVER_ENV_2019_10_0 = "dc0f2bde00cc96a5f5cb796875a21d17c25b9aff5ff62b48637a2c70bebe9f70";

    //blackduck 2019.12.0
    public static final String BLACKDUCK_CONFIG_ENV_2019_12_0 = "d76b15b344b207755b27ee7972355534bb4a8bf14cfb8b2805000427e8efed60";
    public static final String DOCKER_COMPOSE_LOCAL_OVERRIDES_YML_2019_12_0 = "cc8ee040db78cfa3416bc1f1d5ba8e10a0377446f0cc7ff4a9e5ea8b21182359";
    public static final String HUB_WEBSERVER_ENV_2019_12_0 = "dc0f2bde00cc96a5f5cb796875a21d17c25b9aff5ff62b48637a2c70bebe9f70";

    public static final Set<String> HUB_WEBSERVER_ENV = new HashSet<>();
    public static final Set<String> DOCKER_COMPOSE_LOCAL_OVERRIDES_YML = new HashSet<>();
    public static final Set<String> BLACKDUCK_CONFIG_ENV = new HashSet<>();

    public static final Set<String> ALERT_DOCKER_COMPOSE_LOCAL_OVERRIDES_YML = new HashSet<>();

    static {
        HUB_WEBSERVER_ENV.add(HUB_WEBSERVER_ENV_2019_8_1);
        HUB_WEBSERVER_ENV.add(HUB_WEBSERVER_ENV_2019_10_0);
        HUB_WEBSERVER_ENV.add(HUB_WEBSERVER_ENV_2019_10_1);
        HUB_WEBSERVER_ENV.add(HUB_WEBSERVER_ENV_2019_12_0);

        DOCKER_COMPOSE_LOCAL_OVERRIDES_YML.add(DOCKER_COMPOSE_LOCAL_OVERRIDES_YML_2019_8_1);
        DOCKER_COMPOSE_LOCAL_OVERRIDES_YML.add(DOCKER_COMPOSE_LOCAL_OVERRIDES_YML_2019_10_0);
        DOCKER_COMPOSE_LOCAL_OVERRIDES_YML.add(DOCKER_COMPOSE_LOCAL_OVERRIDES_YML_2019_10_1);
        DOCKER_COMPOSE_LOCAL_OVERRIDES_YML.add(DOCKER_COMPOSE_LOCAL_OVERRIDES_YML_2019_12_0);

        BLACKDUCK_CONFIG_ENV.add(BLACKDUCK_CONFIG_ENV_2019_8_1);
        BLACKDUCK_CONFIG_ENV.add(BLACKDUCK_CONFIG_ENV_2019_10_0);
        BLACKDUCK_CONFIG_ENV.add(BLACKDUCK_CONFIG_ENV_2019_10_1);
        BLACKDUCK_CONFIG_ENV.add(BLACKDUCK_CONFIG_ENV_2019_12_0);

        ALERT_DOCKER_COMPOSE_LOCAL_OVERRIDES_YML.add(ALERT_DOCKER_COMPOSE_LOCAL_OVERRIDES_YML_5_0_0);
        ALERT_DOCKER_COMPOSE_LOCAL_OVERRIDES_YML.add(ALERT_DOCKER_COMPOSE_LOCAL_OVERRIDES_YML_5_0_1);
        ALERT_DOCKER_COMPOSE_LOCAL_OVERRIDES_YML.add(ALERT_DOCKER_COMPOSE_LOCAL_OVERRIDES_YML_5_1_0);
        ALERT_DOCKER_COMPOSE_LOCAL_OVERRIDES_YML.add(ALERT_DOCKER_COMPOSE_LOCAL_OVERRIDES_YML_5_2_0);
    }

}
