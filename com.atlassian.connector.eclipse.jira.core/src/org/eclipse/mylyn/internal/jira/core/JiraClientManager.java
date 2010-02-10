/*******************************************************************************
 * Copyright (c) 2004, 2009 Brock Janiczak and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Brock Janiczak - initial API and implementation
 *     Tasktop Technologies - improvements
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
import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.mylyn.commons.core.StatusHandler;
import org.eclipse.mylyn.commons.net.AbstractWebLocation;
import org.eclipse.mylyn.internal.jira.core.model.ServerInfo;
import org.eclipse.mylyn.internal.jira.core.service.JiraAuthenticationException;
import org.eclipse.mylyn.internal.jira.core.service.JiraClient;
import org.eclipse.mylyn.internal.jira.core.service.JiraClientData;
import org.eclipse.mylyn.internal.jira.core.service.JiraConfiguration;
import org.eclipse.mylyn.internal.jira.core.service.JiraException;
import org.eclipse.mylyn.internal.jira.core.service.JiraServiceUnavailableException;

/**
 * Note: This class is not thread safe.
 * 
 * @author Brock Janiczak
 * @author Steffen Pingel
 */
public class JiraClientManager {

	public static final String CONFIGURATION_DATA_FILENAME = "repositoryConfigurations"; //$NON-NLS-1$

	public static final int CONFIGURATION_DATA_VERSION = 1;

	/** The directory that contains the repository configuration data. */
	private final File cacheLocation;

	private final Map<String, JiraClient> clientByUrl = new HashMap<String, JiraClient>();

	private final Map<String, JiraClientData> clientDataByUrl = new HashMap<String, JiraClientData>();

	public JiraClientManager(File cacheLocation) {
		this.cacheLocation = cacheLocation;
	}

	protected void start() {
		// on first load the cache may not exist
		cacheLocation.mkdirs();

		File file = new File(cacheLocation, CONFIGURATION_DATA_FILENAME);
		if (!file.exists()) {
			// clean up legacy data
			File[] clients = this.cacheLocation.listFiles();
			if (clients != null) {
				for (File directory : clients) {
					File oldData = new File(directory, "server.ser"); //$NON-NLS-1$
					if (oldData.exists()) {
						oldData.delete();
						directory.delete();
					}
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
						"Reset JIRA repository configuration cache due to format change")); //$NON-NLS-1$
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

		// update data map from clients
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
					"Error writing JIRA repository configuration cache", e)); //$NON-NLS-1$
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
	public ServerInfo validateConnection(AbstractWebLocation location, JiraConfiguration configuration,
			IProgressMonitor monitor) throws JiraException {
		JiraClient client = createClient(location, configuration);
		return client.getServerInfo(monitor);
	}

	public JiraClient getClient(String url) {
		return clientByUrl.get(url);
	}

	public JiraClient[] getAllClients() {
		return clientByUrl.values().toArray(new JiraClient[clientByUrl.size()]);
	}

	private JiraClient createClient(AbstractWebLocation location, JiraConfiguration configuration) {
//		if (baseUrl.charAt(baseUrl.length() - 1) == '/') {
//			baseUrl = baseUrl.substring(0, baseUrl.length() - 1);
//		}
		return new JiraClient(location, configuration);
	}

	public JiraClient addClient(AbstractWebLocation location, JiraConfiguration configuration) {
		if (clientByUrl.containsKey(location.getUrl())) {
			throw new RuntimeException("A client with that name already exists"); //$NON-NLS-1$
		}

		JiraClient client = createClient(location, configuration);
		JiraClientData data = clientDataByUrl.get(location.getUrl());
		if (data != null) {
			client.getCache().setData(data);
		}
		clientByUrl.put(location.getUrl(), client);

		return client;
	}

	public void refreshClient() {

	}

	public void removeClient(JiraClient client, boolean clearData) {
		// TODO trigger logout?
		if (clearData) {
			clientDataByUrl.remove(client.getBaseUrl());
		} else {
			clientDataByUrl.put(client.getBaseUrl(), client.getCache().getData());
		}
		clientByUrl.remove(client.getBaseUrl());
	}

	public void removeAllClients(boolean clearData) {
		if (clearData) {
			clientDataByUrl.clear();
		}
		clientByUrl.clear();
	}

}
