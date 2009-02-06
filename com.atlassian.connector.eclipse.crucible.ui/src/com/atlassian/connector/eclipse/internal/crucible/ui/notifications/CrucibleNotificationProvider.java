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

package com.atlassian.connector.eclipse.internal.crucible.ui.notifications;

import com.atlassian.connector.eclipse.internal.crucible.core.client.model.IReviewCacheListener;
import com.atlassian.connector.eclipse.internal.crucible.ui.CrucibleUiUtil;
import com.atlassian.theplugin.commons.crucible.api.model.Review;
import com.atlassian.theplugin.commons.crucible.api.model.notification.CrucibleNotification;

import org.eclipse.mylyn.internal.provisional.commons.ui.AbstractNotification;
import org.eclipse.mylyn.internal.tasks.ui.ITaskListNotificationProvider;
import org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin;
import org.eclipse.mylyn.tasks.core.ITask;
import org.eclipse.mylyn.tasks.core.TaskRepository;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Class to provide custom notifications for crucible
 * 
 * @author Shawn Minto
 */
public class CrucibleNotificationProvider implements ITaskListNotificationProvider, IReviewCacheListener {

	private final List<CrucibleReviewNotification> notificationQueue = new ArrayList<CrucibleReviewNotification>();

	private boolean hasRegistered = false;

	public Set<AbstractNotification> getNotifications() {
		synchronized (notificationQueue) {
			if (notificationQueue.isEmpty()) {
				return Collections.emptySet();
			}
			HashSet<AbstractNotification> result = new HashSet<AbstractNotification>(notificationQueue);
			notificationQueue.clear();
			return result;
		}
	}

	public void reviewAdded(final String repositoryUrl, final String taskId, final Review review) {

		TaskRepository taskRepository = CrucibleUiUtil.getCrucibleTaskRepository(repositoryUrl);
		if (taskRepository != null) {
			ITask task = CrucibleUiUtil.getCrucibleTask(taskRepository, taskId);
			if (task == null) {
				queueNotification(repositoryUrl, taskId, task, review, null);
			}
		}
	}

	public void reviewUpdated(final String repositoryUrl, final String taskId, final Review review,
			final List<CrucibleNotification> differences) {
		TaskRepository taskRepository = CrucibleUiUtil.getCrucibleTaskRepository(repositoryUrl);
		if (taskRepository != null) {
			ITask task = CrucibleUiUtil.getCrucibleTask(taskRepository, taskId);
			queueNotification(repositoryUrl, taskId, task, review, differences);
		}
	}

	private void queueNotification(String repositoryUrl, String taskId, ITask task, Review review,
			List<CrucibleNotification> differences) {

		synchronized (notificationQueue) {
			if (!hasRegistered) {
				TasksUiPlugin.getTaskListNotificationManager().addNotificationProvider(this);
				hasRegistered = true;
			}
			String label = review.getPermId().getId() + ": " + review.getName();

			notificationQueue.add(new CrucibleReviewNotification(repositoryUrl, taskId, label, differences));
		}
	}

}
