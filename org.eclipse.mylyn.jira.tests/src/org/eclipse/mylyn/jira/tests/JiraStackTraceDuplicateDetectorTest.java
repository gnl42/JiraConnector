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
import org.eclipse.mylyn.context.tests.support.TestUtil;
import org.eclipse.mylyn.context.tests.support.TestUtil.Credentials;
import org.eclipse.mylyn.context.tests.support.TestUtil.PrivilegeLevel;
import org.eclipse.mylyn.internal.jira.core.JiraClientFactory;
import org.eclipse.mylyn.internal.jira.core.JiraCorePlugin;
import org.eclipse.mylyn.internal.jira.core.JiraTaskDataHandler;
import org.eclipse.mylyn.internal.jira.core.model.JiraIssue;
import org.eclipse.mylyn.internal.jira.core.service.JiraClient;
import org.eclipse.mylyn.internal.jira.core.service.JiraException;
import org.eclipse.mylyn.internal.jira.ui.JiraStackTraceDuplicateDetector;
import org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin;
import org.eclipse.mylyn.tasks.core.AbstractRepositoryConnector;
import org.eclipse.mylyn.tasks.core.AbstractTask;
import org.eclipse.mylyn.tasks.core.RepositoryTaskData;
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.eclipse.mylyn.tasks.core.TaskRepositoryManager;
import org.eclipse.mylyn.tasks.ui.TasksUiUtil;
import org.eclipse.mylyn.tasks.ui.search.SearchHitCollector;
import org.eclipse.mylyn.web.core.AuthenticationCredentials;
import org.eclipse.mylyn.web.core.AuthenticationType;

/**
 * @author Eugene Kuleshov
 */
public class JiraStackTraceDuplicateDetectorTest extends TestCase {

	private TaskRepository repository;

	private TaskRepositoryManager manager;

	private AbstractRepositoryConnector connector;

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
		String kind = JiraCorePlugin.REPOSITORY_KIND;

		Credentials credentials = TestUtil.readCredentials(PrivilegeLevel.USER);
		repository = new TaskRepository(kind, url);
		repository.setCredentials(AuthenticationType.REPOSITORY, new AuthenticationCredentials(credentials.username,
				credentials.password), false);

		manager.addRepository(repository, TasksUiPlugin.getDefault().getRepositoriesFilePath());

		connector = manager.getRepositoryConnector(kind);
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
		AbstractTask task1 = TasksUiUtil.createTask(repository, issue.getKey(), new NullProgressMonitor());
		assertEquals(issue.getSummary(), task1.getSummary());
		assertEquals(false, task1.isCompleted());
		assertNull(task1.getDueDate());

		JiraIssue issue2 = JiraTestUtils.newIssue(client, "testStackTraceDetector1");
		issue2.setDescription(stackTrace);

		RepositoryTaskData data = new RepositoryTaskData(dataHandler.getAttributeFactory(null, null, null),
				connector.getConnectorKind(), repository.getRepositoryUrl(), //
				TasksUiPlugin.getDefault().getNextNewRepositoryTaskId());
		data.setDescription(stackTrace);

		JiraStackTraceDuplicateDetector detector = new JiraStackTraceDuplicateDetector();
		SearchHitCollector collector = detector.getSearchHitCollector(repository, data);
		collector.run(new NullProgressMonitor());

		Set<AbstractTask> tasks = collector.getTasks();
		assertTrue("Expected duplicated task " + issue.getId() + " : " + issue.getKey(), tasks.size() > 0);

		for (AbstractTask task : tasks) {
			if (task.getTaskId().equals(issue.getId())) {
				return;
			}
		}
		fail("Duplicated task not found " + issue.getId() + " : " + issue.getKey());
	}

}
