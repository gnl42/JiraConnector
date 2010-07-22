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

package com.atlassian.connector.eclipse.internal.jira.ui;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.mylyn.commons.core.StatusHandler;
import org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin;
import org.eclipse.mylyn.tasks.core.ITask;
import org.eclipse.mylyn.tasks.core.TaskRepository;

import com.atlassian.connector.eclipse.internal.jira.core.IJiraConstants;
import com.atlassian.connector.eclipse.internal.jira.core.model.JiraWorkLog;
import com.atlassian.connector.eclipse.internal.jira.core.model.JiraWorkLog.AdjustEstimateMethod;
import com.atlassian.connector.eclipse.internal.jira.ui.actions.Messages;

/**
 * @author Jacek Jaroczynski
 */
@SuppressWarnings("restriction")
public class JiraUiUtil {

	/**
	 * @param iTask
	 * @return time passed since last work logged (in seconds)
	 */
	public static long getLoggedActivityTime(ITask iTask) {
		long mylynTimeTicks = TasksUiPlugin.getTaskActivityManager().getElapsedTime(iTask);

		long localTimeTicks = 0;

		String stringLoggedTime = iTask.getAttribute(IJiraConstants.ATTRIBUTE_JIRA_LOGGED_ACTIVITY_TIME);

		if (stringLoggedTime != null && stringLoggedTime.length() > 0) {
			try {
				localTimeTicks = Long.parseLong(stringLoggedTime);
			} catch (NumberFormatException e) {
				StatusHandler.log(new Status(IStatus.WARNING, JiraUiPlugin.PRODUCT_NAME,
						Messages.JiraUiUtil_Cannot_parse_logged_activity_time, e));
			}
		}

		if (mylynTimeTicks >= localTimeTicks) {
			return (mylynTimeTicks - localTimeTicks) / 1000;
		} else {
			StatusHandler.log(new Status(IStatus.WARNING, JiraUiPlugin.PRODUCT_NAME,
					Messages.JiraUiUtil_Logged_activity_time_problem + iTask.getTaskKey()));
			return mylynTimeTicks / 1000;
		}
	}

	public static void clearLoggedActivityTime(ITask task) {
		task.setAttribute(IJiraConstants.ATTRIBUTE_JIRA_LOGGED_ACTIVITY_TIME, ""); //$NON-NLS-1$
	}

	/**
	 * Should be called every time user loggs work done.
	 * 
	 * @param task
	 */
	public static void setLoggedActivityTime(ITask task) {
		task.setAttribute(IJiraConstants.ATTRIBUTE_JIRA_LOGGED_ACTIVITY_TIME,
				Long.toString(TasksUiPlugin.getTaskActivityManager().getElapsedTime(task)));
	}

	public static void updateAdjustEstimateOption(AdjustEstimateMethod adjustEstimate, TaskRepository repository) {
		repository.setProperty(IJiraConstants.ATTRIBUTE_ADJUST_ESTIMATE_OPTION, adjustEstimate.value());
	}

	public static AdjustEstimateMethod getAdjustEstimateOption(TaskRepository repository) {
		try {
			return JiraWorkLog.AdjustEstimateMethod.fromValue(repository.getProperty(IJiraConstants.ATTRIBUTE_ADJUST_ESTIMATE_OPTION));
		} catch (IllegalArgumentException e) {
			return AdjustEstimateMethod.LEAVE;
		}
	}

}
