/*******************************************************************************
 * Copyright (c) 2004, 2008 Tasktop Technologies and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Tasktop Technologies - initial API and implementation
 *******************************************************************************/
package me.glindholm.connector.eclipse.internal.core.client;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.mylyn.commons.net.AbstractWebLocation;
import org.eclipse.mylyn.internal.tasks.core.IRepositoryChangeListener;
import org.eclipse.mylyn.internal.tasks.core.TaskRepositoryChangeEvent;
import org.eclipse.mylyn.internal.tasks.core.TaskRepositoryDelta.Type;
import org.eclipse.mylyn.tasks.core.IRepositoryListener;
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.eclipse.mylyn.tasks.core.TaskRepositoryLocationFactory;

import me.glindholm.bamboo.model.RestInfo;
import me.glindholm.connector.eclipse.internal.bamboo.core.BambooClientManager;
import me.glindholm.connector.eclipse.internal.bamboo.core.BambooCorePlugin;
import me.glindholm.connector.eclipse.internal.bamboo.core.BambooUtil;
import me.glindholm.connector.eclipse.internal.bamboo.core.client.BambooClient;
import me.glindholm.connector.eclipse.internal.bamboo.core.service.BambooLocalConfiguration;
import me.glindholm.theplugin.commons.remoteapi.RemoteApiException;

/**
 * @author George Lindholm
 * @since 4.3.1
 */
public class BambooClientFactory implements IRepositoryListener, IRepositoryChangeListener, IBambooClientFactory {
    private static BambooClientFactory instance = null;

    private final BambooClientManager clientManager;

    private TaskRepositoryLocationFactory taskRepositoryLocationFactory;

    public BambooClientFactory() {
        taskRepositoryLocationFactory = new TaskRepositoryLocationFactory();
        clientManager = BambooCorePlugin.getClientManager();

    }

    @Override
    public BambooClient getBambooClient(@NonNull final TaskRepository repository) {
        BambooClient client = clientManager.getClient(repository);
        if (client == null) {
            final AbstractWebLocation location = taskRepositoryLocationFactory.createWebLocation(repository);
            client = clientManager.addClient(location, BambooUtil.getLocalConfiguration(repository));
        }
        return client;

    }

    public synchronized static BambooClientFactory getDefault() {
        if (instance == null) {
            instance = new BambooClientFactory();
        }
        return instance;
    }

    public void repositoriesRead() {
        // ignore
    }

    @Override
    public synchronized void repositoryAdded(final TaskRepository repository) {
        if (BambooCorePlugin.CONNECTOR_KIND.equals(repository.getConnectorKind())) {
            assert clientManager.getClient(repository) == null;
            getBambooClient(repository);
        }
    }

    @Override
    public synchronized void repositoryRemoved(final TaskRepository repository) {
        if (BambooCorePlugin.CONNECTOR_KIND.equals(repository.getConnectorKind())) {
            final BambooClient client = clientManager.getClient(repository);
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
    public void repositoryChanged(final TaskRepositoryChangeEvent event) {
        final TaskRepository repository = event.getRepository();
        if (BambooCorePlugin.CONNECTOR_KIND.equals(repository.getConnectorKind())) {
            final BambooClient client = clientManager.getClient(repository);
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

    private void updateClient(final BambooClient client, final TaskRepository repository) {
        final BambooLocalConfiguration configuration = BambooUtil.getLocalConfiguration(repository);
        if (!configuration.equals(client.getLocalConfiguration())) {
            client.setLocalConfiguration(configuration);
        }
    }

    public RestInfo validateConnection(final AbstractWebLocation location, final IProgressMonitor monitor) throws RemoteApiException {
        return validateConnection(location, new BambooLocalConfiguration(), monitor);
    }

    public RestInfo validateConnection(final AbstractWebLocation location, final BambooLocalConfiguration configuration, final IProgressMonitor monitor)
            throws RemoteApiException {
        final RestInfo info = clientManager.validateConnection(location, configuration, monitor);

        return info;
    }

    public TaskRepositoryLocationFactory getTaskRepositoryLocationFactory() {
        return taskRepositoryLocationFactory;
    }

    public void setTaskRepositoryLocationFactory(final TaskRepositoryLocationFactory taskRepositoryLocationFactory) {
        this.taskRepositoryLocationFactory = taskRepositoryLocationFactory;
    }

    @Override
    public void repositoryUrlChanged(final TaskRepository repository, final String oldUrl) {
        // nothing to do, the next call to getClient() will create a new client since
        // it's stored by URL
    }

}
