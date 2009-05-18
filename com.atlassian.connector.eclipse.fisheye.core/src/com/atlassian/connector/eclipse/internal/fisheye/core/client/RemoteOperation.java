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

package com.atlassian.connector.eclipse.internal.fisheye.core.client;

import com.atlassian.theplugin.commons.exception.ServerPasswordNotProvidedException;
import com.atlassian.theplugin.commons.remoteapi.RemoteApiException;
import com.atlassian.theplugin.commons.remoteapi.ServerData;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.mylyn.commons.net.Policy;
import org.eclipse.mylyn.tasks.core.TaskRepository;

public abstract class RemoteOperation<T, S> {

	private final IProgressMonitor fMonitor;

	private final TaskRepository taskRepository;

	public RemoteOperation(IProgressMonitor monitor, TaskRepository taskRepository) {
		this.taskRepository = taskRepository;
		this.fMonitor = Policy.monitorFor(monitor);
	}

	public TaskRepository getTaskRepository() {
		return taskRepository;
	}

	public IProgressMonitor getMonitor() {
		return fMonitor;
	}

	public abstract T run(S server, ServerData serverData, IProgressMonitor monitor) throws RemoteApiException,
			ServerPasswordNotProvidedException;

}