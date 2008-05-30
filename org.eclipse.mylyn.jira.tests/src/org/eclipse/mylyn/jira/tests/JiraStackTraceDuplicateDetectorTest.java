/*******************************************************************************
 * Copyright (c) 2004, 2007 Mylyn project committers and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.mylyn.jira.tests;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Set;

import junit.framework.TestCase;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.mylyn.commons.net.AuthenticationCredentials;
import org.eclipse.mylyn.commons.net.AuthenticationType;
import org.eclipse.mylyn.context.tests.support.TestUtil;
import org.eclipse.mylyn.context.tests.support.TestUtil.Credentials;
import org.eclipse.mylyn.context.tests.support.TestUtil.PrivilegeLevel;
import org.eclipse.mylyn.internal.jira.core.JiraAttribute;
import org.eclipse.mylyn.internal.jira.core.JiraClientFactory;
import org.eclipse.mylyn.internal.jira.core.JiraCorePlugin;
import org.eclipse.mylyn.internal.jira.core.JiraRepositoryConnector;
import org.eclipse.mylyn.internal.jira.core.JiraTaskDataHandler;
import org.eclipse.mylyn.internal.jira.core.model.JiraIssue;
import org.eclipse.mylyn.internal.jira.core.service.JiraClient;
import org.eclipse.mylyn.internal.jira.core.service.JiraException;
import org.eclipse.mylyn.internal.jira.ui.JiraSearchHandler;
import org.eclipse.mylyn.internal.tasks.core.TaskRepositoryManager;
import org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin;
import org.eclipse.mylyn.internal.tasks.ui.search.SearchHitCollector;
import org.eclipse.mylyn.internal.tasks.ui.util.TasksUiInternal;
import org.eclipse.mylyn.tasks.core.IRepositoryQuery;
import org.eclipse.mylyn.tasks.core.ITask;
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.eclipse.mylyn.tasks.core.data.TaskData;
import org.eclipse.mylyn.tasks.ui.TasksUi;

/**
 * @author Eugene Kuleshov
 */
public class JiraStackTraceDuplicateDetectorTest extends TestCase {

	private TaskRepository repository;

	private TaskRepositoryManager manager;

	private JiraRepositoryConnector connector;

	private JiraTaskDataHandler dataHandler;

	private JiraClient client;

	@Override
	protected void setUp() throws Exception {
		super.setUp();

		manager = TasksUiPlugin.getRepositoryManager();
		manager.clearRepositories(TasksUiPlugin.getDefault().getRepositoriesFilePath());

		JiraClientFactory.getDefault().clearClients();
	}

	@Override
	protected void tearDown() throws Exception {
		if (client != null) {
			JiraTestUtils.cleanup(client);
		}
	}

	protected void init(String url) throws Exception {
		String kind = JiraCorePlugin.CONNECTOR_KIND;

		Credentials credentials = TestUtil.readCredentials(PrivilegeLevel.USER);
		repository = new TaskRepository(kind, url);
		repository.setCredentials(AuthenticationType.REPOSITORY, new AuthenticationCredentials(credentials.username,
				credentials.password), false);

		manager.addRepository(repository);

		connector = (JiraRepositoryConnector) manager.getRepositoryConnector(kind);
		assertEquals(connector.getConnectorKind(), kind);
		dataHandler = (JiraTaskDataHandler) connector.getTaskDataHandler();
		client = JiraClientFactory.getDefault().getJiraClient(repository);
	}

	public void testStackTraceInDescription() throws Exception {
		init(JiraTestConstants.JIRA_39_URL);

		StringWriter sw = new StringWriter();
		new Exception().printStackTrace(new PrintWriter(sw));
		String stackTrace = sw.toString();

		JiraIssue issue1 = JiraTestUtils.newIssue(client, "testStackTraceDetector1");
		issue1.setDescription(stackTrace);
		issue1 = JiraTestUtils.createIssue(client, issue1);

		verifyDuplicate(stackTrace, issue1);
	}

	public void testStackTraceInComment() throws Exception {
		init(JiraTestConstants.JIRA_39_URL);

		StringWriter sw = new StringWriter();
		new Exception().printStackTrace(new PrintWriter(sw));
		String stackTrace = sw.toString();

		JiraIssue issue1 = JiraTestUtils.createIssue(client, "testStackTraceDetector2");

		client.updateIssue(issue1, stackTrace, null);

		verifyDuplicate(stackTrace, issue1);
	}

	private void verifyDuplicate(String stackTrace, JiraIssue issue) throws JiraException, CoreException {
		ITask task1 = TasksUiInternal.createTask(repository, issue.getKey(), new NullProgressMonitor());
		assertEquals(issue.getSummary(), task1.getSummary());
		assertEquals(false, task1.isCompleted());
		assertNull(task1.getDueDate());

		JiraIssue issue2 = JiraTestUtils.newIssue(client, "testStackTraceDetector1");
		issue2.setDescription(stackTrace);

		TaskData data = new TaskData(dataHandler.getAttributeMapper(repository), repository.getConnectorKind(),
				repository.getRepositoryUrl(), "");
		dataHandler.initializeTaskData(repository, data, null, null);
		data.getRoot().getMappedAttribute(JiraAttribute.DESCRIPTION.id()).setValue(stackTrace);

		JiraSearchHandler detector = new JiraSearchHandler();
		IRepositoryQuery duplicatesQuery = TasksUi.getRepositoryModel().createQuery(repository);
		assertTrue(detector.queryForText(repository, duplicatesQuery, data, stackTrace));
		SearchHitCollector collector = new SearchHitCollector(TasksUiInternal.getTaskList(), repository,
				duplicatesQuery);

		collector.run(new NullProgressMonitor());

		Set<ITask> tasks = collector.getTasks();
		assertTrue("Expected duplicated task " + issue.getId() + " : " + issue.getKey(), tasks.size() > 0);

		for (ITask task : tasks) {
			if (task.getTaskId().equals(issue.getId())) {
				return;
			}
		}
		fail("Duplicated task not found " + issue.getId() + " : " + issue.getKey());
	}
}
