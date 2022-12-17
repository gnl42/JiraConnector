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

package me.glindholm.connector.eclipse.internal.jira.core;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.mylyn.commons.core.StatusHandler;
import org.eclipse.mylyn.commons.net.AbstractWebLocation;
import org.eclipse.mylyn.commons.workbench.WorkbenchUtil;
import org.eclipse.ui.PlatformUI;

import me.glindholm.connector.eclipse.internal.jira.core.model.JiraServerInfo;
import me.glindholm.connector.eclipse.internal.jira.core.service.JiraAuthenticationException;
import me.glindholm.connector.eclipse.internal.jira.core.service.JiraClient;
import me.glindholm.connector.eclipse.internal.jira.core.service.JiraClientData;
import me.glindholm.connector.eclipse.internal.jira.core.service.JiraException;
import me.glindholm.connector.eclipse.internal.jira.core.service.JiraLocalConfiguration;
import me.glindholm.connector.eclipse.internal.jira.core.service.JiraServiceUnavailableException;

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

    private final Map<String, JiraClient> clientByUrl = new HashMap<>();

    private final Map<String, JiraClientData> clientDataByUrl = new HashMap<>();

    public JiraClientManager(final File cacheLocation) {
        this.cacheLocation = cacheLocation;
    }

    protected void start() {
        // on first load the cache may not exist
        cacheLocation.mkdirs();

        final File file = new File(cacheLocation, CONFIGURATION_DATA_FILENAME);
        if (!file.exists()) {
            // clean up legacy data
            final File[] clients = cacheLocation.listFiles();
            if (clients != null) {
                for (final File directory : clients) {
                    final File oldData = new File(directory, "server.ser"); //$NON-NLS-1$
                    if (oldData.exists()) {
                        oldData.delete();
                        directory.delete();
                    }
                }
            }
        } else {
            try (ObjectInputStream in = new ObjectInputStream(new BufferedInputStream(new FileInputStream(file)))) {
                in.readInt(); // version
                final int count = in.readInt();
                for (int i = 0; i < count; i++) {
                    final String url = (String) in.readObject();
                    final JiraClientData data = (JiraClientData) in.readObject();
                    clientDataByUrl.put(url, data);
                }
            } catch (final Throwable e) {
                final String msg = "Update JIRA repository(ies) configuration due to format change";
                PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {
                    @Override
                    public void run() {
                        MessageDialog.openError(WorkbenchUtil.getShell(), "JiraConnector JIRA Connector", msg);
                    }
                });

                StatusHandler.log(new Status(IStatus.INFO, JiraCorePlugin.ID_PLUGIN, msg)); // $NON-NLS-1$
            }
        }
    }

    protected void stop() {
        final File file = new File(cacheLocation, CONFIGURATION_DATA_FILENAME);

        // update data map from clients
        for (final String url : clientByUrl.keySet()) {
            clientDataByUrl.put(url, clientByUrl.get(url).getCache().getData());
        }

        try (ObjectOutputStream out = new ObjectOutputStream(new BufferedOutputStream(new FileOutputStream(file)))) {
            out.writeInt(CONFIGURATION_DATA_VERSION);
            out.writeInt(clientDataByUrl.size());
            for (final String url : clientDataByUrl.keySet()) {
                out.writeObject(url);
                out.writeObject(clientDataByUrl.get(url));
            }
        } catch (final Throwable e) {
            StatusHandler.log(new Status(IStatus.WARNING, JiraCorePlugin.ID_PLUGIN, "Error writing JIRA repository configuration cache", e)); //$NON-NLS-1$
        }
    }

    /**
     * Tests the connection to a server. If the URL is invalid ot the username and
     * password are invalid this method will return with a exceptions carrying the
     * failure reason.
     *
     * @param monitor
     * @param baseUrl  Base URL of the jira installation
     * @param username username to connect with
     * @param password Password to connect with
     * @return Short string describing the server information
     * @throws JiraAuthenticationException     URL was valid but username and
     *                                         password were incorrect
     * @throws JiraServiceUnavailableException URL was not valid
     */
    public JiraServerInfo validateConnection(final AbstractWebLocation location, final JiraLocalConfiguration configuration, final IProgressMonitor monitor)
            throws JiraException {
        final JiraClient client = createClient(location, configuration);

        client.getSession(monitor);

        return client.getServerInfo(monitor);
    }

    public JiraClient getClient(final String url) {
        return clientByUrl.get(url);
    }

    public JiraClient[] getAllClients() {
        return clientByUrl.values().toArray(new JiraClient[clientByUrl.size()]);
    }

    private JiraClient createClient(final AbstractWebLocation location, final JiraLocalConfiguration configuration) {
        // if (baseUrl.charAt(baseUrl.length() - 1) == '/') {
        // baseUrl = baseUrl.substring(0, baseUrl.length() - 1);
        // }
        return new JiraClient(location, configuration);
    }

    public JiraClient addClient(final AbstractWebLocation location, final JiraLocalConfiguration configuration) {
        if (clientByUrl.containsKey(location.getUrl())) {
            throw new RuntimeException("A client with that url already exists"); //$NON-NLS-1$
        }

        final JiraClient client = createClient(location, configuration);
        final JiraClientData data = clientDataByUrl.get(location.getUrl());
        if (data != null) {
            client.getCache().setData(data);
        }
        clientByUrl.put(location.getUrl(), client);

        return client;
    }

    public void refreshClient() {

    }

    public void removeClient(final JiraClient client, final boolean clearData) {
        // TODO trigger logout?
        if (clearData) {
            clientDataByUrl.remove(client.getBaseUrl());
        } else {
            clientDataByUrl.put(client.getBaseUrl(), client.getCache().getData());
        }
        clientByUrl.remove(client.getBaseUrl());
    }

    public void removeAllClients(final boolean clearData) {
        if (clearData) {
            clientDataByUrl.clear();
        }
        clientByUrl.clear();
    }

}
