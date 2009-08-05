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

import com.atlassian.connector.eclipse.internal.bamboo.tests.util.MockBambooBuild;
import com.atlassian.connector.eclipse.internal.bamboo.tests.util.MockBambooClient;
import com.atlassian.connector.eclipse.internal.bamboo.tests.util.MockBambooClientManager;
import com.atlassian.connector.eclipse.internal.bamboo.tests.util.MockBambooRepositoryConnector;
import com.atlassian.theplugin.commons.bamboo.BambooBuild;

import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin;
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.eclipse.mylyn.tasks.ui.TasksUi;
import org.osgi.service.prefs.BackingStoreException;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.Set;

import junit.framework.TestCase;

public class BuildPlanManagerTest extends TestCase {

	private boolean wasAutoRefresh = false;

	private TaskRepository repository;

	private MockBambooClientManager bambooClientManager;

	private final int nrOfBuilds = 5;

	@Override
	protected void setUp() throws Exception {
		//turn off auto refresh
		if (BambooCorePlugin.isAutoRefresh()) {
			this.wasAutoRefresh = true;
			toggleAutoRefresh();
		}
		repository = new TaskRepository(BambooCorePlugin.CONNECTOR_KIND, "http://studio.atlassian.com");
		TasksUi.getRepositoryManager().addRepository(repository);

		File tmp = File.createTempFile("BambooTest", ".tmp");
		MockBambooRepositoryConnector connector = new MockBambooRepositoryConnector();
		bambooClientManager = new MockBambooClientManager(tmp);
		connector.setClientManager(bambooClientManager);
		BambooCorePlugin.setRepositoryConnector(connector);
		super.setUp();
	}

	@Override
	protected void tearDown() throws Exception {
		if (wasAutoRefresh) {
			toggleAutoRefresh();
		}
		TasksUiPlugin.getRepositoryManager().removeRepositoryConnector(BambooCorePlugin.CONNECTOR_KIND);
		super.tearDown();
	}

	public void testRepositoryRemoved() {
		BuildPlanManager buildPlanManager = addSubscribedBuilds();
		TaskRepository repo2 = new TaskRepository(BambooCorePlugin.CONNECTOR_KIND, "http://u.r.l/");
		TasksUi.getRepositoryManager().addRepository(repo2);
		Set<TaskRepository> repositories = TasksUi.getRepositoryManager().getRepositories(
				BambooCorePlugin.CONNECTOR_KIND);
		assertEquals(2, repositories.size());

		MockBambooClient client2 = new MockBambooClient();
		Collection<BambooBuild> expectedBuilds2 = createBuilds("repoRemoved");
		bambooClientManager.addClient(client2, repo2);
		client2.setResponse(expectedBuilds2, repo2);
		joinJob(buildPlanManager.refreshAllBuilds());

		Map<TaskRepository, Collection<BambooBuild>> subscribedBuilds = buildPlanManager.getSubscribedBuilds();

		assertEquals(2, subscribedBuilds.size());

		TasksUiPlugin.getRepositoryManager().removeRepository(repo2,
				TasksUiPlugin.getDefault().getRepositoriesFilePath());
		buildPlanManager.repositoryRemoved(repo2);

		subscribedBuilds = buildPlanManager.getSubscribedBuilds();

		assertEquals(1, subscribedBuilds.size());
	}

	public void testGetSubscribedBuilds() {
		BuildPlanManager buildPlanManager = addSubscribedBuilds();
		Map<TaskRepository, Collection<BambooBuild>> subscribedBuilds = buildPlanManager.getSubscribedBuilds();

		assertEquals(1, subscribedBuilds.size());

		Collection<BambooBuild> builds = subscribedBuilds.get(subscribedBuilds.keySet().iterator().next());

		assertTrue(nrOfBuilds == builds.size());
	}

	public void testGetSubscribedBuildsTaskRepository() {

		BuildPlanManager buildPlanManager = addSubscribedBuilds();
		BambooBuild[] subscribedBuilds = buildPlanManager.getSubscribedBuilds(repository);

		assertEquals(nrOfBuilds, subscribedBuilds.length);
	}

	public void testBuildSubscriptionsChanged() {
		BuildPlanManager buildPlanManager = addSubscribedBuilds();
		Map<TaskRepository, Collection<BambooBuild>> subscribedBuilds = buildPlanManager.getSubscribedBuilds();

		assertEquals(1, subscribedBuilds.size());

		Collection<BambooBuild> builds = subscribedBuilds.get(subscribedBuilds.keySet().iterator().next());

		assertEquals(nrOfBuilds, builds.size());

		MockBambooClient client = (MockBambooClient) bambooClientManager.getClient(repository);

		ArrayList<BambooBuild> expectedBuilds = createBuilds("yek");
		client.setResponse(expectedBuilds, repository);

		joinJob(buildPlanManager.buildSubscriptionsChanged(repository));

		subscribedBuilds = buildPlanManager.getSubscribedBuilds();

		assertEquals(1, subscribedBuilds.size());

		builds = subscribedBuilds.get(subscribedBuilds.keySet().iterator().next());

		assertEquals(nrOfBuilds, builds.size());

		for (BambooBuild build : expectedBuilds) {
			assertTrue("Expected build " + build.getPlanKey() + "-" + build.getNumber()
					+ " not contained in retrieved builds.", builds.contains(build));
		}
	}

