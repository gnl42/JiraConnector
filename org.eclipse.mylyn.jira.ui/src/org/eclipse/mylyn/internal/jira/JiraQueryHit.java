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

/**
 * Represents an issue returned as the result of a Jira Filter (Query)

 * @author Mik Kersten
 * @author Wesley Coelho (initial integration patch)
 */
public class JiraQueryHit extends AbstractQueryHit {

//	private Issue issue = null;

	private JiraTask task = null;

	public JiraQueryHit(JiraTask task, String repositoryUrl, int id) {
		super(repositoryUrl, task.getDescription(), id);
		this.task = task;
//		this.issue = issue;
//		task = (JiraTask)getOrCreateCorrespondingTask();
	}

//	public Issue getIssue() {
//		return issue;
//	}

	public AbstractRepositoryTask getOrCreateCorrespondingTask() {
		return task;
//		if (task == null) {
//			task = JiraRepositoryConnector.createTask(issue, getHandleIdentifier());
//		}
//		if (issue != null) {
//			JiraRepositoryConnector.updateTaskDetails(repositoryUrl, task, issue);
//		} 
//		return task;
	}

	/**
	 * @return null if there is no corresponding report
	 */
	public AbstractRepositoryTask getCorrespondingTask() {
		return task;
	}

	public void setCorrespondingTask(AbstractRepositoryTask task) {
		if (task instanceof JiraTask) {
			this.task = (JiraTask)task;
		}
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
