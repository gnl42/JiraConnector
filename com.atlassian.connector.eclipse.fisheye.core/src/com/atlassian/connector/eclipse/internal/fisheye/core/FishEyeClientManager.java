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

import static com.atlassian.connector.eclipse.internal.core.ServerDataUtil.getServerData;

import com.atlassian.connector.commons.api.ConnectionCfg;
import com.atlassian.connector.commons.fisheye.FishEyeServerFacade2;
import com.atlassian.connector.eclipse.internal.core.client.HttpSessionCallbackImpl;
import com.atlassian.connector.eclipse.internal.core.client.RepositoryClientManager;
import com.atlassian.connector.eclipse.internal.fisheye.core.client.FishEyeClient;
import com.atlassian.connector.eclipse.internal.fisheye.core.client.FishEyeClientData;
import com.atlassian.theplugin.commons.fisheye.FishEyeServerFacadeImpl;
import com.atlassian.theplugin.commons.remoteapi.rest.HttpSessionCallback;

import org.eclipse.mylyn.commons.net.AbstractWebLocation;
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.eclipse.mylyn.tasks.core.TaskRepositoryLocationFactory;

import java.io.File;
import java.util.Map;

public class FishEyeClientManager extends RepositoryClientManager<FishEyeClient, FishEyeClientData> {

	/** follows ACC approach for channeling all calls through a singleton */
	private FishEyeServerFacade2 crucibleServerFacade;

	private final HttpSessionCallbackImpl clientCallback;

	public FishEyeClientManager(File cacheFile) {
		super(cacheFile);
		clientCallback = new HttpSessionCallbackImpl();
	}

	@Override
	public synchronized FishEyeClient getClient(TaskRepository taskRepository) {

		FishEyeClient client = super.getClient(taskRepository);
		AbstractWebLocation location = getTaskRepositoryLocationFactory().createWebLocation(taskRepository);
		client.updateLocation(location);
		ConnectionCfg serverCfg = getServerData(location, taskRepository, false);
		updateHttpSessionCallback(location, serverCfg);

		return client;
	}

	@Override
	protected FishEyeClient createClient(TaskRepository taskRepository, FishEyeClientData data) {
		return createTempClientImpl(taskRepository, data, false);
	}

	public FishEyeClient createTempClient(TaskRepository taskRepository, FishEyeClientData data) {
		return createTempClientImpl(taskRepository, data, true);
	}

	private FishEyeClient createTempClientImpl(TaskRepository taskRepository, FishEyeClientData data,
			boolean isTemporary) {
		AbstractWebLocation location = getTaskRepositoryLocationFactory().createWebLocation(taskRepository);

		ConnectionCfg serverCfg = getServerData(location, taskRepository, isTemporary);
		HttpSessionCallbackImpl callback = getHttpSessionCallback(location, serverCfg);
		FishEyeServerFacade2 fishEyeServer = getFishEyeServerFacade(callback);

		return new FishEyeClient(location, serverCfg, fishEyeServer, data, callback);
	}

	private synchronized FishEyeServerFacade2 getFishEyeServerFacade(HttpSessionCallback callback) {
		if (crucibleServerFacade == null) {
			crucibleServerFacade = FishEyeServerFacadeImpl.getInstance();
			crucibleServerFacade.setCallback(callback);
		}
		return crucibleServerFacade;
	}

	public void deleteTempClient(ConnectionCfg serverData) {
		clientCallback.removeClient(serverData);
	}

	@Override
	public synchronized void repositoryRemoved(TaskRepository taskRepository) {
		super.repositoryRemoved(taskRepository);

		AbstractWebLocation location = getTaskRepositoryLocationFactory().createWebLocation(taskRepository);
		clientCallback.removeClient(location);

	}

	private HttpSessionCallbackImpl getHttpSessionCallback(AbstractWebLocation location, ConnectionCfg serverCfg) {
		updateHttpSessionCallback(location, serverCfg);
		return clientCallback;
	}

	private void updateHttpSessionCallback(AbstractWebLocation location, ConnectionCfg serverCfg) {
		clientCallback.updateHostConfiguration(location, serverCfg);
	}

	@Override
	protected FishEyeClientData createRepositoryConfiguration() {
		return new FishEyeClientData();
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

	/**
	 * For testing purposes only
	 * 
	 * @return
	 */
	public HttpSessionCallbackImpl getClientCallback() {
		return clientCallback;
	}

	/**
	 * For testing purposes only
	 */
	public void clear() {
		clientCallback.clear();
	}

	/*
	 * temporary fix for the broken/not-working serialization mechanism 
	 */
	@Override
	protected void updateClientDataMap(Map<String, FishEyeClient> clientByUrl,
			Map<String, FishEyeClientData> clientDataByUrl) {
		for (String url : clientByUrl.keySet()) {
			if (clientDataByUrl.containsKey(url)) {
				clientDataByUrl.put(url, clientByUrl.get(url).getClientData());
			}
		}
	}
}
