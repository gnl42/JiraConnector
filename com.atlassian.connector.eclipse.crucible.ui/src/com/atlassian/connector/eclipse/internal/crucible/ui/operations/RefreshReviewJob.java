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

package com.atlassian.connector.eclipse.internal.crucible.ui.operations;

import com.atlassian.connector.eclipse.internal.crucible.core.client.CrucibleClient;
import com.atlassian.connector.eclipse.internal.crucible.ui.CrucibleUiPlugin;
import com.atlassian.connector.eclipse.internal.crucible.ui.CrucibleUiUtil;
import com.atlassian.connector.eclipse.internal.crucible.ui.editor.CrucibleReviewChangeJob;
import com.atlassian.theplugin.commons.crucible.api.model.Review;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.mylyn.commons.core.StatusHandler;
import org.eclipse.mylyn.tasks.core.ITask;
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.jetbrains.annotations.Nullable;

public class RefreshReviewJob extends CrucibleReviewChangeJob {

	private final ITask task;

	public RefreshReviewJob(ITask task, TaskRepository taskRepository) {
		super("Retrieving Crucible Review " + task.getTaskKey(), taskRepository);
		this.task = task;
	}

	@Nullable
	public static RefreshReviewJob createForReview(Review review) {
		final ITask mytask = CrucibleUiUtil.getCrucibleTask(review);
		if (mytask == null) {
			StatusHandler.log(new Status(IStatus.ERROR, CrucibleUiPlugin.PLUGIN_ID,
					"Cannot find corresponding Mylyn task for review [" + review.getPermId() + "]. Refresh will fail"));
			return null;
		}

		final TaskRepository taskRepository = CrucibleUiUtil.getCrucibleTaskRepository(review);
		if (taskRepository == null) {
			StatusHandler.log(new Status(IStatus.ERROR, CrucibleUiPlugin.PLUGIN_ID,
					"Cannot find corresponding Mylyn task repository for review [" + review.getPermId()
							+ "]. Refresh will fail"));
			return null;
		}
		return new RefreshReviewJob(mytask, taskRepository);

	}

	@Override
	protected IStatus execute(CrucibleClient client, IProgressMonitor monitor) throws CoreException {
		// check if repositoryData is initialized
		if (client.getClientData() == null || client.getClientData().getCachedUsers().size() == 0
				|| client.getClientData().getCachedProjects().size() == 0) {
			monitor.subTask("Updating Repository Data");
			client.updateRepositoryData(monitor, getTaskRepository());
		}
		monitor.subTask("Retrieving Crucible Review");

		client.getReview(getTaskRepository(), task.getTaskId(), true, monitor);
		return Status.OK_STATUS;
	}
};
