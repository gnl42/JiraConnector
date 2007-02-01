/*******************************************************************************
 * Copyright (c) 2007 Mylar committers and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Brock Janiczak - initial API and implementation
 *******************************************************************************/

package org.eclipse.mylar.internal.jira.core;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.mylar.internal.jira.core.model.ServerInfo;
import org.eclipse.mylar.internal.jira.core.service.AuthenticationException;
import org.eclipse.mylar.internal.jira.core.service.CachedRpcJiraServer;
import org.eclipse.mylar.internal.jira.core.service.JiraServer;
import org.eclipse.mylar.internal.jira.core.service.ServiceUnavailableException;

public class ServerManager {

	private final File cacheLocation;

	private Map<String, JiraServer> serverByName = new HashMap<String, JiraServer>();

	// TODO Use a decent listener list
	private List<JiraServerListener> listeners = new ArrayList<JiraServerListener>();

	public ServerManager(File cacheLocation) {
		this.cacheLocation = cacheLocation;
	}

	protected void start() {
		// On first load the cache may not exist
		cacheLocation.mkdirs();

		File[] servers = this.cacheLocation.listFiles();
		for (int i = 0; i < servers.length; i++) {
			File serverCache = servers[i];
			File serverFile = new File(serverCache, "server.ser");

			ObjectInputStream ois = null;
			try {
				ois = new ObjectInputStream(new BufferedInputStream(new FileInputStream(serverFile)));
				JiraServer server = (JiraServer) ois.readObject();
				// TODO reconnect the services depending on user preferences

				serverByName.put(serverCache.getName(), server);
				ois.close();
			} catch (IOException e) {
				e.printStackTrace();
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			} finally {
				if (ois != null) {
					try {
						ois.close();
					} catch (IOException e) {
					}
				}
			}
		}
	}

	protected void stop() {
		for (Iterator<JiraServer> iServers = serverByName.values().iterator(); iServers.hasNext();) {
			JiraServer server = iServers.next();

			ObjectOutputStream oos = null;
			try {
				File cacheDir = new File(cacheLocation, server.getName());
				cacheDir.mkdirs();

				oos = new ObjectOutputStream(new BufferedOutputStream(new FileOutputStream(new File(cacheDir,
						"server.ser"))));
				oos.writeObject(server);
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				if (oos != null) {
					try {
						oos.close();
					} catch (IOException e) {
					}
				}
			}
		}
	}

	/**
	 * Tests the connection to a server. If the URL is invalid ot the username
	 * and password are invalid this method will return with a exceptions
	 * carrying the failure reason.
	 * 
	 * @param baseUrl
	 *            Base URL of the jira installation
	 * @param username
	 *            username to connect with
	 * @param password
	 *            Password to connect with
	 * @return Short string describing the server information
	 * @throws AuthenticationException
	 *             URL was valid but username and password were incorrect
	 * @throws ServiceUnavailableException
	 *             URL was not valid
	 */
	public String testConnection(String baseUrl, String username, String password) throws AuthenticationException,
			ServiceUnavailableException {
		JiraServer server = createServer("Connection Test", baseUrl, false, username, password);
		ServerInfo serverInfo = server.getServerInfo();
		return "Jira " + serverInfo.getEdition() + " " + serverInfo.getVersion() + " #" + serverInfo.getBuildNumber();
	}

	public JiraServer getServer(String name) {
		return serverByName.get(name);
	}

	public JiraServer[] getAllServers() {
		return serverByName.values().toArray(new JiraServer[serverByName.size()]);
	}

	public JiraServer createServer(String name, String baseUrl, boolean hasSlowConnection, String username,
			String password) {
		if (baseUrl.charAt(baseUrl.length() - 1) == '/') {
			baseUrl = baseUrl.substring(0, baseUrl.length() - 1);
		}

		JiraServer server = new CachedRpcJiraServer(name, baseUrl, hasSlowConnection, username, password);
		return server;
	}

	public void addServer(JiraServer server) {
		if (serverByName.containsKey(server.getName())) {
			throw new RuntimeException("A server with that name already exists");
		}
		serverByName.put(server.getName(), server);
		fireServerAddded(server);
	}

	public void removeServer(JiraServer server) {
		serverByName.remove(server.getName());

		File serverCache = new File(this.cacheLocation, server.getName());
		if (serverCache.exists()) {
			recursiveDelete(serverCache);
		}
		fireServerRemoved(server);
	}

	/**
	 * TODO need to make this a bit smarter. Perhaps have an object to hold
	 * connectino info
	 * 
	 * @param name
	 * @param baseURL
	 * @param username
	 * @param password
	 */
	public void updateServerDetails(String name, String baseURL, boolean hasSlowConnection, String username,
			String password) {
		CachedRpcJiraServer server = (CachedRpcJiraServer) serverByName.get(name);
		// TODO we should really have a modify event
		fireServerRemoved(server);

		// TODO need to flush the server cache since we are possibly a different
		// person
		server.setBaseURL(baseURL);
		server.setSlowConnection(hasSlowConnection);
		server.setCurrentUserName(username);
		server.setCurrentPassword(password);

		fireServerAddded(server);
	}

	public void addServerListener(JiraServerListener listener) {
		listeners.add(listener);
	}

	public void removeServerListener(JiraServerListener listener) {
		listeners.remove(listener);
	}

	private void fireServerRemoved(JiraServer server) {
		for (Iterator<JiraServerListener> iListeners = listeners.iterator(); iListeners.hasNext();) {
			JiraServerListener listener = iListeners.next();
			listener.serverRemoved(server);
		}
	}

	private void fireServerAddded(JiraServer server) {
		for (Iterator<JiraServerListener> iListeners = listeners.iterator(); iListeners.hasNext();) {
			JiraServerListener listener = iListeners.next();
			listener.serverAdded(server);
		}
	}

	private void recursiveDelete(File baseFile) {
		if (baseFile.isFile()) {
			baseFile.delete();
		} else {
			File[] children = baseFile.listFiles();
			for (int i = 0; i < children.length; i++) {
				File file = children[i];
				recursiveDelete(file);
			}
			baseFile.delete();
		}
	}
}
