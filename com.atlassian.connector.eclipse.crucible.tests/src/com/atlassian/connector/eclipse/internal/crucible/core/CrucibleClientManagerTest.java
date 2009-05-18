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

import com.atlassian.theplugin.commons.exception.HttpProxySettingsException;
import com.atlassian.theplugin.commons.remoteapi.ServerData;

import org.apache.commons.httpclient.HttpClient;
import org.eclipse.mylyn.commons.net.AbstractWebLocation;
import org.eclipse.mylyn.commons.net.AuthenticationCredentials;
import org.eclipse.mylyn.commons.net.AuthenticationType;
import org.eclipse.mylyn.tasks.core.TaskRepository;

import junit.framework.TestCase;

public class CrucibleClientManagerTest extends TestCase {

	@Override
	protected void setUp() throws Exception {
		super.setUp();
	}

	@Override
	protected void tearDown() throws Exception {
		super.tearDown();
	}

	public void testGetClientTaskRepository() {
		TaskRepository repo = new TaskRepository(CrucibleCorePlugin.CONNECTOR_KIND, "http://crucible.atlassian.com");
		repo.setCredentials(AuthenticationType.REPOSITORY, new AuthenticationCredentials("user", "pass"), false);

		CrucibleRepositoryConnector repoConnector = new CrucibleRepositoryConnector();
		CrucibleClientManager clientManager = repoConnector.getClientManager();
		AbstractWebLocation location = clientManager.getTaskRepositoryLocationFactory().createWebLocation(repo);
		ServerData serverCfg = getServerData(location, repo, false);

		clientManager.getClient(repo);
		HttpClient httpClient1 = null;
		try {
			httpClient1 = clientManager.getClientCallback().getHttpClient(serverCfg);
		} catch (HttpProxySettingsException e) {
			//
		}
		assertNotNull(httpClient1);

		repo.setCredentials(AuthenticationType.REPOSITORY, new AuthenticationCredentials("user2", "pass2"), false);
		location = clientManager.getTaskRepositoryLocationFactory().createWebLocation(repo);
		serverCfg = getServerData(location, repo, false);
		clientManager.getClient(repo);
		HttpClient httpClient2 = null;
		try {
			httpClient2 = clientManager.getClientCallback().getHttpClient(serverCfg);
		} catch (HttpProxySettingsException e) {
			//
		}
		assertNotNull(httpClient2);
		assertFalse(httpClient1 == httpClient2);
	}

	public void testRepositoryRemoved() throws HttpProxySettingsException {
		TaskRepository repo = new TaskRepository(CrucibleCorePlugin.CONNECTOR_KIND, "http://crucible.atlassian.com");
		repo.setCredentials(AuthenticationType.REPOSITORY, new AuthenticationCredentials("user", "pass"), false);

		CrucibleRepositoryConnector repoConnector = new CrucibleRepositoryConnector();
		CrucibleClientManager clientManager = repoConnector.getClientManager();
		AbstractWebLocation location = clientManager.getTaskRepositoryLocationFactory().createWebLocation(repo);
		ServerData serverCfg = getServerData(location, repo, false);

		clientManager.getClient(repo);
		HttpClient httpClient1 = null;

		httpClient1 = clientManager.getClientCallback().getHttpClient(serverCfg);

		assertNotNull(httpClient1);
		clientManager.repositoryRemoved(repo);
		httpClient1 = null;
		boolean assertion = false;
		try {
			httpClient1 = clientManager.getClientCallback().getHttpClient(serverCfg);
		} catch (AssertionError e) {
			assertion = true;
		}
		assertTrue(assertion);
	}

}
