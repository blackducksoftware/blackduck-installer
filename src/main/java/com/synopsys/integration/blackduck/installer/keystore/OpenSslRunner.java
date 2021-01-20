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

import com.synopsys.integration.blackduck.installer.exception.BlackDuckInstallerException;
import com.synopsys.integration.blackduck.installer.model.ExecutablesRunner;
import com.synopsys.integration.executable.Executable;
import com.synopsys.integration.executable.ExecutableOutput;
import com.synopsys.integration.log.IntLogger;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class OpenSslRunner {
    private final IntLogger intLogger;
    private final ExecutablesRunner executablesRunner;
    private final OpenSslOutputParser openSslOutputParser;

    public OpenSslRunner(IntLogger intLogger, ExecutablesRunner executablesRunner, OpenSslOutputParser openSslOutputParser) {
        this.intLogger = intLogger;
        this.executablesRunner = executablesRunner;
        this.openSslOutputParser = openSslOutputParser;
    }

    public String createCertificateContents(String host, int port) throws BlackDuckInstallerException {
        List<String> commands = new ArrayList<>();
        commands.add("/bin/sh");
        commands.add("-c");
        commands.add(String.format("\"Q\" | openssl s_client -connect %s:%s", host, port));
        Executable executable = Executable.create(new File("."), commands);
        ExecutableOutput executableOutput = executablesRunner.runExecutable(executable);
        String certificateContent = openSslOutputParser.parseCertificateOutput(executableOutput.getStandardOutput());
        intLogger.info("Certificate content:\n" + certificateContent);

        return certificateContent;
    }

}
