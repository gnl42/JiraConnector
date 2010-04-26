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

package com.atlassian.connector.eclipse.internal.jira.ui.actions;

import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.mylyn.tasks.core.ITask;

import com.atlassian.connector.eclipse.internal.jira.core.model.JiraIssue;
import com.atlassian.connector.eclipse.internal.jira.core.service.JiraException;
import com.atlassian.connector.eclipse.internal.jira.ui.IJiraTask;

/**
 * @author Jacek Jaroczynski
 */
@SuppressWarnings("restriction")
public class WatchAction extends AbstractJiraAction {

	public WatchAction() {
		super("Watch Action"); //$NON-NLS-1$
	}

	// TODO jj move action in the context menu up

	@Override
	protected void doAction(final List<IJiraTask> tasks) {

		Job voteJob = new Job(Messages.JiraConnectorUiActions_Watch) {

			@Override
			protected IStatus run(IProgressMonitor monitor) {

				monitor.beginTask(Messages.JiraConnectorUiActions_Watch, tasks.size());

				for (IJiraTask jiraTask : tasks) {

					ITask task = jiraTask.getTask();

					monitor.setTaskName(Messages.JiraConnectorUiActions_Starting_to_watch_issue + task.getTaskKey());

					try {

						final JiraIssue issue = getIssue(task);
						getClient(task).watchIssue(issue, monitor);
					} catch (CoreException e) {
						handleError(Messages.JiraConnectorUiActions_Cannot_get_task_data + task.getTaskKey(), e);
					} catch (JiraException e) {
						handleError(Messages.JiraConnectorUiActions_Starting_to_watch_issue_failed + task.getTaskKey(),
								e);
					}

					monitor.worked(1);
					if (monitor.isCanceled()) {
						break;
					}
				}

				return Status.OK_STATUS;
			}
		};

		voteJob.setUser(true);
		voteJob.schedule();

	}

}
