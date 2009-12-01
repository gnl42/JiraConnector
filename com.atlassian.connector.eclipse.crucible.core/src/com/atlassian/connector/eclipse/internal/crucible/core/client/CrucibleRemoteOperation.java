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

import com.atlassian.connector.commons.crucible.CrucibleServerFacade2;
import com.atlassian.connector.eclipse.internal.core.client.RemoteOperation;
import com.atlassian.connector.eclipse.internal.crucible.core.CrucibleUtil;
import com.atlassian.theplugin.commons.crucible.api.model.Review;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.jetbrains.annotations.Nullable;

public abstract class CrucibleRemoteOperation<T> extends RemoteOperation<T, CrucibleServerFacade2> {

	public CrucibleRemoteOperation(IProgressMonitor monitor, TaskRepository taskRepository) {
		super(monitor, taskRepository);
	}

	@Nullable
	protected String getTaskId(@Nullable Review review) {
		if (review == null) {
			return null;
		}
		return CrucibleUtil.getTaskIdFromPermId(review.getPermId().getId());
	}

}