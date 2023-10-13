/*******************************************************************************
 * Copyright (c) 2004, 2009 Tasktop Technologies and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Tasktop Technologies - initial API and implementation
 *******************************************************************************/

package me.glindholm.connector.eclipse.internal.jira.core;

import java.net.Proxy;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.mylyn.commons.net.AbstractWebLocation;
import org.eclipse.mylyn.internal.tasks.core.IRepositoryChangeListener;
import org.eclipse.mylyn.internal.tasks.core.TaskRepositoryChangeEvent;
import org.eclipse.mylyn.internal.tasks.core.TaskRepositoryDelta.Type;
import org.eclipse.mylyn.tasks.core.IRepositoryListener;
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.eclipse.mylyn.tasks.core.TaskRepositoryLocationFactory;

import me.glindholm.connector.eclipse.internal.jira.core.model.JiraServerInfo;
import me.glindholm.connector.eclipse.internal.jira.core.model.JiraServerVersion;
import me.glindholm.connector.eclipse.internal.jira.core.service.JiraClient;
import me.glindholm.connector.eclipse.internal.jira.core.service.JiraException;
import me.glindholm.connector.eclipse.internal.jira.core.service.JiraLocalConfiguration;
import me.glindholm.connector.eclipse.internal.jira.core.util.JiraUtil;

/**
 * This class acts as a layer of indirection between clients in this project and
 * the server API implemented by the Jira Dashboard, and also abstracts some
 * Mylyn implementation details. It initializes a jiraServer object and serves
 * as the central location to get a reference to it.
 *
 * @author Mik Kersten
 * @author Wesley Coelho (initial integration patch)
 * @author Steffen Pingel
 */
public class JiraClientFactory implements IRepositoryListener, IRepositoryChangeListener, IJiraClientFactory {

    private static JiraClientFactory instance = null;

    private JiraClientManager clientManager = null;

    private TaskRepositoryLocationFactory taskRepositoryLocationFactory;

    private boolean forceTaskRepositoryLocationFactory;

    private JiraClientFactory() {
        taskRepositoryLocationFactory = new TaskRepositoryLocationFactory();
        clientManager = JiraCorePlugin.getClientManager();
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
     * Lazily creates {@link JiraClient} instance
     *
     * @see #validateConnection(String, String, String, Proxy, String, String)
     */
    @Override
    public synchronized JiraClient getJiraClient(final TaskRepository repository) {
        JiraClient client = clientManager.getClient(repository.getRepositoryUrl());
        if (client == null) {
            final AbstractWebLocation location = taskRepositoryLocationFactory.createWebLocation(repository);
            client = clientManager.addClient(location, JiraUtil.getLocalConfiguration(repository));
        }
        return client;
    }

    public synchronized static JiraClientFactory getDefault() {
        if (instance == null) {
            instance = new JiraClientFactory();
        }
        return instance;
    }

    public synchronized void logOutFromAll() {
        final JiraClient[] clients = clientManager.getAllClients();
        for (final JiraClient client : clients) {
            try {
                client.logout(null);
            } catch (final JiraException e) {
                // ignore
            }
        }
    }

    public void repositoriesRead() {
        // ignore
    }

    @Override
    public synchronized void repositoryAdded(final TaskRepository repository) {
        if (JiraCorePlugin.CONNECTOR_KIND.equals(repository.getConnectorKind())) {
            assert clientManager.getClient(repository.getRepositoryUrl()) == null;
            getJiraClient(repository);
        }
    }

    @Override
    public synchronized void repositoryRemoved(final TaskRepository repository) {
        if (JiraCorePlugin.CONNECTOR_KIND.equals(repository.getConnectorKind())) {
            final JiraClient client = clientManager.getClient(repository.getRepositoryUrl());
            if (client != null) {
                clientManager.removeClient(client, true);
            }
        }
    }

    @Override
    public synchronized void repositorySettingsChanged(final TaskRepository repository) {
        // handled by repositoryChanged()
    }

    @Override
    public synchronized void repositoryChanged(final TaskRepositoryChangeEvent event) {
        final TaskRepository repository = event.getRepository();
        if (JiraCorePlugin.CONNECTOR_KIND.equals(repository.getConnectorKind())) {
            final JiraClient client = clientManager.getClient(repository.getRepositoryUrl());
            if (client != null) {
                if (event.getDelta().getType() == Type.ALL) {
                    client.purgeSession();
                    updateClient(client, repository);
                } else if (event.getDelta().getType() == Type.CREDENTIALS || event.getDelta().getType() == Type.PROYX) {
                    updateClient(client, repository);
                    client.purgeSession();
                } else {
                    updateClient(client, repository);
                    client.purgeSession();
                }
            }
        }
    }

    private void updateClient(final JiraClient client, final TaskRepository repository) {
        final JiraLocalConfiguration configuration = JiraUtil.getLocalConfiguration(repository);
        if (!configuration.equals(client.getLocalConfiguration())) {
            client.setLocalConfiguration(configuration);
        }
    }

    public JiraServerInfo validateConnection(final AbstractWebLocation location, final IProgressMonitor monitor) throws JiraException {
        return validateConnection(location, new JiraLocalConfiguration(), monitor);
    }

    /**
     * Validate the server URL and user credentials
     *
     * @param monitor
     * @param serverUrl Location of the Jira Server
     * @param user      Username
     * @param password  Password
     * @return
     * @return String describing validation failure or null if the details are valid
     */
    public JiraServerInfo validateConnection(final AbstractWebLocation location, final JiraLocalConfiguration configuration, final IProgressMonitor monitor)
            throws JiraException {
        final JiraServerInfo info = clientManager.validateConnection(location, configuration, monitor);
        final JiraServerVersion serverVersion = info.getVersion();
        if (serverVersion.isLessThan(JiraServerVersion.MIN_VERSION)) {
            throw new JiraException("JIRA connector requires server " + JiraServerVersion.MIN_VERSION + " or later"); //$NON-NLS-1$ //$NON-NLS-2$
        }
        return info;
    }

    public TaskRepositoryLocationFactory getTaskRepositoryLocationFactory() {
        return taskRepositoryLocationFactory;
    }

    public void setTaskRepositoryLocationFactory(final TaskRepositoryLocationFactory taskRepositoryLocationFactory, final boolean force) {
        if (forceTaskRepositoryLocationFactory) {
            return;
        }

        forceTaskRepositoryLocationFactory = force;
        this.taskRepositoryLocationFactory = taskRepositoryLocationFactory;
    }

    @Override
    public void repositoryUrlChanged(final TaskRepository repository, final String oldUrl) {
        // nothing to do, the next call to getClient() will create a new client since
        // it's stored by URL
    }

}
