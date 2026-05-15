/*******************************************************************************
 * Copyright (c) 2009, 2013 Tasktop Technologies and others.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v. 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Tasktop Technologies - initial API and implementation
 *     See git history
 *******************************************************************************/

package org.eclipse.mylyn.tests.util;

import java.net.Proxy;
import java.util.Arrays;
import java.util.HashSet;

import org.eclipse.core.runtime.Assert;
import org.eclipse.mylyn.commons.net.AbstractWebLocation;
import org.eclipse.mylyn.commons.net.AuthenticationCredentials;
import org.eclipse.mylyn.commons.net.AuthenticationType;
import org.eclipse.mylyn.commons.net.WebLocation;
import org.eclipse.mylyn.commons.net.WebUtil;
import org.eclipse.mylyn.commons.repositories.core.auth.UserCredentials;
import org.eclipse.mylyn.commons.sdk.util.CommonTestUtil;
import org.eclipse.mylyn.commons.sdk.util.CommonTestUtil.PrivilegeLevel;
import org.eclipse.mylyn.internal.tasks.ui.ITasksUiPreferenceConstants;
import org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin;
import org.eclipse.mylyn.internal.tasks.ui.views.TaskListView;
import org.eclipse.mylyn.tasks.core.AbstractRepositoryConnector;
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.eclipse.mylyn.tasks.ui.TasksUi;

/**
 * @author Steffen Pingel
 * @author Thomas Ehrnhoefer
 */
@SuppressWarnings({ "nls", "restriction" })
public abstract class TestFixture {

	/**
	 * Clears all tasks.
	 */
	public static void resetTaskList() throws Exception {
		TasksUi.getTaskActivityManager().deactivateActiveTask();
		TasksUiPlugin.getTaskListExternalizationParticipant().resetTaskList();
		TasksUiPlugin.getTaskActivityManager().getTaskActivationHistory().clear();
		final var view = TaskListView.getFromActivePerspective();
		if (view != null) {
			view.refresh();
		}
	}

	/**
	 * Clears tasks and repositories. When this method returns only the local task repository will exist and the task list will only have
	 * default categories but no tasks.
	 */
	public static void resetTaskListAndRepositories() throws Exception {
		TasksUiPlugin.getRepositoryManager().clearRepositories();
		TasksUiPlugin.getDefault().getLocalTaskRepository();
		resetTaskList();
	}

	/**
	 * @see #resetTaskList()
	 */
	public static void saveAndReadTasklist() throws Exception {
		TasksUiPlugin.getTaskList().notifyElementsChanged(null);
		saveNow();
		resetTaskList();
		TasksUiPlugin.getDefault().initializeDataSources();
	}

	public static void saveNow() throws Exception {
		TasksUiPlugin.getExternalizationManager().saveNow();
	}

	private final String connectorKind;

	private String repositoryName;

	protected final String repositoryUrl;

	private String simpleInfo;

	private String description;

	public TestFixture(final String connectorKind, final String repositoryUrl) {
		this.connectorKind = connectorKind;
		this.repositoryUrl = repositoryUrl;
	}

	protected abstract TestFixture activate();

	protected void configureRepository(final TaskRepository repository) {
	}

	public AbstractRepositoryConnector connector() {
		return TasksUi.getRepositoryConnector(connectorKind);
	}

	public String getConnectorKind() {
		return connectorKind;
	}

	protected abstract TestFixture getDefault();

	public String getInfo() {
		return repositoryName + " " + simpleInfo;
	}

	public String getRepositoryUrl() {
		return repositoryUrl;
	}

	public String getSimpleInfo() {
		return simpleInfo;
	}

	public AbstractWebLocation location() throws Exception {
		return location(PrivilegeLevel.USER);
	}

	public AbstractWebLocation location(final PrivilegeLevel level) throws Exception {
		return location(level, WebUtil.getProxyForUrl(repositoryUrl));
	}

	public AbstractWebLocation location(final PrivilegeLevel level, final Proxy proxy) throws Exception {
		final var credentials = getCredentials(level);
		return location(credentials.getUserName(), credentials.getPassword(), proxy);
	}

	protected UserCredentials getCredentials(final PrivilegeLevel level) {
		return CommonTestUtil.getCredentials(level);
	}

	public AbstractWebLocation location(final String username, final String password) throws Exception {
		return location(username, password, WebUtil.getProxyForUrl(repositoryUrl));
	}

	public AbstractWebLocation location(final String username, final String password, final Proxy proxy) throws Exception {
		return new WebLocation(repositoryUrl, username, password, (host, proxyType) -> proxy);
	}

	public TaskRepository repository() {
		final var repository = new TaskRepository(connectorKind, repositoryUrl);
		final var credentials = getCredentials(PrivilegeLevel.USER);
		repository.setCredentials(AuthenticationType.REPOSITORY,
				new AuthenticationCredentials(credentials.getUserName(), credentials.getPassword()), false);
		return repository;
	}

	protected void resetRepositories() {
	}

	protected void setInfo(final String repositoryName, final String version, final String description) {
		Assert.isNotNull(repositoryName);
		Assert.isNotNull(version);
		this.repositoryName = repositoryName;
		simpleInfo = version;
		this.description = description;
		if (description != null && description.length() > 0) {
			simpleInfo += "/" + description;
		}
	}

	public TaskRepository singleRepository() {
		final var manager = TasksUiPlugin.getRepositoryManager();
		if (manager != null) {
			manager.clearRepositories();
		}
		resetRepositories();

		final var repository = new TaskRepository(connectorKind, repositoryUrl);
		final var credentials = getCredentials(PrivilegeLevel.USER);
		repository.setCredentials(AuthenticationType.REPOSITORY,
				new AuthenticationCredentials(credentials.getUserName(), credentials.getPassword()), true);
		configureRepository(repository);
		if (manager != null) {
			manager.addRepository(repository);
		}
		return repository;
	}

	public void setUpFramework() {
		initializeTasksSettings();
	}

	public static void initializeTasksSettings() {
		try {
			final var plugin = TasksUiPlugin.getDefault();
			if (plugin == null) {
				return;
			}
			final var store = plugin.getPreferenceStore();
			store.setValue(ITasksUiPreferenceConstants.NOTIFICATIONS_ENABLED, false);
			store.setValue(ITasksUiPreferenceConstants.REPOSITORY_SYNCH_SCHEDULE_ENABLED, false);
		} catch (final NoClassDefFoundError e) {
			// ignore, running in headless standalone environment
		}
	}

	public boolean hasTag(final String tag) {
		return false;
	}

	public boolean isExcluded() {
		final var excludeFixture = System.getProperty("mylyn.test.exclude", "");
		final var excludeFixtureArray = excludeFixture.split(",");
		return new HashSet<>(Arrays.asList(excludeFixtureArray)).contains(getRepositoryUrl());
	}

	public String getDescription() {
		return description;
	}

}
