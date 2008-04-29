/*******************************************************************************
 * Copyright (c) 2004, 2007 Mylyn project committers and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.mylyn.internal.jira.core;

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
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.mylyn.commons.core.StatusHandler;
import org.eclipse.mylyn.internal.jira.core.model.ServerInfo;
import org.eclipse.mylyn.internal.jira.core.service.JiraAuthenticationException;
import org.eclipse.mylyn.internal.jira.core.service.JiraClient;
import org.eclipse.mylyn.internal.jira.core.service.JiraClientData;
import org.eclipse.mylyn.internal.jira.core.service.JiraException;
import org.eclipse.mylyn.internal.jira.core.service.JiraServiceUnavailableException;
import org.eclipse.mylyn.web.core.AbstractWebLocation;

/**
 * Note: This class is not thread safe.
 * 
 * @author Brock Janiczak
 * @author Steffen Pingel
 */
public class JiraClientManager {

	public static final String CONFIGURATION_DATA_FILENAME = "repositoryConfigurations";

	public static final int CONFIGURATION_DATA_VERSION = 1;

	/** The directory that contains the repository configuration data. */
	private final File cacheLocation;

	private final Map<String, JiraClient> clientByUrl = new HashMap<String, JiraClient>();

	private final Map<String, JiraClientData> clientDataByUrl = new HashMap<String, JiraClientData>();

	private final List<JiraClientListener> listeners = new ArrayList<JiraClientListener>();

	public JiraClientManager(File cacheLocation) {
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
					JiraClientData data = (JiraClientData) in.readObject();
					clientDataByUrl.put(url, data);
				}
			} catch (Throwable e) {
				StatusHandler.log(new Status(IStatus.INFO, JiraCorePlugin.ID_PLUGIN,
						"Reset JIRA repository configuration cache due to format change"));
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
		for (String url : clientByUrl.keySet()) {
			clientDataByUrl.put(url, clientByUrl.get(url).getCache().getData());
		}

		ObjectOutputStream out = null;
		try {
			out = new ObjectOutputStream(new BufferedOutputStream(new FileOutputStream(file)));
			out.writeInt(CONFIGURATION_DATA_VERSION);
			out.writeInt(clientDataByUrl.size());
			for (String url : clientDataByUrl.keySet()) {
				out.writeObject(url);
				out.writeObject(clientDataByUrl.get(url));
			}
		} catch (Throwable e) {
			StatusHandler.log(new Status(IStatus.WARNING, JiraCorePlugin.ID_PLUGIN,
					"Error writing JIRA repository configuration cache", e));
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
	 * Tests the connection to a server. If the URL is invalid ot the username and password are invalid this method will
	 * return with a exceptions carrying the failure reason.
	 * 
	 * @param monitor
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
	public ServerInfo validateConnection(AbstractWebLocation location, IProgressMonitor monitor) throws JiraException {
		JiraClient client = createClient(location, false);
		return client.getServerInfo(monitor);
	}

	public JiraClient getClient(String url) {
		return clientByUrl.get(url);
	}

	public JiraClient[] getAllClients() {
		return clientByUrl.values().toArray(new JiraClient[clientByUrl.size()]);
	}

	private JiraClient createClient(AbstractWebLocation location, boolean useCompression) {
//		if (baseUrl.charAt(baseUrl.length() - 1) == '/') {
//			baseUrl = baseUrl.substring(0, baseUrl.length() - 1);
//		}

		JiraClient clienht = new JiraClient(location, useCompression);
		return clienht;
	}

	public JiraClient addClient(AbstractWebLocation location, String characterEncoding, boolean useCompression) {
		if (clientByUrl.containsKey(location.getUrl())) {
			throw new RuntimeException("A server with that name already exists");
		}

		JiraClient server = createClient(location, useCompression);
		server.setCharacterEncoding(characterEncoding);
		JiraClientData data = clientDataByUrl.get(location.getUrl());
		if (data != null) {
			server.getCache().setData(data);
		}
		clientByUrl.put(location.getUrl(), server);

		fireClientAddded(server);

		return server;
	}

	public void refreshClient() {

	}

	public void removeClient(JiraClient server) {
		clientDataByUrl.remove(server.getBaseUrl());
		clientByUrl.remove(server.getBaseUrl());

		fireClientRemoved(server);
	}

	public void addClientListener(JiraClientListener listener) {
		listeners.add(listener);
	}

	public void removeClientListener(JiraClientListener listener) {
		listeners.remove(listener);
	}

	private void fireClientRemoved(JiraClient server) {
		for (JiraClientListener listener : listeners) {
			listener.clientRemoved(server);
		}
	}

	private void fireClientAddded(JiraClient server) {
		for (JiraClientListener listener : listeners) {
			listener.clientAdded(server);
		}
	}

	public void removeAllClients(boolean clearData) {
		if (clearData) {
			clientDataByUrl.clear();
		}
		clientByUrl.clear();
	}

}
