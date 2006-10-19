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
import org.eclipse.mylar.internal.jira.JiraQueryHit;
import org.eclipse.mylar.internal.jira.JiraRepositoryConnector;
import org.eclipse.mylar.internal.jira.JiraRepositoryQuery;
import org.eclipse.mylar.internal.jira.JiraServerFacade;
import org.eclipse.mylar.internal.jira.MylarJiraPlugin;
import org.eclipse.mylar.tasks.core.AbstractRepositoryConnector;
import org.eclipse.mylar.tasks.core.TaskRepository;
import org.eclipse.mylar.tasks.ui.TasksUiPlugin;
import org.tigris.jira.core.model.NamedFilter;

/**
 * @author Wesley Coelho (initial integration patch)
 * @author Mik Kersten
 */
public class JiraFilterTest extends TestCase {

	private final static String USER = "mylartest";

	private final static String PASSWORD = "mylartest";

	private final static String SERVER_URL = "http://developer.atlassian.com/jira";

	private JiraServerFacade jiraFacade = null;

	private TaskRepository repository = null;
	
	private JiraRepositoryConnector connector = new JiraRepositoryConnector();

	protected void setUp() throws Exception {
		super.setUp();
		repository = new TaskRepository(MylarJiraPlugin.REPOSITORY_KIND, SERVER_URL);
		repository.setAuthenticationCredentials(USER, PASSWORD);
		TasksUiPlugin.getRepositoryManager().addRepository(repository, TasksUiPlugin.getDefault().getRepositoriesFilePath());
		jiraFacade = JiraServerFacade.getDefault();
	}

	protected void tearDown() throws Exception {
		AbstractRepositoryConnector client = TasksUiPlugin.getRepositoryManager().getRepositoryConnector(
				MylarJiraPlugin.REPOSITORY_KIND);
		assertNotNull(client);
//		MylarTaskListPlugin.getTaskListManager().getTaskList().clearArchive();
		// client.clearArchive();
//		MylarTaskListPlugin.getTaskListManager().getTaskList().clear();
		TasksUiPlugin.getTaskListManager().resetTaskList();
		TasksUiPlugin.getRepositoryManager().removeRepository(repository, TasksUiPlugin.getDefault().getRepositoriesFilePath());
//		jiraFacade.logOutFromAll();
		super.tearDown();
	}
	
	public void testJiraFilterRefresh() {
		NamedFilter[] filters = jiraFacade.getJiraServer(repository).getNamedFilters();
		assertTrue(filters.length > 0);
		JiraRepositoryQuery jFilter = new JiraRepositoryQuery(repository.getUrl(), filters[0], TasksUiPlugin.getTaskListManager().getTaskList());
		TasksUiPlugin.getTaskListManager().getTaskList().addQuery(jFilter);
		assertTrue(jFilter.getHits().size() == 0);
		// jFilter.refreshHits();
		// boolean done = false;

		Job job = TasksUiPlugin.getSynchronizationManager().synchronize(connector, jFilter, null);
		while (job.getResult() == null) {
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		assertTrue(jFilter.getHits().size() > 0);
		JiraQueryHit jHit = (JiraQueryHit) jFilter.getHits().iterator().next();
		assertTrue(jHit.getDescription().length() > 0);
	}

	// TODO: reneable
//	public void testJiraTaskRegistryIntegration() {
//	TaskList taskList = MylarTaskListPlugin.getTaskListManager().getTaskList();
//		AbstractRepositoryConnector client = MylarTaskListPlugin.getRepositoryManager().getRepositoryConnector(
//				MylarJiraPlugin.REPOSITORY_KIND);
//		assertNotNull(client);
//		assertEquals(""+taskList.getArchiveContainer().getChildren(), 0, taskList.getArchiveContainer().getChildren().size());
//		JiraServer server = jiraFacade.getJiraServer(repository);
//		NamedFilter[] namedFilters = server.getNamedFilters();
//		JiraRepositoryQuery filter = new JiraRepositoryQuery(repository.getUrl(), namedFilters[0], MylarTaskListPlugin.getTaskListManager().getTaskList());
//		
//		connector.synchronize(filter, null);
//		// filter.refreshHits();
//		// MylarTaskListPlugin.getTaskListManager().addQuery(filter);		
//		Job job = connector.synchronize(filter, null);
//		while (job.getResult() == null) {
//			// while (filter.isRefreshing()) {
//			try {
//				Thread.sleep(500);
//			} catch (InterruptedException e) {
//				e.printStackTrace();
//			}
//		}
//
//		assertTrue(filter.getHits().size() > 0);
//		JiraQueryHit jHit = (JiraQueryHit) filter.getHits().iterator().next();
//
//		assertNotNull(taskList.getTask(jHit.getHandleIdentifier()));
//	}
}
