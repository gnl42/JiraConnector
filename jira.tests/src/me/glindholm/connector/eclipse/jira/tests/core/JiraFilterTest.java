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

package me.glindholm.connector.eclipse.jira.tests.core;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.GregorianCalendar;
import java.util.Locale;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.mylyn.internal.tasks.core.RepositoryQuery;
import org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin;
import org.eclipse.mylyn.internal.tasks.ui.util.TasksUiInternal;
import org.eclipse.mylyn.tasks.core.ITask.PriorityLevel;
import org.eclipse.mylyn.tasks.core.ITaskMapping;
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.eclipse.mylyn.tasks.ui.TasksUi;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import me.glindholm.connector.eclipse.internal.jira.core.JiraClientFactory;
import me.glindholm.connector.eclipse.internal.jira.core.JiraCorePlugin;
import me.glindholm.connector.eclipse.internal.jira.core.JiraRepositoryConnector;
import me.glindholm.connector.eclipse.internal.jira.core.model.JiraNamedFilter;
import me.glindholm.connector.eclipse.internal.jira.core.model.JiraPriority;
import me.glindholm.connector.eclipse.internal.jira.core.model.JiraProject;
import me.glindholm.connector.eclipse.internal.jira.core.model.filter.ComponentFilter;
import me.glindholm.connector.eclipse.internal.jira.core.model.filter.ContentFilter;
import me.glindholm.connector.eclipse.internal.jira.core.model.filter.DateRangeFilter;
import me.glindholm.connector.eclipse.internal.jira.core.model.filter.FilterDefinition;
import me.glindholm.connector.eclipse.internal.jira.core.model.filter.ProjectFilter;
import me.glindholm.connector.eclipse.jira.tests.util.JiraFixture;
import me.glindholm.connector.eclipse.jira.tests.util.JiraTestResultCollector;
import me.glindholm.connector.eclipse.jira.tests.util.JiraTestUtil;
import me.glindholm.jira.rest.client.api.domain.BasicUser;

/**
 * @author Wesley Coelho (initial integration patch)
 * @author Mik Kersten
 * @author Steffen Pingel
 * @author Thomas Ehrnhoefer (multiple projects selection)
 */
public class JiraFilterTest {

	private TaskRepository repository;

	private JiraRepositoryConnector connector;

	@BeforeEach
	protected void setUp() throws Exception {
		connector = (JiraRepositoryConnector) TasksUi.getRepositoryConnector(JiraCorePlugin.CONNECTOR_KIND);
		repository = JiraFixture.current().singleRepository();
	}

	@AfterEach
	protected void tearDown() throws Exception {
		JiraTestUtil.tearDown();
	}

	@Test
	public void testJiraFilterRefresh() throws Exception {
		final var client = JiraFixture.current().client();
		final var issue = JiraTestUtil.newIssue(client, "testFilterRefresh");
		issue.setAssignee(new BasicUser(null, client.getUserName(), client.getUserName()));
		JiraTestUtil.createIssue(client, issue);

		final var filters = client.getNamedFilters(null);
		assertNotNull(filters, "Expected named filters on server");

		JiraNamedFilter filter = null;
		for (final JiraNamedFilter f : filters) {
			if (f.getName().equals("My Recent")) {
				filter = f;
			}
		}
		assertNotNull(filter, "Filter 'My Recent' not found");

		final var query = (RepositoryQuery) JiraTestUtil.createQuery(repository, filter);
		TasksUiPlugin.getTaskList().addQuery(query);
		assertTrue(query.getChildren().size() == 0);

		TasksUiInternal.synchronizeQuery(connector, query, null, false);
		assertTrue(query.getChildren().size() > 0);
		final var hit = query.getChildren().iterator().next();
		assertTrue(hit.getSummary().length() > 0);
	}

	@Test
	public void testCustomQuery() throws Exception {
		final var summary = "testCustomQuery" + System.currentTimeMillis();
		final var client = JiraClientFactory.getDefault().getJiraClient(repository);
		var issue = JiraTestUtil.newIssue(client, summary);
		issue.setPriority(client.getCache().getPriorityById(JiraPriority.BLOCKER_ID));
		issue = JiraTestUtil.createIssue(client, issue);

		final var filter = new FilterDefinition();
		filter.setContentFilter(new ContentFilter(summary, true, false, false, false));
		final var query = JiraTestUtil.createQuery(repository, filter);

		final var hitCollector = new JiraTestResultCollector();
		connector.performQuery(repository, query, hitCollector, null, null);
		assertEquals(1, hitCollector.results.size());
		final var hit = hitCollector.results.iterator().next();
		final ITaskMapping mapping = connector.getTaskMapping(hit);
		assertEquals(issue.getSummary(), mapping.getSummary());
		assertEquals(PriorityLevel.P1, mapping.getPriorityLevel());
	}

