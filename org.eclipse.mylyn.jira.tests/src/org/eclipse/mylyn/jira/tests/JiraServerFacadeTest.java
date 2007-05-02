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

import junit.framework.TestCase;

import org.eclipse.mylar.context.tests.support.MylarTestUtils;
import org.eclipse.mylar.context.tests.support.MylarTestUtils.Credentials;
import org.eclipse.mylar.context.tests.support.MylarTestUtils.PrivilegeLevel;
import org.eclipse.mylar.internal.jira.core.model.Issue;
import org.eclipse.mylar.internal.jira.core.model.NamedFilter;
import org.eclipse.mylar.internal.jira.core.model.filter.IssueCollector;
import org.eclipse.mylar.internal.jira.core.service.JiraAuthenticationException;
import org.eclipse.mylar.internal.jira.core.service.JiraServer;
import org.eclipse.mylar.internal.jira.core.service.JiraServiceUnavailableException;
import org.eclipse.mylar.internal.jira.ui.JiraServerFacade;
import org.eclipse.mylar.internal.jira.ui.JiraUiPlugin;
import org.eclipse.mylar.tasks.core.AbstractRepositoryConnector;
import org.eclipse.mylar.tasks.core.TaskRepository;
import org.eclipse.mylar.tasks.ui.TasksUiPlugin;

/**
 * @author Wesley Coelho (initial integration patch)
 */
public class JiraServerFacadeTest extends TestCase {

	private final static String USER = "mylartest";

	private final static String PASSWORD = "mylartest";

	private final static String SERVER_URL = "http://developer.atlassian.com/jira";

	private JiraServerFacade jiraFacade = null;

	private TaskRepository repository = null;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		repository = new TaskRepository(JiraUiPlugin.REPOSITORY_KIND, SERVER_URL);
		repository.setAuthenticationCredentials(USER, PASSWORD);
		TasksUiPlugin.getRepositoryManager().addRepository(repository,
				TasksUiPlugin.getDefault().getRepositoriesFilePath());
		jiraFacade = JiraServerFacade.getDefault();
	}

	@Override
	protected void tearDown() throws Exception {
		AbstractRepositoryConnector client = TasksUiPlugin.getRepositoryManager().getRepositoryConnector(
				JiraUiPlugin.REPOSITORY_KIND);
		assertNotNull(client);
		// client.clearArchive();
		// MylarTaskListPlugin.getTaskListManager().getTaskList().clearArchive();
		// MylarTaskListPlugin.getTaskListManager().getTaskList().clear();
		TasksUiPlugin.getTaskListManager().resetTaskList();
		TasksUiPlugin.getRepositoryManager().removeRepository(repository,
				TasksUiPlugin.getDefault().getRepositoriesFilePath());
		jiraFacade.logOutFromAll();
		super.tearDown();
	}

	public void testLogin() throws Exception {
		// This connects and logs into the default Jira repository
		jiraFacade.getJiraServer(repository);

		// Tests connection using the currently specified credentials
		jiraFacade.validateServerAndCredentials(SERVER_URL, USER, PASSWORD, null, null, null);
	}

	public void testLogin381() throws Exception {
		validate(JiraTestConstants.JIRA_381_URL);
	}

	public void testFilterDownload() throws Exception {
		JiraServer jiraServer = jiraFacade.getJiraServer(repository);
		NamedFilter[] filters = jiraServer.getNamedFilters();
		assertTrue(filters.length > 0);
	}

	public void testFilterResults() throws Exception {
		JiraServer jiraServer = jiraFacade.getJiraServer(repository);
		NamedFilter[] filters = jiraServer.getNamedFilters();
		assertTrue(filters.length > 0);

		jiraServer.search(filters[0], new IssueCollector() {

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

			public Exception getException() {
				return null;
			}

			public void setException(Exception e) {
			}

			public int getMaxHits() {
				return Integer.MAX_VALUE;
			}
		});
	}

	public void testServerInfoChange() throws Exception {
		repository.setAuthenticationCredentials("Bogus User", "Bogus Password");
		jiraFacade.repositoryRemoved(repository);

		boolean failedOnBogusUser = false;

		try {
			jiraFacade.getJiraServer(repository).getNamedFilters();
		} catch (Exception e) {
			failedOnBogusUser = true;
		}
		assertTrue(failedOnBogusUser);

		// Check that it works after putting the right password in
		repository.setAuthenticationCredentials(USER, PASSWORD);
		jiraFacade.repositoryRemoved(repository);
		jiraFacade.getJiraServer(repository).getNamedFilters();
	}

	protected void validate(String url) throws Exception {
		Credentials credentials = MylarTestUtils.readCredentials(PrivilegeLevel.USER);
		
		// standard connect
		jiraFacade.validateServerAndCredentials(url, credentials.username, credentials.password, null, null, null);

		// invalid URL		
		try {
			jiraFacade.validateServerAndCredentials("http://non.existant/repository", credentials.username, credentials.password, null, null, null);
			fail("Expected exception");
		} catch (JiraServiceUnavailableException e) {
		}

		// invalid password
		try {
			jiraFacade.validateServerAndCredentials(url, credentials.username, "wrongpassword", null, null, null);
			fail("Expected exception");
		} catch (JiraAuthenticationException e) {
		}

		// invalid username
		try {
			jiraFacade.validateServerAndCredentials(url, "wrongusername", credentials.password, null, null, null);
			fail("Expected exception");
		} catch (JiraAuthenticationException e) {
		}
	}

}