	public void testReInitializeScheduler() {
		//get builds -> should be 0
		BuildPlanManager buildPlanManager = new BuildPlanManager();
		buildPlanManager.initializeScheduler(TasksUi.getRepositoryManager());
		Map<TaskRepository, Collection<BambooBuild>> subscribedBuilds = buildPlanManager.getSubscribedBuilds();
		assertEquals(0, subscribedBuilds.size());

		//create builds and set response to client, DO NOT MANUALLY CALL REFRESH
		MockBambooClient client = new MockBambooClient();
		Collection<BambooBuild> expectedBuilds = createBuilds("key");
		bambooClientManager.addClient(client, repository);
		client.setResponse(expectedBuilds, repository);

		//get refresh interval, reset it to 0, toggle AutoRefresh, triggers reinitialize
		int interval = BambooCorePlugin.getRefreshIntervalMinutes();
		setRefreshIntervalMinutes(0);
		toggleAutoRefresh();
		joinJob(buildPlanManager.reInitializeScheduler());

		//get builds -> should be updated
		subscribedBuilds = buildPlanManager.getSubscribedBuilds();
		assertEquals(1, subscribedBuilds.size());
		Collection<BambooBuild> builds = subscribedBuilds.get(subscribedBuilds.keySet().iterator().next());
		assertTrue(nrOfBuilds == builds.size());
		for (BambooBuild build : expectedBuilds) {
			assertTrue(builds.contains(build));
		}

		//reset time to previous, toggle autoRefresh again
		toggleAutoRefresh();
		setRefreshIntervalMinutes(interval);
		joinJob(buildPlanManager.reInitializeScheduler());

		//create new builds, set response, DO NOT REFRESH MANUALLY
		expectedBuilds = createBuilds("yek");
		client.setResponse(expectedBuilds, repository);

		//get builds should not be updated
		subscribedBuilds = buildPlanManager.getSubscribedBuilds();
		assertTrue(1 == subscribedBuilds.size());
		builds = subscribedBuilds.get(subscribedBuilds.keySet().iterator().next());
		assertTrue(nrOfBuilds == builds.size());
		for (BambooBuild build : expectedBuilds) {
			assertFalse(builds.contains(build));
		}
	}

	private BuildPlanManager addSubscribedBuilds() {
		BuildPlanManager buildPlanManager = new BuildPlanManager();

		buildPlanManager.initializeScheduler(TasksUi.getRepositoryManager());

		MockBambooClient client = new MockBambooClient();
		Collection<BambooBuild> expectedBuilds = createBuilds("key");
		bambooClientManager.addClient(client, repository);
		client.setResponse(expectedBuilds, repository);

		joinJob(buildPlanManager.refreshAllBuilds());
		return buildPlanManager;
	}

	private ArrayList<BambooBuild> createBuilds(String prefix) {
		ArrayList<BambooBuild> expectedBuilds = new ArrayList<BambooBuild>();
		for (int i = 1; i <= nrOfBuilds; i++) {
			expectedBuilds.add(new MockBambooBuild(prefix + String.valueOf(i), "http://studio.atlassian.com"));
		}
		return expectedBuilds;
	}

	private void setRefreshIntervalMinutes(int minutes) {
		IEclipsePreferences preferences = new InstanceScope().getNode(BambooCorePlugin.PLUGIN_ID);
		preferences.putInt(BambooConstants.PREFERENCE_REFRESH_INTERVAL, minutes);
		try {
			preferences.flush();
		} catch (BackingStoreException e) {
			// ignore
		}
	}

	private void toggleAutoRefresh() {
		IEclipsePreferences preferences = new InstanceScope().getNode(BambooCorePlugin.PLUGIN_ID);
		preferences.putBoolean(BambooConstants.PREFERENCE_AUTO_REFRESH, !BambooCorePlugin.isAutoRefresh());
		try {
			preferences.flush();
		} catch (BackingStoreException e) {
			// ignore
		}
	}

	private void joinJob(Job job) {
		if (job != null) {
			try {
				job.join();
			} catch (InterruptedException e) {
				fail("Failed to join job " + job.getName());
			}
		}
	}
}
