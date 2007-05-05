/*******************************************************************************
 * Copyright (c) 2004 - 2006 Mylar committers and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.mylar.jira.tests;

import java.net.Proxy;

import junit.framework.TestCase;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.mylar.context.tests.support.MylarTestUtils;
import org.eclipse.mylar.context.tests.support.MylarTestUtils.Credentials;
import org.eclipse.mylar.context.tests.support.MylarTestUtils.PrivilegeLevel;
import org.eclipse.mylar.internal.jira.core.model.Comment;
import org.eclipse.mylar.internal.jira.core.model.Issue;
import org.eclipse.mylar.internal.jira.core.model.Resolution;
import org.eclipse.mylar.internal.jira.core.model.Version;
import org.eclipse.mylar.internal.jira.core.model.filter.FilterDefinition;
import org.eclipse.mylar.internal.jira.core.service.AbstractJiraServer;
import org.eclipse.mylar.internal.jira.core.service.JiraException;
import org.eclipse.mylar.internal.jira.core.service.JiraRemoteMessageException;
import org.eclipse.mylar.internal.jira.core.service.JiraServer;
import org.eclipse.mylar.internal.jira.core.service.soap.JiraRpcServer;

/**
 * @author Steffen Pingel
 */
public class JiraRpcServerTest extends TestCase {

	private AbstractJiraServer server;

	protected void init(String url, PrivilegeLevel level) throws Exception {
		Credentials credentials = MylarTestUtils.readCredentials(level);
		server = new JiraRpcServer(url, false, credentials.username, credentials.password, Proxy.NO_PROXY, null, null);
	}

	public void testLogin381() throws Exception {
		login(JiraTestConstants.JIRA_381_URL);
	}

	@SuppressWarnings("deprecation")
	private void login(String url) throws Exception {
		init(url, PrivilegeLevel.USER);

		server.login();
		server.logout();
		// should automatically login
		server.refreshDetails(new NullProgressMonitor());

		init(url, PrivilegeLevel.GUEST);
		server.login();
	}

	public void testStartStopIssue() throws Exception {
		startStopIssue(JiraTestConstants.JIRA_381_URL);
	}

	private void startStopIssue(String url) throws Exception {
		init(url, PrivilegeLevel.USER);

		Issue issue = JiraTestUtils.createIssue(server, "testStartStopIssue");
		server.startIssue(issue);
		try {
			server.startIssue(issue);
			fail("Expected JiraRemoteMessageException");
		} catch (JiraRemoteMessageException e) {
			assertEquals("Workflow Action Invalid", e.getMessage());
		}
		server.stopIssue(issue);
		try {
			server.stopIssue(issue);
			fail("Expected JiraRemoteMessageException");
		} catch (JiraException e) {
			assertEquals("Workflow Action Invalid", e.getMessage());
		}
		server.startIssue(issue);
	}

	public void testResolveCloseReopenIssue() throws Exception {
		resolveCloseReopenIssue(JiraTestConstants.JIRA_381_URL);
	}

	private void resolveCloseReopenIssue(String url) throws Exception {
		init(url, PrivilegeLevel.USER);

		Resolution resolution = JiraTestUtils.getFixedResolution(server);
		Issue issue = JiraTestUtils.createIssue(server, "testStartStopIssue");

		server.resolveIssue(issue, resolution, new Version[0], "comment", JiraServer.ASSIGNEE_DEFAULT, "");
		issue = server.getIssueById(issue.getId());
		assertTrue(issue.getStatus().isResolved());
		try {
			server.resolveIssue(issue, resolution, new Version[0], "comment", JiraServer.ASSIGNEE_DEFAULT, "");
			fail("Expected JiraRemoteMessageException");
		} catch (JiraRemoteMessageException e) {
			assertEquals("Workflow Action Invalid", e.getMessage());
		}

		server.closeIssue(issue, resolution, new Version[0], "comment", JiraServer.ASSIGNEE_DEFAULT, "");
		issue = server.getIssueById(issue.getId());
		assertTrue(issue.getStatus().isClosed());
		try {
			server.resolveIssue(issue, resolution, new Version[0], "comment", JiraServer.ASSIGNEE_DEFAULT, "");
			fail("Expected JiraRemoteMessageException");
		} catch (JiraRemoteMessageException e) {
			assertEquals("Workflow Action Invalid", e.getMessage());
		}
		try {
			server.closeIssue(issue, resolution, new Version[0], "comment", JiraServer.ASSIGNEE_DEFAULT, "");
			fail("Expected JiraRemoteMessageException");
		} catch (JiraException e) {
			assertEquals("Workflow Action Invalid", e.getMessage());
		}

		server.reopenIssue(issue, "comment", JiraServer.ASSIGNEE_DEFAULT, "");
		issue = server.getIssueById(issue.getId());
		assertTrue(issue.getStatus().isReopened());
	}

