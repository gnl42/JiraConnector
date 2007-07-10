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
import org.eclipse.mylyn.context.tests.support.TestUtil;
import org.eclipse.mylyn.context.tests.support.TestUtil.Credentials;
import org.eclipse.mylyn.context.tests.support.TestUtil.PrivilegeLevel;
import org.eclipse.mylyn.internal.jira.core.model.Issue;
import org.eclipse.mylyn.internal.jira.core.model.NamedFilter;
import org.eclipse.mylyn.internal.jira.core.model.Priority;
import org.eclipse.mylyn.internal.jira.core.model.filter.ComponentFilter;
import org.eclipse.mylyn.internal.jira.core.model.filter.ContentFilter;
import org.eclipse.mylyn.internal.jira.core.model.filter.FilterDefinition;
import org.eclipse.mylyn.internal.jira.core.model.filter.ProjectFilter;
import org.eclipse.mylyn.internal.jira.core.service.JiraClient;
import org.eclipse.mylyn.internal.jira.ui.JiraClientFacade;
import org.eclipse.mylyn.internal.jira.ui.JiraCustomQuery;
import org.eclipse.mylyn.internal.jira.ui.JiraRepositoryConnector;
import org.eclipse.mylyn.internal.jira.ui.JiraRepositoryQuery;
import org.eclipse.mylyn.internal.jira.ui.JiraTask;
import org.eclipse.mylyn.internal.jira.ui.JiraUiPlugin;
import org.eclipse.mylyn.tasks.core.AbstractRepositoryConnector;
import org.eclipse.mylyn.tasks.core.QueryHitCollector;
import org.eclipse.mylyn.tasks.core.TaskList;
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.eclipse.mylyn.tasks.ui.TaskFactory;
import org.eclipse.mylyn.tasks.ui.TasksUiPlugin;

/**
 * @author Wesley Coelho (initial integration patch)
 * @author Mik Kersten
 * @author Steffen Pingel
 */
public class JiraFilterTest extends TestCase {

	private TaskRepository repository;

	private JiraRepositoryConnector connector;

	private TaskList taskList;

	@Override
	protected void setUp() throws Exception {
		TasksUiPlugin.getSynchronizationManager().setForceSyncExec(true);

		TasksUiPlugin.getRepositoryManager().clearRepositories(TasksUiPlugin.getDefault().getRepositoriesFilePath());
		JiraClientFacade.getDefault().clearClients();

		taskList = TasksUiPlugin.getTaskListManager().getTaskList();

		AbstractRepositoryConnector abstractConnector = TasksUiPlugin.getRepositoryManager().getRepositoryConnector(
				JiraUiPlugin.REPOSITORY_KIND);
		connector = (JiraRepositoryConnector) abstractConnector;

		repository = null;
	}

	@Override
	protected void tearDown() throws Exception {
	}

	protected void init(String url, PrivilegeLevel level) throws Exception {
		Credentials credentials = TestUtil.readCredentials(level);

		if (repository != null) {
			TasksUiPlugin.getRepositoryManager().removeRepository(repository,
					TasksUiPlugin.getDefault().getRepositoriesFilePath());
		}

		repository = new TaskRepository(JiraUiPlugin.REPOSITORY_KIND, JiraTestConstants.JIRA_39_URL);
		repository.setAuthenticationCredentials(credentials.username, credentials.password);
		repository.setCharacterEncoding(JiraClient.CHARSET);

		TasksUiPlugin.getRepositoryManager().addRepository(repository,
				TasksUiPlugin.getDefault().getRepositoriesFilePath());
	}

	public void testJiraFilterRefresh() throws Exception {
		filterRefresh(JiraTestConstants.JIRA_39_URL);
	}

	private void filterRefresh(String url) throws Exception {
		init(url, PrivilegeLevel.USER);

		JiraClient client = JiraClientFacade.getDefault().getJiraClient(repository);
		Issue issue = JiraTestUtils.createIssue(client, "testFilterRefresh");
		issue.setAssignee(client.getUserName());
		client.createIssue(issue);

		NamedFilter[] filters = client.getNamedFilters();
		assertTrue(filters.length > 0);

		NamedFilter filter = filters[0];
		assertEquals("My Issues", filter.getName());

		JiraRepositoryQuery query = new JiraRepositoryQuery(repository.getUrl(), filter);
		taskList.addQuery(query);
		assertTrue(query.getChildren().size() == 0);

		TasksUiPlugin.getSynchronizationManager().synchronize(connector, query, null, false);

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
		JiraClient client = JiraClientFacade.getDefault().getJiraClient(repository);
		Issue issue = JiraTestUtils.createIssue(client, summary);
		issue.setPriority(client.getPriorityById(Priority.BLOCKER_ID));
		issue = client.createIssue(issue);

		FilterDefinition filter = new FilterDefinition();
		filter.setContentFilter(new ContentFilter(summary, true, false, false, false));

		JiraCustomQuery query = new JiraCustomQuery(repository.getUrl(), filter, repository.getCharacterEncoding());

		QueryHitCollector hitCollector = new QueryHitCollector(new TaskFactory(repository, true, false));

		connector.performQuery(query, repository, new NullProgressMonitor(), hitCollector);
		assertEquals(1, hitCollector.getTasks().size());
		assertEquals(issue.getSummary(), hitCollector.getTasks().iterator().next().getSummary());
		//assertEquals(PriorityLevel.P1.toString(), hitCollector.getTaskDataHits().iterator().next().getPriority());
	}

	public void testCustomQueryWithoutRepositoryConfiguraton() throws Exception {
		customQueryWithoutRepositoryConfiguraton(JiraTestConstants.JIRA_39_URL);
	}

	private void customQueryWithoutRepositoryConfiguraton(String url) throws Exception {
		init(url, PrivilegeLevel.USER);

		String summary = "testCustomQueryWithoutRepositoryConfiguraton" + System.currentTimeMillis();
		JiraClient client = JiraClientFacade.getDefault().getJiraClient(repository);
		Issue issue = JiraTestUtils.createIssue(client, summary + " 1");
		client.createIssue(issue);

		Issue issue2 = JiraTestUtils.createIssue(client, summary + " 2");
		assertTrue(issue2.getProject().getComponents().length > 0);
		issue2.setComponents(issue2.getProject().getComponents());
		client.createIssue(issue2);

		FilterDefinition filter = new FilterDefinition();
		filter.setProjectFilter(new ProjectFilter(issue2.getProject()));
		filter.setContentFilter(new ContentFilter(summary, true, false, false, false));
		filter.setComponentFilter(new ComponentFilter(issue2.getProject().getComponents()));

		JiraCustomQuery query = new JiraCustomQuery(repository.getUrl(), filter, repository.getCharacterEncoding());
		QueryHitCollector hitCollector = new QueryHitCollector(new TaskFactory(repository, true, false));
		connector.performQuery(query, repository, new NullProgressMonitor(), hitCollector);
		assertEquals(1, hitCollector.getTasks().size());
		assertEquals(issue2.getSummary(), hitCollector.getTasks().iterator().next().getSummary());

		hitCollector = new QueryHitCollector(new TaskFactory(repository, true, false));
		JiraClientFacade.getDefault().clearClientsAndConfigurationData();
		connector.performQuery(query, repository, new NullProgressMonitor(), hitCollector);
		assertEquals(1, hitCollector.getTasks().size());
		assertEquals(issue2.getSummary(), hitCollector.getTasks().iterator().next().getSummary());
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
