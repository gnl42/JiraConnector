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
import org.eclipse.mylar.internal.jira.core.service.AbstractJiraServer;
import org.eclipse.mylar.internal.jira.core.service.JiraAuthenticationException;
import org.eclipse.mylar.internal.jira.core.service.JiraException;
import org.eclipse.mylar.internal.jira.core.service.JiraServer;
import org.eclipse.mylar.internal.jira.core.service.JiraServerData;
import org.eclipse.mylar.internal.jira.core.service.JiraServiceUnavailableException;
import org.eclipse.mylar.internal.jira.core.service.soap.JiraRpcServer;

/**
 * Note: This class is not thread safe.
 * 
 * @author Brock Janiczak
 * @author Steffen Pingel
 */
public class ServerManager {

	public static final String CONFIGURATION_DATA_FILENAME = "repositoryConfigurations";
	
	public static final int CONFIGURATION_DATA_VERSION = 1;
	
	/** The directory that contains the repository configuration data. */
	private final File cacheLocation;

	private Map<String, AbstractJiraServer> serverByUrl = new HashMap<String, AbstractJiraServer>();

	private Map<String, JiraServerData> serverDataByUrl = new HashMap<String, JiraServerData>();

	private List<JiraServerListener> listeners = new ArrayList<JiraServerListener>();

	public ServerManager(File cacheLocation) {
		this.cacheLocation = cacheLocation;
	}

	protected void start() {
		// on first load the cache may not exist
		cacheLocation.mkdirs();

		File file = new File(cacheLocation, CONFIGURATION_DATA_FILENAME);
		if (!file.exists()) {
			// clean up legacy data
			File[] servers = this.cacheLocation.listFiles();
			for (File directory : servers) {
				File oldData = new File(directory, "server.ser");
				if (oldData.exists()) {
					oldData.delete();
					directory.delete();
				}
			}
		} else {
			ObjectInputStream in = null;
			try {
				in = new ObjectInputStream(new BufferedInputStream(new FileInputStream(file)));
				in.readInt(); // version
				int count = in.readInt();
				for (int i = 0; i < count; i++) {
					String url = (String) in.readObject();
					JiraServerData data = (JiraServerData) in.readObject();
					serverDataByUrl.put(url, data);					
				}
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
		File file = new File(cacheLocation, CONFIGURATION_DATA_FILENAME);

		// update data map from servers
		for (String url : serverByUrl.keySet()) {
			serverDataByUrl.put(url, serverByUrl.get(url).getData());	
		}

		ObjectOutputStream out = null;
		try {
			out = new ObjectOutputStream(new BufferedOutputStream(new FileOutputStream(file)));
			out.writeInt(CONFIGURATION_DATA_VERSION);
			out.writeInt(serverDataByUrl.size());
			for (String url : serverDataByUrl.keySet()) {
				out.writeObject(url);
				out.writeObject(serverDataByUrl.get(url));	
			}
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
	 * @throws JiraAuthenticationException
	 *             URL was valid but username and password were incorrect
	 * @throws JiraServiceUnavailableException
	 *             URL was not valid
	 */
	public ServerInfo testConnection(String baseUrl, String username, String password, Proxy proxy, String httpUser,
			String httpPassword) throws JiraException {
		JiraServer server = createServer(baseUrl, false, username, password, proxy, httpUser,
				httpPassword);
		server.refreshServerInfo(new NullProgressMonitor());
		return server.getServerInfo();
	}

	public JiraServer getServer(String url) {
		return serverByUrl.get(url);
	}

	public JiraServer[] getAllServers() {
		return serverByUrl.values().toArray(new JiraServer[serverByUrl.size()]);
	}

	private AbstractJiraServer createServer(String baseUrl, boolean useCompression, String username,
			String password, Proxy proxy, String httpUser, String httpPassword) {
		if (baseUrl.charAt(baseUrl.length() - 1) == '/') {
			baseUrl = baseUrl.substring(0, baseUrl.length() - 1);
		}

		JiraRpcServer server = new JiraRpcServer(baseUrl, useCompression, username, password, proxy,
				httpUser, httpPassword);
		return server;
	}

	public JiraServer addServer(String baseUrl, boolean useCompression, String username,
			String password, Proxy proxy, String httpUser, String httpPassword) {
		if (serverByUrl.containsKey(baseUrl)) {
			throw new RuntimeException("A server with that name already exists");
		}
		
		AbstractJiraServer server = createServer(baseUrl, useCompression, username, password, proxy, httpUser, httpPassword);
		JiraServerData data = serverDataByUrl.get(baseUrl);
		if (data != null) {
			server.setData(data);
		}
		serverByUrl.put(baseUrl, server);
		
		fireServerAddded(server);
		
		return server;
	}

	public void removeServer(JiraServer server) {
		serverDataByUrl.remove(server.getBaseURL());
		serverByUrl.remove(server.getBaseURL());

		fireServerRemoved(server);
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

	public void removeAllServers(boolean clearData) {
		if (clearData) {
			serverDataByUrl.clear();
		}
		serverByUrl.clear();
	}
	
}
