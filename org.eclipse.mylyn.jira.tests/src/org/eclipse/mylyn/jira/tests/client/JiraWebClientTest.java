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

package org.eclipse.mylyn.jira.tests.client;

import junit.framework.TestCase;

import org.eclipse.mylyn.commons.net.WebLocation;
import org.eclipse.mylyn.context.tests.support.TestUtil;
import org.eclipse.mylyn.context.tests.support.TestUtil.Credentials;
import org.eclipse.mylyn.context.tests.support.TestUtil.PrivilegeLevel;
import org.eclipse.mylyn.internal.jira.core.model.JiraIssue;
import org.eclipse.mylyn.internal.jira.core.model.Project;
import org.eclipse.mylyn.internal.jira.core.service.JiraClient;
import org.eclipse.mylyn.internal.jira.core.service.web.JiraWebClient;
import org.eclipse.mylyn.internal.jira.core.service.web.JiraWebSession;
import org.eclipse.mylyn.jira.tests.util.JiraTestConstants;
import org.eclipse.mylyn.jira.tests.util.JiraTestUtil;

/**
 * @author Steffen Pingel
 */
public class JiraWebClientTest extends TestCase {

	private JiraWebClient webClient;

	private JiraClient client;

	private JiraWebSession webSession;

	@Override
	protected void tearDown() throws Exception {
		JiraTestUtil.tearDown();
	}

	protected void init(String url, PrivilegeLevel level) throws Exception {
		Credentials credentials = TestUtil.readCredentials(level);
		client = new JiraClient(new WebLocation(url, credentials.username, credentials.password));
		webSession = new JiraWebSession(client);
		webClient = new JiraWebClient(client, webSession);

		JiraTestUtil.refreshDetails(client);
	}

	public void testCreateIssue() throws Exception {
		createIssue(JiraTestConstants.JIRA_LATEST_URL);
	}

	private void createIssue(String url) throws Exception {
		init(url, PrivilegeLevel.USER);

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
		init(JiraTestConstants.JIRA_LATEST_URL, PrivilegeLevel.USER);
		JiraIssue issue = JiraTestUtil.createIssue(client, "testDoInSession");
		webClient.updateIssue(issue, "updated", null);
		issue = client.getIssueByKey(issue.getKey(), null);
		assertEquals(1, issue.getComments().length);
		webSession.doLogout(null);
		webClient.updateIssue(issue, "updatedAgain", null);
		issue = client.getIssueByKey(issue.getKey(), null);
		assertNotNull(issue);
		assertEquals(2, issue.getComments().length);
	}

}
