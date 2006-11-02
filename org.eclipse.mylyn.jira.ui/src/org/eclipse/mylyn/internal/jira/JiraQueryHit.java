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

import org.eclipse.mylar.tasks.core.AbstractQueryHit;
import org.eclipse.mylar.tasks.core.AbstractRepositoryTask;
import org.eclipse.mylar.tasks.core.TaskList;

/**
 * Represents an issue returned as the result of a Jira Filter (Query)

 * @author Mik Kersten
 * @author Wesley Coelho (initial integration patch)
 */
public class JiraQueryHit extends AbstractQueryHit {

	private String key = null;
	
//	private String description = "";
	
	private boolean completed;

	public JiraQueryHit(TaskList taskList, String description, String repositoryUrl, String id, String key, boolean completed) {
		super(taskList, repositoryUrl, description, id);
//		this.description = description;
		this.key = key;
		this.completed = completed;
	}

	protected AbstractRepositoryTask createTask() {
		return JiraRepositoryConnector.createTask(super.getHandleIdentifier(), key, description);
	}

	public boolean isCompleted() {
		if (task != null) {
			return task.isCompleted();
		} else {
			return completed;
		}
	}

	public void setDescription(String description) {
		if (task != null) {
			task.setDescription(description);
		} else {
			this.description = description;
		}
	}

	@Override
	public String getIdLabel() {
		return key;
	}
}
