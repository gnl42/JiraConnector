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

import com.atlassian.theplugin.commons.exception.HttpProxySettingsException;
import com.atlassian.theplugin.commons.remoteapi.ServerData;

import org.apache.commons.httpclient.HttpClient;
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
		ServerData serverConfiguration = getServerData(bambooClientManager.getTaskRepositoryLocationFactory()
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
		ServerData serverConfiguration = getServerData(bambooClientManager.getTaskRepositoryLocationFactory()
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
		serverConfiguration = getServerData(bambooClientManager.getTaskRepositoryLocationFactory().createWebLocation(
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
}
