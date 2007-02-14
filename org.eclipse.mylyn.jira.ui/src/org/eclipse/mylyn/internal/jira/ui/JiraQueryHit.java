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

package org.eclipse.mylar.internal.jira.ui;

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

	public JiraQueryHit(TaskList taskList, String description, String repositoryUrl, String id, String key) {
		super(taskList, repositoryUrl, description, id);
		this.key = key;
	}

	public String getKey() {
		return key;
	}

	@Override
	protected AbstractRepositoryTask createTask() {
		return JiraRepositoryConnector.createTask(super.getRepositoryUrl(), super.getTaskId(), key, summary);
	}

	@Override
	public String getUrl() {
		return JiraRepositoryConnector.getTaskUrl(getRepositoryUrl(), key);
	}
	
	@Override
	public String getIdentifyingLabel() {
		return key;
	}
}
