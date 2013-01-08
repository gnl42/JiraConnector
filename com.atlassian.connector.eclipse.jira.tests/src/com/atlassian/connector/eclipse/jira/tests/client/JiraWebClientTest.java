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

package com.atlassian.connector.eclipse.jira.tests.client;

import junit.framework.TestCase;

import com.atlassian.connector.eclipse.internal.jira.core.model.JiraIssue;
import com.atlassian.connector.eclipse.internal.jira.core.model.Project;
import com.atlassian.connector.eclipse.internal.jira.core.service.JiraClient;
import com.atlassian.connector.eclipse.internal.jira.core.service.web.JiraWebClient;
import com.atlassian.connector.eclipse.internal.jira.core.service.web.JiraWebSession;
import com.atlassian.connector.eclipse.jira.tests.util.JiraFixture;
import com.atlassian.connector.eclipse.jira.tests.util.JiraTestUtil;

/**
 * @author Steffen Pingel
 */
public class JiraWebClientTest extends TestCase {

	private JiraWebClient webClient;

	private JiraClient client;

	private JiraWebSession webSession;

	@Override
	protected void setUp() throws Exception {
		client = JiraFixture.current().client();
		webSession = new JiraWebSession(client);
		webClient = new JiraWebClient(client, webSession);
	}

	@Override
	protected void tearDown() throws Exception {
		JiraTestUtil.tearDown();
	}

	public void testCreateIssue() throws Exception {
		JiraIssue issue = new JiraIssue();
		Project project = client.getCache().getProjects()[0];
		issue.setProject(project);
		issue.setType(client.getCache().getIssueTypes()[0]);
		issue.setSummary("testCreateIssue");

		String key = webClient.createIssue(issue, null);
		try {
			String projectName = project.getKey();
			assertEquals(projectName, key.substring(0, projectName.length()));
		} finally {
			try {
				client.deleteIssue(client.getIssueByKey(key, null), null);
			} catch (Exception e) {
				// ignore
			}
		}
	}

	public void testDoInSession() throws Exception {

		// webclient is not used any more

//		JiraIssue issue = JiraTestUtil.createIssue(client, "testDoInSession");
//		webClient.updateIssue(issue, "updated", null);
//		issue = client.getIssueByKey(issue.getKey(), null);
//		assertEquals(1, issue.getComments().length);
//		webSession.doLogout(null);
//		webClient.updateIssue(issue, "updatedAgain", null);
//		issue = client.getIssueByKey(issue.getKey(), null);
//		assertNotNull(issue);
//		assertEquals(2, issue.getComments().length);
	}

}
