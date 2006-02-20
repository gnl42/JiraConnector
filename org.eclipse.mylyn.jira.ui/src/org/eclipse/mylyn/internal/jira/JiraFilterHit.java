/*******************************************************************************
 * Copyright (c) 2006 - 2006 Mylar eclipse.org project and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Mylar project committers - initial API and implementation
 *******************************************************************************/

package org.eclipse.mylar.internal.jira;

import org.eclipse.mylar.provisional.tasklist.AbstractQueryHit;
import org.eclipse.mylar.provisional.tasklist.AbstractRepositoryTask;
import org.eclipse.mylar.provisional.tasklist.ITask;
import org.eclipse.mylar.provisional.tasklist.MylarTaskListPlugin;
import org.tigris.jira.core.model.Issue;

/**
 * Represents an issue returned as the result of a Jira Filter (Query)
 * 
 * @author Wesley Coelho (initial integration patch)
 * @author Mik Kersten
 */
public class JiraFilterHit extends AbstractQueryHit {

	private Issue issue = null;

	private AbstractRepositoryTask task = null;

	public JiraFilterHit(Issue issue, String repositoryUrl, int id) {
		super(repositoryUrl, issue.getSummary(), id);
		this.issue = issue;
		task = getOrCreateCorrespondingTask();
		MylarTaskListPlugin.getTaskListManager().getTaskList().addTaskToArchive(task);
	}

	public Issue getIssue() {
		return issue;
	}

	public AbstractRepositoryTask getOrCreateCorrespondingTask() {
		if (task == null) {
			ITask archiveTask = MylarTaskListPlugin.getTaskListManager().getTaskList().getTaskFromArchive(
					getHandleIdentifier());
			if (archiveTask instanceof JiraTask) {
				task = (JiraTask) archiveTask;
			} else {
				task = new JiraTask(getHandleIdentifier(), issue.getSummary(), true);
				MylarTaskListPlugin.getTaskListManager().getTaskList().addTaskToArchive(task);
			}
		}
		if (issue != null) {
			String url = repositoryUrl + MylarJiraPlugin.ISSUE_URL_PREFIX + issue.getKey();
			task.setUrl(url);
			if (issue.getResolution() != null) {
				task.setCompleted(true);
			} else {
				task.setCompleted(false);
			}
			
			if (issue.getPriority() != null) {
				String translatedPriority = JiraTask.PriorityLevel.fromPriority(issue.getPriority()).toString();
				task.setPriority(translatedPriority);
				task.setKind(issue.getType().getName());
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
		if (issue != null && issue.getStatus() != null) {
			return issue.getStatus().isClosed() || issue.getStatus().isResolved();
		} else {
			return false;
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
	}

	// public String getHandleIdentifier() {
	// return handl;
	// }

	public void setHandleIdentifier(String id) {
		task.setHandleIdentifier(id);
	}

	public boolean isLocal() {
		return false;
	}

}
