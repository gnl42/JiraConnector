/*******************************************************************************
 * Copyright (c) 2004, 2007 Mylyn project committers and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.mylyn.jira.tests;

import junit.framework.TestCase;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.mylyn.commons.net.AuthenticationCredentials;
import org.eclipse.mylyn.commons.net.AuthenticationType;
import org.eclipse.mylyn.context.tests.support.TestUtil;
import org.eclipse.mylyn.context.tests.support.TestUtil.Credentials;
import org.eclipse.mylyn.context.tests.support.TestUtil.PrivilegeLevel;
import org.eclipse.mylyn.internal.jira.core.JiraClientFactory;
import org.eclipse.mylyn.internal.jira.core.JiraCorePlugin;
import org.eclipse.mylyn.internal.jira.core.JiraCustomQuery;
import org.eclipse.mylyn.internal.jira.core.JiraRepositoryConnector;
import org.eclipse.mylyn.internal.jira.core.JiraRepositoryQuery;
import org.eclipse.mylyn.internal.jira.core.JiraTask;
import org.eclipse.mylyn.internal.jira.core.model.JiraIssue;
import org.eclipse.mylyn.internal.jira.core.model.NamedFilter;
import org.eclipse.mylyn.internal.jira.core.model.Priority;
import org.eclipse.mylyn.internal.jira.core.model.filter.ComponentFilter;
import org.eclipse.mylyn.internal.jira.core.model.filter.ContentFilter;
import org.eclipse.mylyn.internal.jira.core.model.filter.FilterDefinition;
import org.eclipse.mylyn.internal.jira.core.model.filter.ProjectFilter;
import org.eclipse.mylyn.internal.jira.core.service.JiraClient;
import org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin;
import org.eclipse.mylyn.internal.tasks.ui.util.TasksUiInternal;
import org.eclipse.mylyn.jira.tests.util.ResultCollector;
import org.eclipse.mylyn.tasks.core.AbstractRepositoryConnector;
import org.eclipse.mylyn.tasks.core.ITaskList;
import org.eclipse.mylyn.tasks.core.TaskRepository;

/**
 * @author Wesley Coelho (initial integration patch)
 * @author Mik Kersten
 * @author Steffen Pingel
 */
public class JiraFilterTest extends TestCase {

	private TaskRepository repository;

	private JiraRepositoryConnector connector;

	private ITaskList taskList;

	@Override
	protected void setUp() throws Exception {
		TasksUiPlugin.getRepositoryManager().clearRepositories(TasksUiPlugin.getDefault().getRepositoriesFilePath());
		JiraClientFactory.getDefault().clearClients();

		taskList = TasksUiPlugin.getTaskList();

		AbstractRepositoryConnector abstractConnector = TasksUiPlugin.getRepositoryManager().getRepositoryConnector(
				JiraCorePlugin.CONNECTOR_KIND);
		connector = (JiraRepositoryConnector) abstractConnector;

		repository = null;
	}

	@Override
	protected void tearDown() throws Exception {
		if (repository != null) {
			JiraClient client = JiraClientFactory.getDefault().getJiraClient(repository);
			JiraTestUtils.cleanup(client);
		}
	}

	protected void init(String url, PrivilegeLevel level) throws Exception {
		Credentials credentials = TestUtil.readCredentials(level);

		if (repository != null) {
			TasksUiPlugin.getRepositoryManager().removeRepository(repository,
					TasksUiPlugin.getDefault().getRepositoriesFilePath());
		}

		repository = new TaskRepository(JiraCorePlugin.CONNECTOR_KIND, JiraTestConstants.JIRA_39_URL);
		repository.setCredentials(AuthenticationType.REPOSITORY, new AuthenticationCredentials(credentials.username,
				credentials.password), false);
		repository.setCharacterEncoding(JiraClient.DEFAULT_CHARSET);

		TasksUiPlugin.getRepositoryManager().addRepository(repository,
				TasksUiPlugin.getDefault().getRepositoriesFilePath());
	}

	public void testJiraFilterRefresh() throws Exception {
		filterRefresh(JiraTestConstants.JIRA_39_URL);
	}

	private void filterRefresh(String url) throws Exception {
		init(url, PrivilegeLevel.USER);

		JiraClient client = JiraClientFactory.getDefault().getJiraClient(repository);
		JiraIssue issue = JiraTestUtils.newIssue(client, "testFilterRefresh");
		issue.setAssignee(client.getUserName());
		JiraTestUtils.createIssue(client, issue);

		NamedFilter[] filters = client.getNamedFilters(null);
		assertTrue(filters.length > 1);

		NamedFilter filter = filters[1];
		assertEquals("My Issues", filter.getName());

		JiraRepositoryQuery query = new JiraRepositoryQuery(repository.getRepositoryUrl(), filter);
		taskList.addQuery(query);
		assertTrue(query.getChildren().size() == 0);

		TasksUiInternal.synchronizeQuery(connector, query, null, false);

		assertTrue(query.getChildren().size() > 0);
		JiraTask hit = (JiraTask) query.getChildren().iterator().next();
		assertTrue(hit.getSummary().length() > 0);
	}

