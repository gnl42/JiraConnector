/*******************************************************************************
* Copyright (c) 2004, 2008 Tasktop Technologies and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Tasktop Technologies - initial API and implementation
 *******************************************************************************/

package org.eclipse.mylyn.jira.tests.core;

import junit.framework.TestCase;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.mylyn.context.tests.support.TestUtil.PrivilegeLevel;
import org.eclipse.mylyn.internal.jira.core.JiraClientFactory;
import org.eclipse.mylyn.internal.jira.core.JiraCorePlugin;
import org.eclipse.mylyn.internal.jira.core.JiraRepositoryConnector;
import org.eclipse.mylyn.internal.jira.core.model.JiraIssue;
import org.eclipse.mylyn.internal.jira.core.model.NamedFilter;
import org.eclipse.mylyn.internal.jira.core.model.Priority;
import org.eclipse.mylyn.internal.jira.core.model.filter.ComponentFilter;
import org.eclipse.mylyn.internal.jira.core.model.filter.ContentFilter;
import org.eclipse.mylyn.internal.jira.core.model.filter.FilterDefinition;
import org.eclipse.mylyn.internal.jira.core.model.filter.ProjectFilter;
import org.eclipse.mylyn.internal.jira.core.service.JiraClient;
import org.eclipse.mylyn.internal.tasks.core.RepositoryQuery;
import org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin;
import org.eclipse.mylyn.internal.tasks.ui.util.TasksUiInternal;
import org.eclipse.mylyn.jira.tests.util.JiraTestConstants;
import org.eclipse.mylyn.jira.tests.util.JiraTestResultCollector;
import org.eclipse.mylyn.jira.tests.util.JiraTestUtil;
import org.eclipse.mylyn.tasks.core.IRepositoryQuery;
import org.eclipse.mylyn.tasks.core.ITask;
import org.eclipse.mylyn.tasks.core.ITaskMapping;
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.eclipse.mylyn.tasks.core.ITask.PriorityLevel;
import org.eclipse.mylyn.tasks.core.data.TaskData;
import org.eclipse.mylyn.tasks.ui.TasksUi;

/**
 * @author Wesley Coelho (initial integration patch)
 * @author Mik Kersten
 * @author Steffen Pingel
 */
public class JiraFilterTest extends TestCase {

	private TaskRepository repository;

	private JiraRepositoryConnector connector;

	@Override
	protected void setUp() throws Exception {
		JiraTestUtil.setUp();
		connector = (JiraRepositoryConnector) TasksUi.getRepositoryConnector(JiraCorePlugin.CONNECTOR_KIND);
	}

	@Override
	protected void tearDown() throws Exception {
		JiraTestUtil.tearDown();
	}

	protected void init(String url, PrivilegeLevel level) throws Exception {
		repository = JiraTestUtil.init(url, level);
	}

	public void testJiraFilterRefresh() throws Exception {
		filterRefresh(JiraTestConstants.JIRA_LATEST_URL);
	}

	private void filterRefresh(String url) throws Exception {
		init(url, PrivilegeLevel.USER);

		JiraClient client = JiraClientFactory.getDefault().getJiraClient(repository);
		JiraIssue issue = JiraTestUtil.newIssue(client, "testFilterRefresh");
		issue.setAssignee(client.getUserName());
		JiraTestUtil.createIssue(client, issue);

		NamedFilter[] filters = client.getNamedFilters(null);
		assertTrue(filters.length > 1);

		NamedFilter filter = filters[1];
		assertEquals("My Issues", filter.getName());

		RepositoryQuery query = (RepositoryQuery) JiraTestUtil.createQuery(repository, filter);
		TasksUiPlugin.getTaskList().addQuery(query);
		assertTrue(query.getChildren().size() == 0);

		TasksUiInternal.synchronizeQuery(connector, query, null, false);
		assertTrue(query.getChildren().size() > 0);
		ITask hit = query.getChildren().iterator().next();
		assertTrue(hit.getSummary().length() > 0);
	}

	public void testCustomQuery() throws Exception {
		customQuery(JiraTestConstants.JIRA_LATEST_URL);
	}

	private void customQuery(String url) throws Exception {
		init(url, PrivilegeLevel.USER);

		String summary = "testCustomQuery" + System.currentTimeMillis();
		JiraClient client = JiraClientFactory.getDefault().getJiraClient(repository);
		JiraIssue issue = JiraTestUtil.newIssue(client, summary);
		issue.setPriority(client.getCache().getPriorityById(Priority.BLOCKER_ID));
		issue = JiraTestUtil.createIssue(client, issue);

		FilterDefinition filter = new FilterDefinition();
		filter.setContentFilter(new ContentFilter(summary, true, false, false, false));
		IRepositoryQuery query = JiraTestUtil.createQuery(repository, filter);

		JiraTestResultCollector hitCollector = new JiraTestResultCollector();
		connector.performQuery(repository, query, hitCollector, null, null);
		assertEquals(1, hitCollector.results.size());
		TaskData hit = hitCollector.results.iterator().next();
		ITaskMapping mapping = connector.getTaskMapping(hit);
		assertEquals(issue.getSummary(), mapping.getSummary());
		assertEquals(PriorityLevel.P1, mapping.getPriorityLevel());
	}

	public void testCustomQueryWithoutRepositoryConfiguraton() throws Exception {
		customQueryWithoutRepositoryConfiguraton(JiraTestConstants.JIRA_LATEST_URL);
	}

	private void customQueryWithoutRepositoryConfiguraton(String url) throws Exception {
		init(url, PrivilegeLevel.USER);

		String summary = "testCustomQueryWithoutRepositoryConfiguraton" + System.currentTimeMillis();
		JiraClient client = JiraClientFactory.getDefault().getJiraClient(repository);
		JiraTestUtil.createIssue(client, summary + " 1");

		JiraIssue issue2 = JiraTestUtil.newIssue(client, summary + " 2");
		assertTrue(issue2.getProject().getComponents().length > 0);
		issue2.setComponents(issue2.getProject().getComponents());
		JiraTestUtil.createIssue(client, issue2);

		FilterDefinition filter = new FilterDefinition();
		filter.setProjectFilter(new ProjectFilter(issue2.getProject()));
		filter.setContentFilter(new ContentFilter(summary, true, false, false, false));
		filter.setComponentFilter(new ComponentFilter(issue2.getProject().getComponents()));

		IRepositoryQuery query = JiraTestUtil.createQuery(repository, filter);
		JiraTestResultCollector hitCollector = new JiraTestResultCollector();
		connector.performQuery(repository, query, hitCollector, null, null);
		assertEquals(1, hitCollector.results.size());
		ITaskMapping taskMapping = connector.getTaskMapping(hitCollector.results.iterator().next());
		assertEquals(issue2.getSummary(), taskMapping.getSummary());

		hitCollector = new JiraTestResultCollector();
		JiraClientFactory.getDefault().clearClientsAndConfigurationData();
		connector.performQuery(repository, query, hitCollector, null, new NullProgressMonitor());
		assertEquals(1, hitCollector.results.size());
		taskMapping = connector.getTaskMapping(hitCollector.results.iterator().next());
		assertEquals(issue2.getSummary(), taskMapping.getSummary());
	}

}
