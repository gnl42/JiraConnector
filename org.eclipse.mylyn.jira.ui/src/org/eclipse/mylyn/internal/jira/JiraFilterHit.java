/*******************************************************************************
 * Copyright (c) 2006 - 2006 University Of British Columbia and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     University Of British Columbia - initial API and implementation
 *******************************************************************************/

package org.eclipse.mylar.internal.jira;

import org.eclipse.mylar.internal.core.util.MylarStatusHandler;
import org.eclipse.mylar.internal.tasklist.AbstractRepositoryClient;
import org.eclipse.mylar.internal.tasklist.AbstractRepositoryTask;
import org.eclipse.mylar.internal.tasklist.IQueryHit;
import org.eclipse.mylar.internal.tasklist.MylarTaskListPlugin;
import org.eclipse.mylar.internal.tasklist.TaskRepository;
import org.eclipse.mylar.internal.tasklist.ui.TaskListImages;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.tigris.jira.core.model.Issue;

/**
 * Represents an issue returned as the result of a Jira Filter (Query)
 * 
 * @author Wesley Coelho (initial integration patch)
 * @author Mik Kersten
 */
public class JiraFilterHit implements IQueryHit {

	private Issue issue = null;

	private AbstractRepositoryTask task = null;

	private String repositoryUrl = "N/A";

	public JiraFilterHit(Issue issue) {
		this.issue = issue;
		TaskRepository repository = MylarTaskListPlugin.getRepositoryManager().getDefaultRepository(
				MylarJiraPlugin.JIRA_REPOSITORY_KIND);
		if (repository != null) {
			setRepositoryUrl(repository.getUrl() + MylarJiraPlugin.ISSUE_URL_PREFIX + issue.getKey());
		} else {
			MylarStatusHandler.fail(new RuntimeException("JiraFilterHit couldn't get repository"),
					"Couldn't get repository for Jira Hit", false);
		}
		task = getOrCreateCorrespondingTask();
	}

	public String getRepositoryUrl() {
		return repositoryUrl;
	}

	public Issue getIssue() {
		return issue;
	}

	public void setRepositoryUrl(String repositoryUrl) {
		this.repositoryUrl = repositoryUrl;
	}

	public AbstractRepositoryTask getOrCreateCorrespondingTask() {
		if (task == null) {
			task = new JiraTask(getRepositoryUrl(), issue.getSummary(), true);
			AbstractRepositoryClient client = MylarTaskListPlugin.getRepositoryManager().getRepositoryClient(
					MylarJiraPlugin.JIRA_REPOSITORY_KIND);
			if (client != null) {
				client.addTaskToArchive(task);
			} else {
				MylarStatusHandler.log("No Jira Client for Jira Task", this);
			}
		}
		return task;
	}

	/**
	 * @return null if there is no corresponding report
	 */
	public AbstractRepositoryTask getCorrespondingTask() {
		return task;
	}

	public void setCorrespondingTask(AbstractRepositoryTask task) {
		this.task = task;
	}

	public boolean isCompleted() {
		return false; // issue.getStatus().isClosed() ||
						// issue.getStatus().isResolved();
	}

	public Image getIcon() {
		return TaskListImages.getImage(TaskListImages.TASK_WEB);
	}

	public Image getStatusIcon() {
		if (task != null) {
			return task.getStatusIcon();
		} else {
			return TaskListImages.getImage(TaskListImages.TASK_INACTIVE);
		}
	}

	public boolean isDragAndDropEnabled() {
		return false;
	}

	public String getPriority() {
		return task.getPriority();
	}

	public String getDescription() {
		return issue.getSummary();
	}

	public void setDescription(String description) {
		task.setDescription(description);
		issue.setDescription(description);
	}

	public String getHandleIdentifier() {
		return getRepositoryUrl();
	}

	public void setHandleIdentifier(String id) {
		task.setHandleIdentifier(id);
	}

	public boolean isLocal() {
		return false;
	}

	public boolean isActivatable() {
		return true;
	}

	public Font getFont() {
		if (task != null) {
			return task.getFont();
		}
		return null;
	}

	public String getToolTipText() {
		return issue.getDescription();
	}

	public String getStringForSortingDescription() {
		return issue.getKey();
	}
}
