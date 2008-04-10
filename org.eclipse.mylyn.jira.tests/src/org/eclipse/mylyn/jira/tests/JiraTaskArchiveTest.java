/*******************************************************************************
 * Copyright (c) 2004, 2007 Mylyn project committers and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.mylyn.jira.tests;

import junit.framework.TestCase;

import org.eclipse.mylyn.internal.jira.core.JiraCorePlugin;
import org.eclipse.mylyn.internal.jira.core.JiraTask;
import org.eclipse.mylyn.tasks.core.AbstractRepositoryConnector;
import org.eclipse.mylyn.tasks.core.TaskList;
import org.eclipse.mylyn.tasks.core.TaskRepository;
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
//		jiraRepository = new TaskRepository(MylynJiraPlugin.REPOSITORY_KIND, SERVER_URL);
//		jiraRepository.setAuthenticationCredentials(USER, PASSWORD);
//		MylynTaskListPlugin.getRepositoryManager().addRepository(jiraRepository);
//		jiraFacade = JiraServerFacade.getDefault();
		TasksUiPlugin.getTaskListManager().resetTaskList();
		taskList = TasksUiPlugin.getTaskListManager().getTaskList();
	}

	@Override
	protected void tearDown() throws Exception {
		AbstractRepositoryConnector client = TasksUiPlugin.getRepositoryManager().getRepositoryConnector(
				JiraCorePlugin.REPOSITORY_KIND);
		assertNotNull(client);
//		taskList.clearArchive();
//		MylynTaskListPlugin.getTaskListManager().getTaskList().clear();
		TasksUiPlugin.getTaskListManager().resetTaskList();
//		MylynTaskListPlugin.getRepositoryManager().removeRepository(jiraRepository);
//		jiraFacade.logOutFromAll();
		super.tearDown();
	}

	public void testJiraTaskRegistry() {
		AbstractRepositoryConnector client = TasksUiPlugin.getRepositoryManager().getRepositoryConnector(
				JiraCorePlugin.REPOSITORY_KIND);
		assertNotNull(client);

		TaskRepository repository = new TaskRepository(JiraCorePlugin.REPOSITORY_KIND, "repo");
		TasksUiPlugin.getRepositoryManager().addRepository(repository,
				TasksUiPlugin.getDefault().getRepositoriesFilePath());

		JiraTask task1 = new JiraTask("repo", HANDLE1, LABEL);
		task1.setLastReadTimeStamp("now");
		JiraTask task2 = new JiraTask("repo", HANDLE1, LABEL);
		task2.setLastReadTimeStamp("now");
		taskList.addTask(task1);
		taskList.addTask(task2);

		assertEquals(1, taskList.getOrphanContainer("repo").getChildren().size());
		assertEquals(taskList.getTask("repo-" + HANDLE1), task1);

		TasksUiPlugin.getRepositoryManager().removeRepository(repository,
				TasksUiPlugin.getDefault().getRepositoriesFilePath());
		TasksUiPlugin.getTaskListManager().resetTaskList();
	}
}