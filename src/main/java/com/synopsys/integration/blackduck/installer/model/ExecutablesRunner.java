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

import com.synopsys.integration.blackduck.installer.exception.BlackDuckInstallerException;
import com.synopsys.integration.executable.Executable;
import com.synopsys.integration.executable.ExecutableOutput;
import com.synopsys.integration.executable.ExecutableRunner;
import com.synopsys.integration.executable.ExecutableRunnerException;

import java.util.List;

public class ExecutablesRunner {
    private ExecutableRunner executableRunner;

    public ExecutablesRunner(ExecutableRunner executableRunner) {
        this.executableRunner = executableRunner;
    }

    public int runExecutables(List<Executable> executables) throws BlackDuckInstallerException {
        int overallReturnCode = 0;
        for (Executable executable : executables) {
            overallReturnCode += runExecutableCode(executable);
        }

        return overallReturnCode;
    }

    public ExecutableOutput runExecutable(Executable executable) throws BlackDuckInstallerException {
        try {
            ExecutableOutput executableOutput = executableRunner.execute(executable);
            return executableOutput;
        } catch (ExecutableRunnerException e) {
            throw new BlackDuckInstallerException("Exception running executable: " + executable.getExecutableDescription(), e);
        }
    }

    public int runExecutableCode(Executable executable) throws BlackDuckInstallerException {
        return Math.abs(runExecutable(executable).getReturnCode());
    }

}
