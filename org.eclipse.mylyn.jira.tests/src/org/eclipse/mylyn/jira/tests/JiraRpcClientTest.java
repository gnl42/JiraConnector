/*******************************************************************************
 * Copyright (c) 2004 - 2006 Mylar committers and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.mylyn.jira.tests;

import java.io.File;
import java.net.Proxy;

import junit.framework.TestCase;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.mylyn.context.tests.support.TestUtil;
import org.eclipse.mylyn.context.tests.support.TestUtil.Credentials;
import org.eclipse.mylyn.context.tests.support.TestUtil.PrivilegeLevel;
import org.eclipse.mylyn.internal.jira.core.model.Attachment;
import org.eclipse.mylyn.internal.jira.core.model.Comment;
import org.eclipse.mylyn.internal.jira.core.model.Issue;
import org.eclipse.mylyn.internal.jira.core.model.Resolution;
import org.eclipse.mylyn.internal.jira.core.model.Version;
import org.eclipse.mylyn.internal.jira.core.model.filter.FilterDefinition;
import org.eclipse.mylyn.internal.jira.core.service.AbstractJiraClient;
import org.eclipse.mylyn.internal.jira.core.service.JiraClient;
import org.eclipse.mylyn.internal.jira.core.service.JiraException;
import org.eclipse.mylyn.internal.jira.core.service.JiraRemoteMessageException;
import org.eclipse.mylyn.internal.jira.core.service.soap.JiraRpcClient;
import org.eclipse.mylyn.internal.jira.ui.JiraTaskDataHandler;

/**
 * @author Steffen Pingel
 */
public class JiraRpcClientTest extends TestCase {

	private AbstractJiraClient client;

	protected void init(String url, PrivilegeLevel level) throws Exception {
		Credentials credentials = TestUtil.readCredentials(level);
		client = new JiraRpcClient(url, false, credentials.username, credentials.password, Proxy.NO_PROXY, null, null);

		JiraTestUtils.refreshDetails(client);
	}

	public void testLogin381() throws Exception {
		login(JiraTestConstants.JIRA_39_URL);
	}

	@SuppressWarnings("deprecation")
	private void login(String url) throws Exception {
		init(url, PrivilegeLevel.USER);

		client.login();
		client.logout();
		// should automatically login
		client.refreshDetails(new NullProgressMonitor());

		init(url, PrivilegeLevel.GUEST);
		client.login();
	}

	public void testStartStopIssue() throws Exception {
		startStopIssue(JiraTestConstants.JIRA_39_URL);
	}

	private void startStopIssue(String url) throws Exception {
		init(url, PrivilegeLevel.USER);

		Issue issue = JiraTestUtils.createIssue(client, "testStartStopIssue");

		String startOperation = JiraTestUtils.getOperation(client, issue.getKey(), "start");

		client.advanceIssueWorkflow(issue, startOperation, null);
		try {
			client.advanceIssueWorkflow(issue, startOperation, null);
			fail("Expected JiraRemoteMessageException");
		} catch (JiraRemoteMessageException e) {
			assertEquals("Workflow Action Invalid", e.getMessage());
		}

		String stopOperation = JiraTestUtils.getOperation(client, issue.getKey(), "stop");

		client.advanceIssueWorkflow(issue, stopOperation, null);
		try {
			client.advanceIssueWorkflow(issue, stopOperation, null);
			fail("Expected JiraRemoteMessageException");
		} catch (JiraException e) {
			assertEquals("Workflow Action Invalid", e.getMessage());
		}
		client.advanceIssueWorkflow(issue, startOperation, null);
	}

	public void testResolveCloseReopenIssue() throws Exception {
		resolveCloseReopenIssue(JiraTestConstants.JIRA_39_URL);
	}

