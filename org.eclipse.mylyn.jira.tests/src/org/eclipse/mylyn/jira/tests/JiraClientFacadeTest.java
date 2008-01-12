/*******************************************************************************
 * Copyright (c) 2004, 2007 Mylyn project committers and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.mylyn.jira.tests;

import junit.framework.TestCase;

import org.eclipse.mylyn.context.tests.support.TestUtil;
import org.eclipse.mylyn.context.tests.support.TestUtil.Credentials;
import org.eclipse.mylyn.context.tests.support.TestUtil.PrivilegeLevel;
import org.eclipse.mylyn.internal.jira.core.service.JiraAuthenticationException;
import org.eclipse.mylyn.internal.jira.core.service.JiraClient;
import org.eclipse.mylyn.internal.jira.core.service.JiraException;
import org.eclipse.mylyn.internal.jira.core.service.JiraServiceUnavailableException;
import org.eclipse.mylyn.internal.jira.ui.JiraClientFactory;
import org.eclipse.mylyn.internal.jira.ui.JiraUiPlugin;
import org.eclipse.mylyn.internal.jira.ui.JiraUtils;
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.eclipse.mylyn.tasks.ui.TasksUiPlugin;
import org.eclipse.mylyn.web.core.WebLocation;

/**
 * @author Wesley Coelho (initial integration patch)
 * @author Steffen Pingel
 */
public class JiraClientFacadeTest extends TestCase {

	private JiraClientFactory jiraFacade = null;

	@Override
	protected void setUp() throws Exception {
		super.setUp();

		jiraFacade = JiraClientFactory.getDefault();

		TasksUiPlugin.getTaskListManager().resetTaskList();
	}

	@Override
	protected void tearDown() throws Exception {
		jiraFacade.logOutFromAll();
	}

	public void testValidate39() throws Exception {
		validate(JiraTestConstants.JIRA_39_URL);
	}

	public void testValidate() throws Exception {
		// invalid URL		
		try {
			jiraFacade.validateConnection(new WebLocation("http://non.existant/repository", "user", "password"));
			fail("Expected exception");
		} catch (JiraServiceUnavailableException e) {
		}

		// RPC not enabled
		try {
			jiraFacade.validateConnection(new WebLocation("http://mylyn.eclipse.org/jira-invalid", "user", "password"));
			fail("Expected exception");
		} catch (JiraServiceUnavailableException e) {
			assertEquals("JIRA RPC services are not enabled. Please contact your JIRA administrator.", e.getMessage());
		}
	}

	public void testChangeCredentials() throws Exception {
		Credentials credentials = TestUtil.readCredentials(PrivilegeLevel.USER);
		TaskRepository repository = new TaskRepository(JiraUiPlugin.REPOSITORY_KIND, JiraTestConstants.JIRA_39_URL);
		repository.setAuthenticationCredentials(credentials.username, credentials.password);
		TasksUiPlugin.getRepositoryManager().addRepository(repository,
				TasksUiPlugin.getDefault().getRepositoriesFilePath());

		repository.setAuthenticationCredentials("Bogus User", "Bogus Password");
		jiraFacade.repositoryRemoved(repository);

		try {
			jiraFacade.getJiraClient(repository).getNamedFilters();
			fail("Expected to fail on bogus user");
		} catch (JiraException e) {
			// ignore
		}

		// check that it works after putting the right password in
		repository.setAuthenticationCredentials(credentials.username, credentials.password);
		jiraFacade.repositoryRemoved(repository);
		jiraFacade.getJiraClient(repository).getNamedFilters();
	}

	protected void validate(String url) throws Exception {
		Credentials credentials = TestUtil.readCredentials(PrivilegeLevel.USER);

		// standard connect
		jiraFacade.validateConnection(new WebLocation(url, credentials.username, credentials.password));

		// invalid password
		try {
			jiraFacade.validateConnection(new WebLocation(url, credentials.username, "wrongpassword"));
			fail("Expected exception");
		} catch (JiraAuthenticationException e) {
		}
	}

	public void testCharacterEncoding() throws Exception {
		Credentials credentials = TestUtil.readCredentials(PrivilegeLevel.USER);
		TaskRepository repository = new TaskRepository(JiraUiPlugin.REPOSITORY_KIND, JiraTestConstants.JIRA_39_URL);
		repository.setAuthenticationCredentials(credentials.username, credentials.password);
		assertFalse(JiraUtils.getCharacterEncodingValidated(repository));

		JiraClient client = jiraFacade.getJiraClient(repository);
		assertEquals("ISO-8859-1", client.getCharacterEncoding());

		repository.setCharacterEncoding("UTF-8");
		jiraFacade.repositorySettingsChanged(repository);
		client = jiraFacade.getJiraClient(repository);
		assertEquals("ISO-8859-1", client.getCharacterEncoding());

		JiraUtils.setCharacterEncodingValidated(repository, true);
		jiraFacade.repositorySettingsChanged(repository);
		client = jiraFacade.getJiraClient(repository);
		assertEquals("UTF-8", client.getCharacterEncoding());
	}

}
