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

package me.glindholm.connector.eclipse.internal.bamboo.core.client;

import me.glindholm.connector.eclipse.internal.core.client.RemoteSessionOperation;
import me.glindholm.theplugin.commons.bamboo.api.BambooSession;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.mylyn.tasks.core.TaskRepository;

public abstract class BambooRemoteSessionOperation<T> extends RemoteSessionOperation<T, BambooSession> {

	public BambooRemoteSessionOperation(IProgressMonitor monitor, TaskRepository taskRepository) {
		super(monitor, taskRepository);
	}
}