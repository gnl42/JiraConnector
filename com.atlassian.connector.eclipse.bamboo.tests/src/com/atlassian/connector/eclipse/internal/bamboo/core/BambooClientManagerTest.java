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
import com.atlassian.connector.eclipse.internal.bamboo.core.configuration.EclipseBambooServerCfg;
import com.atlassian.theplugin.commons.cfg.BambooServerCfg;
import com.atlassian.theplugin.commons.exception.HttpProxySettingsException;

import org.apache.commons.httpclient.HttpClient;
import org.eclipse.mylyn.commons.net.AbstractWebLocation;
import org.eclipse.mylyn.commons.net.AuthenticationCredentials;
import org.eclipse.mylyn.commons.net.AuthenticationType;
import org.eclipse.mylyn.tasks.core.TaskRepository;

import junit.framework.TestCase;

public class BambooClientManagerTest extends TestCase {

	public void testRepositoryRemoved() throws HttpProxySettingsException {
		TaskRepository repository = new TaskRepository(BambooCorePlugin.CONNECTOR_KIND, "http://studio.atlassian.com");
		repository.setCredentials(AuthenticationType.REPOSITORY, new AuthenticationCredentials("username", "password"),
				false);
		BambooClientManager bambooClientManager = new BambooRepositoryConnector().getClientManager();
		BambooServerCfg serverConfiguration = getServerCfg(bambooClientManager.getTaskRepositoryLocationFactory()
				.createWebLocation(repository), repository, false);
		bambooClientManager.getClient(repository);
		HttpClient httpClient = null;
		try {
			httpClient = bambooClientManager.getClientCallback().getHttpClient(serverConfiguration);
		} catch (HttpProxySettingsException e) {
			// ignore
		}
		assertNotNull(httpClient);
		bambooClientManager.repositoryRemoved(repository);
		httpClient = null;
		boolean assertion = false;
		try {
			httpClient = bambooClientManager.getClientCallback().getHttpClient(serverConfiguration);
		} catch (AssertionError e) {
			assertion = true;
		}
		assertTrue(assertion);
	}

	public void testGetClient() {
		TaskRepository repository = new TaskRepository(BambooCorePlugin.CONNECTOR_KIND, "http://studio.atlassian.com");
		repository.setCredentials(AuthenticationType.REPOSITORY, new AuthenticationCredentials("username", "password"),
				false);
		BambooClientManager bambooClientManager = new BambooRepositoryConnector().getClientManager();
		BambooServerCfg serverConfiguration = getServerCfg(bambooClientManager.getTaskRepositoryLocationFactory()
				.createWebLocation(repository), repository, false);
		bambooClientManager.getClient(repository);
		HttpClient httpClient = null;
		try {
			httpClient = bambooClientManager.getClientCallback().getHttpClient(serverConfiguration);
		} catch (HttpProxySettingsException e) {
			// igonre
		}
		assertNotNull(httpClient);
		repository.setCredentials(AuthenticationType.REPOSITORY, new AuthenticationCredentials("nameuser", "wordpass"),
				false);
		serverConfiguration = getServerCfg(bambooClientManager.getTaskRepositoryLocationFactory().createWebLocation(
				repository), repository, false);
		bambooClientManager.getClient(repository);
		HttpClient httpClient2 = null;
		try {
			httpClient2 = bambooClientManager.getClientCallback().getHttpClient(serverConfiguration);
		} catch (HttpProxySettingsException e) {
			// ignore
		}
		assertNotNull(httpClient2);
		assertFalse(httpClient == httpClient2);
	}

	public void testCreateDeleteTempClient() {
		TaskRepository repository = new TaskRepository(BambooCorePlugin.CONNECTOR_KIND, "http://studio.atlassian.com");
		repository.setCredentials(AuthenticationType.REPOSITORY, new AuthenticationCredentials("username", "password"),
				false);
		BambooClientManager bambooClientManager = new BambooRepositoryConnector().getClientManager();
		BambooClient client1 = bambooClientManager.createTempClient(repository, new BambooClientData());
		assertNotNull(client1);
		BambooClient client2 = bambooClientManager.createTempClient(repository, new BambooClientData());
		assertNotNull(client2);
		assertFalse(client1 == client2);
		assertTrue(bambooClientManager.getTempClients().containsKey(client1));
		assertTrue(bambooClientManager.getTempClients().containsKey(client2));
		bambooClientManager.deleteTempClient(client1);
		assertFalse(bambooClientManager.getTempClients().containsKey(client1));
		bambooClientManager.deleteTempClient(client2);
		assertFalse(bambooClientManager.getTempClients().containsKey(client2));
		bambooClientManager.deleteTempClient(null);
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
}
