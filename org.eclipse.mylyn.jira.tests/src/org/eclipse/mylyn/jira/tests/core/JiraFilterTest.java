/*******************************************************************************
 * Copyright (c) 2004, 2009 Tasktop Technologies and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Tasktop Technologies - initial API and implementation
 *******************************************************************************/

package org.eclipse.mylyn.jira.tests.core;

import java.util.GregorianCalendar;
import java.util.Locale;

import junit.framework.TestCase;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.mylyn.internal.jira.core.JiraClientFactory;
import org.eclipse.mylyn.internal.jira.core.JiraCorePlugin;
import org.eclipse.mylyn.internal.jira.core.JiraRepositoryConnector;
import org.eclipse.mylyn.internal.jira.core.model.JiraIssue;
import org.eclipse.mylyn.internal.jira.core.model.NamedFilter;
import org.eclipse.mylyn.internal.jira.core.model.Priority;
import org.eclipse.mylyn.internal.jira.core.model.Project;
import org.eclipse.mylyn.internal.jira.core.model.filter.ComponentFilter;
import org.eclipse.mylyn.internal.jira.core.model.filter.ContentFilter;
import org.eclipse.mylyn.internal.jira.core.model.filter.DateRangeFilter;
import org.eclipse.mylyn.internal.jira.core.model.filter.FilterDefinition;
import org.eclipse.mylyn.internal.jira.core.model.filter.ProjectFilter;
import org.eclipse.mylyn.internal.jira.core.service.JiraClient;
import org.eclipse.mylyn.internal.tasks.core.RepositoryQuery;
import org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin;
import org.eclipse.mylyn.internal.tasks.ui.util.TasksUiInternal;
import org.eclipse.mylyn.jira.tests.util.JiraFixture;
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
 * @author Thomas Ehrnhoefer (multiple projects selection)
 */
public class JiraFilterTest extends TestCase {

	private TaskRepository repository;

	private JiraRepositoryConnector connector;

	private JiraClient client;

	@Override
	protected void setUp() throws Exception {
		client = JiraFixture.current().client();
		connector = (JiraRepositoryConnector) TasksUi.getRepositoryConnector(JiraCorePlugin.CONNECTOR_KIND);
		repository = JiraFixture.current().singleRepository();
	}

	@Override
	protected void tearDown() throws Exception {
		JiraTestUtil.tearDown();
	}

	public void testJiraFilterRefresh() throws Exception {
		JiraIssue issue = JiraTestUtil.newIssue(client, "testFilterRefresh");
		issue.setAssignee(client.getUserName());
		JiraTestUtil.createIssue(client, issue);

		NamedFilter[] filters = client.getNamedFilters(null);
		assertNotNull("Expected named filters on server", filters);

		NamedFilter filter = null;
		for (NamedFilter f : filters) {
			if (f.getName().equals("My Recent")) {
				filter = f;
			}
		}
		assertNotNull("Filter 'My Recent' not found", filter);

		RepositoryQuery query = (RepositoryQuery) JiraTestUtil.createQuery(repository, filter);
		TasksUiPlugin.getTaskList().addQuery(query);
		assertTrue(query.getChildren().size() == 0);

		TasksUiInternal.synchronizeQuery(connector, query, null, false);
		assertTrue(query.getChildren().size() > 0);
		ITask hit = query.getChildren().iterator().next();
		assertTrue(hit.getSummary().length() > 0);
	}

	public void testCustomQuery() throws Exception {
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
		String summary = "testCustomQueryWithoutRepositoryConfiguraton" + System.currentTimeMillis();
		JiraClient client = JiraClientFactory.getDefault().getJiraClient(repository);
		JiraTestUtil.createIssue(client, summary + " 1");

		JiraIssue issue2 = JiraTestUtil.newIssue(client, summary + " 2");
		assertTrue(issue2.getProject().getComponents().length > 0);
		issue2.setComponents(issue2.getProject().getComponents());
		JiraTestUtil.createIssue(client, issue2);

		FilterDefinition filter = new FilterDefinition();
		filter.setProjectFilter(new ProjectFilter(new Project[] { issue2.getProject() }));
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

	public void testCustomQueryWrongLocale() throws Exception {
		FilterDefinition filter = new FilterDefinition();
		GregorianCalendar date = new GregorianCalendar(2008, 9, 1);
		filter.setCreatedDateFilter(new DateRangeFilter(date.getTime(), date.getTime()));
		IRepositoryQuery query = JiraTestUtil.createQuery(repository, filter);

		JiraTestResultCollector hitCollector = new JiraTestResultCollector();
		IStatus result = connector.performQuery(repository, query, hitCollector, null, null);
		assertEquals(IStatus.OK, result.getSeverity());

		String originalProptery = repository.getProperty("jira.locale");
		try {
			repository.setProperty("jira.locale", Locale.GERMANY.toString());
			filter = new FilterDefinition();
			date = new GregorianCalendar(2008, 9, 1);
			filter.setCreatedDateFilter(new DateRangeFilter(date.getTime(), date.getTime()));
			query = JiraTestUtil.createQuery(repository, filter);
			result = connector.performQuery(repository, query, hitCollector, null, null);
			assertEquals("Date not localized.", IStatus.ERROR, result.getSeverity());
		} finally {
			repository.setProperty("jira.locale", originalProptery);
		}
	}

	public void testCustomQueryWrongDatePattern() throws Exception {
		FilterDefinition filter = new FilterDefinition();
		GregorianCalendar date = new GregorianCalendar(2008, 9, 1);
		filter.setCreatedDateFilter(new DateRangeFilter(date.getTime(), date.getTime()));
		IRepositoryQuery query = JiraTestUtil.createQuery(repository, filter);

		JiraTestResultCollector hitCollector = new JiraTestResultCollector();
		IStatus result = connector.performQuery(repository, query, hitCollector, null, null);
		assertEquals(Status.OK_STATUS, result);

		String originalProptery = repository.getProperty("jira.locale");
		try {
			repository.setProperty("jira.datePattern", "MM/dd/yyyy");
			filter = new FilterDefinition();
			date = new GregorianCalendar(2008, 9, 1);
			filter.setCreatedDateFilter(new DateRangeFilter(date.getTime(), date.getTime()));
			query = JiraTestUtil.createQuery(repository, filter);
			result = connector.performQuery(repository, query, hitCollector, null, null);
			assertEquals("Date not localized.", IStatus.ERROR, result.getSeverity());
		} finally {
			repository.setProperty("jira.locale", originalProptery);
		}
	}

}
