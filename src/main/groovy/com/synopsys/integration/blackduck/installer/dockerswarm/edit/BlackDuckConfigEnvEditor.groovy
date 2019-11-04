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

class BlackDuckConfigEnvEditor extends PropertyFileEditor {
    public static final String HUB_PROXY_HOST_KEY = 'HUB_PROXY_HOST='
    public static final String HUB_PROXY_PORT_KEY = 'HUB_PROXY_PORT='
    public static final String HUB_PROXY_SCHEME_KEY = 'HUB_PROXY_SCHEME='
    public static final String HUB_PROXY_USER_KEY = 'HUB_PROXY_USER='
    public static final String HUB_KB_HOST_KEY = 'HUB_KB_HOST='

    def tokensToAdd = [:]
    def tokensToEdit = [:]

    BlackDuckConfigEnvEditor(IntLogger logger, HashUtility hashUtility, String lineSeparator, String proxyHost, int proxyPort, String proxyScheme, String proxyUser, String customKbHost) {
        super(logger, hashUtility, lineSeparator)

        addTokenIfApplicable(tokensToAdd, HUB_KB_HOST_KEY, customKbHost)

        addTokenIfApplicable(tokensToEdit, HUB_PROXY_HOST_KEY, proxyHost)
        addTokenIfApplicable(tokensToEdit, HUB_PROXY_PORT_KEY, proxyPort)
        addTokenIfApplicable(tokensToEdit, HUB_PROXY_SCHEME_KEY, proxyScheme)
        addTokenIfApplicable(tokensToEdit, HUB_PROXY_USER_KEY, proxyUser)
    }

    String getFilename() {
        'blackduck-config.env'
    }

    String getComputedHash() {
        PreComputedHashes.BLACKDUCK_CONFIG_ENV
    }

    void edit(File installDirectory) {
        ConfigFile configFile = createConfigFile(installDirectory)

        configFile.fileToEdit.withWriter { w ->
            tokensToAdd.each { key, value ->
                writeLine(w, key, value)
            }
            writeBlank(w)

            configFile.originalCopy.eachLine { line ->
                editLine(w, tokensToEdit, line)
            }
        }
    }

}
