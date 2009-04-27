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
import com.atlassian.connector.eclipse.internal.bamboo.core.client.BambooClient;
import com.atlassian.connector.eclipse.internal.bamboo.core.client.BambooClientData;
import com.atlassian.connector.eclipse.internal.bamboo.core.client.BambooHttpSessionCallback;
import com.atlassian.theplugin.commons.remoteapi.ServerData;

import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.eclipse.mylyn.tasks.core.TaskRepositoryLocationFactory;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * Mock implementation of the BambooClientManager
 * 
 * @author Thomas Ehrnhoefer
 */
public class MockBambooClientManager extends BambooClientManager {

	private final Map<TaskRepository, MockBambooClient> clients = new HashMap<TaskRepository, MockBambooClient>();

	public MockBambooClientManager(File tmp) {
		super(tmp);
	}

	@Override
	protected BambooClient createClient(TaskRepository taskRepository, BambooClientData data) {
		return null;
	}

	@Override
	protected BambooClientData createRepositoryConfiguration() {
		return null;
	}

	@Override
	public BambooClient createTempClient(TaskRepository taskRepository, BambooClientData data) {
		return null;
	}

	@Override
	public void deleteTempClient(ServerData client) {
	}

	@Override
	public synchronized BambooClient getClient(TaskRepository taskRepository) {
		return clients.get(taskRepository);
	}

	@Override
	public BambooHttpSessionCallback getClientCallback() {
		return null;
	}

	@Override
	public TaskRepositoryLocationFactory getTaskRepositoryLocationFactory() {
		return null;
	}

//	@Override
//	public Map<BambooClient, ServerCfg> getTempClients() {
//		// ignore
//		return new HashMap<BambooClient, ServerCfg>();
//	}

	@Override
	public synchronized void repositoryRemoved(TaskRepository repository) {
	}

	@Override
	public void readCache() {
	}

	@Override
	public void repositoriesRead() {
	}

	@Override
	public synchronized void repositoryAdded(TaskRepository repository) {
	}

	@Override
	public synchronized void repositorySettingsChanged(TaskRepository repository) {
	}

	@Override
	public void repositoryUrlChanged(TaskRepository repository, String oldUrl) {
	}

	@Override
	public void setTaskRepositoryLocationFactory(TaskRepositoryLocationFactory taskRepositoryLocationFactory) {
	}

	@Override
	public void writeCache() {
	}

	public void addClient(MockBambooClient client, TaskRepository repository) {
		clients.put(repository, client);
	}

}
