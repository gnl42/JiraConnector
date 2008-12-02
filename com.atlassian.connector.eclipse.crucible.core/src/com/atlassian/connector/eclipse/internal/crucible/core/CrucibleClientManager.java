/*******************************************************************************
 * Copyright (c) 2008 Atlassian and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Atlassian - initial API and implementation
 ******************************************************************************/

package com.atlassian.connector.eclipse.internal.crucible.core;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.mylyn.commons.net.AbstractWebLocation;
import org.eclipse.mylyn.commons.net.AuthenticationCredentials;
import org.eclipse.mylyn.commons.net.AuthenticationType;
import org.eclipse.mylyn.internal.provisional.tasks.core.RepositoryClientManager;
import org.eclipse.mylyn.tasks.core.TaskRepository;

import com.atlassian.connector.eclipse.internal.crucible.core.client.CrucibleClient;
import com.atlassian.connector.eclipse.internal.crucible.core.client.CrucibleClientData;
import com.atlassian.connector.eclipse.internal.crucible.core.client.CrucibleHttpSessionCallback;
import com.atlassian.connector.eclipse.internal.crucible.core.configuration.EclipseCrucibleServerCfg;
import com.atlassian.theplugin.commons.cfg.CrucibleServerCfg;
import com.atlassian.theplugin.commons.cfg.ServerCfg;
import com.atlassian.theplugin.commons.crucible.CrucibleServerFacade;
import com.atlassian.theplugin.commons.crucible.CrucibleServerFacadeImpl;
import com.atlassian.theplugin.commons.remoteapi.rest.HttpSessionCallback;

/**
 * Class to manage the clients and data on a per-repository basis
 * 
 * @author Shawn Minto
 */
public class CrucibleClientManager extends RepositoryClientManager<CrucibleClient, CrucibleClientData> {

	// list of clients used for validating credentials.  Should only be created through the RepositorySettingsPage
	private final Map<CrucibleClient, ServerCfg> tempClients = new HashMap<CrucibleClient, ServerCfg>();

	private CrucibleServerFacade crucibleServer;

	private final CrucibleHttpSessionCallback callback;

	public CrucibleClientManager(File cacheFile) {
		super(cacheFile);
		callback = new CrucibleHttpSessionCallback();
	}

	@Override
	protected CrucibleClient createClient(TaskRepository taskRepository, CrucibleClientData data) {
		AbstractWebLocation location = getTaskRepositoryLocationFactory().createWebLocation(taskRepository);

		CrucibleServerCfg serverCfg = getServerCfg(location, taskRepository, false);
		HttpSessionCallback callback = getHttpSessionCallback(location, serverCfg);
		CrucibleServerFacade crucibleServer = getCrucibleServer(serverCfg, callback);

		return new CrucibleClient(location, serverCfg, crucibleServer, data);
	}

	public CrucibleClient createTempClient(TaskRepository taskRepository, CrucibleClientData data) {
		AbstractWebLocation location = getTaskRepositoryLocationFactory().createWebLocation(taskRepository);

		CrucibleServerCfg serverCfg = getServerCfg(location, taskRepository, true);
		HttpSessionCallback callback = getHttpSessionCallback(location, serverCfg);
		CrucibleServerFacade crucibleServer = getCrucibleServer(serverCfg, callback);

		CrucibleClient tempClient = new CrucibleClient(location, serverCfg, crucibleServer, data);
		tempClients.put(tempClient, serverCfg);
		return tempClient;
	}

	private synchronized CrucibleServerFacade getCrucibleServer(CrucibleServerCfg serverCfg,
			HttpSessionCallback callback) {
		if (crucibleServer == null) {
			crucibleServer = CrucibleServerFacadeImpl.getInstance();
			crucibleServer.setCallback(callback);
		}
		return crucibleServer;
	}

	public void deleteTempClient(CrucibleClient client) {
		ServerCfg serverCfg = tempClients.remove(client);
		callback.removeClient(serverCfg);
	}

	@Override
	public synchronized void repositoryRemoved(TaskRepository repository) {
		super.repositoryRemoved(repository);
		// TODO remove the client from the callback
	}

	private HttpSessionCallback getHttpSessionCallback(AbstractWebLocation location, CrucibleServerCfg serverCfg) {
		callback.initialize(location, serverCfg);
		return callback;
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
}
