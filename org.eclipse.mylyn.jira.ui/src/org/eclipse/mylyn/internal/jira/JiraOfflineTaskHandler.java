/*******************************************************************************
 * Copyright (c) 2004 - 2006 Mylar committers and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.mylar.internal.jira;

import java.io.UnsupportedEncodingException;
import java.net.Proxy;
import java.util.Date;
import java.util.Set;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.mylar.tasks.core.AbstractAttributeFactory;
import org.eclipse.mylar.tasks.core.AbstractRepositoryTask;
import org.eclipse.mylar.tasks.core.IOfflineTaskHandler;
import org.eclipse.mylar.tasks.core.RepositoryTaskData;
import org.eclipse.mylar.tasks.core.TaskRepository;
import org.tigris.jira.core.service.JiraServer;

/**
 * @author Mik Kersten
 * 
 */
public class JiraOfflineTaskHandler implements IOfflineTaskHandler {

	private AbstractAttributeFactory attributeFactory = new JiraAttributeFactory();

	private JiraRepositoryConnector connector;

	public JiraOfflineTaskHandler(JiraRepositoryConnector connector) {
		this.connector = connector;
	}

	public RepositoryTaskData downloadTaskData(TaskRepository repository, String taskId, Proxy proxySettings)
			throws CoreException {

		RepositoryTaskData data = new RepositoryTaskData(attributeFactory, MylarJiraPlugin.REPOSITORY_KIND, repository
				.getUrl(), taskId);

		JiraServer server = JiraServerFacade.getDefault().getJiraServer(repository);

		// Issue jiraIssue = server.getIssue();
		//			
		// client.updateAttributes(new NullProgressMonitor(), false);
		// TracTicket ticket = client.getTicket(id);
		// createDefaultAttributes(attributeFactory, data, client, true);
		// updateTaskData(repository, attributeFactory, data, ticket);
		// return data;

		return null;

	}

	public AbstractAttributeFactory getAttributeFactory() {
		return null;
	}

	public Set<AbstractRepositoryTask> getChangedSinceLastSync(TaskRepository repository,
			Set<AbstractRepositoryTask> tasks, Proxy proxySettings) throws CoreException, UnsupportedEncodingException {
		return null;
	}

	public Date getDateForAttributeType(String attributeKey, String dateString) {
		return null;
	}

}
