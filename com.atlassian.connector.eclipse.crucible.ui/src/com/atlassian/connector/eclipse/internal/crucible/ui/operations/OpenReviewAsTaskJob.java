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

import com.atlassian.connector.eclipse.internal.crucible.core.CrucibleCorePlugin;
import com.atlassian.connector.eclipse.internal.crucible.core.CrucibleUtil;
import com.atlassian.connector.eclipse.internal.crucible.ui.CrucibleUiUtil;
import com.atlassian.theplugin.commons.crucible.api.model.BasicReview;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.mylyn.commons.core.StatusHandler;
import org.eclipse.mylyn.internal.tasks.ui.util.TasksUiInternal;
import org.eclipse.mylyn.tasks.core.ITask;
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.eclipse.mylyn.tasks.core.data.TaskData;
import org.eclipse.mylyn.tasks.ui.TasksUi;
import org.eclipse.mylyn.tasks.ui.TasksUiUtil;
import org.eclipse.swt.widgets.Display;

public class OpenReviewAsTaskJob extends Job {

	private final BasicReview review;

	private final TaskRepository taskRepository;

	public OpenReviewAsTaskJob(TaskRepository taskRepository, BasicReview review) {
		super("Opening created review as task");
		this.review = review;
		this.taskRepository = taskRepository;
	}

	@SuppressWarnings("restriction")
	@Override
	public IStatus run(IProgressMonitor monitor) {
		final ITask task = TasksUi.getRepositoryModel().createTask(taskRepository,
				CrucibleUtil.getTaskIdFromPermId(review.getPermId().getId()));
		try {
			TaskData taskData = CrucibleUiUtil.getClient(review).getTaskData(taskRepository, task.getTaskId(), monitor);
			CrucibleCorePlugin.getRepositoryConnector().updateTaskFromTaskData(taskRepository, task, taskData);
			TasksUiInternal.getTaskList().addTask(task, null);
			Display.getDefault().asyncExec(new Runnable() {
				public void run() {
					TasksUiUtil.openTask(task);
				}
			});
		} catch (CoreException e) {
			StatusHandler.log(e.getStatus());
		}
		return Status.OK_STATUS;
	}

}
