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

import com.atlassian.connector.eclipse.internal.bamboo.core.BambooUtil;
import com.atlassian.connector.eclipse.internal.bamboo.core.BuildPlanManager;
import com.atlassian.connector.eclipse.internal.bamboo.core.BuildsChangedEvent;
import com.atlassian.connector.eclipse.internal.bamboo.core.BuildsChangedListener;
import com.atlassian.theplugin.commons.bamboo.BambooBuild;

import org.eclipse.mylyn.internal.provisional.commons.ui.AbstractNotification;
import org.eclipse.mylyn.internal.tasks.ui.ITaskListNotificationProvider;
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
		BuildPlanManager.getInstance().addBuildsChangedListener(this);
		notifications = new HashSet<BambooNotification>();
		TasksUiPlugin.getTaskListNotificationManager().addNotificationProvider(this);
	}

	public Set<AbstractNotification> getNotifications() {
		Set<AbstractNotification> toReturn = new HashSet<AbstractNotification>(notifications);
		notifications.clear();
		return toReturn;
	}

	public void buildsAdded(BuildsChangedEvent event) {
		//ignore added builds
	}

	public void buildsChanged(BuildsChangedEvent event) {
		for (TaskRepository key : event.getChangedBuilds().keySet()) {
			for (BambooBuild build : event.getChangedBuilds().get(key)) {
				//for each build get equivalent old build
				for (BambooBuild oldBuild : event.getOldBuilds().get(key)) {
					if (BambooUtil.isSameBuildPlan(build, oldBuild)) {
						if (build.getStatus() != oldBuild.getStatus()) {
							//build status changed
							notifications.add(new BambooNotification(build, BambooNotification.CHANGE.CHANGED));
						}
					}
				}
			}
		}
	}

	public void buildsRemoved(BuildsChangedEvent event) {
		//ignore removed builds
	}

}
