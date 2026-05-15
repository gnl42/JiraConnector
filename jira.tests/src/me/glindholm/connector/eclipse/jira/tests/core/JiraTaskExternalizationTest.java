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

package me.glindholm.connector.eclipse.jira.tests.core;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Date;

import org.eclipse.mylyn.internal.tasks.core.AbstractTask;
import org.eclipse.mylyn.internal.tasks.core.RepositoryQuery;
import org.eclipse.mylyn.internal.tasks.core.RepositoryTaskHandleUtil;
import org.eclipse.mylyn.internal.tasks.core.TaskList;
import org.eclipse.mylyn.internal.tasks.core.TaskTask;
import org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin;
import org.eclipse.mylyn.tasks.core.ITask;
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.eclipse.mylyn.tests.util.TestFixture;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import me.glindholm.connector.eclipse.internal.jira.core.JiraCorePlugin;
import me.glindholm.connector.eclipse.internal.jira.core.model.JiraNamedFilter;
import me.glindholm.connector.eclipse.internal.jira.core.model.filter.FilterDefinition;
import me.glindholm.connector.eclipse.jira.tests.util.JiraFixture;
import me.glindholm.connector.eclipse.jira.tests.util.JiraTestUtil;

/**
 * @author Wesley Coelho (initial integration patch)
 * @author Steffen Pingel
 */
public class JiraTaskExternalizationTest  {

	private static final String ISSUE_DESCRIPTION = "Issue Description";

	private static final String TEST_LABEL = "TestLabel";

	private static final String TEST_TASK = "TestTask";

	private TaskRepository repository;

	private TaskList taskList;

	@BeforeEach
	protected void setUp() throws Exception {
		JiraTestUtil.setUp();
		taskList = TasksUiPlugin.getTaskList();
		repository = JiraTestUtil.init(JiraFixture.current().getRepositoryUrl());
		TestFixture.resetTaskList();
	}

	@AfterEach
	protected void tearDown() throws Exception {
		JiraTestUtil.tearDown();
	}

	@Test
	public void testNamedFilterNotRenamed() throws Exception {
		final var filter = new JiraNamedFilter();
		filter.setName("f-name");
		final var query = (RepositoryQuery) JiraTestUtil.createQuery(repository, filter);
		query.setSummary("f-name");
		query.setHandleIdentifier("q-name");
		taskList.addQuery(query);

		assertEquals(1, taskList.getQueries().size());
		assertEquals("f-name", taskList.getQueries().iterator().next().getSummary());
		assertEquals("q-name", taskList.getQueries().iterator().next().getHandleIdentifier());

		TestFixture.saveAndReadTasklist();
		assertEquals(1, taskList.getQueries().size());
		assertEquals("f-name", taskList.getQueries().iterator().next().getSummary());
		assertEquals("q-name", taskList.getQueries().iterator().next().getHandleIdentifier());
	}

	@Test
	public void testCustomQueryRename() throws Exception {
		final var filter = new FilterDefinition();
		final var query = (RepositoryQuery) JiraTestUtil.createQuery(repository, filter);
		query.setSummary("q-name");
		query.setHandleIdentifier("handle");
		taskList.addQuery(query);

		assertEquals(1, taskList.getQueries().size());
		assertEquals("q-name", taskList.getQueries().iterator().next().getSummary());
		assertEquals("handle", taskList.getQueries().iterator().next().getHandleIdentifier());

		TestFixture.saveAndReadTasklist();
		assertEquals(1, taskList.getQueries().size());
		assertEquals("q-name", taskList.getQueries().iterator().next().getSummary());
		assertEquals("handle", taskList.getQueries().iterator().next().getHandleIdentifier());
	}

	@Test
	public void testCompletionSave() throws Exception {
		final ITask jiraTask = new TaskTask(JiraCorePlugin.CONNECTOR_KIND, repository.getRepositoryUrl(), TEST_TASK);
		jiraTask.setSummary(TEST_LABEL);
		jiraTask.setCompletionDate(new Date());
		taskList.addTask(jiraTask);

		TestFixture.saveAndReadTasklist();
		final var task = taskList.getTask(repository.getRepositoryUrl(), TEST_TASK);
		assertTrue(task.isCompleted());
	}

	@Test
	public void testJiraTaskSave() throws Exception {
		final ITask jiraTask = new TaskTask(JiraCorePlugin.CONNECTOR_KIND, repository.getRepositoryUrl(), TEST_TASK);
		jiraTask.setSummary(TEST_LABEL);
		final var testUrl = "http://foo";
		jiraTask.setUrl(testUrl);
		taskList.addTask(jiraTask);

		TestFixture.saveAndReadTasklist();
		var taskFound = false;
		for (final AbstractTask task : taskList.getAllTasks()) {
			if (task.getRepositoryUrl().equals(repository.getRepositoryUrl())) {
				taskFound = true;
				// check that the URL of the task is it's handle
				assertEquals(testUrl, task.getUrl());
				break;
			}
		}
		assertTrue(taskFound, "The saved Jira task was not found");
	}

	@Test
	public void testJiraFilterHitSave() throws Exception {
		final var namedFilter = new JiraNamedFilter();
		namedFilter.setName("Test Filter");
		namedFilter.setId("123456");
		namedFilter.setDescription("Test Filter Description");
		namedFilter.setViewUrl("filterUrl");
		final var query = (RepositoryQuery) JiraTestUtil.createQuery(repository, namedFilter);
		final var filterUrl = query.getUrl();

		final ITask jiraTask = new TaskTask(JiraCorePlugin.CONNECTOR_KIND, repository.getRepositoryUrl(), "" + 123);
		jiraTask.setSummary(ISSUE_DESCRIPTION);
		jiraTask.setTaskKey("KEY-123");
		taskList.addTask(jiraTask);
		TasksUiPlugin.getTaskList().addTask(jiraTask);
		assertNotNull(taskList.getTask(jiraTask.getHandleIdentifier()));

		TasksUiPlugin.getTaskList().addQuery(query);
		TasksUiPlugin.getTaskList().addTask(jiraTask, query);
		assertNotNull(taskList.getTask(jiraTask.getHandleIdentifier()));

		TestFixture.saveAndReadTasklist();

		final var queries = taskList.getQueries();
		RepositoryQuery savedFilter = null;
		for (final RepositoryQuery taskListQuery : queries) {
			if (taskListQuery.getHandleIdentifier().equals(query.getHandleIdentifier())) {
				savedFilter = taskListQuery;
				break;
			}
		}

		assertNotNull(savedFilter);
		if (savedFilter == null) {
			return;
		}
		assertEquals(savedFilter.getUrl(), filterUrl);

		assertTrue(savedFilter.getChildren().size() > 0);

		final var savedHit = savedFilter.getChildren().iterator().next();

		assertEquals(jiraTask.getTaskKey(), savedHit.getTaskKey());
		assertEquals(jiraTask.getSummary(), savedHit.getSummary());

		final var handle = RepositoryTaskHandleUtil.getHandle(savedHit.getRepositoryUrl(), "123");
		assertEquals(handle, savedHit.getHandleIdentifier());

		final var client = TasksUiPlugin.getRepositoryManager().getRepositoryConnector(
				JiraCorePlugin.CONNECTOR_KIND);
		assertNotNull(client);
		assertNotNull(taskList.getTask(savedHit.getHandleIdentifier()));
	}

}
