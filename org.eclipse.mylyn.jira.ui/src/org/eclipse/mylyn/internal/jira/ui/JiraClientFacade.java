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

package org.eclipse.mylyn.internal.jira.ui;

import java.net.Proxy;

import org.eclipse.mylyn.internal.jira.core.JiraClientManager;
import org.eclipse.mylyn.internal.jira.core.JiraCorePlugin;
import org.eclipse.mylyn.internal.jira.core.model.ServerInfo;
import org.eclipse.mylyn.internal.jira.core.service.JiraAuthenticationException;
import org.eclipse.mylyn.internal.jira.core.service.JiraClient;
import org.eclipse.mylyn.internal.jira.core.service.JiraException;
import org.eclipse.mylyn.internal.jira.core.service.JiraServiceUnavailableException;
import org.eclipse.mylyn.monitor.core.StatusHandler;
import org.eclipse.mylyn.tasks.core.ITaskRepositoryListener;
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.eclipse.mylyn.tasks.ui.TasksUiPlugin;

/**
 * This class acts as a layer of indirection between clients in this project and the server API implemented by the Jira
 * Dashboard, and also abstracts some Mylar implementation details. It initializes a jiraServer object and serves as the
 * central location to get a reference to it.
 * 
 * @author Mik Kersten
 * @author Wesley Coelho (initial integration patch)
 * @author Steffen Pingel
 */
public class JiraClientFacade implements ITaskRepositoryListener {

	public final static JiraVersion MIN_VERSION = new JiraVersion("3.3.3");

	private JiraClientManager clientManager = null;

	private static JiraClientFacade instance = null;

	private JiraClientFacade() {
		TasksUiPlugin.getRepositoryManager().addListener(this);
		clientManager = JiraCorePlugin.getDefault().getServerManager();
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
	 * Lazily creates server.
	 * 
	 * @see #validateServerAndCredentials(String, String, String, Proxy, String, String)
	 */
	public synchronized JiraClient getJiraClient(TaskRepository repository) {
		JiraClient server = clientManager.getClient(repository.getUrl());
		if (server == null) {
			String userName = repository.getUserName();
			String password = repository.getPassword();
			server = clientManager.addClient(repository.getUrl(), //
					Boolean.parseBoolean(repository.getProperty(JiraRepositoryConnector.COMPRESSION_KEY)), //
					userName == null ? "" : userName, //
					password == null ? "" : password, //
					repository.getProxy(), //
					repository.getHttpUser(), repository.getHttpPassword());
		}
		return server;
	}

	public synchronized static JiraClientFacade getDefault() {
		if (instance == null) {
			instance = new JiraClientFacade();
		}
		return instance;
	}

	public synchronized void logOutFromAll() {
		try {
			JiraClient[] allServers = clientManager.getAllClients();
			for (int i = 0; i < allServers.length; i++) {
				allServers[i].logout();
			}
		} catch (Exception e) {
			// ignore
		}
	}

	public void repositoriesRead() {
		// ignore
	}

	public synchronized void repositoryAdded(TaskRepository repository) {
		if (repository.getConnectorKind().equals(JiraUiPlugin.REPOSITORY_KIND)) {
			assert clientManager.getClient(repository.getUrl()) == null;
			getJiraClient(repository);
		}
	}

	public synchronized void repositoryRemoved(TaskRepository repository) {
		if (repository.getConnectorKind().equals(JiraUiPlugin.REPOSITORY_KIND)) {
			JiraClient server = clientManager.getClient(repository.getUrl());
			removeServer(server);
		}
	}

	public synchronized void repositorySettingsChanged(TaskRepository repository) {
		repositoryRemoved(repository);
		repositoryAdded(repository);
	}

	private synchronized void removeServer(JiraClient server) {
		if (server != null) {
			server.logout();
			clientManager.removeClient(server);
		}
	}

	/**
	 * Validate the server URL and user credentials
	 * 
	 * @param serverUrl
	 *            Location of the Jira Server
	 * @param user
	 *            Username
	 * @param password
	 *            Password
	 * @return String describing validation failure or null if the details are valid
	 */
	public void validateServerAndCredentials(String serverUrl, String user, String password, Proxy proxy,
			String httpUser, String httpPassword) throws JiraException {
		ServerInfo info = clientManager.testConnection(serverUrl, user, password, proxy, httpUser, httpPassword);
		JiraVersion serverVersion = new JiraVersion(info.getVersion());
		if (MIN_VERSION.compareTo(serverVersion) > 0) {
			throw new JiraException("JIRA connector requires server " + MIN_VERSION + " or later");
		}
	}
	
	/*
	 * TODO: refactor
	 */
	public static void handleConnectionException(Exception e) {
		if (e instanceof JiraServiceUnavailableException) {
			StatusHandler.fail(e, "Jira Connection Failure.\n\n"
					+ "Please check your network connection and Jira server settings in the Task Repositories view.",
					true);
		} else if (e instanceof JiraAuthenticationException) {
			StatusHandler.fail(e, "Jira Authentication Failed.\n\n"
					+ "Please check your Jira username and password in the Task Repositories view", true);
		} else if (e instanceof RuntimeException) {
			StatusHandler.fail(e, "No Jira repository found.\n\n"
					+ "Please verify that a vaild Jira repository exists in the Task Repositories view", true);
		} else {
			StatusHandler.fail(e, "Could not connect to Jira repository.\n\n"
					+ "Please check your credentials in the Task Repositories view", true);
		}
	}

}
