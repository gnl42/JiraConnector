/*******************************************************************************
 * Copyright (c) 2009 Atlassian and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Atlassian - initial API and implementation
 ******************************************************************************/

package me.glindholm.connector.eclipse.internal.bamboo.core;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.mylyn.commons.core.StatusHandler;
import org.eclipse.mylyn.commons.net.AbstractWebLocation;
import org.eclipse.mylyn.commons.workbench.WorkbenchUtil;
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.eclipse.ui.PlatformUI;

import me.glindholm.bamboo.model.RestInfo;
import me.glindholm.connector.eclipse.internal.bamboo.core.client.BambooClient;
import me.glindholm.connector.eclipse.internal.bamboo.core.client.BambooClientData;
import me.glindholm.connector.eclipse.internal.bamboo.core.service.BambooClientCache;
import me.glindholm.connector.eclipse.internal.bamboo.core.service.BambooLocalConfiguration;
import me.glindholm.theplugin.commons.remoteapi.RemoteApiException;

/**
 * Class to manage the clients and data on a per-repository basis
 *
 * @author Shawn Minto
 * @author Wojciech Seliga
 */
public class BambooClientManager {
    public static final String CONFIGURATION_DATA_FILENAME = "bambooRepositoryConfigurations"; //$NON-NLS-1$

    public static final int CONFIGURATION_DATA_VERSION = 1;

    private final Map<String, BambooClient> clientByUrl = new HashMap<>();

    private final Map<String, BambooClientData> clientDataByUrl = new HashMap<>();

    private final File cacheLocation;

    public BambooClientManager(final File cacheLocation) {
        Assert.isNotNull(cacheLocation);
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
                    final BambooClientData data = (BambooClientData) in.readObject();
                    clientDataByUrl.put(url, data);
                }
            } catch (final Throwable e) {
                final String msg = "Update Bamboo repository(ies) configuration due to format change";
                PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {
                    @Override
                    public void run() {
                        MessageDialog.openError(WorkbenchUtil.getShell(), "JiraConnector Bamboo Connector", msg);
                    }
                });

                StatusHandler.log(new Status(IStatus.INFO, BambooCorePlugin.ID_PLUGIN, msg)); // $NON-NLS-1$
            }
        }
    }

    protected void stop() {
        final File file = new File(cacheLocation, CONFIGURATION_DATA_FILENAME);

        // update data map from clients
        for (final String url : clientByUrl.keySet()) {
            final BambooClientCache cache = clientByUrl.get(url).getCache();
            final BambooClientData clientData = cache.getData();
            clientDataByUrl.put(url, clientData);
        }

        try (ObjectOutputStream out = new ObjectOutputStream(new BufferedOutputStream(new FileOutputStream(file)))) {
            out.writeInt(CONFIGURATION_DATA_VERSION);
            out.writeInt(clientDataByUrl.size());
            for (final String url : clientDataByUrl.keySet()) {
                out.writeObject(url);
                final BambooClientData clientData = clientDataByUrl.get(url);
                out.writeObject(clientData);
            }
        } catch (final Throwable e) {
            StatusHandler.log(new Status(IStatus.WARNING, BambooCorePlugin.ID_PLUGIN, "Error writing Bamboo repository configuration cache", e)); //$NON-NLS-1$
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
    public RestInfo validateConnection(final AbstractWebLocation location, final BambooLocalConfiguration configuration, final IProgressMonitor monitor)
            throws RemoteApiException {
        final BambooClient client = createClient(location, configuration);

        client.getCurrentUser(monitor);

        return client.getServerInfo(monitor);
    }

    public BambooClient getClient(final TaskRepository repository) {
        return clientByUrl.get(repository.getUrl());
    }

    public Collection<BambooClient> getAllClients() {
        return clientByUrl.values();
    }

    private BambooClient createClient(final AbstractWebLocation location, final BambooLocalConfiguration configuration) {
        // if (baseUrl.charAt(baseUrl.length() - 1) == '/') {
        // baseUrl = baseUrl.substring(0, baseUrl.length() - 1);
        // }
        return new BambooClient(location, configuration);
    }

    public BambooClient addClient(final AbstractWebLocation location, final BambooLocalConfiguration configuration) {
        if (clientByUrl.containsKey(location.getUrl())) {
            throw new RuntimeException("A client with that url already exists"); //$NON-NLS-1$
        }

        final BambooClient client = createClient(location, configuration);
        final BambooClientData data = clientDataByUrl.get(location.getUrl());
        if (data != null) {
            client.getCache().setData(data);
        }
        clientByUrl.put(location.getUrl(), client);

        return client;
    }

    public void refreshClient() {

    }

    public void removeClient(final BambooClient client, final boolean clearData) {
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

    @Override
    public String toString() {
        final StringBuilder builder = new StringBuilder();
        builder.append("BambooClientManager [clientByUrl=").append(clientByUrl).append(", clientDataByUrl=").append(clientDataByUrl).append(", cacheLocation=")
                .append(cacheLocation).append("]");
        return builder.toString();
    }

}
