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

import com.atlassian.connector.eclipse.internal.crucible.core.CrucibleCorePlugin;
import com.atlassian.connector.eclipse.internal.crucible.core.client.model.IReviewCacheListener;
import com.atlassian.theplugin.commons.crucible.api.model.Review;
import com.atlassian.theplugin.commons.crucible.api.model.notification.CrucibleNotification;

import org.eclipse.mylyn.tasks.core.ITask;
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.eclipse.mylyn.tasks.ui.TasksUi;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;

import java.util.List;

/**
 * Class to manage displaying notifications of review changes to the user
 * 
 * @author Shawn Minto
 */
public class CrucibleNotificationManager implements IReviewCacheListener {

	public void reviewAdded(final String repositoryUrl, final String taskId, final Review review) {

		TaskRepository taskRepository = TasksUi.getRepositoryManager().getRepository(CrucibleCorePlugin.CONNECTOR_KIND,
				repositoryUrl);
		if (taskRepository != null) {
			ITask task = TasksUi.getRepositoryModel().getTask(taskRepository, taskId);
			if (task == null) {
				Display.getDefault().asyncExec(new Runnable() {
					public void run() {
						CrucibleNotificationPopupInput input = new CrucibleNotificationPopupInput(repositoryUrl,
								taskId, review);
						Shell shell = new Shell(PlatformUI.getWorkbench().getDisplay());
						if (shell != null) {
							CrucibleNotificationPopup popup = new CrucibleNotificationPopup(shell);
							popup.setContents(input);
							popup.open();
						}
					}
				});
			}
		}
	}

	public void reviewUpdated(final String repositoryUrl, final String taskId, final Review review,
			final List<CrucibleNotification> differences) {
		Display.getDefault().asyncExec(new Runnable() {
			public void run() {
				CrucibleNotificationPopupInput input = new CrucibleNotificationPopupInput(repositoryUrl, taskId,
						review, differences);
				Shell shell = new Shell(PlatformUI.getWorkbench().getDisplay());
				if (shell != null) {
					CrucibleNotificationPopup popup = new CrucibleNotificationPopup(shell);
					popup.setContents(input);
					popup.open();
				}
			}
		});

	}

}
