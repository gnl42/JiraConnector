/*******************************************************************************
 * Copyright (c) 2004, 2009 Eugene Kuleshov and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Eugene Kuleshov - initial API and implementation
 *     Tasktop Technologies - improvements
 *******************************************************************************/

package me.glindholm.connector.eclipse.jira.tests.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.eclipse.jface.text.Region;
import org.eclipse.mylyn.internal.tasks.core.TaskTask;
import org.eclipse.mylyn.tasks.core.ITask;
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.eclipse.mylyn.tasks.ui.TasksUi;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import me.glindholm.connector.eclipse.internal.jira.core.JiraClientFactory;
import me.glindholm.connector.eclipse.internal.jira.core.JiraCorePlugin;
import me.glindholm.connector.eclipse.internal.jira.ui.JiraConnectorUi;
import me.glindholm.connector.eclipse.jira.tests.util.JiraFixture;
import me.glindholm.connector.eclipse.jira.tests.util.JiraTestUtil;

/**
 * @author Eugene Kuleshov
 * @author Steffen Pingel
 */
public class JiraConnectorUiTest  {

	private JiraConnectorUi connectorUi;

	@BeforeEach
	protected void setUp() throws Exception {
		connectorUi = (JiraConnectorUi) TasksUi.getRepositoryConnectorUi(JiraCorePlugin.CONNECTOR_KIND);
		JiraTestUtil.setUp();
	}

	@AfterEach
	protected void tearDown() throws Exception {
		JiraTestUtil.tearDown();
	}

	protected void init(final String url) throws Exception {
	}

	@Test
	public void testFindHyperlinks() throws Exception {
		final var repository = JiraTestUtil.init(JiraFixture.current().getRepositoryUrl());
		final var client = JiraClientFactory.getDefault().getJiraClient(repository);
		JiraTestUtil.refreshDetails(client);

		var result = connectorUi.findHyperlinks(repository, "foo", -1, 0);
		assertNull(result);

		result = connectorUi.findHyperlinks(repository, "PRONE", -1, 0);
		assertNull(result);

		result = connectorUi.findHyperlinks(repository, "PRONE-1", -1, 0);
		assertNotNull(result);
		assertEquals(1, result.length);
		assertEquals(new Region(0, 7), result[0].getHyperlinkRegion());

		result = connectorUi.findHyperlinks(repository, " PRONE-1", 2, 3);
		assertNotNull(result);
		assertEquals(1, result.length);
		assertEquals(new Region(4, 7), result[0].getHyperlinkRegion());

		result = connectorUi.findHyperlinks(repository, " PRONE-1 abc PRONE-23 ABC-123 ", 2, 3);
		assertNotNull(result);
		assertEquals(2, result.length);
		assertEquals(new Region(4, 7), result[0].getHyperlinkRegion());
		assertEquals(new Region(16, 8), result[1].getHyperlinkRegion());

		result = connectorUi.findHyperlinks(repository, " PRONE-1 abc PRONE-2 ABC-123 ", -1, 3);
		assertNotNull(result);
		assertEquals(2, result.length);
		assertEquals(new Region(4, 7), result[0].getHyperlinkRegion());
		assertEquals(new Region(16, 7), result[1].getHyperlinkRegion());

		result = connectorUi.findHyperlinks(repository, "PRONE-PRONE-1", -1, 0);
		assertNull(result);
	}

	@Test
	public void testGetTaskHistoryUrl() {
		final var repository = new TaskRepository(JiraCorePlugin.CONNECTOR_KIND, "http://mylyn.eclipse.org");
		final ITask task = new TaskTask(JiraCorePlugin.CONNECTOR_KIND, repository.getRepositoryUrl(), "456");
		task.setTaskKey("ABC-123");
		assertEquals(
				"http://mylyn.eclipse.org/browse/ABC-123?page=me.glindholm.jira.plugin.system.issuetabpanels:changehistory-tabpanel",
				connectorUi.getTaskHistoryUrl(repository, task));
	}

}
