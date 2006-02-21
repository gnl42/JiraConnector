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

import org.eclipse.mylar.internal.core.util.MylarStatusHandler;
import org.eclipse.mylar.provisional.tasklist.ITaskRepositoryListener;
import org.eclipse.mylar.provisional.tasklist.MylarTaskListPlugin;
import org.eclipse.mylar.provisional.tasklist.TaskRepository;
import org.tigris.jira.core.JiraCorePlugin;
import org.tigris.jira.core.ServerManager;
import org.tigris.jira.core.service.CachedRpcJiraServer;
import org.tigris.jira.core.service.JiraServer;
import org.tigris.jira.core.service.exceptions.AuthenticationException;
import org.tigris.jira.core.service.exceptions.ServiceUnavailableException;

/**
 * This class acts as a layer of indirection between clients in this project and
 * the server API implemented by the Jira Dashboard, and also abstracts some
 * Mylar implementation details. It initializes a jiraServer object and serves
 * as the central location to get a reference to it.
 * 
 * @author Wesley Coelho (initial integration patch)
 * @author Mik Kersten
 */
public class JiraServerFacade implements ITaskRepositoryListener {

	private ServerManager serverManager = null;

	private static JiraServerFacade instance = null;

	public JiraServerFacade() {
		MylarTaskListPlugin.getRepositoryManager().addListener(this);
		serverManager = JiraCorePlugin.getDefault().getServerManager();
	}

	/**
	 * Lazily creates server.
	 */
	public JiraServer getJiraServer(TaskRepository repository) {	
		try {
			String serverHostname = repository.getUrl().getHost();
			JiraServer server = serverManager.getServer(serverHostname);
			if (server == null) {
				server = serverManager.createServer(serverHostname, repository.getUrl().toExternalForm(), false, repository.getUserName(), repository.getPassword());
				serverManager.addServer(server);
			}
			server.login();
			return server;
		} catch (ServiceUnavailableException sue) {
			throw sue;
		} catch (RuntimeException e) {
			MylarStatusHandler.log("Error connecting to Jira Server", this);
			throw e;
		}
	}

//	/**
//	 * Initializes the jiraServer object and logs in so that requests can be
//	 * made on it.
//	 */
//	private JiraServer initJiraServer(TaskRepository repository) {
//		try {
//			jiraServer = serverManager.createServer(SERVER_NAME, repository.getUrl().toExternalForm(), false, repository.getUserName(), repository.getPassword());
//			serverManager.removeServer(jiraServer);
//			serverManager.addServer(jiraServer);
//
//			jiraServer.login();
//		} catch (ServiceUnavailableException sue) {
//			jiraServer = null;
//			throw sue;
//		} catch (RuntimeException e) {
//			jiraServer = null;
//			MylarStatusHandler.log("Error connecting to Jira Server", this);
//			throw e;
//		}
//		return jiraServer;
//	}

	public static JiraServerFacade getDefault() {
		if (instance == null) {
			instance = new JiraServerFacade();
		}
		return instance;
	}

	public void logOutFromAll() {
		try {
			JiraServer[] allServers = serverManager.getAllServers();
			for (int i = 0; i < allServers.length; i++) {
				allServers[i].logout();
			}
//			if (jiraServer != null) {
//				jiraServer.logout();
//				serverManager.removeServer(jiraServer);
//				jiraServer = null;
//			}
		} catch (Exception e) {
			MylarStatusHandler.log(e, "Error logging out of Jira Server");
		}
	}

	/**
	 * Notifies of changes to the set of Task Repositories. Creates a new jira
	 * server with the updated server information.
	 */
	public void repositorySetUpdated() {
//		try {
//			if (jiraServer != null) {
//				jiraServer.logout();
//			}
//			jiraServer = null;
//			jiraServer = getJiraServer();
//		} catch (Exception e) {
//			MylarStatusHandler.fail(e, "Failed to connect to server after repository settings change", true);
//		}
	}

	/** Returns true if all of the serverURL, user name, and password are valid */
	public boolean validateServerAndCredentials(String serverUrl, String user, String password) {
		try {
			// TODO: use test method on ServerManager
			CachedRpcJiraServer jiraServer = new CachedRpcJiraServer("ConnectionTest", serverUrl, false, user, password);
			jiraServer.login();
			jiraServer.logout();
		} catch (Exception e) {
			return false;
		}
		return true;
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