	private void resolveCloseReopenIssue(String url) throws Exception {
		init(url, PrivilegeLevel.USER);

		Resolution resolution = JiraTestUtils.getFixedResolution(client);
		Issue issue = JiraTestUtils.createIssue(client, "testStartStopIssue");
		issue.setResolution(resolution);
		issue.setFixVersions(new Version[0]);

		String resolveOperation = JiraTestUtils.getOperation(client, issue.getKey(), "resolve");

		client.advanceIssueWorkflow(issue, resolveOperation, "comment");

		issue = client.getIssueByKey(issue.getKey());
		assertTrue(issue.getStatus().isResolved());

		try {
			client.advanceIssueWorkflow(issue, resolveOperation, "comment");
			fail("Expected JiraRemoteMessageException");
		} catch (JiraRemoteMessageException e) {
			assertEquals("Workflow Action Invalid", e.getMessage());
		}

		// have to get "close" operation after resolving issue
		String closeOperation = JiraTestUtils.getOperation(client, issue.getKey(), "close");

		client.advanceIssueWorkflow(issue, closeOperation, "comment");
		issue = client.getIssueByKey(issue.getKey());
		assertTrue(issue.getStatus().isClosed());

		try {
			client.advanceIssueWorkflow(issue, resolveOperation, "comment");
			fail("Expected JiraRemoteMessageException");
		} catch (JiraRemoteMessageException e) {
			assertEquals("Workflow Action Invalid", e.getMessage());
		}

		try {
			client.advanceIssueWorkflow(issue, closeOperation, "comment");
			fail("Expected JiraRemoteMessageException");
		} catch (JiraException e) {
			assertEquals("Workflow Action Invalid", e.getMessage());
		}

		String reopenOperation = JiraTestUtils.getOperation(client, issue.getKey(), "reopen");

		client.advanceIssueWorkflow(issue, reopenOperation, "comment");
		issue = client.getIssueByKey(issue.getKey());
		assertTrue(issue.getStatus().isReopened());
	}

	public void testGetIdFromKey() throws Exception {
		getIdFromKey(JiraTestConstants.JIRA_39_URL);
	}

	private void getIdFromKey(String url) throws Exception {
		init(url, PrivilegeLevel.GUEST);

		Issue issue = JiraTestUtils.createIssue(client, "getIdFromKey");
		String key = client.getKeyFromId(issue.getId());
		assertEquals(issue.getKey(), key);

		try {
			key = client.getKeyFromId("invalid");
			fail("Expected JiraException, got: " + key);
		} catch (JiraException e) {
		}

		try {
			key = client.getKeyFromId("1");
			fail("Expected JiraException, got: " + key);
		} catch (JiraException e) {
		}

	}

	public void testReassign() throws Exception {
		reassign(JiraTestConstants.JIRA_39_URL);
	}

	private void reassign(String url) throws Exception {
		init(url, PrivilegeLevel.USER);

		Issue issue = JiraTestUtils.createIssue(client, "testReassign");
		issue.setAssignee("nonexistantuser");
		try {
			client.updateIssue(issue, "comment");
			fail("Expected JiraException");
		} catch (JiraRemoteMessageException e) {
			assertEquals("User 'nonexistantuser' cannot be assigned issues.", e.getHtmlMessage());
		}

		try {
			client.assignIssueTo(issue, JiraClient.ASSIGNEE_NONE, "", "");
			fail("Expected JiraException");
		} catch (JiraRemoteMessageException e) {
			assertEquals("Issues must be assigned.", e.getHtmlMessage());
		}

		try {
			client.assignIssueTo(issue, JiraClient.ASSIGNEE_SELF, "", "");
		} catch (JiraRemoteMessageException e) {
			assertEquals("Issue already assigned to Test User 1 (" + client.getUserName() + ").", e.getHtmlMessage());
		}

		String guestUsername = TestUtil.readCredentials(PrivilegeLevel.GUEST).username;
		try {
			client.assignIssueTo(issue, JiraClient.ASSIGNEE_USER, guestUsername, "");
		} catch (JiraRemoteMessageException e) {
			assertEquals("User 'guest@mylar.eclipse.org' cannot be assigned issues.", e.getHtmlMessage());
		}

		client.assignIssueTo(issue, JiraClient.ASSIGNEE_DEFAULT, "", "");
		issue = client.getIssueByKey(issue.getKey());
		assertEquals("admin@mylar.eclipse.org", issue.getAssignee());

		client.assignIssueTo(issue, JiraClient.ASSIGNEE_SELF, "", "");
		issue = client.getIssueByKey(issue.getKey());
		assertEquals(client.getUserName(), issue.getAssignee());

		init(url, PrivilegeLevel.GUEST);
		try {
			client.assignIssueTo(issue, JiraClient.ASSIGNEE_SELF, "", "");
			fail("Expected JiraException");
		} catch (JiraException e) {
		}
	}

