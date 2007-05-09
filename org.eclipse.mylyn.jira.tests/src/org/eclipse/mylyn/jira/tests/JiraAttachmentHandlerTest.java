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

import java.io.File;

import junit.framework.TestCase;

import org.eclipse.mylar.context.tests.support.MylarTestUtils;
import org.eclipse.mylar.context.tests.support.MylarTestUtils.Credentials;
import org.eclipse.mylar.context.tests.support.MylarTestUtils.PrivilegeLevel;
import org.eclipse.mylar.internal.jira.core.model.Issue;
import org.eclipse.mylar.internal.jira.core.service.JiraClient;
import org.eclipse.mylar.internal.jira.ui.JiraRepositoryConnector;
import org.eclipse.mylar.internal.jira.ui.JiraClientFacade;
import org.eclipse.mylar.internal.jira.ui.JiraUiPlugin;
import org.eclipse.mylar.tasks.core.AbstractRepositoryConnector;
import org.eclipse.mylar.tasks.core.AbstractRepositoryTask;
import org.eclipse.mylar.tasks.core.IAttachmentHandler;
import org.eclipse.mylar.tasks.core.RepositoryTaskData;
import org.eclipse.mylar.tasks.core.TaskRepository;
import org.eclipse.mylar.tasks.ui.TasksUiPlugin;

/**
 * @author Steffen Pingel
 */
public class JiraAttachmentHandlerTest extends TestCase {

	private TaskRepository repository;

	private JiraRepositoryConnector connector;

	private IAttachmentHandler attachmentHandler;

	private JiraClient server;

	@Override
	protected void setUp() throws Exception {
		TasksUiPlugin.getSynchronizationManager().setForceSyncExec(true);

		TasksUiPlugin.getRepositoryManager().clearRepositories(TasksUiPlugin.getDefault().getRepositoriesFilePath());
		JiraClientFacade.getDefault().clearClients();
		
		AbstractRepositoryConnector abstractConnector = TasksUiPlugin.getRepositoryManager().getRepositoryConnector(
				JiraUiPlugin.REPOSITORY_KIND);
		connector = (JiraRepositoryConnector) abstractConnector;
		attachmentHandler = connector.getAttachmentHandler();
		
		repository = null;
	}

	@Override
	protected void tearDown() throws Exception {
	}

	protected void init(String url, PrivilegeLevel level) throws Exception {
		Credentials credentials = MylarTestUtils.readCredentials(level);

		if (repository != null) {
			TasksUiPlugin.getRepositoryManager().removeRepository(repository,
					TasksUiPlugin.getDefault().getRepositoriesFilePath());			
		}
		
		repository = new TaskRepository(JiraUiPlugin.REPOSITORY_KIND, JiraTestConstants.JIRA_381_URL);
		repository.setAuthenticationCredentials(credentials.username, credentials.password);
		repository.setCharacterEncoding(JiraClient.CHARSET);

		TasksUiPlugin.getRepositoryManager().addRepository(repository,
				TasksUiPlugin.getDefault().getRepositoriesFilePath());
		
		server = JiraClientFacade.getDefault().getJiraClient(repository);
		JiraTestUtils.refreshDetails(server);
	}

	public void testAttachFile() throws Exception {
		attachFile(JiraTestConstants.JIRA_381_URL);
	}

	private void attachFile(String url) throws Exception {
		init(url, PrivilegeLevel.USER);

		Issue issue = JiraTestUtils.createIssue(server, "testAttachFile");
		
		File file = File.createTempFile("attachment", null);
		file.deleteOnExit();
		JiraTestUtils.writeFile(file, "Mylar".getBytes());
		
		AbstractRepositoryTask task = connector.createTaskFromExistingId(repository, issue.getKey());
		attachmentHandler.uploadAttachment(repository, task, "", "", file, "text/plain", false);
		
		TasksUiPlugin.getSynchronizationManager().synchronize(connector, task, true, null);
		RepositoryTaskData taskData = TasksUiPlugin.getDefault().getTaskDataManager().getNewTaskData(task.getHandleIdentifier());
		assertEquals(1, taskData.getAttachments().size());
		
		byte[] data = attachmentHandler.getAttachmentData(repository, taskData.getAttachments().get(0));
		assertEquals("Mylar", new String(data));
		
		file.delete();
		attachmentHandler.downloadAttachment(repository, taskData.getAttachments().get(0), file);
		assertTrue(file.exists());
		data = JiraTestUtils.readFile(file);
		assertEquals("Mylar", new String(data));
	}

}
