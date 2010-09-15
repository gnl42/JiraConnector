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

package com.atlassian.connector.eclipse.internal.crucible.core.client;

import com.atlassian.connector.eclipse.internal.core.client.RemoteSessionOperation;
import com.atlassian.theplugin.commons.crucible.api.CrucibleSession;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.mylyn.tasks.core.TaskRepository;

public abstract class CrucibleRemoteSessionOperation<T> extends RemoteSessionOperation<T, CrucibleSession> {

	public CrucibleRemoteSessionOperation(IProgressMonitor monitor, TaskRepository taskRepository) {
		super(monitor, taskRepository);
	}

}
