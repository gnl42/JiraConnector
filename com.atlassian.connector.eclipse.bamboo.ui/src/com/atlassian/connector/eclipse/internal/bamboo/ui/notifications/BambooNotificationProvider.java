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

package com.atlassian.connector.eclipse.internal.bamboo.ui.notifications;

import com.atlassian.connector.eclipse.internal.bamboo.core.BambooCorePlugin;
import com.atlassian.connector.eclipse.internal.bamboo.core.BambooUtil;
import com.atlassian.connector.eclipse.internal.bamboo.core.BuildsChangedEvent;
import com.atlassian.connector.eclipse.internal.bamboo.core.BuildsChangedListener;
import com.atlassian.theplugin.commons.bamboo.BambooBuild;

import org.eclipse.mylyn.commons.notifications.ui.AbstractUiNotification;
import org.eclipse.mylyn.internal.tasks.ui.ITaskListNotificationProvider;
import org.eclipse.mylyn.internal.tasks.ui.TaskListNotificationManager;
import org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin;
import org.eclipse.mylyn.tasks.core.TaskRepository;

import java.util.HashSet;
import java.util.Set;

/**
 * Provides notification of changed/new/removed builds
 * 
 * @author Thomas Ehrnhoefer
 */
public class BambooNotificationProvider implements ITaskListNotificationProvider, BuildsChangedListener {

	private final Set<BambooNotification> notifications;

	public BambooNotificationProvider() {
		BambooCorePlugin.getBuildPlanManager().addBuildsChangedListener(this);
		notifications = new HashSet<BambooNotification>();
		TaskListNotificationManager taskListNotificationManager = TasksUiPlugin.getTaskListNotificationManager();
		taskListNotificationManager.addNotificationProvider(this);
	}

	public void dispose() {
		BambooCorePlugin.getBuildPlanManager().removeBuildsChangedListener(this);
	}

//	public Set<AbstractNotification> getNotifications() {
	public Set<AbstractUiNotification> getNotifications() {
		Set<AbstractUiNotification> toReturn = new HashSet<AbstractUiNotification>(notifications);
		notifications.clear();
		return toReturn;
	}

	public void buildsUpdated(BuildsChangedEvent event) {
		BambooUtil.runActionForChangedBuild(event, new BambooUtil.BuildChangeAction() {
			public void run(BambooBuild build, TaskRepository repository) {
				notifications.add(new BambooNotification(build, repository, BambooNotification.CHANGE.CHANGED));
			}
		});
	}

}