	@Test
	public void testCustomQueryWithoutRepositoryConfiguraton() throws Exception {
		final var summary = "testCustomQueryWithoutRepositoryConfiguraton" + System.currentTimeMillis();
		final var client = JiraClientFactory.getDefault().getJiraClient(repository);
		JiraTestUtil.createIssue(client, summary + " 1");

		final var issue2 = JiraTestUtil.newIssue(client, summary + " 2");
		assertTrue(issue2.getProject().getComponents().length > 0);
		issue2.setComponents(issue2.getProject().getComponents());
		JiraTestUtil.createIssue(client, issue2);

		final var filter = new FilterDefinition();
		filter.setProjectFilter(new ProjectFilter(new JiraProject[] { issue2.getProject() }));
		filter.setContentFilter(new ContentFilter(summary, true, false, false, false));
		filter.setComponentFilter(new ComponentFilter(issue2.getProject().getComponents(), false));

		// run query query to verify component filter is used
		var query = JiraTestUtil.createQuery(repository, filter);
		var hitCollector = new JiraTestResultCollector();
		connector.performQuery(repository, query, hitCollector, null, null);
		assertEquals(1, hitCollector.results.size());
		ITaskMapping taskMapping = connector.getTaskMapping(hitCollector.results.iterator().next());
		assertEquals(issue2.getSummary(), taskMapping.getSummary());

		// re-run to verify that configuration is refreshed prior to running query
		hitCollector = new JiraTestResultCollector();
		JiraClientFactory.getDefault().clearClientsAndConfigurationData();
		connector.performQuery(repository, query, hitCollector, null, null);
		assertEquals(1, hitCollector.results.size());
		taskMapping = connector.getTaskMapping(hitCollector.results.iterator().next());
		assertEquals("Expected issue2, if issue1 is returned the component filter was ignored", issue2.getSummary(),
				taskMapping.getSummary());

		// query for issues without component as well
		filter.setComponentFilter(new ComponentFilter(issue2.getProject().getComponents(), true));
		query = JiraTestUtil.createQuery(repository, filter);
		hitCollector = new JiraTestResultCollector();
		JiraClientFactory.getDefault().clearClientsAndConfigurationData();
		connector.performQuery(repository, query, hitCollector, null, null);
		assertEquals(2,
				hitCollector.results.size(),
				"Expected issue2 and issue1 (query for issues without component as well)");
	}

	@Test
	public void testCustomQueryWrongLocale() throws Exception {
		var filter = new FilterDefinition();
		var date = new GregorianCalendar(2008, 9, 1);
		filter.setCreatedDateFilter(new DateRangeFilter(date.toInstant(), date.toInstant()));
		var query = JiraTestUtil.createQuery(repository, filter);

		final var hitCollector = new JiraTestResultCollector();
		var result = connector.performQuery(repository, query, hitCollector, null, null);
		assertEquals(IStatus.OK, result.getSeverity());

		repository.setProperty("jira.locale", Locale.GERMANY.toString());
		filter = new FilterDefinition();
		date = new GregorianCalendar(2008, 9, 1);
		filter.setCreatedDateFilter(new DateRangeFilter(date.toInstant(), date.toInstant()));
		query = JiraTestUtil.createQuery(repository, filter);
		result = connector.performQuery(repository, query, hitCollector, null, null);
		//		assertEquals("Date not localized.", IStatus.ERROR, result.getSeverity());
		// TaskRepository locale is not used in date parsing in REST
		assertEquals(IStatus.OK, result.getSeverity());
	}

	@Test
	public void testCustomQueryWrongDatePattern() throws Exception {
		final var filter = new FilterDefinition();
		final var date = new GregorianCalendar(2008, 9, 1);
		filter.setCreatedDateFilter(new DateRangeFilter(date.toInstant(), date.toInstant()));
		final var query = JiraTestUtil.createQuery(repository, filter);

		final var hitCollector = new JiraTestResultCollector();
		final var result = connector.performQuery(repository, query, hitCollector, null, null);
		assertEquals(Status.OK_STATUS, result);

		// repository date pattern is not used in REST (all dates are formatted
		// according to REST pattern)
		//		repository.setProperty("jira.datePattern", "MM/dd/yyyy");
		//		filter = new FilterDefinition();
		//		date = new GregorianCalendar(2008, 9, 1);
		//		filter.setCreatedDateFilter(new DateRangeFilter(date.getTime(), date.getTime()));
		//		query = JiraTestUtil.createQuery(repository, filter);
		//		result = connector.performQuery(repository, query, hitCollector, null, null);
		//		assertEquals("Date not localized.", IStatus.ERROR, result.getSeverity());
	}

}
