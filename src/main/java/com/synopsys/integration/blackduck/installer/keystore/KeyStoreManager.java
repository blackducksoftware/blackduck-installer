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
package com.synopsys.integration.blackduck.installer.keystore;

import com.synopsys.integration.blackduck.installer.exception.IntegrationKeyStoreException;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;

public class KeyStoreManager {
    public KeyStore createKeyStore(KeyStoreRequest keyStoreRequest) throws IntegrationKeyStoreException {
        KeyStore keystore;
        try {
            keystore = KeyStore.getInstance(keyStoreRequest.getKeyStoreType());
        } catch (KeyStoreException e) {
            throw new IntegrationKeyStoreException(String.format("Could not create the keystore at %s using type %s: %s", keyStoreRequest.getKeyStoreFile().getAbsolutePath(), keyStoreRequest.getKeyStoreType(), e.getMessage()), e);
        }

        try (FileInputStream keyStoreStream = new FileInputStream(keyStoreRequest.getKeyStoreFile())) {
            keystore.load(keyStoreStream, keyStoreRequest.getPassword());
        } catch (CertificateException | NoSuchAlgorithmException | IOException e) {
            throw new IntegrationKeyStoreException("Could not load the keystore contents - please ensure the password is correct: " + e.getMessage(), e);
        }

        return keystore;
    }

    public void addCertificateToKeyStore(KeyStore keyStore, KeyStoreRequest keyStoreRequest, CertificateRequest certificateRequest) throws IntegrationKeyStoreException {
        storeCertificate(keyStore, certificateRequest);

        saveKeyStore(keyStore, keyStoreRequest);
    }

    private void saveKeyStore(KeyStore keyStore, KeyStoreRequest keyStoreRequest) throws IntegrationKeyStoreException {
        try (FileOutputStream outputStream = new FileOutputStream(keyStoreRequest.getKeyStoreFile())) {
            keyStore.store(outputStream, keyStoreRequest.getPassword());
        } catch (IOException | CertificateException | NoSuchAlgorithmException | KeyStoreException e) {
            throw new IntegrationKeyStoreException(String.format("Could not save the keystore: " + e.getMessage()), e);
        }
    }

    private void storeCertificate(KeyStore keyStore, CertificateRequest certificateRequest) throws IntegrationKeyStoreException {
        CertificateFactory cf;
        try {
            cf = CertificateFactory.getInstance(certificateRequest.getCertificateType());
        } catch (CertificateException e) {
            throw new IntegrationKeyStoreException(String.format("Could not create a certificate factory for type %s: %s",certificateRequest.getCertificateType(), e.getMessage()), e);
        }

        try (InputStream certificateStream = new FileInputStream(certificateRequest.getCertificateFile())) {
            Certificate certs =  cf.generateCertificate(certificateStream);
            keyStore.setCertificateEntry(certificateRequest.getAlias(), certs);
        } catch (IOException | CertificateException | KeyStoreException e) {
            throw new IntegrationKeyStoreException(String.format("Could not store the certificate %s with alias %s in the keystore: %s", certificateRequest.getCertificateFile().getAbsolutePath(), certificateRequest.getAlias(), e.getMessage()), e);
        }
    }

}
