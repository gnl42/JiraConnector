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
import java.net.Proxy;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.mylar.core.MylarStatusHandler;
import org.eclipse.mylar.internal.jira.core.model.ServerInfo;
import org.eclipse.mylar.internal.jira.core.service.AuthenticationException;
import org.eclipse.mylar.internal.jira.core.service.CachedRpcJiraServer;
import org.eclipse.mylar.internal.jira.core.service.JiraServer;
import org.eclipse.mylar.internal.jira.core.service.JiraServerData;
import org.eclipse.mylar.internal.jira.core.service.ServiceUnavailableException;

/**
 * Note: This class is not thread safe.
 * 
 * @author Brock Janiczak
 * @author Steffen Pingel
 */
public class ServerManager {

	private final File cacheLocation;

	private Map<String, CachedRpcJiraServer> serverByName = new HashMap<String, CachedRpcJiraServer>();

	private Map<String, JiraServerData> serverDataByName = new HashMap<String, JiraServerData>();

	private List<JiraServerListener> listeners = new ArrayList<JiraServerListener>();

	public ServerManager(File cacheLocation) {
		this.cacheLocation = cacheLocation;
	}

	protected void start() {
		// on first load the cache may not exist
		cacheLocation.mkdirs();

		File[] servers = this.cacheLocation.listFiles();
		for (int i = 0; i < servers.length; i++) {
			File serverCache = servers[i];
			File serverFile = new File(serverCache, "server.ser");

			ObjectInputStream in = null;
			try {
				in = new ObjectInputStream(new BufferedInputStream(new FileInputStream(serverFile)));
				JiraServerData data = (JiraServerData) in.readObject();
				serverDataByName.put(serverCache.getName(), data);
			} catch (Throwable e) {
				MylarStatusHandler.log("Reset JIRA repository configuration cache due to format update", false);
			} finally {
				if (in != null) {
					try {
						in.close();
					} catch (IOException e) {
					}
				}
			}
		}
	}

	protected void stop() {
		for (CachedRpcJiraServer server : serverByName.values()) {
			ObjectOutputStream out = null;
			try {
				File cacheDir = new File(cacheLocation, server.getName());
				cacheDir.mkdirs();

				out = new ObjectOutputStream(new BufferedOutputStream(new FileOutputStream(new File(cacheDir,
						"server.ser"))));
				out.writeObject(server.getData());
			} catch (Throwable e) {
				MylarStatusHandler.fail(e, "Error writing JIRA repository configuration cache", false);
			} finally {
				if (out != null) {
					try {
						out.close();
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
	public ServerInfo testConnection(String baseUrl, String username, String password, Proxy proxy, String httpUser,
			String httpPassword) throws AuthenticationException, ServiceUnavailableException {
		JiraServer server = createServer("Connection Test", baseUrl, false, username, password, proxy, httpUser,
				httpPassword);
		server.refreshServerInfo(new NullProgressMonitor());
		return server.getServerInfo();
	}

	public JiraServer getServer(String name) {
		return serverByName.get(name);
	}

	public JiraServer[] getAllServers() {
		return serverByName.values().toArray(new JiraServer[serverByName.size()]);
	}

	private CachedRpcJiraServer createServer(String name, String baseUrl, boolean useCompression, String username,
			String password, Proxy proxy, String httpUser, String httpPassword) {
		if (baseUrl.charAt(baseUrl.length() - 1) == '/') {
			baseUrl = baseUrl.substring(0, baseUrl.length() - 1);
		}

		CachedRpcJiraServer server = new CachedRpcJiraServer(name, baseUrl, useCompression, username, password, proxy,
				httpUser, httpPassword);
		return server;
	}

	public JiraServer addServer(String name, String baseUrl, boolean useCompression, String username,
			String password, Proxy proxy, String httpUser, String httpPassword) {
		if (serverByName.containsKey(name)) {
			throw new RuntimeException("A server with that name already exists");
		}
		
		CachedRpcJiraServer server = createServer(name, baseUrl, useCompression, username, password, proxy, httpUser, httpPassword);
		JiraServerData data = serverDataByName.get(name);
		if (data != null) {
			server.setData(data);
		}
		serverByName.put(name, server);
		
		fireServerAddded(server);
		
		return server;
	}

	public void removeServer(JiraServer server) {
		serverDataByName.remove(server.getName());
		serverByName.remove(server.getName());

		File serverCache = new File(this.cacheLocation, server.getName());
		if (serverCache.exists()) {
			recursiveDelete(serverCache);
		}
		fireServerRemoved(server);
	}

	// /**
	// * TODO need to make this a bit smarter. Perhaps have an object to hold
	// * connectino info
	// *
	// * @param name
	// * @param baseURL
	// * @param username
	// * @param password
	// */
	// public void updateServerDetails(String name, String baseURL, boolean
	// hasSlowConnection, String username,
	// String password) {
	// CachedRpcJiraServer server = (CachedRpcJiraServer)
	// serverByName.get(name);
	// // TODO we should really have a modify event
	// fireServerRemoved(server);
	//
	// // TODO need to flush the server cache since we are possibly a different
	// // person
	// server.setBaseURL(baseURL);
	// server.setSlowConnection(hasSlowConnection);
	// server.setCurrentUserName(username);
	// server.setCurrentPassword(password);
	//
	// fireServerAddded(server);
	// }

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


	public void removeAllServers() {
		serverByName.clear();
	}
	
}
