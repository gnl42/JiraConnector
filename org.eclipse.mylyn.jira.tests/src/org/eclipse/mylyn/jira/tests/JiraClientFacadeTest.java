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
import org.eclipse.mylar.internal.jira.core.service.JiraAuthenticationException;
import org.eclipse.mylar.internal.jira.core.service.JiraServiceUnavailableException;
import org.eclipse.mylar.internal.jira.ui.JiraClientFacade;
import org.eclipse.mylar.internal.jira.ui.JiraUiPlugin;
import org.eclipse.mylar.tasks.core.TaskRepository;
import org.eclipse.mylar.tasks.ui.TasksUiPlugin;

/**
 * @author Wesley Coelho (initial integration patch)
 * @author Steffen Pingel
 */
public class JiraClientFacadeTest extends TestCase {

	private JiraClientFacade jiraFacade = null;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		
		jiraFacade = JiraClientFacade.getDefault();
		
		TasksUiPlugin.getTaskListManager().resetTaskList();
	}

	@Override
	protected void tearDown() throws Exception {
		jiraFacade.logOutFromAll();
	}

	public void testLogin381() throws Exception {
		validate(JiraTestConstants.JIRA_381_URL);
	}

	public void testChangeCredentials() throws Exception {
		Credentials credentials = MylarTestUtils.readCredentials(PrivilegeLevel.USER);
		TaskRepository repository = new TaskRepository(JiraUiPlugin.REPOSITORY_KIND, JiraTestConstants.JIRA_381_URL);
		repository.setAuthenticationCredentials(credentials.username, credentials.password);
		TasksUiPlugin.getRepositoryManager().addRepository(repository,
				TasksUiPlugin.getDefault().getRepositoriesFilePath());

		repository.setAuthenticationCredentials("Bogus User", "Bogus Password");
		jiraFacade.repositoryRemoved(repository);

		boolean failedOnBogusUser = false;

		try {
			jiraFacade.getJiraClient(repository).getNamedFilters();
		} catch (Exception e) {
			failedOnBogusUser = true;
		}
		assertTrue(failedOnBogusUser);

		// check that it works after putting the right password in
		repository.setAuthenticationCredentials(credentials.username, credentials.password);
		jiraFacade.repositoryRemoved(repository);
		jiraFacade.getJiraClient(repository).getNamedFilters();
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