	public void testFindIssues() throws Exception {
		findIssues(JiraTestConstants.JIRA_39_URL);
	}

	private void findIssues(String url) throws Exception {
		init(url, PrivilegeLevel.USER);

		FilterDefinition filter = new FilterDefinition();
		MockIssueCollector collector = new MockIssueCollector();
		client.search(filter, collector);
		assertTrue(collector.done);
	}

	public void testAddComment() throws Exception {
		addComment(JiraTestConstants.JIRA_39_URL);
	}

	private void addComment(String url) throws Exception {
		init(url, PrivilegeLevel.USER);

		Issue issue = JiraTestUtils.createIssue(client, "testAddComment");
		client.addCommentToIssue(issue, "comment 1");
		issue = client.getIssueByKey(issue.getKey());
		Comment comment = getComment(issue, "comment 1");
		assertNotNull(comment);
		assertEquals(client.getUserName(), comment.getAuthor());

		init(url, PrivilegeLevel.GUEST);

		client.addCommentToIssue(issue, "comment guest");
		issue = client.getIssueByKey(issue.getKey());
		comment = getComment(issue, "comment guest");
		assertNotNull(comment);
		assertEquals(client.getUserName(), comment.getAuthor());
	}

	private Comment getComment(Issue issue, String text) {
		for (Comment comment : issue.getComments()) {
			if (text.equals(comment.getComment())) {
				return comment;
			}
		}
		return null;
	}

	public void testAttachFile() throws Exception {
		attachFile(JiraTestConstants.JIRA_39_URL);
	}

	private void attachFile(String url) throws Exception {
		init(url, PrivilegeLevel.USER);

		File file = File.createTempFile("mylar", null);
		file.deleteOnExit();

		Issue issue = JiraTestUtils.createIssue(client, "testAttachFile");
		// test attaching an empty file
		try {
			client.attachFile(issue, "", file.getName(), file, "application/binary");
			fail("Expected JiraException");
		} catch (JiraRemoteMessageException e) {
		}

		client.attachFile(issue, "", "my.filename.1", new byte[] { 'M', 'y', 'l', 'a', 'r' }, "application/binary");
		issue = client.getIssueByKey(issue.getKey());
		Attachment attachment = getAttachment(issue, "my.filename.1");
		assertNotNull(attachment);
		assertEquals(client.getUserName(), attachment.getAuthor());
		assertEquals(5, attachment.getSize());
		assertNotNull(attachment.getCreated());
	}

	private Attachment getAttachment(Issue issue, String filename) {
		for (Attachment attachment : issue.getAttachments()) {
			if (filename.equals(attachment.getName())) {
				return attachment;
			}
		}
		return null;
	}

	public void testCreateIssue() throws Exception {
		createIssue(JiraTestConstants.JIRA_39_URL);
	}

