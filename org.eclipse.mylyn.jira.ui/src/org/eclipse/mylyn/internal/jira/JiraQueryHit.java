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

 * @author Mik Kersten
 * @author Wesley Coelho (initial integration patch)
 */
public class JiraQueryHit extends AbstractQueryHit {

	private Issue issue = null;

	private AbstractRepositoryTask task = null;

	public JiraQueryHit(Issue issue, String repositoryUrl, int id) {
		super(repositoryUrl, issue.getSummary(), id);
		this.issue = issue;
		task = getOrCreateCorrespondingTask();
	}

	public Issue getIssue() {
		return issue;
	}

	public AbstractRepositoryTask getOrCreateCorrespondingTask() {
		if (task == null) {
			String description = issue.getKey() + ": " + issue.getSummary();
			ITask existingTask = MylarTaskListPlugin.getTaskListManager().getTaskList().getTask(
					getHandleIdentifier());
			if (existingTask instanceof JiraTask) {
				task = (JiraTask) existingTask;
			} else { 
				task = new JiraTask(getHandleIdentifier(), description, true);
				MylarTaskListPlugin.getTaskListManager().getTaskList().addTask(task);
			}
		}
		if (issue != null) {
			if (issue.getKey() != null) {
				String url = repositoryUrl + MylarJiraPlugin.ISSUE_URL_PREFIX + issue.getKey();
				task.setUrl(url);
				if (issue.getDescription() != null) {
					task.setDescription(issue.getKey() + ": " + issue.getSummary());
				}
			} 
			if (issue.getStatus() != null && (issue.getStatus().isClosed() || issue.getStatus().isResolved())) {
				task.setCompleted(true);
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
		return task.isCompleted();
	}

	public String getPriority() {
		return task.getPriority();
	}

	public String getDescription() {
		return task.getDescription();
	}

	public void setDescription(String description) {
		task.setDescription(description);
	}

	public void setHandleIdentifier(String id) {
		task.setHandleIdentifier(id);
	}

	public boolean isLocal() {
		return false;
	}
}
