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

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.mylyn.internal.tasks.core.AbstractTask;
import org.eclipse.ui.PlatformUI;

import com.atlassian.connector.eclipse.internal.jira.core.model.JiraIssue;
import com.atlassian.connector.eclipse.internal.jira.core.service.JiraClient;
import com.atlassian.connector.eclipse.internal.jira.core.service.JiraException;

/**
 * @author Jacek Jaroczynski
 */
@SuppressWarnings("restriction")
public class VoteAction extends AbstractJiraAction {

	public VoteAction() {
		super("Vote Action"); //$NON-NLS-1$
	}

	@Override
	protected void doAction(final List<AbstractTask> tasks) {

		Job voteJob = new Job(Messages.JiraConnectorUiActions_Vote) {

			@Override
			protected IStatus run(IProgressMonitor monitor) {

				monitor.beginTask(Messages.JiraConnectorUiActions_Vote, tasks.size());

				List<String> cannotVote = new ArrayList<String>();

				for (AbstractTask task : tasks) {

					monitor.setTaskName(Messages.JiraConnectorUiActions_Voting_for_issue + task.getTaskKey());

					JiraClient client = getClient(task);

					try {
						final JiraIssue issue = getIssue(task);
						if (issue.canUserVote(client.getUserName())) {
							client.voteIssue(issue, monitor);
						} else {
							cannotVote.add(issue.getKey());
						}
					} catch (CoreException e) {
						handleError(Messages.JiraConnectorUiActions_Cannot_get_task_data + task.getTaskKey(), e);
					} catch (JiraException e) {
						handleError(Messages.JiraConnectorUiActions_Voting_failed + task.getTaskKey(), e);
					}

					monitor.worked(1);
					if (monitor.isCanceled()) {
						break;
					}
				}

				if (cannotVote.size() > 0) {
					final String issues = StringUtils.join(cannotVote, ","); //$NON-NLS-1$

					PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {
						public void run() {
							String message = Messages.JiraConnectorUiActions_Cannot_vote + issues
									+ ". " + Messages.JiraConnectorUiActions_Reported_or_issue_closed; //$NON-NLS-1$
							handleInformation(message);
						}
					});
				}

				return Status.OK_STATUS;
			}
		};

		voteJob.setUser(true);
		voteJob.schedule();

	}

}