	private void createIssue(String url) throws Exception {
		init(url, PrivilegeLevel.USER);

		Issue issue = new Issue();
		issue.setProject(client.getProjects()[0]);
		issue.setType(client.getIssueTypes()[0]);
		issue.setSummary("testCreateIssue");
		issue.setAssignee(client.getUserName());

		Issue createdIssue = client.createIssue(issue);
		assertEquals(issue.getProject(), createdIssue.getProject());
		assertEquals(issue.getType(), createdIssue.getType());
		assertEquals(issue.getSummary(), createdIssue.getSummary());
		assertEquals(issue.getAssignee(), createdIssue.getAssignee());
		assertEquals(client.getUserName(), createdIssue.getReporter());
		// TODO why are these null?
		// assertNotNull(issue.getCreated());
		// assertNotNull(issue.getUpdated());

		init(url, PrivilegeLevel.GUEST);

		createdIssue = client.createIssue(issue);
		assertEquals(issue.getProject(), createdIssue.getProject());
		assertEquals(issue.getType(), createdIssue.getType());
		assertEquals(issue.getSummary(), createdIssue.getSummary());
		assertEquals("admin@mylar.eclipse.org", createdIssue.getAssignee());
		assertEquals(client.getUserName(), createdIssue.getReporter());
	}

	public void testGetIssueLeadingSpacesInDescription() throws Exception {
		getIssueLeadingSpacesInDescripton(JiraTestConstants.JIRA_39_URL);
	}

	private void getIssueLeadingSpacesInDescripton(String url) throws Exception {
		init(url, PrivilegeLevel.USER);

		Issue issue = new Issue();
		issue.setProject(client.getProjects()[0]);
		issue.setType(client.getIssueTypes()[0]);
		issue.setSummary("testCreateIssue");
		String description = "  leading spaces\n  more spaces";
		issue.setDescription(description);
		issue.setAssignee(client.getUserName());

		issue = client.createIssue(issue);
		issue = client.getIssueByKey(issue.getKey());
		assertEquals(description, JiraTaskDataHandler.convertHtml(issue.getDescription()));

		issue.setDescription(JiraTaskDataHandler.convertHtml(issue.getDescription()));
		client.updateIssue(issue, "");
		issue = client.getIssueByKey(issue.getKey());
		assertEquals(description, JiraTaskDataHandler.convertHtml(issue.getDescription()));
	}

	public void testUpdateIssue() throws Exception {
		updateIssue(JiraTestConstants.JIRA_39_URL);
	}

	private void updateIssue(String url) throws Exception {
		init(url, PrivilegeLevel.USER);

		Issue issue = new Issue();
		issue.setProject(client.getProjects()[0]);
		issue.setType(client.getIssueTypes()[0]);
		issue.setSummary("testUpdateIssue");
		issue.setAssignee(client.getUserName());

		issue = client.createIssue(issue);
		issue.setSummary("testUpdateIssueChanged");
		client.updateIssue(issue, "");
		issue = client.getIssueByKey(issue.getKey());
		assertEquals("testUpdateIssueChanged", issue.getSummary());
		assertNotNull(issue.getUpdated());

		init(url, PrivilegeLevel.GUEST);
		try {
			client.updateIssue(issue, "");
			fail("Expected JiraException");
		} catch (JiraRemoteMessageException e) {
		}

		init(url, PrivilegeLevel.GUEST);

		issue.setSummary("testUpdateIssueGuest");
		issue = client.createIssue(issue);
		issue.setSummary("testUpdateIssueGuestChanged");
		try {
			client.updateIssue(issue, "");
			fail("Expected JiraException");
		} catch (JiraRemoteMessageException e) {
		}
	}

	public void testWatchUnwatchIssue() throws Exception {
		watchUnwatchIssue(JiraTestConstants.JIRA_39_URL);
	}

	private void watchUnwatchIssue(String url) throws Exception {
		init(url, PrivilegeLevel.USER);

		Issue issue = JiraTestUtils.createIssue(client, "testWatchUnwatch");
		assertFalse(issue.isWatched());
		client.watchIssue(issue);
		issue = client.getIssueByKey(issue.getKey());
		// flag is never set
//		assertTrue(issue.isWatched());

		client.unwatchIssue(issue);
		issue = client.getIssueByKey(issue.getKey());
		assertFalse(issue.isWatched());
		client.unwatchIssue(issue);
		issue = client.getIssueByKey(issue.getKey());
		assertFalse(issue.isWatched());
	}

}
