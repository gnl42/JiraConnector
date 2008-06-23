/*******************************************************************************
 * Copyright (c) 2004, 2007 Mylyn project committers and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.mylyn.jira.tests;

import java.util.Date;
import java.util.Set;

import junit.framework.TestCase;

import org.eclipse.mylyn.internal.jira.core.JiraCorePlugin;
import org.eclipse.mylyn.internal.jira.core.model.NamedFilter;
import org.eclipse.mylyn.internal.jira.core.model.filter.FilterDefinition;
import org.eclipse.mylyn.internal.tasks.core.AbstractTask;
import org.eclipse.mylyn.internal.tasks.core.RepositoryQuery;
import org.eclipse.mylyn.internal.tasks.core.RepositoryTaskHandleUtil;
import org.eclipse.mylyn.internal.tasks.core.TaskList;
import org.eclipse.mylyn.internal.tasks.core.TaskTask;
import org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin;
import org.eclipse.mylyn.jira.tests.util.JiraTestConstants;
import org.eclipse.mylyn.jira.tests.util.JiraTestUtil;
import org.eclipse.mylyn.tasks.core.AbstractRepositoryConnector;
import org.eclipse.mylyn.tasks.core.ITask;
import org.eclipse.mylyn.tasks.core.TaskRepository;

/**
 * @author Wesley Coelho (initial integration patch)
 * @author Steffen Pingel
 */
public class JiraTaskExternalizationTest extends TestCase {

	private static final String ISSUE_DESCRIPTION = "Issue Description";

	private static final String TEST_LABEL = "TestLabel";

	private static final String TEST_TASK = "TestTask";

	private TaskRepository repository;

	private TaskList taskList;

	@Override
	protected void setUp() throws Exception {
		JiraTestUtil.setUp();
		taskList = TasksUiPlugin.getTaskList();
		repository = JiraTestUtil.init(JiraTestConstants.JIRA_39_URL);
		resetTaskList();
	}

	@Override
	protected void tearDown() throws Exception {
		JiraTestUtil.tearDown();
	}

	@SuppressWarnings("deprecation")
	private void resetTaskList() {
		org.eclipse.mylyn.internal.tasks.ui.TaskListManager manager = TasksUiPlugin.getTaskListManager();
		manager.resetTaskList();
	}

	@SuppressWarnings("deprecation")
	private void saveAndReadTaskList() {
		org.eclipse.mylyn.internal.tasks.ui.TaskListManager manager = TasksUiPlugin.getTaskListManager();
		manager.saveTaskList();
		manager.resetTaskList();
		manager.readExistingOrCreateNewList();
	}

	public void testNamedFilterNotRenamed() {
		NamedFilter filter = new NamedFilter();
		filter.setName("f-name");
		RepositoryQuery query = (RepositoryQuery) JiraTestUtil.createQuery(repository, filter);
		query.setSummary("f-name");
		query.setHandleIdentifier("q-name");
		taskList.addQuery(query);

		assertEquals(1, taskList.getQueries().size());
		assertEquals("f-name", taskList.getQueries().iterator().next().getSummary());
		assertEquals("q-name", taskList.getQueries().iterator().next().getHandleIdentifier());

		saveAndReadTaskList();
		assertEquals(1, taskList.getQueries().size());
		assertEquals("f-name", taskList.getQueries().iterator().next().getSummary());
		assertEquals("q-name", taskList.getQueries().iterator().next().getHandleIdentifier());
	}

	public void testCustomQueryRename() {
		FilterDefinition filter = new FilterDefinition();
		RepositoryQuery query = (RepositoryQuery) JiraTestUtil.createQuery(repository, filter);
		query.setSummary("q-name");
		query.setHandleIdentifier("handle");
		taskList.addQuery(query);

		assertEquals(1, taskList.getQueries().size());
		assertEquals("q-name", taskList.getQueries().iterator().next().getSummary());
		assertEquals("handle", taskList.getQueries().iterator().next().getHandleIdentifier());

		saveAndReadTaskList();
		assertEquals(1, taskList.getQueries().size());
		assertEquals("q-name", taskList.getQueries().iterator().next().getSummary());
		assertEquals("handle", taskList.getQueries().iterator().next().getHandleIdentifier());
	}

	public void testCompletionSave() {
		ITask jiraTask = new TaskTask(JiraCorePlugin.CONNECTOR_KIND, repository.getRepositoryUrl(), TEST_TASK);
		jiraTask.setSummary(TEST_LABEL);
		jiraTask.setCompletionDate(new Date());
		taskList.addTask(jiraTask);

		saveAndReadTaskList();
		ITask task = taskList.getTask(repository.getRepositoryUrl(), TEST_TASK);
		assertTrue(task.isCompleted());
	}

	public void testJiraTaskSave() {
		ITask jiraTask = new TaskTask(JiraCorePlugin.CONNECTOR_KIND, repository.getRepositoryUrl(), TEST_TASK);
		jiraTask.setSummary(TEST_LABEL);
		String testUrl = "http://foo";
		jiraTask.setUrl(testUrl);
		taskList.addTask(jiraTask);

		saveAndReadTaskList();
		boolean taskFound = false;
		for (AbstractTask task : taskList.getAllTasks()) {
			if (task.getRepositoryUrl().equals(repository.getRepositoryUrl())) {
				taskFound = true;
				// check that the URL of the task is it's handle
				assertEquals(testUrl, task.getUrl());
				break;
			}
		}
		assertTrue("The saved Jira task was not found", taskFound);
	}

	public void testJiraFilterHitSave() {
		NamedFilter namedFilter = new NamedFilter();
		namedFilter.setName("Test Filter");
		namedFilter.setId("123456");
		namedFilter.setDescription("Test Filter Description");
		RepositoryQuery query = (RepositoryQuery) JiraTestUtil.createQuery(repository, namedFilter);
		String filterUrl = query.getUrl();

		ITask jiraTask = new TaskTask(JiraCorePlugin.CONNECTOR_KIND, repository.getRepositoryUrl(), "" + 123);
		jiraTask.setSummary(ISSUE_DESCRIPTION);
		jiraTask.setTaskKey("KEY-123");
		taskList.addTask(jiraTask);
		TasksUiPlugin.getTaskList().addTask(jiraTask);
		assertNotNull(taskList.getTask(jiraTask.getHandleIdentifier()));

		TasksUiPlugin.getTaskList().addQuery(query);
		TasksUiPlugin.getTaskList().addTask(jiraTask, query);
		assertNotNull(taskList.getTask(jiraTask.getHandleIdentifier()));

		saveAndReadTaskList();

		Set<RepositoryQuery> queries = taskList.getQueries();
		RepositoryQuery savedFilter = null;
		for (RepositoryQuery taskListQuery : queries) {
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

		ITask savedHit = savedFilter.getChildren().iterator().next();

		assertEquals(jiraTask.getTaskKey(), savedHit.getTaskKey());
		assertEquals(jiraTask.getSummary(), savedHit.getSummary());

		String handle = RepositoryTaskHandleUtil.getHandle(savedHit.getRepositoryUrl(), "123");
		assertEquals(handle, savedHit.getHandleIdentifier());

		AbstractRepositoryConnector client = TasksUiPlugin.getRepositoryManager().getRepositoryConnector(
				JiraCorePlugin.CONNECTOR_KIND);
		assertNotNull(client);
		assertNotNull(taskList.getTask(savedHit.getHandleIdentifier()));
	}

}
