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

package com.atlassian.connector.eclipse.internal.crucible.ui;

import static com.atlassian.connector.eclipse.internal.core.ServerDataUtil.getServerData;

import com.atlassian.connector.eclipse.internal.crucible.core.CrucibleClientManager;
import com.atlassian.connector.eclipse.internal.crucible.core.CrucibleCorePlugin;
import com.atlassian.connector.eclipse.internal.crucible.core.CrucibleRepositoryConnector;
import com.atlassian.connector.eclipse.internal.crucible.core.client.CrucibleClient;
import com.atlassian.theplugin.commons.exception.HttpProxySettingsException;
import com.atlassian.theplugin.commons.remoteapi.ServerData;

import org.apache.commons.httpclient.HttpClient;
import org.eclipse.mylyn.commons.net.AbstractWebLocation;
import org.eclipse.mylyn.commons.net.AuthenticationCredentials;
import org.eclipse.mylyn.commons.net.AuthenticationType;
import org.eclipse.mylyn.internal.tasks.core.TaskRepositoryManager;
import org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin;
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.eclipse.mylyn.tasks.ui.TasksUi;

import junit.framework.TestCase;

public class CrucibleClientManagerUiTest extends TestCase {

	@Override
	protected void setUp() throws Exception {
		super.setUp();

		for (TaskRepository repository : TasksUi.getRepositoryManager().getAllRepositories()) {
			((TaskRepositoryManager) TasksUi.getRepositoryManager()).removeRepository(repository,
					TasksUiPlugin.getDefault().getRepositoriesFilePath());
		}

		CrucibleCorePlugin.getRepositoryConnector().getClientManager().clear();
	}

	@Override
	protected void tearDown() throws Exception {
		super.tearDown();
	}

	public void testRepositoryRemoved() throws HttpProxySettingsException {
		TaskRepository repo = new TaskRepository(CrucibleCorePlugin.CONNECTOR_KIND, "http://crucible.atlassian.com");
		repo.setCredentials(AuthenticationType.REPOSITORY, new AuthenticationCredentials("user", "pass"), true);

		TasksUi.getRepositoryManager().addRepository(repo);

		CrucibleRepositoryConnector repoConnector = CrucibleCorePlugin.getRepositoryConnector();
		CrucibleClientManager clientManager = repoConnector.getClientManager();
		AbstractWebLocation location = clientManager.getTaskRepositoryLocationFactory().createWebLocation(repo);
		ServerData serverCfg = getServerData(location, repo, false);

		boolean assertion = false;
		HttpClient httpClient1 = null;
		try {
			httpClient1 = clientManager.getClientCallback().getHttpClient(serverCfg);
		} catch (AssertionError e) {
			assertion = true;
		}

		assertTrue(assertion);

		assertNull(httpClient1);

		CrucibleClient client = clientManager.getClient(repo);
		assertNotNull(client);

		httpClient1 = clientManager.getClientCallback().getHttpClient(serverCfg);

		assertNotNull(httpClient1);

		((TaskRepositoryManager) TasksUi.getRepositoryManager()).removeRepository(repo, TasksUiPlugin.getDefault()
				.getRepositoriesFilePath());

		httpClient1 = null;
		try {
			httpClient1 = clientManager.getClientCallback().getHttpClient(serverCfg);
		} catch (AssertionError e) {
			// ignore since this is what we want
			return;
		}

		// we should never get here as the assertion should have happened
		assertFalse(true);
	}

	// TODO test added and changed as well
}
