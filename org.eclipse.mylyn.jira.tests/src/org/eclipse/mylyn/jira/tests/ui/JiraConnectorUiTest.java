/*******************************************************************************
* Copyright (c) 2004, 2008 Tasktop Technologies and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Eugene Kuleshov - initial API and implementation
 *     Tasktop Technologies - improvements
 *******************************************************************************/

package org.eclipse.mylyn.jira.tests.ui;

import junit.framework.TestCase;

import org.eclipse.mylyn.internal.jira.core.JiraCorePlugin;
import org.eclipse.mylyn.internal.jira.ui.JiraConnectorUi;
import org.eclipse.mylyn.internal.tasks.core.TaskTask;
import org.eclipse.mylyn.tasks.core.ITask;
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.eclipse.mylyn.tasks.ui.TasksUi;

/**
 * @author Eugene Kuleshov
 * @author Steffen Pingel
 */
public class JiraConnectorUiTest extends TestCase {

	private JiraConnectorUi connectorUi;

	@Override
	protected void setUp() throws Exception {
		connectorUi = (JiraConnectorUi) TasksUi.getRepositoryConnectorUi(JiraCorePlugin.CONNECTOR_KIND);
	}

	public void testFindHyperlinks() {
		TaskRepository repository = new TaskRepository(JiraCorePlugin.CONNECTOR_KIND, "http://u.net");
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
		TaskRepository repository = new TaskRepository(JiraCorePlugin.CONNECTOR_KIND, "http://mylyn.eclipse.org");
		ITask task = new TaskTask(JiraCorePlugin.CONNECTOR_KIND, repository.getRepositoryUrl(), "456");
		task.setTaskKey("ABC-123");
		assertEquals("http://mylyn.eclipse.org/browse/ABC-123?page=history", connectorUi.getTaskHistoryUrl(repository,
				task));
	}

}
