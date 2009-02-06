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

import com.atlassian.connector.eclipse.internal.bamboo.core.client.BambooClient;
import com.atlassian.connector.eclipse.internal.bamboo.core.client.BambooClientData;
import com.atlassian.connector.eclipse.internal.bamboo.core.client.BambooHttpSessionCallback;
import com.atlassian.connector.eclipse.internal.bamboo.core.configuration.EclipseBambooServerCfg;
import com.atlassian.theplugin.commons.bamboo.BambooServerFacade;
import com.atlassian.theplugin.commons.bamboo.BambooServerFacadeImpl;
import com.atlassian.theplugin.commons.cfg.BambooServerCfg;
import com.atlassian.theplugin.commons.cfg.ServerCfg;
import com.atlassian.theplugin.commons.remoteapi.rest.HttpSessionCallback;
import com.atlassian.theplugin.commons.util.LoggerImpl;

import org.eclipse.mylyn.commons.net.AbstractWebLocation;
import org.eclipse.mylyn.commons.net.AuthenticationCredentials;
import org.eclipse.mylyn.commons.net.AuthenticationType;
import org.eclipse.mylyn.tasks.core.TaskRepository;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * Class to manage the clients and data on a per-repository basis
 * 
 * @author Shawn Minto
 */
public class BambooClientManager extends RepositoryClientManager<BambooClient, BambooClientData> {

	// list of clients used for validating credentials.  Should only be created through the RepositorySettingsPage
	private final Map<BambooClient, ServerCfg> tempClients = new HashMap<BambooClient, ServerCfg>();

	private BambooServerFacade crucibleServerFacade;

	private final BambooHttpSessionCallback clientCallback;

	public BambooClientManager(File cacheFile) {
		super(cacheFile);
		clientCallback = new BambooHttpSessionCallback();
	}

	@Override
	public synchronized BambooClient getClient(TaskRepository taskRepository) {
		BambooClient client = super.getClient(taskRepository);
		AbstractWebLocation location = getTaskRepositoryLocationFactory().createWebLocation(taskRepository);
		BambooServerCfg serverCfg = getServerCfg(location, taskRepository, false);
		updateHttpSessionCallback(location, serverCfg);

		return client;
	}

	private void updateHttpSessionCallback(AbstractWebLocation location, BambooServerCfg serverCfg) {
		clientCallback.updateHostConfiguration(location, serverCfg);
	}

	@Override
	protected BambooClient createClient(TaskRepository taskRepository, BambooClientData data) {
		AbstractWebLocation location = getTaskRepositoryLocationFactory().createWebLocation(taskRepository);

		BambooServerCfg serverCfg = getServerCfg(location, taskRepository, false);
		HttpSessionCallback callback = getHttpSessionCallback(location, serverCfg);
		BambooServerFacade crucibleServer = getServer(serverCfg, callback);

		return new BambooClient(location, serverCfg, crucibleServer, data);
	}

	public BambooClient createTempClient(TaskRepository taskRepository, BambooClientData data) {
		AbstractWebLocation location = getTaskRepositoryLocationFactory().createWebLocation(taskRepository);

		BambooServerCfg serverCfg = getServerCfg(location, taskRepository, true);
		HttpSessionCallback callback = getHttpSessionCallback(location, serverCfg);
		BambooServerFacade crucibleServer = getServer(serverCfg, callback);

		BambooClient tempClient = new BambooClient(location, serverCfg, crucibleServer, data);
		tempClients.put(tempClient, serverCfg);
		return tempClient;
	}

	private synchronized BambooServerFacade getServer(BambooServerCfg serverCfg, HttpSessionCallback callback) {
		if (crucibleServerFacade == null) {
			crucibleServerFacade = BambooServerFacadeImpl.getInstance(LoggerImpl.getInstance());
			crucibleServerFacade.setCallback(callback);
		}
		return crucibleServerFacade;
	}

	public void deleteTempClient(BambooClient client) {
		ServerCfg serverCfg = tempClients.remove(client);
		clientCallback.removeClient(serverCfg);
	}

	@Override
	public synchronized void repositoryRemoved(TaskRepository repository) {
		super.repositoryRemoved(repository);

		AbstractWebLocation location = getTaskRepositoryLocationFactory().createWebLocation(repository);
		BambooServerCfg serverCfg = getServerCfg(location, repository, false);
		clientCallback.removeClient(serverCfg);

		BuildPlanManager.getInstance().repositoryRemoved(repository);
	}

	private HttpSessionCallback getHttpSessionCallback(AbstractWebLocation location, BambooServerCfg serverCfg) {
		clientCallback.initialize(location, serverCfg);
		return clientCallback;
	}

	private BambooServerCfg getServerCfg(AbstractWebLocation location, TaskRepository taskRepository,
			boolean isTemporary) {

		AuthenticationCredentials credentials = location.getCredentials(AuthenticationType.REPOSITORY);
		String username = "";
		String password = "";
		if (credentials != null) {
			username = credentials.getUserName();
			password = credentials.getPassword();
		}

		EclipseBambooServerCfg config = new EclipseBambooServerCfg(taskRepository.getRepositoryLabel(),
				location.getUrl(), isTemporary);
		config.setUsername(username);
		config.setPassword(password);

		return config;
	}

	@Override
	protected BambooClientData createRepositoryConfiguration() {
		return new BambooClientData();
	}
}
