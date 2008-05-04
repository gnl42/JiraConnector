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
import org.eclipse.mylyn.commons.net.AuthenticationCredentials;
import org.eclipse.mylyn.commons.net.AuthenticationType;
import org.eclipse.mylyn.context.tests.support.TestUtil;
import org.eclipse.mylyn.context.tests.support.TestUtil.Credentials;
import org.eclipse.mylyn.context.tests.support.TestUtil.PrivilegeLevel;
import org.eclipse.mylyn.internal.jira.core.JiraClientFactory;
import org.eclipse.mylyn.internal.jira.core.JiraCorePlugin;
import org.eclipse.mylyn.internal.jira.core.JiraRepositoryConnector;
import org.eclipse.mylyn.internal.jira.core.model.JiraIssue;
import org.eclipse.mylyn.internal.jira.core.service.JiraClient;
import org.eclipse.mylyn.internal.tasks.core.deprecated.AbstractAttachmentHandler;
import org.eclipse.mylyn.internal.tasks.core.deprecated.FileAttachment;
import org.eclipse.mylyn.internal.tasks.core.deprecated.RepositoryTaskData;
import org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin;
import org.eclipse.mylyn.internal.tasks.ui.util.TasksUiInternal;
import org.eclipse.mylyn.tasks.core.AbstractRepositoryConnector;
import org.eclipse.mylyn.tasks.core.AbstractTask;
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.eclipse.mylyn.tasks.ui.TasksUiUtil;

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
		TasksUiPlugin.getRepositoryManager().clearRepositories(TasksUiPlugin.getDefault().getRepositoriesFilePath());
		JiraClientFactory.getDefault().clearClients();

		AbstractRepositoryConnector abstractConnector = TasksUiPlugin.getRepositoryManager().getRepositoryConnector(
				JiraCorePlugin.CONNECTOR_KIND);
		connector = (JiraRepositoryConnector) abstractConnector;
		attachmentHandler = connector.getAttachmentHandler();

		repository = null;
	}

	@Override
	protected void tearDown() throws Exception {
		if (client != null) {
			JiraTestUtils.cleanup(client);
		}
	}

	protected void init(String url, PrivilegeLevel level) throws Exception {
		Credentials credentials = TestUtil.readCredentials(level);

		if (repository != null) {
			TasksUiPlugin.getRepositoryManager().removeRepository(repository,
					TasksUiPlugin.getDefault().getRepositoriesFilePath());
		}

		repository = new TaskRepository(JiraCorePlugin.CONNECTOR_KIND, JiraTestConstants.JIRA_39_URL);
		repository.setCredentials(AuthenticationType.REPOSITORY, new AuthenticationCredentials(credentials.username,
				credentials.password), false);
		repository.setCharacterEncoding(JiraClient.DEFAULT_CHARSET);

		TasksUiPlugin.getRepositoryManager().addRepository(repository,
				TasksUiPlugin.getDefault().getRepositoriesFilePath());

		client = JiraClientFactory.getDefault().getJiraClient(repository);
		JiraTestUtils.refreshDetails(client);
	}

	public void testAttachFile() throws Exception {
		attachFile(JiraTestConstants.JIRA_39_URL);
	}

	private void attachFile(String url) throws Exception {
		init(url, PrivilegeLevel.USER);

		JiraIssue issue = JiraTestUtils.createIssue(client, "testAttachFile");

		File file = File.createTempFile("attachment", null);
		file.deleteOnExit();
		JiraTestUtils.writeFile(file, "Mylyn".getBytes());

		AbstractTask task = TasksUiUtil.createTask(repository, issue.getKey(), new NullProgressMonitor());
		FileAttachment attachment = new FileAttachment(file);
		attachment.setContentType("text/plain");
		attachmentHandler.uploadAttachment(repository, task, attachment, "", new NullProgressMonitor());

		TasksUiInternal.synchronizeTask(connector, task, true, null);
		RepositoryTaskData taskData = TasksUiPlugin.getTaskDataStorageManager().getNewTaskData(task.getRepositoryUrl(),
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
