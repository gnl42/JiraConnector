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

import com.atlassian.connector.eclipse.internal.crucible.core.client.CrucibleClient;
import com.atlassian.connector.eclipse.internal.crucible.core.client.CrucibleClientData;
import com.atlassian.connector.eclipse.internal.crucible.core.client.CrucibleHttpSessionCallback;
import com.atlassian.connector.eclipse.internal.crucible.core.client.model.ReviewCache;
import com.atlassian.connector.eclipse.internal.crucible.core.configuration.EclipseCrucibleServerCfg;
import com.atlassian.theplugin.commons.cfg.CrucibleServerCfg;
import com.atlassian.theplugin.commons.cfg.ServerCfg;
import com.atlassian.theplugin.commons.crucible.CrucibleServerFacade;
import com.atlassian.theplugin.commons.crucible.CrucibleServerFacadeImpl;
import com.atlassian.theplugin.commons.remoteapi.rest.HttpSessionCallback;

import org.eclipse.mylyn.commons.net.AbstractWebLocation;
import org.eclipse.mylyn.commons.net.AuthenticationCredentials;
import org.eclipse.mylyn.commons.net.AuthenticationType;
import org.eclipse.mylyn.internal.provisional.tasks.core.RepositoryClientManager;
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.eclipse.mylyn.tasks.core.TaskRepositoryLocationFactory;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * Class to manage the clients and data on a per-repository basis
 * 
 * @author Shawn Minto
 */
public class CrucibleClientManager extends RepositoryClientManager<CrucibleClient, CrucibleClientData> {

	// list of clients used for validating credentials.  Should only be created through the RepositorySettingsPage
	private final Map<CrucibleClient, ServerCfg> tempClients = new HashMap<CrucibleClient, ServerCfg>();

	private CrucibleServerFacade crucibleServerFacade;

	private final CrucibleHttpSessionCallback clientCallback;

	private final ReviewCache cachedReviewManager;

	public CrucibleClientManager(File cacheFile, ReviewCache cachedReviewManager) {
		super(cacheFile);
		clientCallback = new CrucibleHttpSessionCallback();
		this.cachedReviewManager = cachedReviewManager;
	}

	@Override
	public synchronized CrucibleClient getClient(TaskRepository taskRepository) {

		CrucibleClient client = super.getClient(taskRepository);
		AbstractWebLocation location = getTaskRepositoryLocationFactory().createWebLocation(taskRepository);
		CrucibleServerCfg serverCfg = getServerCfg(location, taskRepository, false);
		updateHttpSessionCallback(location, serverCfg);

		return client;
	}

	@Override
	protected CrucibleClient createClient(TaskRepository taskRepository, CrucibleClientData data) {
		AbstractWebLocation location = getTaskRepositoryLocationFactory().createWebLocation(taskRepository);

		CrucibleServerCfg serverCfg = getServerCfg(location, taskRepository, false);
		HttpSessionCallback callback = getHttpSessionCallback(location, serverCfg);
		CrucibleServerFacade crucibleServer = getCrucibleServer(serverCfg, callback);

		return new CrucibleClient(location, serverCfg, crucibleServer, data, cachedReviewManager);
	}

	public CrucibleClient createTempClient(TaskRepository taskRepository, CrucibleClientData data) {
		AbstractWebLocation location = getTaskRepositoryLocationFactory().createWebLocation(taskRepository);

		CrucibleServerCfg serverCfg = getServerCfg(location, taskRepository, true);
		HttpSessionCallback callback = getHttpSessionCallback(location, serverCfg);
		CrucibleServerFacade crucibleServer = getCrucibleServer(serverCfg, callback);

		CrucibleClient tempClient = new CrucibleClient(location, serverCfg, crucibleServer, data, cachedReviewManager);
		tempClients.put(tempClient, serverCfg);
		return tempClient;
	}

	private synchronized CrucibleServerFacade getCrucibleServer(CrucibleServerCfg serverCfg,
			HttpSessionCallback callback) {
		if (crucibleServerFacade == null) {
			crucibleServerFacade = CrucibleServerFacadeImpl.getInstance();
			crucibleServerFacade.setCallback(callback);
		}
		return crucibleServerFacade;
	}

	public void deleteTempClient(CrucibleClient client) {
		ServerCfg serverCfg = tempClients.remove(client);
		clientCallback.removeClient(serverCfg);
	}

	@Override
	public synchronized void repositoryRemoved(TaskRepository repository) {
		super.repositoryRemoved(repository);
		// TODO remove the client from the callback
	}

	private HttpSessionCallback getHttpSessionCallback(AbstractWebLocation location, CrucibleServerCfg serverCfg) {
		updateHttpSessionCallback(location, serverCfg);
		return clientCallback;
	}

	private void updateHttpSessionCallback(AbstractWebLocation location, CrucibleServerCfg serverCfg) {
		clientCallback.updateHostConfiguration(location, serverCfg);
	}

	private CrucibleServerCfg getServerCfg(AbstractWebLocation location, TaskRepository taskRepository,
			boolean isTemporary) {

		AuthenticationCredentials credentials = location.getCredentials(AuthenticationType.REPOSITORY);
		String username = "";
		String password = "";
		if (credentials != null) {
			username = credentials.getUserName();
			password = credentials.getPassword();
		}

		EclipseCrucibleServerCfg config = new EclipseCrucibleServerCfg(taskRepository.getRepositoryLabel(),
				location.getUrl(), isTemporary);
		config.setUsername(username);
		config.setPassword(password);

		return config;
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
}
