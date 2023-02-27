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

import java.io.File;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.mylyn.tasks.core.AbstractRepositoryConnector;
import org.eclipse.mylyn.tasks.core.IRepositoryQuery;
import org.eclipse.mylyn.tasks.core.ITask;
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.eclipse.mylyn.tasks.core.data.TaskData;
import org.eclipse.mylyn.tasks.core.data.TaskDataCollector;
import org.eclipse.mylyn.tasks.core.sync.ISynchronizationSession;

import me.glindholm.connector.eclipse.internal.bamboo.core.client.BambooClient;
import me.glindholm.connector.eclipse.internal.core.client.BambooClientFactory;
import me.glindholm.theplugin.commons.remoteapi.RemoteApiException;

/**
 * Core integration for Mylyn tasks framework.
 *
 * @author Shawn Minto
 */
public class BambooRepositoryConnector extends AbstractRepositoryConnector {

    private static final String REPOSITORY_LABEL = "Bamboo (supports 8.0.0 and later)";

//    private BambooClientManager clientManager;
//
    private File repositoryConfigurationCacheFile;

    public BambooRepositoryConnector() {
        BambooCorePlugin.setRepositoryConnector(this);
        if (BambooCorePlugin.getDefault() != null) {
            repositoryConfigurationCacheFile = BambooCorePlugin.getDefault().getRepositoryConfigurationCacheFile();
        }

    }

    @Override
    public boolean canCreateNewTask(final TaskRepository repository) {
        return false;
    }

    @Override
    public boolean canCreateTaskFromKey(final TaskRepository repository) {
        return false;
    }

    @Override
    public boolean canQuery(final TaskRepository repository) {
        return false;
    }

    @Override
    public boolean canSynchronizeTask(final TaskRepository taskRepository, final ITask task) {
        return false;
    }

//    public synchronized BambooClientManager getClientManager() {
//        if (clientManager == null) {
//            clientManager = new BambooClientManager(getRepositoryConfigurationCacheFile());
//        }
//        return clientManager;
//    }

    @Override
    public String getConnectorKind() {
        return BambooCorePlugin.CONNECTOR_KIND;
    }

    @Override
    public String getLabel() {
        return REPOSITORY_LABEL;
    }

    public File getRepositoryConfigurationCacheFile() {
        return repositoryConfigurationCacheFile;
    }

    @Override
    public String getRepositoryUrlFromTaskUrl(final String taskFullUrl) {
        return null;
    }

    @Override
    public TaskData getTaskData(final TaskRepository taskRepository, final String taskId, final IProgressMonitor monitor) throws CoreException {
        return null;
    }

    @Override
    public String getTaskIdFromTaskUrl(final String taskFullUrl) {
        return null;
    }

    @Override
    public String getTaskUrl(final String repositoryUrl, final String taskId) {
        return null;
    }

    @Override
    public boolean hasTaskChanged(final TaskRepository taskRepository, final ITask task, final TaskData taskData) {
        return false;
    }

    @Override
    public IStatus performQuery(final TaskRepository repository, final IRepositoryQuery query, final TaskDataCollector resultCollector,
            final ISynchronizationSession event, final IProgressMonitor monitor) {
        return Status.OK_STATUS;
    }

    @Override
    public void updateRepositoryConfiguration(final TaskRepository taskRepository, final IProgressMonitor monitor) throws CoreException {
        final BambooClient client = BambooClientFactory.getDefault().getBambooClient(taskRepository);
        try {
            client.updateRepositoryData(monitor, taskRepository);
        } catch (final RemoteApiException e) {

            throw new CoreException(new IStatus() {

                @Override
                public IStatus[] getChildren() {
                    // TODO Auto-generated method stub
                    return null;
                }

                @Override
                public int getCode() {
                    // TODO Auto-generated method stub
                    return 0;
                }

                @Override
                public Throwable getException() {
                    // TODO Auto-generated method stub
                    return e;
                }

                @Override
                public String getMessage() {
                    // TODO Auto-generated method stub
                    return e.getMessage();
                }

                @Override
                public String getPlugin() {
                    // TODO Auto-generated method stub
                    return null;
                }

                @Override
                public int getSeverity() {
                    // TODO Auto-generated method stub
                    return 0;
                }

                @Override
                public boolean isMultiStatus() {
                    // TODO Auto-generated method stub
                    return false;
                }

                @Override
                public boolean isOK() {
                    // TODO Auto-generated method stub
                    return false;
                }

                @Override
                public boolean matches(final int severityMask) {
                    // TODO Auto-generated method stub
                    return false;
                }
            });
        }
    }

    @Override
    public void updateTaskFromTaskData(final TaskRepository taskRepository, final ITask task, final TaskData taskData) {
    }

    public synchronized void flush() {
//        if (clientManager != null) {
//            clientManager.writeCache();
//        }
    }

}
