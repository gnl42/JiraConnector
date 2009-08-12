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

package com.atlassian.connector.eclipse.internal.crucible.core;

import static com.atlassian.connector.eclipse.internal.core.ServerDataUtil.getServerData;

import com.atlassian.connector.commons.api.ConnectionCfg;
import com.atlassian.connector.commons.crucible.CrucibleServerFacade2;
import com.atlassian.connector.eclipse.internal.core.client.HttpSessionCallbackImpl;
import com.atlassian.connector.eclipse.internal.core.client.RepositoryClientManager;
import com.atlassian.connector.eclipse.internal.crucible.core.client.CrucibleClient;
import com.atlassian.connector.eclipse.internal.crucible.core.client.CrucibleClientData;
import com.atlassian.connector.eclipse.internal.crucible.core.client.model.ReviewCache;
import com.atlassian.connector.eclipse.internal.fisheye.core.FishEyeCorePlugin;
import com.atlassian.connector.eclipse.internal.fisheye.core.client.FishEyeClient;
import com.atlassian.connector.eclipse.internal.fisheye.core.client.FishEyeClientData;
import com.atlassian.theplugin.commons.crucible.CrucibleServerFacadeImpl;
import com.atlassian.theplugin.commons.crucible.api.model.CrucibleUserCacheImpl;
import com.atlassian.theplugin.commons.remoteapi.rest.HttpSessionCallback;

import org.eclipse.mylyn.commons.net.AbstractWebLocation;
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.eclipse.mylyn.tasks.core.TaskRepositoryLocationFactory;

import java.io.File;
import java.util.Map;

/**
 * Class to manage the clients and data on a per-repository basis
 * 
 * @author Shawn Minto
 */
public class CrucibleClientManager extends RepositoryClientManager<CrucibleClient, CrucibleClientData> {

	// the server facade
	private CrucibleServerFacade2 crucibleServerFacade;

	private final HttpSessionCallbackImpl clientCallback;

	private final ReviewCache cachedReviewManager;

	public CrucibleClientManager(File cacheFile, ReviewCache cachedReviewManager) {
		super(cacheFile);
		clientCallback = new HttpSessionCallbackImpl();
		this.cachedReviewManager = cachedReviewManager;
	}

	@Override
	public synchronized CrucibleClient getClient(TaskRepository taskRepository) {
		CrucibleClient client = super.getClient(taskRepository);
		AbstractWebLocation location = getTaskRepositoryLocationFactory().createWebLocation(taskRepository);
		client.updateLocation(location);
		ConnectionCfg serverCfg = getServerData(location, taskRepository, false);
		updateHttpSessionCallback(location, serverCfg);
		return client;
	}

	/**
	 * 
	 * @param taskRepository
	 * @return <code>null</code> when given taskRepository does not represent FishEye
	 */
	public synchronized FishEyeClient getFishEyeClient(TaskRepository taskRepository) {
		CrucibleCorePlugin.getDefault();
		if (!CrucibleRepositoryConnector.isFishEye(taskRepository)) {
			return null;
		}
		final FishEyeClient client = FishEyeCorePlugin.getDefault()
				.getRepositoryConnector()
				.getClientManager()
				.getClient(taskRepository);
		AbstractWebLocation location = FishEyeCorePlugin.getDefault()
				.getRepositoryConnector()
				.getClientManager()
				.getTaskRepositoryLocationFactory()
				.createWebLocation(taskRepository);
//		client. updateLocation(newLocation)
//		AbstractWebLocation location = getTaskRepositoryLocationFactory().createWebLocation(taskRepository);
		client.updateLocation(location);
		ConnectionCfg serverCfg = getServerData(location, taskRepository, false);
		updateHttpSessionCallback(location, serverCfg);
		return client;
	}

	@Override
	protected CrucibleClient createClient(TaskRepository taskRepository, CrucibleClientData data) {
		return createClient(taskRepository, data, false);
	}

	public CrucibleClient createTempClient(TaskRepository taskRepository, CrucibleClientData data) {
		return createClient(taskRepository, data, true);
	}

	private CrucibleClient createClient(TaskRepository taskRepository, CrucibleClientData data, boolean isTemporary) {
		AbstractWebLocation location = getTaskRepositoryLocationFactory().createWebLocation(taskRepository);
		ConnectionCfg serverCfg = getServerData(location, taskRepository, isTemporary);
		updateHttpSessionCallback(location, serverCfg);
		CrucibleServerFacade2 crucibleServer = getCrucibleServer(clientCallback);
		return new CrucibleClient(location, serverCfg, crucibleServer, data, cachedReviewManager, clientCallback);
	}

	public FishEyeClient createTempFishEyeClient(TaskRepository taskRepository, FishEyeClientData data) {
		return FishEyeCorePlugin.getDefault().getRepositoryConnector().getClientManager().createTempClient(
				taskRepository, data);
	}

	public void deleteTempFishEyeClient(ConnectionCfg serverData) {
		FishEyeCorePlugin.getDefault().getRepositoryConnector().getClientManager().deleteTempClient(serverData);
	}

	private synchronized CrucibleServerFacade2 getCrucibleServer(HttpSessionCallback callback) {
		if (crucibleServerFacade == null) {
			crucibleServerFacade = new CrucibleServerFacadeImpl(new CrucibleUserCacheImpl(), callback);
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

	private void updateHttpSessionCallback(AbstractWebLocation location, ConnectionCfg serverCfg) {
		clientCallback.updateHostConfiguration(location, serverCfg);
	}

	@Override
	protected CrucibleClientData createRepositoryConfiguration() {
		return new CrucibleClientData();
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
	protected void updateClientDataMap(Map<String, CrucibleClient> clientByUrl,
			Map<String, CrucibleClientData> clientDataByUrl) {
		for (String url : clientByUrl.keySet()) {
			if (clientDataByUrl.containsKey(url)) {
				clientDataByUrl.put(url, (clientByUrl.get(url)).getClientData());
			}

		}
	}
}
