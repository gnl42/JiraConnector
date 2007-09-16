/*******************************************************************************
 * Copyright (c) 2004, 2007 Mylyn project committers and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.mylyn.jira.tests;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

import junit.framework.TestCase;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.mylyn.context.tests.support.TestUtil;
import org.eclipse.mylyn.context.tests.support.TestUtil.Credentials;
import org.eclipse.mylyn.context.tests.support.TestUtil.PrivilegeLevel;
import org.eclipse.mylyn.internal.jira.core.model.Issue;
import org.eclipse.mylyn.internal.jira.core.service.JiraClient;
import org.eclipse.mylyn.internal.jira.ui.JiraClientFacade;
import org.eclipse.mylyn.internal.jira.ui.JiraRepositoryConnector;
import org.eclipse.mylyn.internal.jira.ui.JiraUiPlugin;
import org.eclipse.mylyn.tasks.core.AbstractAttachmentHandler;
import org.eclipse.mylyn.tasks.core.AbstractRepositoryConnector;
import org.eclipse.mylyn.tasks.core.AbstractTask;
import org.eclipse.mylyn.tasks.core.FileAttachment;
import org.eclipse.mylyn.tasks.core.RepositoryTaskData;
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.eclipse.mylyn.tasks.ui.TasksUiPlugin;

/**
 * @author Steffen Pingel
 */
public class JiraAttachmentHandlerTest extends TestCase {

	private TaskRepository repository;

	private JiraRepositoryConnector connector;

	private AbstractAttachmentHandler attachmentHandler;

	private JiraClient client;

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
		Credentials credentials = TestUtil.readCredentials(level);

		if (repository != null) {
			TasksUiPlugin.getRepositoryManager().removeRepository(repository,
					TasksUiPlugin.getDefault().getRepositoriesFilePath());
		}

		repository = new TaskRepository(JiraUiPlugin.REPOSITORY_KIND, JiraTestConstants.JIRA_39_URL);
		repository.setAuthenticationCredentials(credentials.username, credentials.password);
		repository.setCharacterEncoding(JiraClient.DEFAULT_CHARSET);

		TasksUiPlugin.getRepositoryManager().addRepository(repository,
				TasksUiPlugin.getDefault().getRepositoriesFilePath());

		client = JiraClientFacade.getDefault().getJiraClient(repository);
		JiraTestUtils.refreshDetails(client);
	}

	public void testAttachFile() throws Exception {
		attachFile(JiraTestConstants.JIRA_39_URL);
	}

	private void attachFile(String url) throws Exception {
		init(url, PrivilegeLevel.USER);

		Issue issue = JiraTestUtils.createIssue(client, "testAttachFile");
		issue = client.createIssue(issue);

		File file = File.createTempFile("attachment", null);
		file.deleteOnExit();
		JiraTestUtils.writeFile(file, "Mylyn".getBytes());

		AbstractTask task = connector.createTaskFromExistingId(repository, issue.getKey(), new NullProgressMonitor());
		FileAttachment attachment = new FileAttachment(file);
		attachment.setContentType("text/plain");
		attachmentHandler.uploadAttachment(repository, task, attachment, "", new NullProgressMonitor());

		TasksUiPlugin.getSynchronizationManager().synchronize(connector, task, true, null);
		RepositoryTaskData taskData = TasksUiPlugin.getTaskDataManager().getNewTaskData(task.getRepositoryUrl(),
				task.getTaskId());
		assertNotNull(taskData);
		assertEquals(1, taskData.getAttachments().size());

		InputStream in = attachmentHandler.getAttachmentAsStream(repository, taskData.getAttachments().get(0),
				new NullProgressMonitor());
		try {
			byte[] data = new byte[5];
			in.read(data);
			assertEquals("Mylyn", new String(data));
		} finally {
			in.close();
		}

		file.delete();

		attachmentHandler.downloadAttachment(repository, taskData.getAttachments().get(0), new FileOutputStream(file),
				new NullProgressMonitor());
		assertTrue(file.exists());
		byte[] data = JiraTestUtils.readFile(file);
		assertEquals("Mylyn", new String(data));
	}

}
