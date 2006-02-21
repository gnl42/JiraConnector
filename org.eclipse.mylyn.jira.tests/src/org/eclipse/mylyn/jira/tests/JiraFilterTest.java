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

import java.net.URL;

import junit.framework.TestCase;

import org.eclipse.mylar.internal.jira.JiraFilter;
import org.eclipse.mylar.internal.jira.JiraFilterHit;
import org.eclipse.mylar.internal.jira.JiraServerFacade;
import org.eclipse.mylar.internal.jira.MylarJiraPlugin;
import org.eclipse.mylar.provisional.tasklist.AbstractRepositoryClient;
import org.eclipse.mylar.provisional.tasklist.MylarTaskListPlugin;
import org.eclipse.mylar.provisional.tasklist.TaskRepository;
import org.tigris.jira.core.model.NamedFilter;

/**
 * @author Wesley Coelho (initial integration patch) *
 */
public class JiraFilterTest extends TestCase {

	private final static String USER = "mylartest";

	private final static String PASSWORD = "mylartest";

	private final static String SERVER_URL = "http://developer.atlassian.com/jira";

	private JiraServerFacade jiraFacade = null;

	private TaskRepository repository = null;

	protected void setUp() throws Exception {
		super.setUp();
		URL repoURL = new URL(SERVER_URL);
		repository = new TaskRepository(MylarJiraPlugin.JIRA_REPOSITORY_KIND,
				repoURL);
		repository.setAuthenticationCredentials(USER, PASSWORD);
		MylarTaskListPlugin.getRepositoryManager().addRepository(repository);
		jiraFacade = JiraServerFacade.getDefault();
	}

	protected void tearDown() throws Exception {
		AbstractRepositoryClient client = MylarTaskListPlugin
				.getRepositoryManager().getRepositoryClient(
						MylarJiraPlugin.JIRA_REPOSITORY_KIND);
		assertNotNull(client);
		MylarTaskListPlugin.getTaskListManager().getTaskList().clearArchive();
//		client.clearArchive();
		MylarTaskListPlugin.getTaskListManager().getTaskList().clear();
		MylarTaskListPlugin.getRepositoryManager().removeRepository(repository);
		jiraFacade.logOutFromAll();
		super.tearDown();
	}

	public void testJiraFilterRefresh() {
		NamedFilter[] filters = jiraFacade.getJiraServer(repository).getNamedFilters();
		assertTrue(filters.length > 0);
		JiraFilter jFilter = new JiraFilter(repository.getUrl().toExternalForm(), filters[0]);
		assertTrue(jFilter.getHits().size() == 0);
		jFilter.refreshHits();
		while (jFilter.isRefreshing()) {
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		assertTrue(jFilter.getHits().size() > 0);
		JiraFilterHit jHit = (JiraFilterHit) jFilter.getHits().iterator().next();
		assertTrue(jHit.getDescription().length() > 0);
	}
}
