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

import junit.framework.TestCase;

import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.mylar.internal.jira.JiraRepositoryQuery;
import org.eclipse.mylar.internal.jira.JiraFilterHit;
import org.eclipse.mylar.internal.jira.JiraRepositoryConnector;
import org.eclipse.mylar.internal.jira.JiraServerFacade;
import org.eclipse.mylar.internal.jira.JiraTask;
import org.eclipse.mylar.internal.jira.MylarJiraPlugin;
import org.eclipse.mylar.provisional.tasklist.AbstractRepositoryConnector;
import org.eclipse.mylar.provisional.tasklist.MylarTaskListPlugin;
import org.eclipse.mylar.provisional.tasklist.TaskList;
import org.eclipse.mylar.provisional.tasklist.TaskRepository;
import org.tigris.jira.core.model.NamedFilter;

/**
 * @author Wesley Coelho (initial integration patch)
 */
public class JiraTaskArchiveTest extends TestCase {

	private static final String LABEL = "Label";

	private static final String HANDLE1 = "Handle1";

	private final static String USER = "mylartest";

	private final static String PASSWORD = "mylartest";

	private final static String SERVER_URL = "http://developer.atlassian.com/jira";

	private JiraServerFacade jiraFacade = null;

	private TaskRepository jiraRepository = null;

	private TaskList taskList;

	private JiraRepositoryConnector connector = new JiraRepositoryConnector();

	protected void setUp() throws Exception {
		super.setUp();
		jiraRepository = new TaskRepository(MylarJiraPlugin.JIRA_REPOSITORY_KIND, SERVER_URL);
		jiraRepository.setAuthenticationCredentials(USER, PASSWORD);
		MylarTaskListPlugin.getRepositoryManager().addRepository(jiraRepository);
		jiraFacade = JiraServerFacade.getDefault();
		taskList = MylarTaskListPlugin.getTaskListManager().getTaskList();
	}

	protected void tearDown() throws Exception {
		AbstractRepositoryConnector client = MylarTaskListPlugin.getRepositoryManager().getRepositoryConnector(
				MylarJiraPlugin.JIRA_REPOSITORY_KIND);
		assertNotNull(client);
//		taskList.clearArchive();
//		MylarTaskListPlugin.getTaskListManager().getTaskList().clear();
		MylarTaskListPlugin.getTaskListManager().resetTaskList();
		MylarTaskListPlugin.getRepositoryManager().removeRepository(jiraRepository);
		jiraFacade.logOutFromAll();
		super.tearDown();
	}

	public void testJiraTaskRegistry() {
		AbstractRepositoryConnector client = MylarTaskListPlugin.getRepositoryManager().getRepositoryConnector(
				MylarJiraPlugin.JIRA_REPOSITORY_KIND);
		assertNotNull(client);

		JiraTask task1 = new JiraTask(HANDLE1, LABEL, true);
		JiraTask task2 = new JiraTask(HANDLE1, LABEL, true);

		taskList.addTask(task1);
		taskList.addTask(task2);

		assertTrue(taskList.getArchiveContainer().getChildren().size() == 1);
		assertEquals(taskList.getTask(HANDLE1), task1);
	}

	public void testJiraTaskRegistryIntegration() {
		AbstractRepositoryConnector client = MylarTaskListPlugin.getRepositoryManager().getRepositoryConnector(
				MylarJiraPlugin.JIRA_REPOSITORY_KIND);
		assertNotNull(client);
		
		// TODO: put back?
//		taskList.clearArchive();

		assertEquals(""+taskList.getArchiveContainer().getChildren(), 0, taskList.getArchiveContainer().getChildren().size());

		NamedFilter[] namedFilters = jiraFacade.getJiraServer(jiraRepository).getNamedFilters();
		JiraRepositoryQuery filter = new JiraRepositoryQuery(jiraRepository.getUrl(), namedFilters[0], MylarTaskListPlugin.getTaskListManager().getTaskList());

		connector.synchronize(filter, null);
		// filter.refreshHits();
		// MylarTaskListPlugin.getTaskListManager().addQuery(filter);

		Job job = connector.synchronize(filter, null);
		while (job.getResult() == null) {
			// while (filter.isRefreshing()) {
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

		assertTrue(filter.getHits().size() > 0);
		JiraFilterHit jHit = (JiraFilterHit) filter.getHits().iterator().next();

		assertNotNull(taskList.getTask(jHit.getHandleIdentifier()));
	}
}
