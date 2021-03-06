/*
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

package org.apache.sshd.git;

import java.util.concurrent.ExecutorService;

import org.apache.sshd.common.util.GenericUtils;
import org.apache.sshd.common.util.ValidateUtils;
import org.apache.sshd.common.util.threads.ExecutorServiceCarrier;
import org.apache.sshd.server.command.AbstractDelegatingCommandFactory;
import org.apache.sshd.server.command.Command;
import org.apache.sshd.server.command.CommandFactory;
import org.apache.sshd.server.shell.UnknownCommand;

/**
 * TODO Add javadoc
 *
 * @author <a href="mailto:dev@mina.apache.org">Apache MINA SSHD Project</a>
 */
public abstract class AbstractGitCommandFactory
        extends AbstractDelegatingCommandFactory
        implements ExecutorServiceCarrier, GitLocationResolverCarrier {
    private final String cmdPrefix;
    private GitLocationResolver rootDirResolver;
    private ExecutorService executorService;
    private boolean shutdownOnExit;

    /**
     * @param name Command factory logical name
     * @param cmdPrefix The command prefix used to detect and intercept GIT commands handled by this
     * factory (never {@code null}/empty)
     */
    protected AbstractGitCommandFactory(String name, String cmdPrefix) {
        super(name);

        this.cmdPrefix = ValidateUtils.checkNotNullAndNotEmpty(cmdPrefix, "No command prefix provided");
    }

    public String getCommandPrefix() {
        return cmdPrefix;
    }

    @Override
    public ExecutorService getExecutorService() {
        return executorService;
    }

    @Override
    public boolean isShutdownOnExit() {
        return shutdownOnExit;
    }

    public AbstractGitCommandFactory withExecutorService(ExecutorService executorService) {
        this.executorService = executorService;
        return this;
    }

    public AbstractGitCommandFactory withShutdownOnExit(boolean shutdownOnExit) {
        this.shutdownOnExit = shutdownOnExit;
        return this;
    }

    @Override
    public GitLocationResolver getGitLocationResolver() {
        return rootDirResolver;
    }

    public AbstractGitCommandFactory withGitLocationResolver(GitLocationResolver rootDirResolver) {
        this.rootDirResolver = rootDirResolver;
        return this;
    }

    public AbstractGitCommandFactory withDelegate(CommandFactory delegate) {
        setDelegateCommandFactory(delegate);
        return this;
    }

    @Override
    public boolean isSupportedCommand(String command) {
        if (GenericUtils.isEmpty(command)) {
            return false;
        }

        String prefix = getCommandPrefix();
        return command.startsWith(prefix);
    }

    @Override
    protected Command executeSupportedCommand(String command) {
        return createGitCommand(command);
    }

    @Override
    protected Command createUnsupportedCommand(String command) {
        return new UnknownCommand(command);
    }

    protected abstract AbstractGitCommand createGitCommand(String command);
}
