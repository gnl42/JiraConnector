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

import com.atlassian.theplugin.commons.exception.HttpProxySettingsException;

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
	}

	@Override
	protected void tearDown() throws Exception {
		super.tearDown();
	}

	public void testRepositoryRemoved() throws HttpProxySettingsException {
//		TaskRepository repo = new TaskRepository(CrucibleCorePlugin.CONNECTOR_KIND, "http://crucible.atlassian.com");
//		repo.setCredentials(AuthenticationType.REPOSITORY, new AuthenticationCredentials("user", "pass"), true);
//
//		TasksUi.getRepositoryManager().addRepository(repo);
//
//		CrucibleRepositoryConnector repoConnector = CrucibleCorePlugin.getRepositoryConnector();
//		CrucibleClientManager clientManager = repoConnector.getClientManager();
//		AbstractWebLocation location = clientManager.getTaskRepositoryLocationFactory().createWebLocation(repo);
//		CrucibleServerCfg serverCfg = clientManager.getServerCfg(location, repo, false);
//
//		CrucibleClient client = clientManager.getClient(repo);
//		assertNotNull(client);
//
//		HttpClient httpClient1 = clientManager.getClientCallback().getHttpClient(serverCfg);
//
//		assertNotNull(httpClient1);
//
//		((TaskRepositoryManager) TasksUi.getRepositoryManager()).removeRepository(repo, TasksUiPlugin.getDefault()
//				.getRepositoriesFilePath());
//
//		httpClient1 = null;
//		try {
//			httpClient1 = clientManager.getClientCallback().getHttpClient(serverCfg);
//		} catch (AssertionError e) {
//			// ignore since this is what we want
//			return;
//		}
//		assertNull(httpClient1);
	}

	// TODO test added and changed as well
}
