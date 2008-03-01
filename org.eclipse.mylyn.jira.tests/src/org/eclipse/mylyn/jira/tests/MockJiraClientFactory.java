/*******************************************************************************
 * Copyright (c) 2004, 2007 Mylyn project committers and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.mylyn.jira.tests;

import org.eclipse.mylyn.internal.jira.core.service.JiraClient;
import org.eclipse.mylyn.internal.jira.ui.IJiraClientFactory;
import org.eclipse.mylyn.tasks.core.TaskRepository;

/**
 * @author Eugene Kuleshov
 */
public class MockJiraClientFactory implements IJiraClientFactory {

	private final JiraClient client;

	public MockJiraClientFactory(JiraClient client) {
		this.client = client;
	}

	public JiraClient getJiraClient(TaskRepository repository) {
		return client;
	}

}
