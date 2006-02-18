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

import java.net.URL;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import junit.framework.TestCase;

import org.eclipse.mylar.internal.jira.JiraFilter;
import org.eclipse.mylar.internal.jira.JiraFilterHit;
import org.eclipse.mylar.internal.jira.JiraTask;
import org.eclipse.mylar.internal.jira.MylarJiraPlugin;
import org.eclipse.mylar.internal.tasklist.AbstractRepositoryClient;
import org.eclipse.mylar.internal.tasklist.AbstractRepositoryQuery;
import org.eclipse.mylar.internal.tasklist.ITask;
import org.eclipse.mylar.internal.tasklist.MylarTaskListPlugin;
import org.eclipse.mylar.internal.tasklist.TaskList;
import org.eclipse.mylar.internal.tasklist.TaskListManager;
import org.eclipse.mylar.internal.tasklist.TaskRepository;
import org.tigris.jira.core.model.Issue;
import org.tigris.jira.core.model.NamedFilter;

/**
 * @author Wesley Coelho (initial integration patch)
 */
public class JiraTaskExternalizationTest extends TestCase {

	private static final String ISSUE_SUMMARY = "Issue Summary";

	private static final String ISSUE_DESCRIPTION = "Issue Description";

	private static final String ISSUE_KEY = "Issue Key";

	private final static String USER = "mylartest";

	private final static String PASSWORD = "mylartest";

	private final static String SERVER_URL = "http://developer.atlassian.com/jira";

	private static final String TEST_LABEL = "TestLabel";

	private static final String TEST_TASK = "TestTask";

	private TaskListManager manager = MylarTaskListPlugin.getTaskListManager();

	private TaskRepository jiraRepo = null;
	
	private TaskList taskList = MylarTaskListPlugin.getTaskListManager().getTaskList();

	protected void setUp() throws Exception {
		super.setUp();
		URL repoURL = new URL(SERVER_URL);
		jiraRepo = new TaskRepository(MylarJiraPlugin.JIRA_REPOSITORY_KIND,
				repoURL);
		jiraRepo.setAuthenticationCredentials(USER, PASSWORD);
		MylarTaskListPlugin.getRepositoryManager().addRepository(jiraRepo);

	}

	protected void tearDown() throws Exception {
		AbstractRepositoryClient client = MylarTaskListPlugin
				.getRepositoryManager().getRepositoryClient(
						MylarJiraPlugin.JIRA_REPOSITORY_KIND);
		assertNotNull(client);
		taskList.clearArchive();
		MylarTaskListPlugin.getTaskListManager().getTaskList().clear();
		MylarTaskListPlugin.getRepositoryManager().removeRepository(jiraRepo);
		super.tearDown();
	}

	public void testJiraTaskSave() {

		JiraTask jiraTask = new JiraTask(TEST_TASK, TEST_LABEL, true);
		manager.moveToRoot(jiraTask);

		manager.saveTaskList();

		TaskList newTaskList = new TaskList();

		manager.getTaskListWriter().readTaskList(newTaskList,
				manager.getTaskListFile());

		Set taskSet = newTaskList.getAllTasks();

		boolean taskFound = false;
		for (Iterator iter = taskSet.iterator(); iter.hasNext();) {
			ITask currTask = (ITask) iter.next();
			if (currTask instanceof JiraTask
					&& currTask.getHandleIdentifier().equals(TEST_TASK)) {
				taskFound = true;
				// Check that the URL of the Jira task is it's handle
				assertTrue(currTask.getUrl().equals(TEST_TASK));
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
		JiraFilter jiraFilter = new JiraFilter(namedFilter, false);
		String filterUrl = jiraFilter.getQueryUrl();

		Issue jiraIssue = new Issue();
		jiraIssue.setKey(ISSUE_KEY);
		jiraIssue.setDescription(ISSUE_DESCRIPTION);
		jiraIssue.setSummary(ISSUE_SUMMARY);
		JiraFilterHit jiraHit = new JiraFilterHit(jiraIssue);
		jiraFilter.addHit(jiraHit);
		MylarTaskListPlugin.getTaskListManager().addQuery(jiraFilter);

		manager.saveTaskList();
		TaskList newTaskList = new TaskList();
		manager.getTaskListWriter().readTaskList(newTaskList,
				manager.getTaskListFile());

		List<AbstractRepositoryQuery> queries = newTaskList.getQueries();

		JiraFilter savedFilter = null;
		for (AbstractRepositoryQuery query : queries) {
			if (query.getHandleIdentifier().equals(
					jiraFilter.getHandleIdentifier())) {
				savedFilter = (JiraFilter) query;
				break;
			}
		}

		assertNotNull(savedFilter);
		assertEquals(savedFilter.getQueryUrl(), filterUrl);

		assertTrue(savedFilter.getHits().size() > 0);

		JiraFilterHit savedHit = (JiraFilterHit) savedFilter.getHits().iterator().next();

		JiraTask jTask = (JiraTask) savedHit.getCorrespondingTask();

		assertEquals(ISSUE_SUMMARY, jTask.getDescription());
		assertEquals(jTask.getHandleIdentifier(), jiraHit.getRepositoryUrl());

		AbstractRepositoryClient client = MylarTaskListPlugin
				.getRepositoryManager().getRepositoryClient(
						MylarJiraPlugin.JIRA_REPOSITORY_KIND);
		assertNotNull(client);
		assertNotNull(taskList.getTaskFromArchive(savedHit.getHandleIdentifier()));

	}
}
