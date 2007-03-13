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

import java.net.MalformedURLException;
import java.net.Proxy;
import java.net.URL;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.mylar.core.MylarStatusHandler;
import org.eclipse.mylar.internal.jira.core.JiraCorePlugin;
import org.eclipse.mylar.internal.jira.core.ServerManager;
import org.eclipse.mylar.internal.jira.core.model.ServerInfo;
import org.eclipse.mylar.internal.jira.core.service.AuthenticationException;
import org.eclipse.mylar.internal.jira.core.service.JiraException;
import org.eclipse.mylar.internal.jira.core.service.JiraServer;
import org.eclipse.mylar.internal.jira.core.service.ServiceUnavailableException;
import org.eclipse.mylar.tasks.core.ITaskRepositoryListener;
import org.eclipse.mylar.tasks.core.TaskRepository;
import org.eclipse.mylar.tasks.ui.TasksUiPlugin;

/**
 * This class acts as a layer of indirection between clients in this project and
 * the server API implemented by the Jira Dashboard, and also abstracts some
 * Mylar implementation details. It initializes a jiraServer object and serves
 * as the central location to get a reference to it.
 * 
 * @author Mik Kersten
 * @author Wesley Coelho (initial integration patch)
 * @author Steffen Pingel
 */
public class JiraServerFacade implements ITaskRepositoryListener {

	public final static String MIN_VERSION = "3.3.3";

	public final static int MIN_BUILD_NUMBER = 99;

	private ServerManager serverManager = null;

	private static JiraServerFacade instance = null;

	public JiraServerFacade() {
		TasksUiPlugin.getRepositoryManager().addListener(this);
		serverManager = JiraCorePlugin.getDefault().getServerManager();
	}

	/**
	 * Lazily creates server.
	 * 
	 * @see #validateServerAndCredentials(String, String, String, Proxy, String,
	 *      String)
	 */
	public synchronized JiraServer getJiraServer(TaskRepository repository) {
		try {
			String serverHostname = getServerHost(repository);
			JiraServer server = serverManager.getServer(serverHostname);
			if (server == null) {
				String userName = repository.getUserName();
				String password = repository.getPassword();
				server = serverManager.addServer(serverHostname, repository.getUrl(), //
						Boolean.parseBoolean(repository.getProperty(JiraRepositoryConnector.COMPRESSION_KEY)), //
						userName == null ? "" : userName, //
						password == null ? "" : password, //
						repository.getProxy(), //
						repository.getHttpUser(), repository.getHttpPassword());
			}
			return server;
		} catch (ServiceUnavailableException sue) {
			throw sue;
		} catch (RuntimeException e) {
			MylarStatusHandler.log("Error connecting to Jira Server", this);
			throw e;
		}
	}

	public synchronized static JiraServerFacade getDefault() {
		if (instance == null) {
			instance = new JiraServerFacade();
		}
		return instance;
	}

	public synchronized void logOutFromAll() {
		try {
			JiraServer[] allServers = serverManager.getAllServers();
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
		if (repository.getKind().equals(JiraUiPlugin.REPOSITORY_KIND)) {
			getJiraServer(repository);
		}
	}

	public synchronized void repositoryRemoved(TaskRepository repository) {
		if (repository.getKind().equals(JiraUiPlugin.REPOSITORY_KIND)) {
			String serverHostname = getServerHost(repository);
			JiraServer server = serverManager.getServer(serverHostname);
			removeServer(server);
		}
	}

	public synchronized void repositorySettingsChanged(TaskRepository repository) {
		repositoryRemoved(repository);
		repositoryAdded(repository);
	}

	/**
	 * Forces a reset of the {@link JiraServer} object and updates the
	 * repository configuration.
	 */
	public synchronized void refreshServerSettings(TaskRepository repository, IProgressMonitor monitor) {
		// XXX disabled because this breaks objects holding a reference to the
		// same server
		// XXX this block of code is a work around: bug 164543, bug 167697
		// String serverHostname = getServerHost(repository);
		// JiraServer server = serverManager.getServer(serverHostname);
		// if (server != null) {
		// server.logout();
		// serverManager.removeServer(server);
		// }

		JiraServer server = getJiraServer(repository);
		server.refreshDetails(monitor);
	}

	private synchronized void removeServer(JiraServer server) {
		if (server != null) {
			server.logout();
			serverManager.removeServer(server);
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
	 * @return String describing validation failure or null if the details are
	 *         valid
	 */
	public void validateServerAndCredentials(String serverUrl, String user, String password, Proxy proxy,
			String httpUser, String httpPassword) throws JiraException {
		ServerInfo info = serverManager.testConnection(serverUrl, user, password, proxy, httpUser, httpPassword);
		if (MIN_VERSION.compareTo(info.getVersion()) > 0) {
			throw new JiraException("Mylar requires JIRA version " + MIN_VERSION + " or later");
		}
	}

	private static String getServerHost(TaskRepository repository) {
		try {
			return new URL(repository.getUrl()).getHost();
		} catch (MalformedURLException ex) {
			throw new RuntimeException("Invalid url " + repository.getUrl(), ex);
		}
	}

	/**
	 * TODO: refactor
	 */
	public static void handleConnectionException(Exception e) {
		if (e instanceof ServiceUnavailableException) {
			MylarStatusHandler.fail(e, "Jira Connection Failure.\n\n"
					+ "Please check your network connection and Jira server settings in the Task Repositories view.",
					true);
		} else if (e instanceof AuthenticationException) {
			MylarStatusHandler.fail(e, "Jira Authentication Failed.\n\n"
					+ "Please check your Jira username and password in the Task Repositories view", true);
		} else if (e instanceof RuntimeException) {
			MylarStatusHandler.fail(e, "No Jira repository found.\n\n"
					+ "Please verify that a vaild Jira repository exists in the Task Repositories view", true);
		} else {
			MylarStatusHandler.fail(e, "Could not connect to Jira repository.\n\n"
					+ "Please check your credentials in the Task Repositories view", true);
		}
	}

}
