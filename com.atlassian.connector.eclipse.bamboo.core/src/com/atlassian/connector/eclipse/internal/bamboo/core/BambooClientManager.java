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

package com.atlassian.connector.eclipse.internal.bamboo.core;

import static com.atlassian.connector.eclipse.internal.core.ServerDataUtil.getServerData;

import com.atlassian.connector.commons.api.BambooServerFacade2;
import com.atlassian.connector.commons.api.ConnectionCfg;
import com.atlassian.connector.eclipse.internal.bamboo.core.client.BambooClient;
import com.atlassian.connector.eclipse.internal.bamboo.core.client.BambooClientData;
import com.atlassian.connector.eclipse.internal.core.client.HttpSessionCallbackImpl;
import com.atlassian.connector.eclipse.internal.core.client.RepositoryClientManager;
import com.atlassian.theplugin.commons.bamboo.BambooServerFacadeImpl;
import com.atlassian.theplugin.commons.util.LoggerImpl;

import org.eclipse.mylyn.commons.net.AbstractWebLocation;
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.eclipse.mylyn.tasks.core.TaskRepositoryLocationFactory;

import java.io.File;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Class to manage the clients and data on a per-repository basis
 * 
 * @author Shawn Minto
 * @author Wojciech Seliga
 */
public class BambooClientManager extends RepositoryClientManager<BambooClient, BambooClientData> {

	private BambooServerFacade2 bambooServerFacade;

	private final HttpSessionCallbackImpl clientCallback;

	public BambooClientManager(File cacheFile) {
		super(cacheFile);
		clientCallback = new HttpSessionCallbackImpl();
	}

	@Override
	public synchronized BambooClient getClient(TaskRepository taskRepository) {
		BambooClient client = super.getClient(taskRepository);
		AbstractWebLocation location = getTaskRepositoryLocationFactory().createWebLocation(taskRepository);
		ConnectionCfg serverCfg = getServerData(location, taskRepository, false);
		updateHttpSessionCallback(location, serverCfg);

		return client;
	}

	private void updateHttpSessionCallback(AbstractWebLocation location, ConnectionCfg serverCfg) {
		clientCallback.updateHostConfiguration(location, serverCfg);
	}

	@Override
	protected BambooClient createClient(TaskRepository taskRepository, BambooClientData data) {
		return createClient(taskRepository, data, false);
	}

	private BambooClient createClient(TaskRepository taskRepository, BambooClientData data, boolean isTemporary) {
		AbstractWebLocation location = getTaskRepositoryLocationFactory().createWebLocation(taskRepository);

		ConnectionCfg serverCfg = getServerData(location, taskRepository, isTemporary);
		configureHttpSessionCallback(location, serverCfg);
		BambooServerFacade2 facade = getBambooFacade();

		return new BambooClient(location, serverCfg, facade, data, clientCallback);
	}

	private synchronized BambooServerFacade2 getBambooFacade() {
		if (bambooServerFacade == null) {
			bambooServerFacade = new BambooServerFacadeImpl(LoggerImpl.getInstance(), clientCallback);
		}
		return bambooServerFacade;
	}

	public BambooClient createTempClient(TaskRepository taskRepository, BambooClientData data) {
		return createClient(taskRepository, data, true);
	}

	public void deleteTempClient(ConnectionCfg serverData) {
		clientCallback.removeClient(serverData);
	}

	@Override
	public synchronized void repositoryRemoved(TaskRepository repository) {
		super.repositoryRemoved(repository);

		AbstractWebLocation location = getTaskRepositoryLocationFactory().createWebLocation(repository);
		ConnectionCfg serverCfg = getServerData(location, repository, false);
		clientCallback.removeClient(serverCfg);

		BambooCorePlugin.getBuildPlanManager().repositoryRemoved(repository);
	}

	private void configureHttpSessionCallback(AbstractWebLocation location, ConnectionCfg serverCfg) {
		clientCallback.updateHostConfiguration(location, serverCfg);
	}

	@Override
	public TaskRepositoryLocationFactory getTaskRepositoryLocationFactory() {
		TaskRepositoryLocationFactory parentFactory = super.getTaskRepositoryLocationFactory();
		if (parentFactory == null) {
			return new TaskRepositoryLocationFactory();
		} else {
			return parentFactory;
		}
	}

	@Override
	protected BambooClientData createRepositoryConfiguration() {
		return new BambooClientData();
	}

	/**
	 * For testing purposes only
	 * 
	 * @return
	 */
	public HttpSessionCallbackImpl getClientCallback() {
		return clientCallback;
	}

	protected BambooClientData getConfiguration(BambooClient client) {
		return client.getClientData();
	}

	/*
	 * temporary fix for the broken/not-working serialization mechanism 
	 */
	@Override
	protected void updateClientDataMap(Map<String, BambooClient> clientByUrl,
			Map<String, BambooClientData> clientDataByUrl) {
		for (Entry<String, BambooClient> entry : clientByUrl.entrySet()) {
			String url = entry.getKey();
			if (clientDataByUrl.containsKey(url)) {
				clientDataByUrl.put(url, getConfiguration(entry.getValue()));
			}
		}
	}

	@Override
	protected void removeClient(TaskRepository repository, Map<String, BambooClient> clientByUrl,
			Map<String, BambooClientData> clientDataByUrl) {
		String url = repository.getRepositoryUrl();
		BambooClient client = clientByUrl.remove(url);
		if (client != null) {
			if (clientDataByUrl.containsKey(url)) {
				clientDataByUrl.put(url, getConfiguration(client));
			}
		}
	}
}
