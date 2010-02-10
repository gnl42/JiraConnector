/*******************************************************************************
 * Copyright (c) 2004, 2008 Eugene Kuleshov and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Eugene Kuleshov - initial API and implementation
 *******************************************************************************/

package com.atlassian.connector.eclipse.jira.tests.util;

import org.eclipse.mylyn.tasks.core.TaskRepository;

import com.atlassian.connector.eclipse.internal.jira.core.IJiraClientFactory;
import com.atlassian.connector.eclipse.internal.jira.core.service.JiraClient;

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
