/*******************************************************************************
 * Copyright (c) 2004, 2007 Mylyn project committers and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.mylyn.jira.tests;

import java.io.File;
import java.io.InputStream;
import java.util.List;

import junit.framework.TestCase;

import org.eclipse.mylyn.context.tests.support.TestUtil.PrivilegeLevel;
import org.eclipse.mylyn.internal.jira.core.JiraClientFactory;
import org.eclipse.mylyn.internal.jira.core.JiraCorePlugin;
import org.eclipse.mylyn.internal.jira.core.JiraRepositoryConnector;
import org.eclipse.mylyn.internal.jira.core.JiraTaskAttachmentHandler;
import org.eclipse.mylyn.internal.jira.core.model.JiraIssue;
import org.eclipse.mylyn.internal.jira.core.service.JiraClient;
import org.eclipse.mylyn.internal.tasks.core.data.FileTaskAttachmentSource;
import org.eclipse.mylyn.jira.tests.util.JiraTestConstants;
import org.eclipse.mylyn.jira.tests.util.JiraTestUtil;
import org.eclipse.mylyn.tasks.core.ITask;
import org.eclipse.mylyn.tasks.core.ITaskAttachment;
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.eclipse.mylyn.tasks.ui.TasksUi;

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
		attachFile(JiraTestConstants.JIRA_39_URL);
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
