/*******************************************************************************
 * Copyright (c) 2009 Atlassian and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Atlassian - initial API and implementation
 ******************************************************************************/

package me.glindholm.connector.eclipse.internal.core.client;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.mylyn.commons.net.Policy;
import org.eclipse.mylyn.tasks.core.TaskRepository;

import me.glindholm.connector.commons.api.ConnectionCfg;
import me.glindholm.theplugin.commons.exception.ServerPasswordNotProvidedException;
import me.glindholm.theplugin.commons.remoteapi.RemoteApiException;

public abstract class RemoteOperation<T, C> {

    private final IProgressMonitor fMonitor;

    private final TaskRepository taskRepository;

    public RemoteOperation(final IProgressMonitor monitor, final TaskRepository taskRepository) {
        fMonitor = Policy.monitorFor(monitor);
        this.taskRepository = taskRepository;
    }

    public IProgressMonitor getMonitor() {
        return fMonitor;
    }

    public abstract T run(C server, ConnectionCfg serverCfg, IProgressMonitor monitor) throws RemoteApiException, ServerPasswordNotProvidedException;

    public TaskRepository getTaskRepository() {
        return taskRepository;
    }

}