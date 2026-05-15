/*******************************************************************************
 * Copyright (c) 2004, 2009 Tasktop Technologies and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Tasktop Technologies - initial API and implementation
 *******************************************************************************/

package me.glindholm.connector.eclipse.jira.tests.core;

import static org.junit.jupiter.api.Assertions.assertThrows;

import org.eclipse.mylyn.commons.net.AuthenticationCredentials;
import org.eclipse.mylyn.commons.net.AuthenticationType;
import org.eclipse.mylyn.commons.net.WebLocation;
import org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin;
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.eclipse.mylyn.tests.util.TestFixture;
import org.eclipse.mylyn.tests.util.TestUtil;
import org.eclipse.mylyn.tests.util.TestUtil.PrivilegeLevel;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import me.glindholm.connector.eclipse.internal.jira.core.JiraClientFactory;
import me.glindholm.connector.eclipse.internal.jira.core.JiraCorePlugin;
import me.glindholm.connector.eclipse.internal.jira.core.service.JiraAuthenticationException;
import me.glindholm.connector.eclipse.internal.jira.core.service.JiraException;
import me.glindholm.connector.eclipse.jira.tests.util.JiraFixture;

/**
 * @author Wesley Coelho (initial integration patch)
 * @author Steffen Pingel
 */
public class JiraClientFactoryTest  {

	private JiraClientFactory clientFactory;

	@BeforeEach
	protected void setUp() throws Exception {
		clientFactory = JiraClientFactory.getDefault();
		TestFixture.resetTaskListAndRepositories();
	}

	@AfterEach
	protected void tearDown() throws Exception {
		clientFactory.clearClients();
	}

	@Test
	public void testValidate39() throws Exception {
		validate(jiraUrl());
	}

	private String jiraUrl() {
		return JiraFixture.current().getRepositoryUrl();
	}

	@Test
	public void testChangeCredentials() throws Exception {
		final var credentials = TestUtil.readCredentials(PrivilegeLevel.USER);
		final var repository = new TaskRepository(JiraCorePlugin.CONNECTOR_KIND, jiraUrl());
		repository.setCredentials(AuthenticationType.REPOSITORY, new AuthenticationCredentials(credentials.username,
				credentials.password), false);
		clientFactory.repositoryRemoved(repository);
		TasksUiPlugin.getRepositoryManager().addRepository(repository);

		repository.setCredentials(AuthenticationType.REPOSITORY, new AuthenticationCredentials("Bogus User",
				"Bogus Password"), false);
		clientFactory.repositoryRemoved(repository);

		assertThrows(JiraException.class, () ->
		clientFactory.getJiraClient(repository).getNamedFilters(null));

		// check that it works after putting the right password in
		repository.setCredentials(AuthenticationType.REPOSITORY, new AuthenticationCredentials(credentials.username,
				credentials.password), false);
		clientFactory.repositoryRemoved(repository);
		clientFactory.getJiraClient(repository).getNamedFilters(null);
	}

	protected void validate(final String url) throws Exception {
		final var credentials = TestUtil.readCredentials(PrivilegeLevel.USER);

		// standard connect
		clientFactory.validateConnection(new WebLocation(url, credentials.username, credentials.password), null);

		// invalid password
		assertThrows(JiraAuthenticationException.class, () ->
		clientFactory.validateConnection(new WebLocation(url, credentials.username, "wrongpassword"), null));
	}

	//    @Test
	//    public void testCharacterEncoding() throws Exception {
	//        Credentials credentials = TestUtil.readCredentials(PrivilegeLevel.USER);
	//        TaskRepository repository = new TaskRepository(JiraCorePlugin.CONNECTOR_KIND, jiraUrl());
	//        repository.setCredentials(AuthenticationType.REPOSITORY, new AuthenticationCredentials(credentials.username,
	//                credentials.password), false);
	//        assertFalse(JiraUtil.getCharacterEncodingValidated(repository));
	//
	//        JiraClient client = clientFactory.getJiraClient(repository);
	//
	//        // Due to different login mechanism for JIRA 4.x default encoding is set to UTF-8
	//        // where JIRA 3.x returns ISO8859-1
	//        boolean jira8x = client.getServerInfo(new NullProgressMonitor()).getVersion().compareTo(JiraServerVersion.JIRA_8_0) >= 0;
	//        String expectedEncoding = jira8x ? "UTF-8" : "ISO-8859-1";
	//
	//        assertEquals(expectedEncoding, client.getCharacterEncoding(new NullProgressMonitor()));
	//
	//        repository.setCharacterEncoding("UTF-8");
	//        clientFactory.repositoryChanged(new TaskRepositoryChangeEvent(this, repository, new TaskRepositoryDelta(
	//                Type.PROPERTY)));
	//        client = clientFactory.getJiraClient(repository);
	//        assertEquals(expectedEncoding, client.getCharacterEncoding(new NullProgressMonitor()));
	//
	//        JiraUtil.setCharacterEncodingValidated(repository, true);
	//        clientFactory.repositoryChanged(new TaskRepositoryChangeEvent(this, repository, new TaskRepositoryDelta(
	//                Type.PROPERTY)));
	//        client = clientFactory.getJiraClient(repository);
	//        assertEquals("UTF-8", client.getCharacterEncoding(new NullProgressMonitor()));
	//    }
}
