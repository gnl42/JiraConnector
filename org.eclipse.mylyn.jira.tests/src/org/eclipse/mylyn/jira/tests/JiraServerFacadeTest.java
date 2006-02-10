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

import org.eclipse.mylar.internal.jira.JiraServerFacade;
import org.eclipse.mylar.internal.jira.MylarJiraPlugin;
import org.eclipse.mylar.internal.tasklist.AbstractRepositoryClient;
import org.eclipse.mylar.internal.tasklist.MylarTaskListPlugin;
import org.eclipse.mylar.internal.tasklist.TaskRepository;
import org.tigris.jira.core.model.Issue;
import org.tigris.jira.core.model.NamedFilter;
import org.tigris.jira.core.model.filter.IssueCollector;
import org.tigris.jira.core.service.JiraServer;

/**
 * @author Wesley Coelho (initial integration patch)
 */
public class JiraServerFacadeTest extends TestCase {

	private final static String USER = "mylartest";

	private final static String PASSWORD = "mylartest";

	private final static String SERVER_URL = "http://developer.atlassian.com/jira";

	private JiraServerFacade jiraFacade = null;

	private TaskRepository jiraRepo = null;

	protected void setUp() throws Exception {
		super.setUp();
		URL repoURL = new URL(SERVER_URL);
		jiraRepo = new TaskRepository(MylarJiraPlugin.JIRA_REPOSITORY_KIND,
				repoURL);
		jiraRepo.setAuthenticationCredentials(USER, PASSWORD);
		MylarTaskListPlugin.getRepositoryManager().addRepository(jiraRepo);
		jiraFacade = JiraServerFacade.getDefault();
	}

	protected void tearDown() throws Exception {
		AbstractRepositoryClient client = MylarTaskListPlugin
				.getRepositoryManager().getRepositoryClient(
						MylarJiraPlugin.JIRA_REPOSITORY_KIND);
		assertNotNull(client);
		client.clearArchive();
		MylarTaskListPlugin.getTaskListManager().getTaskList().clear();
		MylarTaskListPlugin.getRepositoryManager().removeRepository(jiraRepo);
		jiraFacade.logOut();
		super.tearDown();
	}

	public void testLogin() {
		// This connects and logs into the default Jira repository
		jiraFacade.getJiraServer();

		// Tests connection using the currently specified credentials
		jiraFacade.validateServerAndCredentials(SERVER_URL, USER, PASSWORD);
	}

	public void testFilterDownload() {
		JiraServer jiraServer = jiraFacade.getJiraServer();
		NamedFilter[] filters = jiraServer.getNamedFilters();
		assertTrue(filters.length > 0);
	}

	public void testFilterResults() {
		JiraServer jiraServer = jiraFacade.getJiraServer();
		NamedFilter[] filters = jiraServer.getNamedFilters();
		assertTrue(filters.length > 0);

		jiraServer.executeNamedFilter(filters[0], new IssueCollector() {

			private boolean issueCollected = false;

			public void start() {
			}

			public void collectIssue(Issue issue) {
				issueCollected = true;
			}

			public boolean isCancelled() {
				return false;
			}

			public void done() {
				if (!issueCollected) {
					fail("No Issues were collected");
				}
			}
		});
	}

	public void testServerInfoChange() {
		jiraRepo.setAuthenticationCredentials("Bogus User", "Bogus Password");
		jiraFacade.repositorySetUpdated();

		boolean failedOnBogusUser = false;

		try {
			jiraFacade.getJiraServer().getNamedFilters();
		} catch (Exception e) {
			failedOnBogusUser = true;
		}

		assertTrue(failedOnBogusUser);

		// Check that it works after putting the right password in
		jiraRepo.setAuthenticationCredentials(USER, PASSWORD);
		jiraFacade.repositorySetUpdated();
		jiraFacade.getJiraServer().getNamedFilters();
	}

}
