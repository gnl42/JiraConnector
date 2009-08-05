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

import com.atlassian.connector.commons.fisheye.FishEyeServerFacade2;
import com.atlassian.connector.eclipse.internal.core.client.RemoteOperation;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.mylyn.tasks.core.TaskRepository;

public abstract class FishEyeRemoteOperation<T> extends RemoteOperation<T, FishEyeServerFacade2> {

	public FishEyeRemoteOperation(IProgressMonitor monitor, TaskRepository taskRepository) {
		super(monitor, taskRepository);
	}

}