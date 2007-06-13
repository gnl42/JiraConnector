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

package org.eclipse.mylyn.jira.tests;

import junit.framework.TestCase;

import org.eclipse.mylyn.internal.jira.ui.JiraTask;
import org.eclipse.mylyn.internal.jira.ui.JiraUiPlugin;
import org.eclipse.mylyn.tasks.core.AbstractRepositoryConnector;
import org.eclipse.mylyn.tasks.core.TaskList;
import org.eclipse.mylyn.tasks.ui.TasksUiPlugin;

/**
 * @author Wesley Coelho (initial integration patch)
 * @author Mik Kersten
 */
public class JiraTaskArchiveTest extends TestCase {

	private static final String LABEL = "Label";

	private static final String HANDLE1 = "Handle1";

	private TaskList taskList;

//	private JiraRepositoryConnector connector = new JiraRepositoryConnector();

	@Override
	protected void setUp() throws Exception {
		super.setUp();
//		jiraRepository = new TaskRepository(MylarJiraPlugin.REPOSITORY_KIND, SERVER_URL);
//		jiraRepository.setAuthenticationCredentials(USER, PASSWORD);
//		MylarTaskListPlugin.getRepositoryManager().addRepository(jiraRepository);
//		jiraFacade = JiraServerFacade.getDefault();
		TasksUiPlugin.getTaskListManager().resetTaskList();
		taskList = TasksUiPlugin.getTaskListManager().getTaskList();
	}

	@Override
	protected void tearDown() throws Exception {
		AbstractRepositoryConnector client = TasksUiPlugin.getRepositoryManager().getRepositoryConnector(
				JiraUiPlugin.REPOSITORY_KIND);
		assertNotNull(client);
//		taskList.clearArchive();
//		MylarTaskListPlugin.getTaskListManager().getTaskList().clear();
		TasksUiPlugin.getTaskListManager().resetTaskList();
//		MylarTaskListPlugin.getRepositoryManager().removeRepository(jiraRepository);
//		jiraFacade.logOutFromAll();
		super.tearDown();
	}
	
	public void testJiraTaskRegistry() {
		AbstractRepositoryConnector client = TasksUiPlugin.getRepositoryManager().getRepositoryConnector(
				JiraUiPlugin.REPOSITORY_KIND);
		assertNotNull(client);
		assertEquals(0, taskList.getArchiveContainer().getChildren().size());
		JiraTask task1 = new JiraTask("repo", HANDLE1, LABEL);
		JiraTask task2 = new JiraTask("repo", HANDLE1, LABEL);

		taskList.addTask(task1);
		taskList.addTask(task2);

		assertEquals(1, taskList.getArchiveContainer().getChildren().size());
		assertEquals(taskList.getTask("repo-" + HANDLE1), task1);
	}
}