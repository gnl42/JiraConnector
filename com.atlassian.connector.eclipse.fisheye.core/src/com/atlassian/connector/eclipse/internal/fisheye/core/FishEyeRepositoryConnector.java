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

package com.atlassian.connector.eclipse.internal.fisheye.core;

import com.atlassian.connector.eclipse.internal.fisheye.core.client.FishEyeClient;

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

import java.io.File;

public class FishEyeRepositoryConnector extends AbstractRepositoryConnector {

	private static final String REPOSITORY_LABEL = "FishEye (supports 2.0 and later)";

	private FishEyeClientManager clientManager;

	private final File repositoryConfigurationCacheFile;

	public FishEyeRepositoryConnector() {
		FishEyeCorePlugin.getDefault().setRepositoryConnector(this);
		this.repositoryConfigurationCacheFile = FishEyeCorePlugin.getDefault().getRepositoryConfigurationCacheFile();
	}

	@Override
	public boolean canCreateNewTask(TaskRepository repository) {
		return false;
	}

	@Override
	public boolean canCreateTaskFromKey(TaskRepository repository) {
		return false;
	}

	@Override
	public boolean canQuery(TaskRepository repository) {
		return false;
	}

	@Override
	public boolean canSynchronizeTask(TaskRepository taskRepository, ITask task) {
		return false;
	}

	public synchronized FishEyeClientManager getClientManager() {
		if (clientManager == null) {
			clientManager = new FishEyeClientManager(getRepositoryConfigurationCacheFile());
		}
		return clientManager;
	}

	@Override
	public String getConnectorKind() {
		return FishEyeCorePlugin.CONNECTOR_KIND;
	}

	@Override
	public String getLabel() {
		return REPOSITORY_LABEL;
	}

	public File getRepositoryConfigurationCacheFile() {
		return repositoryConfigurationCacheFile;
	}

	@Override
	public String getRepositoryUrlFromTaskUrl(String taskFullUrl) {
		return null;
	}

	@Override
	public TaskData getTaskData(TaskRepository taskRepository, String taskId, IProgressMonitor monitor)
			throws CoreException {
		return null;
	}

	@Override
	public String getTaskIdFromTaskUrl(String taskFullUrl) {
		return null;
	}

	@Override
	public String getTaskUrl(String repositoryUrl, String taskId) {
		return null;
	}

	@Override
	public boolean hasTaskChanged(TaskRepository taskRepository, ITask task, TaskData taskData) {
		return false;
	}

	@Override
	public IStatus performQuery(TaskRepository repository, IRepositoryQuery query, TaskDataCollector resultCollector,
			ISynchronizationSession event, IProgressMonitor monitor) {
		return Status.OK_STATUS;
	}

	@Override
	public void updateRepositoryConfiguration(TaskRepository taskRepository, IProgressMonitor monitor)
			throws CoreException {
		FishEyeClient client = getClientManager().getClient(taskRepository);
		client.updateRepositoryData(monitor, taskRepository);
	}

	@Override
	public void updateTaskFromTaskData(TaskRepository taskRepository, ITask task, TaskData taskData) {
	}

	public synchronized void flush() {
		if (clientManager != null) {
			clientManager.writeCache();
		}
	}

}
