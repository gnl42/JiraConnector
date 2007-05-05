/*******************************************************************************
 * Copyright (c) 2006 - 2006 Mylar eclipse.org project and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Mylar project committers - initial API and implementation
 *******************************************************************************/

package org.eclipse.mylar.jira.tests;

import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.mylar.context.tests.support.MylarTestUtils;
import org.eclipse.mylar.context.tests.support.MylarTestUtils.Credentials;
import org.eclipse.mylar.context.tests.support.MylarTestUtils.PrivilegeLevel;
import org.eclipse.mylar.internal.jira.core.model.Issue;
import org.eclipse.mylar.internal.jira.core.model.NamedFilter;
import org.eclipse.mylar.internal.jira.core.model.filter.ComponentFilter;
import org.eclipse.mylar.internal.jira.core.model.filter.ContentFilter;
import org.eclipse.mylar.internal.jira.core.model.filter.FilterDefinition;
import org.eclipse.mylar.internal.jira.core.model.filter.ProjectFilter;
import org.eclipse.mylar.internal.jira.core.service.JiraServer;
import org.eclipse.mylar.internal.jira.ui.JiraCustomQuery;
import org.eclipse.mylar.internal.jira.ui.JiraQueryHit;
import org.eclipse.mylar.internal.jira.ui.JiraRepositoryConnector;
import org.eclipse.mylar.internal.jira.ui.JiraRepositoryQuery;
import org.eclipse.mylar.internal.jira.ui.JiraServerFacade;
import org.eclipse.mylar.internal.jira.ui.JiraUiPlugin;
import org.eclipse.mylar.tasks.core.AbstractQueryHit;
import org.eclipse.mylar.tasks.core.AbstractRepositoryConnector;
import org.eclipse.mylar.tasks.core.QueryHitCollector;
import org.eclipse.mylar.tasks.core.TaskList;
import org.eclipse.mylar.tasks.core.TaskRepository;
import org.eclipse.mylar.tasks.ui.TasksUiPlugin;

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

		taskList = TasksUiPlugin.getTaskListManager().getTaskList();
	}

	@Override
	protected void tearDown() throws Exception {
	}

	protected void init(String url, PrivilegeLevel level) throws Exception {
		String kind = JiraUiPlugin.REPOSITORY_KIND;
		Credentials credentials = MylarTestUtils.readCredentials(level);

		repository = new TaskRepository(kind, JiraTestConstants.JIRA_381_URL);
		repository.setAuthenticationCredentials(credentials.username, credentials.password);
		repository.setCharacterEncoding(JiraServer.CHARSET);

		TasksUiPlugin.getRepositoryManager().addRepository(repository,
				TasksUiPlugin.getDefault().getRepositoriesFilePath());

		AbstractRepositoryConnector abstractConnector = TasksUiPlugin.getRepositoryManager().getRepositoryConnector(
				kind);
		assertEquals(abstractConnector.getRepositoryType(), kind);

		connector = (JiraRepositoryConnector) abstractConnector;
	}

	public void testJiraFilterRefresh() throws Exception {
		filterRefresh(JiraTestConstants.JIRA_381_URL);
	}

	private void filterRefresh(String url) throws Exception {
		init(url, PrivilegeLevel.USER);

		JiraServer server = JiraServerFacade.getDefault().getJiraServer(repository);
		Issue issue = JiraTestUtils.createIssue(server, "testFilterRefresh");
		issue.setAssignee(server.getUserName());
		server.updateIssue(issue, "comment");

		NamedFilter[] filters = server.getNamedFilters();
		assertTrue(filters.length > 0);

		NamedFilter filter = filters[0];
		assertEquals("My Issues", filter.getName());
		
		JiraRepositoryQuery query = new JiraRepositoryQuery(repository.getUrl(), filter, taskList);
		taskList.addQuery(query);
		assertTrue(query.getHits().size() == 0);

		TasksUiPlugin.getSynchronizationManager().synchronize(connector, query, null);

		assertTrue(query.getHits().size() > 0);
		JiraQueryHit hit = (JiraQueryHit) query.getHits().iterator().next();
		assertTrue(hit.getSummary().length() > 0);
	}

	public void testCustomQuery() throws Exception {
		customQuery(JiraTestConstants.JIRA_381_URL);
	}

	private void customQuery(String url) throws Exception {
		init(url, PrivilegeLevel.USER);

		String summary = "testCustomQuery" + System.currentTimeMillis();
		JiraServer server = JiraServerFacade.getDefault().getJiraServer(repository);
		Issue issue = JiraTestUtils.createIssue(server, summary);

		FilterDefinition filter = new FilterDefinition();
		filter.setContentFilter(new ContentFilter(summary, true, false, false, false));

		JiraCustomQuery query = new JiraCustomQuery(repository.getUrl(), filter, repository.getCharacterEncoding(),
				taskList);

		MockQueryHitCollector hitCollector = new MockQueryHitCollector(taskList);

		connector.performQuery(query, repository, new NullProgressMonitor(), hitCollector);
		assertEquals(1, hitCollector.results.size());
		assertEquals(issue.getSummary(), hitCollector.results.get(0).getSummary());
	}

	public void testCustomQueryWithoutRepositoryConfiguraton() throws Exception {
		customQueryWithoutRepositoryConfiguraton(JiraTestConstants.JIRA_381_URL);
	}

	private void customQueryWithoutRepositoryConfiguraton(String url) throws Exception {
		init(url, PrivilegeLevel.USER);

		String summary = "testCustomQueryWithoutRepositoryConfiguraton" + System.currentTimeMillis();
		JiraServer server = JiraServerFacade.getDefault().getJiraServer(repository);
		JiraTestUtils.createIssue(server, summary + " 1");
		Issue issue2 = JiraTestUtils.createIssue(server, summary + " 2");
		issue2.setComponents(issue2.getProject().getComponents());
		server.updateIssue(issue2, "comment");

		FilterDefinition filter = new FilterDefinition();
		filter.setProjectFilter(new ProjectFilter(issue2.getProject()));
		filter.setContentFilter(new ContentFilter(summary, true, false, false, false));
		filter.setComponentFilter(new ComponentFilter(issue2.getProject().getComponents()));

		JiraCustomQuery query = new JiraCustomQuery(repository.getUrl(), filter, repository.getCharacterEncoding(),
				taskList);
		MockQueryHitCollector hitCollector = new MockQueryHitCollector(taskList);
		connector.performQuery(query, repository, new NullProgressMonitor(), hitCollector);
		assertEquals(1, hitCollector.results.size());
		assertEquals(issue2.getSummary(), hitCollector.results.get(0).getSummary());

		hitCollector.results.clear();
		JiraServerFacade.getDefault().clearServersAndConfigurationData();
		connector.performQuery(query, repository, new NullProgressMonitor(), hitCollector);
		assertEquals(1, hitCollector.results.size());
		assertEquals(issue2.getSummary(), hitCollector.results.get(0).getSummary());
	}
	
	private class MockQueryHitCollector extends QueryHitCollector {

		public List<AbstractQueryHit> results = new ArrayList<AbstractQueryHit>();

		private MockQueryHitCollector(TaskList tasklist) {
			super(tasklist);
		}

		@Override
		public void addMatch(AbstractQueryHit hit) {
			results.add(hit);
		}
	}


	// TODO: reneable
// public void testJiraTaskRegistryIntegration() {
// TaskList taskList = MylarTaskListPlugin.getTaskListManager().getTaskList();
// AbstractRepositoryConnector client =
// MylarTaskListPlugin.getRepositoryManager().getRepositoryConnector(
// JiraUiPlugin.REPOSITORY_KIND);
// assertNotNull(client);
// assertEquals(""+taskList.getArchiveContainer().getChildren(), 0,
// taskList.getArchiveContainer().getChildren().size());
// JiraServer server = jiraFacade.getJiraServer(repository);
// NamedFilter[] namedFilters = server.getNamedFilters();
// JiraRepositoryQuery filter = new JiraRepositoryQuery(repository.getUrl(),
// namedFilters[0], MylarTaskListPlugin.getTaskListManager().getTaskList());
//		
// connector.synchronize(filter, null);
// // filter.refreshHits();
// // MylarTaskListPlugin.getTaskListManager().addQuery(filter);
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
