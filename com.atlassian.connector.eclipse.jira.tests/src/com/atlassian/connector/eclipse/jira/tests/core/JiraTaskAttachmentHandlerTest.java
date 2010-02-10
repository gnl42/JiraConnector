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

package com.atlassian.connector.eclipse.jira.tests.core;

import java.io.File;
import java.io.InputStream;
import java.util.List;

import junit.framework.TestCase;

import org.eclipse.mylyn.internal.tasks.core.data.FileTaskAttachmentSource;
import org.eclipse.mylyn.tasks.core.ITask;
import org.eclipse.mylyn.tasks.core.ITaskAttachment;
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.eclipse.mylyn.tasks.ui.TasksUi;
import org.eclipse.mylyn.tests.util.TestUtil.PrivilegeLevel;

import com.atlassian.connector.eclipse.internal.jira.core.JiraClientFactory;
import com.atlassian.connector.eclipse.internal.jira.core.JiraCorePlugin;
import com.atlassian.connector.eclipse.internal.jira.core.JiraRepositoryConnector;
import com.atlassian.connector.eclipse.internal.jira.core.JiraTaskAttachmentHandler;
import com.atlassian.connector.eclipse.internal.jira.core.model.JiraIssue;
import com.atlassian.connector.eclipse.internal.jira.core.service.JiraClient;
import com.atlassian.connector.eclipse.jira.tests.util.JiraTestConstants;
import com.atlassian.connector.eclipse.jira.tests.util.JiraTestUtil;

/**
 * @author Steffen Pingel
 */
public class JiraTaskAttachmentHandlerTest extends TestCase {

	private TaskRepository repository;

	private JiraRepositoryConnector connector;

	private JiraTaskAttachmentHandler attachmentHandler;

	private JiraClient client;

	@Override
	protected void setUp() throws Exception {
		JiraTestUtil.setUp();
		connector = (JiraRepositoryConnector) TasksUi.getRepositoryConnector(JiraCorePlugin.CONNECTOR_KIND);
		attachmentHandler = connector.getTaskAttachmentHandler();
	}

	@Override
	protected void tearDown() throws Exception {
		JiraTestUtil.tearDown();
	}

	protected void init(String url, PrivilegeLevel level) throws Exception {
		repository = JiraTestUtil.init(url, level);
		client = JiraClientFactory.getDefault().getJiraClient(repository);
		JiraTestUtil.refreshDetails(client);
	}

	public void testAttachFile() throws Exception {
		attachFile(JiraTestConstants.JIRA_LATEST_URL);
	}

	private void attachFile(String url) throws Exception {
		init(url, PrivilegeLevel.USER);

		JiraIssue issue = JiraTestUtil.createIssue(client, "testAttachFile");
		ITask task = JiraTestUtil.createTask(repository, issue.getKey());
		File file = File.createTempFile("attachment", null);
		file.deleteOnExit();
		JiraTestUtil.writeFile(file, "Mylyn".getBytes());

		FileTaskAttachmentSource attachment = new FileTaskAttachmentSource(file);
		attachment.setContentType("text/plain");
		attachmentHandler.postContent(repository, task, attachment, "", null, null);

		task = JiraTestUtil.createTask(repository, issue.getKey());
		List<ITaskAttachment> attachments = JiraTestUtil.getTaskAttachments(task);
		assertEquals(1, attachments.size());

		InputStream in = attachmentHandler.getContent(repository, task, attachments.get(0).getTaskAttribute(), null);
		try {
			byte[] data = new byte[5];
			in.read(data);
			assertEquals("Mylyn", new String(data));
		} finally {
			in.close();
		}
	}

}
