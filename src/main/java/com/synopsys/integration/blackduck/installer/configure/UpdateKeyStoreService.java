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

import com.synopsys.integration.blackduck.installer.exception.BlackDuckInstallerException;
import com.synopsys.integration.blackduck.installer.exception.IntegrationKeyStoreException;
import com.synopsys.integration.blackduck.installer.keystore.CertificateRequest;
import com.synopsys.integration.blackduck.installer.keystore.KeyStoreManager;
import com.synopsys.integration.blackduck.installer.keystore.KeyStoreRequest;
import com.synopsys.integration.blackduck.installer.keystore.OpenSslRunner;
import com.synopsys.integration.log.IntLogger;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.KeyStore;
import java.security.KeyStoreException;

public class UpdateKeyStoreService {
    private final IntLogger intLogger;
    private final KeyStoreManager keyStoreManager;
    private final KeyStoreRequest keyStoreRequest;
    private final boolean keyStoreUpdate;
    private final boolean keyStoreUpdateForce;
    private final String blackDuckHost;
    private final int blackDuckPort;
    private final OpenSslRunner openSslRunner;

    public UpdateKeyStoreService(IntLogger intLogger, KeyStoreManager keyStoreManager, KeyStoreRequest keyStoreRequest, boolean keyStoreUpdate, boolean keyStoreUpdateForce, String blackDuckHost, int blackDuckPort, OpenSslRunner openSslRunner) {
        this.intLogger = intLogger;
        this.keyStoreManager = keyStoreManager;
        this.keyStoreRequest = keyStoreRequest;
        this.keyStoreUpdate = keyStoreUpdate;
        this.keyStoreUpdateForce = keyStoreUpdateForce;
        this.blackDuckHost = blackDuckHost;
        this.blackDuckPort = blackDuckPort;
        this.openSslRunner = openSslRunner;
    }

    public boolean canAttemptKeyStoreUpdate() {
        return keyStoreUpdate;
    }

    public boolean updateKeyStoreWithBlackDuckCertificate(File installDirectory) throws BlackDuckInstallerException, IntegrationKeyStoreException {
        if (!keyStoreUpdate) {
            intLogger.warn("The keystore can not be automatically updated unless update.keystore=true.");
            return false;
        }

        String alias = blackDuckHost + "_blackduck";
        KeyStore keyStore = keyStoreManager.createKeyStore(keyStoreRequest);
        try {
            if (keyStore.containsAlias(alias)) {
                if (!keyStoreUpdateForce) {
                    intLogger.error(String.format("The keystore already has an entry for the alias %s - if this is an outdated entry, you can set update.keystore.force=true.", alias));
                    return false;
                } else {
                    intLogger.info(String.format("The keystore already has an entry for the alias %s, but since update.keystore.force=true, it will be replaced.", alias));
                    keyStore.deleteEntry(alias);
                }
            }
        } catch (KeyStoreException e) {
            throw new IntegrationKeyStoreException(String.format("Could not check the keystore for alias %s: %s", alias, e.getMessage()), e);
        }

        String certificateContents = openSslRunner.createCertificateContents(blackDuckHost, blackDuckPort);
        File certificateFile = new File(installDirectory, String.format("%s_blackduck_cert.pem", blackDuckHost));
        try {
            FileUtils.write(certificateFile, certificateContents, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new IntegrationKeyStoreException("Could not write the certificate file: " + e.getMessage(), e);
        }

        CertificateRequest certificateRequest = new CertificateRequest(certificateFile, alias, "X.509");
        keyStoreManager.addCertificateToKeyStore(keyStore, keyStoreRequest, certificateRequest);
        return true;
    }

}