	public void testCustomQuery() throws Exception {
		customQuery(JiraTestConstants.JIRA_39_URL);
	}

	private void customQuery(String url) throws Exception {
		init(url, PrivilegeLevel.USER);

		String summary = "testCustomQuery" + System.currentTimeMillis();
		JiraClient client = JiraClientFactory.getDefault().getJiraClient(repository);
		JiraIssue issue = JiraTestUtils.newIssue(client, summary);
		issue.setPriority(client.getCache().getPriorityById(Priority.BLOCKER_ID));
		issue = JiraTestUtils.createIssue(client, issue);

		FilterDefinition filter = new FilterDefinition();
		filter.setContentFilter(new ContentFilter(summary, true, false, false, false));

		JiraCustomQuery query = new JiraCustomQuery(repository.getRepositoryUrl(), filter,
				repository.getCharacterEncoding());

		ResultCollector hitCollector = new ResultCollector();
		connector.performQuery(repository, query, hitCollector, null, null);
		assertEquals(1, hitCollector.results.size());
		assertEquals(issue.getSummary(), hitCollector.results.iterator().next().getSummary());
		//assertEquals(PriorityLevel.P1.toString(), hitCollector.getTaskDataHits().iterator().next().getPriority());
	}

	public void testCustomQueryWithoutRepositoryConfiguraton() throws Exception {
		customQueryWithoutRepositoryConfiguraton(JiraTestConstants.JIRA_39_URL);
	}

	private void customQueryWithoutRepositoryConfiguraton(String url) throws Exception {
		init(url, PrivilegeLevel.USER);

		String summary = "testCustomQueryWithoutRepositoryConfiguraton" + System.currentTimeMillis();
		JiraClient client = JiraClientFactory.getDefault().getJiraClient(repository);
		JiraTestUtils.createIssue(client, summary + " 1");

		JiraIssue issue2 = JiraTestUtils.newIssue(client, summary + " 2");
		assertTrue(issue2.getProject().getComponents().length > 0);
		issue2.setComponents(issue2.getProject().getComponents());
		JiraTestUtils.createIssue(client, issue2);

		FilterDefinition filter = new FilterDefinition();
		filter.setProjectFilter(new ProjectFilter(issue2.getProject()));
		filter.setContentFilter(new ContentFilter(summary, true, false, false, false));
		filter.setComponentFilter(new ComponentFilter(issue2.getProject().getComponents()));

		JiraCustomQuery query = new JiraCustomQuery(repository.getRepositoryUrl(), filter,
				repository.getCharacterEncoding());
		ResultCollector hitCollector = new ResultCollector();
		connector.performQuery(repository, query, hitCollector, null, null);
		assertEquals(1, hitCollector.results.size());
		assertEquals(issue2.getSummary(), hitCollector.results.iterator().next().getSummary());

		hitCollector = new ResultCollector();
		JiraClientFactory.getDefault().clearClientsAndConfigurationData();
		connector.performQuery(repository, query, hitCollector, null, new NullProgressMonitor());
		assertEquals(1, hitCollector.results.size());
		assertEquals(issue2.getSummary(), hitCollector.results.iterator().next().getSummary());
	}

//	private class MockQueryHitCollector extends QueryHitCollector {
//
//		public List<AbstractQueryHit> results = new ArrayList<AbstractQueryHit>();
//
//		private MockQueryHitCollector(TaskList tasklist) {
//			super(tasklist);
//		}
//
//		@Override
//		public void addMatch(AbstractQueryHit hit) {
//			results.add(hit);
//		}
//	}

	// TODO: reneable
// public void testJiraTaskRegistryIntegration() {
// TaskList taskList = MylynTaskListPlugin.getTaskListManager().getTaskList();
// AbstractRepositoryConnector client =
// MylynTaskListPlugin.getRepositoryManager().getRepositoryConnector(
// JiraUiPlugin.REPOSITORY_KIND);
// assertNotNull(client);
// assertEquals(""+taskList.getArchiveContainer().getChildren(), 0,
// taskList.getArchiveContainer().getChildren().size());
// JiraServer server = jiraFacade.getJiraServer(repository);
// NamedFilter[] namedFilters = server.getNamedFilters();
// JiraRepositoryQuery filter = new JiraRepositoryQuery(repository.getUrl(),
// namedFilters[0], MylynTaskListPlugin.getTaskListManager().getTaskList());
//		
// connector.synchronize(filter, null);
// // filter.refreshHits();
// // MylynTaskListPlugin.getTaskListManager().addQuery(filter);
// Job job = connector.synchronize(filter, null);
// while (job.getResult() == null) {
// // while (filter.isRefreshing()) {
// try {
// Thread.sleep(500);
// } catch (InterruptedException e) {
// e.printStackTrace();
// }
// }
//
// assertTrue(filter.getHits().size() > 0);
// JiraQueryHit jHit = (JiraQueryHit) filter.getHits().iterator().next();
//
// assertNotNull(taskList.getTask(jHit.getHandleIdentifier()));
// }
}
