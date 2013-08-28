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

package com.atlassian.connector.eclipse.internal.jira.ui;

import java.net.Proxy;

import org.eclipse.mylyn.internal.tasks.ui.TaskRepositoryLocationUi;
import org.eclipse.mylyn.tasks.core.TaskRepository;

import com.atlassian.connector.eclipse.internal.jira.ui.wizards.JiraTaskRepositoryLocation;

@SuppressWarnings("restriction")
public class JiraTaskRepositoryLocationUi extends TaskRepositoryLocationUi {

	private final JiraTaskRepositoryLocation jiraTaskRepositoryLocation;

	public JiraTaskRepositoryLocationUi(TaskRepository taskRepository) {
		super(taskRepository);

		jiraTaskRepositoryLocation = new JiraTaskRepositoryLocation(taskRepository);
	}

	@Override
	public Proxy getProxyForHost(String host, String proxyType) {
		return jiraTaskRepositoryLocation.getProxyForHost(host, proxyType);
	}

}
