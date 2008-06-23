/*******************************************************************************
 * Copyright (c) 2004, 2007 Mylyn project committers and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.mylyn.internal.jira.core;

import java.net.Proxy;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.mylyn.commons.net.AbstractWebLocation;
import org.eclipse.mylyn.internal.jira.core.model.JiraVersion;
import org.eclipse.mylyn.internal.jira.core.model.ServerInfo;
import org.eclipse.mylyn.internal.jira.core.service.JiraClient;
import org.eclipse.mylyn.internal.jira.core.service.JiraException;
import org.eclipse.mylyn.internal.jira.core.util.JiraUtil;
import org.eclipse.mylyn.tasks.core.IRepositoryListener;
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.eclipse.mylyn.tasks.core.TaskRepositoryLocationFactory;

/**
 * This class acts as a layer of indirection between clients in this project and the server API implemented by the Jira
 * Dashboard, and also abstracts some Mylyn implementation details. It initializes a jiraServer object and serves as the
 * central location to get a reference to it.
 * 
 * @author Mik Kersten
 * @author Wesley Coelho (initial integration patch)
 * @author Steffen Pingel
 */
public class JiraClientFactory implements IRepositoryListener, IJiraClientFactory {

	private static JiraClientFactory instance = null;

	private JiraClientManager clientManager = null;

	private TaskRepositoryLocationFactory taskRepositoryLocationFactory;

	private boolean forceTaskRepositoryLocationFactory;

	private JiraClientFactory() {
		this.taskRepositoryLocationFactory = new TaskRepositoryLocationFactory();
		this.clientManager = JiraCorePlugin.getDefault().getClientManager();
	}

	/* For testing. */
	public void clearClients() {
		clientManager.removeAllClients(false);
	}

	/* For testing. */
	public void clearClientsAndConfigurationData() {
		clientManager.removeAllClients(true);
	}

	/**
	 * Lazily creates {@link JiraClient} instance
	 * 
	 * @see #validateConnection(String, String, String, Proxy, String, String)
	 */
	public synchronized JiraClient getJiraClient(TaskRepository repository) {
		JiraClient client = clientManager.getClient(repository.getRepositoryUrl());
		if (client == null) {
			AbstractWebLocation location = taskRepositoryLocationFactory.createWebLocation(repository);
			String characterEncoding = null;
			if (JiraUtil.getCharacterEncodingValidated(repository)) {
				characterEncoding = repository.getCharacterEncoding();
			}
			client = clientManager.addClient(location, characterEncoding, JiraUtil.getCompression(repository));
		}
		return client;
	}

	public synchronized static JiraClientFactory getDefault() {
		if (instance == null) {
			instance = new JiraClientFactory();
		}
		return instance;
	}

	public synchronized void logOutFromAll() {
		JiraClient[] clients = clientManager.getAllClients();
		for (JiraClient client : clients) {
			try {
				client.logout(null);
			} catch (JiraException e) {
				// ignore
			}
		}
	}

	public void repositoriesRead() {
		// ignore
	}

	public synchronized void repositoryAdded(TaskRepository repository) {
		if (repository.getConnectorKind().equals(JiraCorePlugin.CONNECTOR_KIND)) {
			assert clientManager.getClient(repository.getRepositoryUrl()) == null;
			getJiraClient(repository);
		}
	}

	public synchronized void repositoryRemoved(TaskRepository repository) {
		if (repository.getConnectorKind().equals(JiraCorePlugin.CONNECTOR_KIND)) {
			JiraClient client = clientManager.getClient(repository.getRepositoryUrl());
			if (client != null) {
				clientManager.removeClient(client, true);
			}
		}
	}

	public synchronized void repositorySettingsChanged(TaskRepository repository) {
		if (repository.getConnectorKind().equals(JiraCorePlugin.CONNECTOR_KIND)) {
			JiraClient client = clientManager.getClient(repository.getRepositoryUrl());
			if (client != null) {
				clientManager.removeClient(client, false);
			}
		}
	}

	/**
	 * Validate the server URL and user credentials
	 * 
	 * @param monitor
	 * 
	 * @param serverUrl
	 *            Location of the Jira Server
	 * @param user
	 *            Username
	 * @param password
	 *            Password
	 * @return
	 * @return String describing validation failure or null if the details are valid
	 */
	public ServerInfo validateConnection(AbstractWebLocation location, IProgressMonitor monitor) throws JiraException {
		ServerInfo info = clientManager.validateConnection(location, monitor);
		JiraVersion serverVersion = new JiraVersion(info.getVersion());
		if (JiraVersion.MIN_VERSION.compareTo(serverVersion) > 0) {
			throw new JiraException("JIRA connector requires server " + JiraVersion.MIN_VERSION + " or later");
		}
		return info;
	}

	public TaskRepositoryLocationFactory getTaskRepositoryLocationFactory() {
		return taskRepositoryLocationFactory;
	}

	public void setTaskRepositoryLocationFactory(TaskRepositoryLocationFactory taskRepositoryLocationFactory,
			boolean force) {
		if (forceTaskRepositoryLocationFactory) {
			return;
		}

		this.forceTaskRepositoryLocationFactory = force;
		this.taskRepositoryLocationFactory = taskRepositoryLocationFactory;
	}

	public void repositoryUrlChanged(TaskRepository repository, String oldUrl) {
		// ignore
	}

}
