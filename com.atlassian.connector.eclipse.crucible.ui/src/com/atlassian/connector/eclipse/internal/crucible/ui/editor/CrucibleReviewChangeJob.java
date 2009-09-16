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

package com.atlassian.connector.eclipse.internal.crucible.ui.editor;

import com.atlassian.connector.eclipse.internal.core.jobs.JobWithStatus;
import com.atlassian.connector.eclipse.internal.crucible.core.CrucibleCorePlugin;
import com.atlassian.connector.eclipse.internal.crucible.core.CrucibleRepositoryConnector;
import com.atlassian.connector.eclipse.internal.crucible.core.client.CrucibleClient;
import com.atlassian.connector.eclipse.internal.crucible.ui.CrucibleUiPlugin;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin;
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.eclipse.mylyn.tasks.core.sync.SynchronizationJob;

import java.util.Collections;

/**
 * Job to submit or retrieve changes for a review
 * 
 * @author Shawn Minto
 */
public abstract class CrucibleReviewChangeJob extends JobWithStatus {

	private final TaskRepository taskRepository;

	private final boolean waitForTaskListSync;

	protected CrucibleReviewChangeJob(String name, TaskRepository taskRepository) {
		this(name, taskRepository, false);
	}

	protected CrucibleReviewChangeJob(String name, TaskRepository taskRepository, boolean waitForTaskListSync) {
		super(name);
		this.taskRepository = taskRepository;
		this.waitForTaskListSync = waitForTaskListSync;
	}

	@Override
	public void runImpl(IProgressMonitor monitor) {
		setStatus(Status.CANCEL_STATUS);
		CrucibleRepositoryConnector connector = CrucibleCorePlugin.getRepositoryConnector();
		CrucibleClient client = connector.getClientManager().getClient(taskRepository);
		if (client == null) {
			setStatus(new Status(IStatus.ERROR, CrucibleUiPlugin.PLUGIN_ID,
					"Unable to get client, please try to refresh"));
			return;
		}
		try {
			IStatus result = execute(client, monitor);
			setStatus(result);
			SynchronizationJob synchronizeRepositoriesJob = TasksUiPlugin.getTaskJobFactory()
					.createSynchronizeRepositoriesJob(Collections.singleton(taskRepository));
			synchronizeRepositoriesJob.schedule();
			if (waitForTaskListSync) {
				try {
					synchronizeRepositoriesJob.join();
				} catch (InterruptedException e) {
					//ignore
				}
			}
		} catch (CoreException e) {
			setStatus(e.getStatus());
		}
		setStatus(Status.OK_STATUS);
	}

	protected abstract IStatus execute(CrucibleClient client, IProgressMonitor monitor) throws CoreException;
}