/*******************************************************************************
 * Copyright (c) 2004, 2007 Mylyn project committers and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.mylyn.internal.jira.ui;

import junit.framework.TestCase;

import org.eclipse.mylyn.internal.jira.core.JiraTask;
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.eclipse.mylyn.tasks.tests.connector.MockRepositoryConnector;

/**
 * @author Eugene Kuleshov
 * @author Steffen Pingel
 */
public class JiraConnectorUiTest extends TestCase {

	public void testFindHyperlinks() {
		TaskRepository repository = new TaskRepository(MockRepositoryConnector.REPOSITORY_KIND, "http://u.net");
		JiraConnectorUi connectorUi = new JiraConnectorUi();
		connectorUi.findHyperlinks(repository, "foo", -1, 0);
		connectorUi.findHyperlinks(repository, "foo", 0, 0);
		connectorUi.findHyperlinks(repository, "foo", 1, 0);
		connectorUi.findHyperlinks(repository, "foo", 2, 0);
		connectorUi.findHyperlinks(repository, "foo", 3, 0);
		connectorUi.findHyperlinks(repository, "foo", 4, 0);

		connectorUi.findHyperlinks(repository, "foo boo", -1, 0);
		connectorUi.findHyperlinks(repository, "foo boo", 0, 0);
		connectorUi.findHyperlinks(repository, "foo boo", 1, 0);
		connectorUi.findHyperlinks(repository, "foo boo", 2, 0);
		connectorUi.findHyperlinks(repository, "foo boo", 3, 0);
		connectorUi.findHyperlinks(repository, "foo boo", 4, 0);
		connectorUi.findHyperlinks(repository, "foo boo", 5, 0);
		connectorUi.findHyperlinks(repository, "foo boo", 6, 0);
		connectorUi.findHyperlinks(repository, "foo boo", 7, 0);
		connectorUi.findHyperlinks(repository, "foo boo", 8, 0);
		connectorUi.findHyperlinks(repository, "foo boo", 9, 0);
	}

	public void testGetTaskHistoryUrl() {
		TaskRepository repository = new TaskRepository(MockRepositoryConnector.REPOSITORY_KIND,
				"http://mylyn.eclipse.org");
		JiraConnectorUi connectorUi = new JiraConnectorUi();
		JiraTask task = new JiraTask(repository.getRepositoryUrl(), "ABC-12", "");
		assertEquals("http://mylyn.eclipse.org/browse/ABC-123?page=history", connectorUi.getTaskHistoryUrl(repository,
				task));
	}

}