	public void testGetIdFromKey() throws Exception {
		getIdFromKey(JiraTestConstants.JIRA_381_URL);
	}

	private void getIdFromKey(String url) throws Exception {
		init(url, PrivilegeLevel.GUEST);

		Issue issue = JiraTestUtils.createIssue(server, "getIdFromKey");
		String key = server.getKeyFromId(issue.getId());
		assertEquals(issue.getKey(), key);

		try {
			key = server.getKeyFromId("invalid");
			fail("Expected JiraException, got: " + key);
		} catch (JiraException e) {
		}

		try {
			key = server.getKeyFromId("1");
			fail("Expected JiraException, got: " + key);
		} catch (JiraException e) {
		}

	}

	public void testReassign() throws Exception {
		reassign(JiraTestConstants.JIRA_381_URL);
	}

	private void reassign(String url) throws Exception {
		init(url, PrivilegeLevel.USER);

		Issue issue = JiraTestUtils.createIssue(server, "testReassign");
		issue.setAssignee("nonexistantuser");
		try {
			server.updateIssue(issue, "comment");
			fail("Expected JiraException");
		} catch (JiraRemoteMessageException e) {
			assertEquals("User 'nonexistantuser' cannot be assigned issues.", e.getMessage());
		}

		try {
			server.assignIssueTo(issue, JiraServer.ASSIGNEE_NONE, "", "");
			fail("Expected JiraException");
		} catch (JiraRemoteMessageException e) {
			assertEquals("Issues must be assigned.", e.getMessage());
		}

		try {
			server.assignIssueTo(issue, JiraServer.ASSIGNEE_SELF, "", "");
		} catch (JiraRemoteMessageException e) {
			assertEquals("Issue already assigned to Test User 1 (" + server.getUserName() + ").", e.getMessage());
		}
		
		String guestUsername = MylarTestUtils.readCredentials(PrivilegeLevel.GUEST).username;
		try {
			server.assignIssueTo(issue, JiraServer.ASSIGNEE_USER, guestUsername, "");
		} catch (JiraRemoteMessageException e) {
			assertEquals("User 'guest@mylar.eclipse.org' cannot be assigned issues.", e.getMessage());
		}

		server.assignIssueTo(issue, JiraServer.ASSIGNEE_DEFAULT, "", "");
		issue = server.getIssueById(issue.getId());
		assertEquals("admin@mylar.eclipse.org", issue.getAssignee());

		server.assignIssueTo(issue, JiraServer.ASSIGNEE_SELF, "", "");
		issue = server.getIssueById(issue.getId());
		assertEquals(server.getUserName(), issue.getAssignee());

		init(url, PrivilegeLevel.GUEST);
		try {
			server.assignIssueTo(issue, JiraServer.ASSIGNEE_SELF, "", "");
			fail("Expected JiraException");
		} catch (JiraException e) {
		}
	}

	public void testFindIssues() throws Exception {
		findIssues(JiraTestConstants.JIRA_381_URL);
	}

	private void findIssues(String url) throws Exception {
		init(url, PrivilegeLevel.USER);

		JiraTestUtils.refreshDetails(server);
		FilterDefinition filter = new FilterDefinition();
		MockIssueCollector collector = new MockIssueCollector();
		server.search(filter, collector);
		assertTrue(collector.done);
	}

	public void testAddComment() throws Exception {
		addComment(JiraTestConstants.JIRA_381_URL);
	}

	private void addComment(String url) throws Exception {
		init(url, PrivilegeLevel.USER);

		Issue issue = JiraTestUtils.createIssue(server, "testAddComment");
		server.addCommentToIssue(issue, "comment 1");
		issue = server.getIssueById(issue.getId());
		Comment comment = getComment(issue, "comment 1");
		assertNotNull(comment);
		assertEquals(server.getUserName(), comment.getAuthor());
		
		init(url, PrivilegeLevel.GUEST);
		JiraTestUtils.refreshDetails(server);
		server.addCommentToIssue(issue, "comment guest");
		issue = server.getIssueById(issue.getId());
		comment = getComment(issue, "comment guest");
		assertNotNull(comment);
		assertEquals(server.getUserName(), comment.getAuthor());
	}

	private Comment getComment(Issue issue, String text) {
		for (Comment comment : issue.getComments()) {
			if (text.equals(comment.getComment())) {
				return comment;
			}
		}
		return null;
	}

}
