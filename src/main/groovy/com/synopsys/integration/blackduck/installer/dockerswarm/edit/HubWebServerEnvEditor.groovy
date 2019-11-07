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
package com.synopsys.integration.blackduck.installer.dockerswarm.edit


import com.synopsys.integration.blackduck.installer.hash.HashUtility
import com.synopsys.integration.blackduck.installer.hash.PreComputedHashes
import com.synopsys.integration.log.IntLogger

class HubWebServerEnvEditor extends PropertyFileEditor {
    public static final String WEBSERVER_HOST_KEY = 'PUBLIC_HUB_WEBSERVER_HOST='
    public static final String USE_ALERT_KEY = 'USE_ALERT='

    def tokensToEdit = [:]

    HubWebServerEnvEditor(IntLogger logger, HashUtility hashUtility, String lineSeparator, HubWebServerEnvTokens hubWebServerEnvTokens) {
        super(logger, hashUtility, lineSeparator)

        addTokenIfApplicable(tokensToEdit, WEBSERVER_HOST_KEY, hubWebServerEnvTokens.webServerHost)
        addTokenIfApplicable(tokensToEdit, USE_ALERT_KEY, hubWebServerEnvTokens.useAlert ? 1 : 0)
    }

    String getFilename() {
        'hub-webserver.env'
    }

    String getComputedHash() {
        PreComputedHashes.HUB_WEBSERVER_ENV
    }

    void edit(File installDirectory) {
        ConfigFile configFile = createConfigFile(installDirectory)

        configFile.fileToEdit.withWriter { w ->
            configFile.originalCopy.eachLine { line ->
                editLine(w, tokensToEdit, line)
            }
        }
    }

}
