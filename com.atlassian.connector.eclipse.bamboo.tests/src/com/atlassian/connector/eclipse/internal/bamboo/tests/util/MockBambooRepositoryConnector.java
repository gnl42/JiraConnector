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

package com.atlassian.connector.eclipse.internal.bamboo.tests.util;

import com.atlassian.connector.eclipse.internal.bamboo.core.BambooClientManager;
import com.atlassian.connector.eclipse.internal.bamboo.core.BambooRepositoryConnector;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.mylyn.tasks.core.IRepositoryQuery;
import org.eclipse.mylyn.tasks.core.ITask;
import org.eclipse.mylyn.tasks.core.ITaskMapping;
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.eclipse.mylyn.tasks.core.data.AbstractTaskAttachmentHandler;
import org.eclipse.mylyn.tasks.core.data.AbstractTaskDataHandler;
import org.eclipse.mylyn.tasks.core.data.TaskData;
import org.eclipse.mylyn.tasks.core.data.TaskDataCollector;
import org.eclipse.mylyn.tasks.core.data.TaskRelation;
import org.eclipse.mylyn.tasks.core.sync.ISynchronizationSession;

import java.io.File;
import java.util.Collection;

/**
 * Mock implementation of the Bamboo Repository Conenctor
 * 
 * @author Thomas Ehrnhoefer
 */
public class MockBambooRepositoryConnector extends BambooRepositoryConnector {

	private MockBambooClientManager mockClientManager;

	@Override
	public boolean canCreateNewTask(TaskRepository repository) {
		// ignore
		return false;
	}

	@Override
	public boolean canCreateTaskFromKey(TaskRepository repository) {
		// ignore
		return false;
	}

	@Override
	public boolean canQuery(TaskRepository repository) {
		// ignore
		return false;
	}

	@Override
	public boolean canSynchronizeTask(TaskRepository taskRepository, ITask task) {
		// ignore
		return false;
	}

	@Override
	public synchronized BambooClientManager getClientManager() {
		// ignore
		return mockClientManager;
	}

	public void setClientManager(MockBambooClientManager manager) {
		this.mockClientManager = manager;
	}

	@Override
	public String getConnectorKind() {
		// ignore
		return "";
	}

	@Override
	public String getLabel() {
		// ignore
		return "";
	}

	@Override
	public File getRepositoryConfigurationCacheFile() {
		// ignore
		return null;
	}

	@Override
	public String getRepositoryUrlFromTaskUrl(String taskFullUrl) {
		// ignore
		return "";
	}

	@Override
	public TaskData getTaskData(TaskRepository taskRepository, String taskId, IProgressMonitor monitor)
			throws CoreException {
		// ignore
		return null;
	}

	@Override
	public String getTaskIdFromTaskUrl(String taskFullUrl) {
		// ignore
		return "";
	}

	@Override
	public String getTaskUrl(String repositoryUrl, String taskId) {
		// ignore
		return "";
	}

	@Override
	public boolean hasTaskChanged(TaskRepository taskRepository, ITask task, TaskData taskData) {
		// ignore
		return false;
	}

	@Override
	public IStatus performQuery(TaskRepository repository, IRepositoryQuery query, TaskDataCollector resultCollector,
			ISynchronizationSession event, IProgressMonitor monitor) {
		// ignore
		return null;
	}

	@Override
	public void updateRepositoryConfiguration(TaskRepository taskRepository, IProgressMonitor monitor)
			throws CoreException {
		// ignore
	}

	@Override
	public void updateTaskFromTaskData(TaskRepository taskRepository, ITask task, TaskData taskData) {
		// ignore
		super.updateTaskFromTaskData(taskRepository, task, taskData);
	}

	@Override
	public String getShortLabel() {
		// ignore
		return "";
	}

	@Override
	public AbstractTaskAttachmentHandler getTaskAttachmentHandler() {
		// ignore
		return null;
	}

	@Override
	public AbstractTaskDataHandler getTaskDataHandler() {
		// ignore
		return null;
	}

	@Override
	public String getTaskIdPrefix() {
		// ignore
		return "";
	}

	@Override
	public String[] getTaskIdsFromComment(TaskRepository repository, String comment) {
		// ignore
		return null;
	}

	@Override
	public ITaskMapping getTaskMapping(TaskData taskData) {
		// ignore
		return null;
	}

	@Override
	public Collection<TaskRelation> getTaskRelations(TaskData taskData) {
		// ignore
		return null;
	}

	@Override
	public boolean hasLocalCompletionState(TaskRepository taskRepository, ITask task) {
		// ignore
		return false;
	}

	@Override
	public boolean hasRepositoryDueDate(TaskRepository taskRepository, ITask task, TaskData taskData) {
		// ignore
		return false;
	}

	@Override
	public boolean isRepositoryConfigurationStale(TaskRepository repository, IProgressMonitor monitor)
			throws CoreException {
		// ignore
		return false;
	}

	@Override
	public boolean isUserManaged() {
		// ignore
		return false;
	}

	@Override
	public void postSynchronization(ISynchronizationSession event, IProgressMonitor monitor) throws CoreException {
		// ignore
	}

	@Override
	public void preSynchronization(ISynchronizationSession event, IProgressMonitor monitor) throws CoreException {
		// ignore
	}

}
