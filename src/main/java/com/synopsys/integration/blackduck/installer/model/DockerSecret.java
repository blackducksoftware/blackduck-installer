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
package com.synopsys.integration.blackduck.installer.model;

public class DockerSecret {
    public static final String SECRET_CERT = "WEBSERVER_CUSTOM_CERT_FILE";
    public static final String SECRET_KEY = "WEBSERVER_CUSTOM_KEY_FILE";
    public static final String SECRET_ALERT_ENCRYPTION_PASSWORD = "ALERT_ENCRYPTION_PASSWORD";
    public static final String SECRET_ALERT_ENCRYPTION_GLOBAL_SALT = "ALERT_ENCRYPTION_GLOBAL_SALT";
    public static final String SECRET_ALERT_DB_USERNAME = "ALERT_DB_USERNAME";
    public static final String SECRET_ALERT_DB_PASSWORD = "ALERT_DB_PASSWORD";
    private final String label;
    private final String path;

    public DockerSecret(String label, String path) {
        this.label = label;
        this.path = path;
    }

    public static DockerSecret createCert(String secretPath) {
        return new DockerSecret(SECRET_CERT, secretPath);
    }

    public static DockerSecret createKey(String secretPath) {
        return new DockerSecret(SECRET_KEY, secretPath);
    }

    public static DockerSecret createAlertPassword(String secretPath) {
        return new DockerSecret(SECRET_ALERT_ENCRYPTION_PASSWORD, secretPath);
    }

    public static DockerSecret createAlertSalt(String secretPath) {
        return new DockerSecret(SECRET_ALERT_ENCRYPTION_GLOBAL_SALT, secretPath);
    }

    public static DockerSecret createAlertDBUser(String secretPath) {
        return new DockerSecret(SECRET_ALERT_DB_USERNAME, secretPath);
    }

    public static DockerSecret createAlertDBPassword(String secretPath) {
        return new DockerSecret(SECRET_ALERT_DB_PASSWORD, secretPath);
    }

    public String getLabel() {
        return label;
    }

    public String getPath() {
        return path;
    }

}
