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

package me.glindholm.connector.eclipse.jira.tests.core;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.File;

import org.eclipse.mylyn.internal.tasks.core.data.FileTaskAttachmentSource;
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.eclipse.mylyn.tasks.ui.TasksUi;
import org.eclipse.mylyn.tests.util.TestUtil.PrivilegeLevel;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import me.glindholm.connector.eclipse.internal.jira.core.JiraClientFactory;
import me.glindholm.connector.eclipse.internal.jira.core.JiraCorePlugin;
import me.glindholm.connector.eclipse.internal.jira.core.JiraRepositoryConnector;
import me.glindholm.connector.eclipse.internal.jira.core.JiraTaskAttachmentHandler;
import me.glindholm.connector.eclipse.internal.jira.core.service.JiraClient;
import me.glindholm.connector.eclipse.jira.tests.util.JiraFixture;
import me.glindholm.connector.eclipse.jira.tests.util.JiraTestUtil;

/**
 * @author Steffen Pingel
 */
public class JiraTaskAttachmentHandlerTest  {

	private TaskRepository repository;

	private JiraRepositoryConnector connector;

	private JiraTaskAttachmentHandler attachmentHandler;

	private JiraClient client;

	@BeforeEach
	protected void setUp() throws Exception {
		JiraTestUtil.setUp();
		connector = (JiraRepositoryConnector) TasksUi.getRepositoryConnector(JiraCorePlugin.CONNECTOR_KIND);
		attachmentHandler = connector.getTaskAttachmentHandler();
	}

	@AfterEach
	protected void tearDown() throws Exception {
		JiraTestUtil.tearDown();
	}

	protected void init(final String url, final PrivilegeLevel level) throws Exception {
		repository = JiraTestUtil.init(url, level);
		client = JiraClientFactory.getDefault().getJiraClient(repository);
		JiraTestUtil.refreshDetails(client);
	}

	@Test
	public void testAttachFile() throws Exception {
		attachFile(JiraFixture.current().getRepositoryUrl());
	}

	private void attachFile(final String url) throws Exception {
		init(url, PrivilegeLevel.USER);

		final var issue = JiraTestUtil.createIssue(client, "testAttachFile");
		var task = JiraTestUtil.createTask(repository, issue.getKey());
		final var file = File.createTempFile("attachment", null);
		file.deleteOnExit();
		JiraTestUtil.writeFile(file, "Mylyn".getBytes());

		final var attachment = new FileTaskAttachmentSource(file);
		attachment.setContentType("text/plain");
		attachmentHandler.postContent(repository, task, attachment, "", null, null);

		task = JiraTestUtil.createTask(repository, issue.getKey());
		final var attachments = JiraTestUtil.getTaskAttachments(task);
		assertEquals(1, attachments.size());

		final var in = attachmentHandler.getContent(repository, task, attachments.get(0).getTaskAttribute(), null);
		try (in) {
			final var data = new byte[5];
			in.read(data);
			assertEquals("Mylyn", new String(data));
		}
	}

}
