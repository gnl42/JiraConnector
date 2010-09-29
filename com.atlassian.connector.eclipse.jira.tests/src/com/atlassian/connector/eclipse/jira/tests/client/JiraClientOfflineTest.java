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

import org.eclipse.mylyn.commons.net.WebLocation;
import org.eclipse.mylyn.commons.tests.support.TestProxy;

import com.atlassian.connector.eclipse.internal.jira.core.model.JiraWorkLog;
import com.atlassian.connector.eclipse.internal.jira.core.service.JiraClient;
import com.atlassian.connector.eclipse.internal.jira.core.service.JiraException;
import com.atlassian.connector.eclipse.internal.jira.core.service.JiraServiceUnavailableException;
import com.atlassian.connector.eclipse.jira.tests.util.JiraTestUtil;

/**
 * @author Steffen Pingel
 */
public class JiraClientOfflineTest extends TestCase {

	private JiraClient client;

	private TestProxy server;

	private String manglePort(String message) {
		return message.replaceAll("http:\\/\\/localhost\\/", "http://localhost:" + server.getPort() + "/");
	}

	@Override
	protected void setUp() throws Exception {
		server = new TestProxy();
		server.startAndWait();
		client = new JiraClient(new WebLocation("http://localhost:" + server.getPort(), "", ""));
	}

	@Override
	protected void tearDown() throws Exception {
		server.stop();
	}

	public void testBrowseIssueRedirect() throws Exception {
		server.addResponse(JiraTestUtil.getMessage("web/login-success-response"));
		server.addResponse(JiraTestUtil.getMessage("soap/login-success-response"));
		server.addResponse(JiraTestUtil.getMessage("soap/get-server-info-3-13-1-success-response"));
		server.addResponse(JiraTestUtil.getMessage("web/user-preferences-get-response"));
		server.addResponse(manglePort(JiraTestUtil.getMessage("web/browse-issue-redirect-response")));
		server.addResponse(manglePort(JiraTestUtil.getMessage("web/browse-issue-redirect-response")));
		server.addResponse(manglePort(JiraTestUtil.getMessage("web/browse-issue-redirect-response")));
		server.addResponse(manglePort(JiraTestUtil.getMessage("web/browse-issue-redirect-response")));
		try {
			client.getIssueByKey("KEY-1", null);
			fail("Maximum number of query redirects reached. Should throw an exception");
		} catch (JiraException e) {
			// succeeded
		}
	}

	public void testGetWorklogs() throws Exception {
		server.addResponse(JiraTestUtil.getMessage("soap/login-success-response"));
		server.addResponse(JiraTestUtil.getMessage("soap/get-worklogs-two-entries-success-response"));
		JiraWorkLog[] worklogs = client.getWorklogs("KEY-1", null);
		assertNotNull(worklogs);
		assertEquals(2, worklogs.length);
	}

	public void testHttpsRedirect() throws Exception {
		client.getLocalConfiguration().setFollowRedirects(false);
		try {
			server.addResponse(JiraTestUtil.getMessage("soap/login-redirect-response"));
			client.getServerInfo(null);
			fail("Expected JiraServiceUnavailableException");
		} catch (JiraServiceUnavailableException e) {
		}
	}

}
