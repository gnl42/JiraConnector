/*******************************************************************************
 * Copyright (c) 2004, 2009 Tasktop Technologies and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Tasktop Technologies - initial API and implementation
 *     Eugene Kuleshov - improvements
 *******************************************************************************/

package com.atlassian.connector.eclipse.jira.tests.client;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.either;
import static org.hamcrest.Matchers.equalTo;

import java.io.File;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashSet;
import java.util.Set;

import junit.framework.TestCase;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.mylyn.commons.net.AuthenticationType;
import org.eclipse.mylyn.commons.net.WebLocation;
import org.eclipse.mylyn.tests.util.TestUtil;
import org.eclipse.mylyn.tests.util.TestUtil.Credentials;
import org.eclipse.mylyn.tests.util.TestUtil.PrivilegeLevel;

import com.atlassian.connector.eclipse.internal.jira.core.model.Attachment;
import com.atlassian.connector.eclipse.internal.jira.core.model.Comment;
import com.atlassian.connector.eclipse.internal.jira.core.model.IssueField;
import com.atlassian.connector.eclipse.internal.jira.core.model.JiraIssue;
import com.atlassian.connector.eclipse.internal.jira.core.model.JiraWorkLog;
import com.atlassian.connector.eclipse.internal.jira.core.model.Project;
import com.atlassian.connector.eclipse.internal.jira.core.model.Resolution;
import com.atlassian.connector.eclipse.internal.jira.core.model.SecurityLevel;
import com.atlassian.connector.eclipse.internal.jira.core.model.ServerInfo;
import com.atlassian.connector.eclipse.internal.jira.core.model.Version;
import com.atlassian.connector.eclipse.internal.jira.core.model.filter.FilterDefinition;
import com.atlassian.connector.eclipse.internal.jira.core.service.JiraClient;
import com.atlassian.connector.eclipse.internal.jira.core.service.JiraException;
import com.atlassian.connector.eclipse.internal.jira.core.service.JiraRemoteMessageException;
import com.atlassian.connector.eclipse.internal.jira.core.service.JiraServiceUnavailableException;
import com.atlassian.connector.eclipse.jira.tests.util.JiraFixture;
import com.atlassian.connector.eclipse.jira.tests.util.JiraTestUtil;
import com.atlassian.connector.eclipse.jira.tests.util.MockIssueCollector;

/**
 * @author Steffen Pingel
 * @author Eugene Kuleshov
 * @author Thomas Ehrnhoefer
 */
public class JiraClientTest extends TestCase {

	private JiraClient client;

	@Override
	protected void setUp() throws Exception {
		client = JiraFixture.current().client();
	}

	@Override
	protected void tearDown() throws Exception {
		JiraTestUtil.tearDown();
	}

	public void testLogin() throws Exception {
		client.login(null);
		client.logout(null);
		// should automatically login
		client.getCache().refreshDetails(new NullProgressMonitor());

		client = JiraFixture.current().client(PrivilegeLevel.GUEST);
		client.login(null);
	}

	public void testStartStopIssue() throws Exception {
		JiraIssue issue = JiraTestUtil.createIssue(client, "testStartStopIssue");

		String startOperation = JiraTestUtil.getOperation(client, issue.getKey(), "start");

		client.advanceIssueWorkflow(issue, startOperation, null, null);
		try {
			client.advanceIssueWorkflow(issue, startOperation, null, null);
			fail("Expected JiraRemoteMessageException");
		} catch (JiraRemoteMessageException e) {
			assertThat(
					e.getMessage(),
					either(
							containsString("It seems that you have tried to perform a workflow operation (Start Progress) that is not valid for the current state of this issue ")).or(
							equalTo("Workflow Action Invalid")));
		}

		String stopOperation = JiraTestUtil.getOperation(client, issue.getKey(), "stop");

		client.advanceIssueWorkflow(issue, stopOperation, null, null);
		try {
			client.advanceIssueWorkflow(issue, stopOperation, null, null);
			fail("Expected JiraRemoteMessageException");
		} catch (JiraException e) {
			assertThat(
					e.getMessage(),
					either(
							containsString("It seems that you have tried to perform a workflow operation (Stop Progress) that is not valid for the current state of this issue ")).or(
							equalTo("Workflow Action Invalid")));
		}
		client.advanceIssueWorkflow(issue, startOperation, null, null);
	}

	public void testResolveCloseReopenIssue() throws Exception {
		final String resolveMsg = "It seems that you have tried to perform a workflow operation (Resolve Issue) that is not valid for the current state of this issue ";
		Resolution resolution = JiraTestUtil.getFixedResolution(client);
		JiraIssue issue = JiraTestUtil.createIssue(client, "testStartStopIssue");

		issue.setResolution(resolution);
		issue.setFixVersions(new Version[0]);

		String resolveOperation = JiraTestUtil.getOperation(client, issue.getKey(), "resolve");

		client.advanceIssueWorkflow(issue, resolveOperation, "comment", null);

		issue = client.getIssueByKey(issue.getKey(), null);
		assertEquals("Resolved", issue.getStatus().getName());

		try {
			client.advanceIssueWorkflow(issue, resolveOperation, "comment", null);
			fail("Expected JiraRemoteMessageException");
		} catch (JiraRemoteMessageException e) {
			assertThat(e.getMessage(), either(containsString(resolveMsg)).or(equalTo("Workflow Action Invalid")));
		}

		// have to get "close" operation after resolving issue
		String closeOperation = JiraTestUtil.getOperation(client, issue.getKey(), "close");

		client.advanceIssueWorkflow(issue, closeOperation, "comment", null);
		issue = client.getIssueByKey(issue.getKey(), null);
		assertEquals("Closed", issue.getStatus().getName());

		try {
			client.advanceIssueWorkflow(issue, resolveOperation, "comment", null);
			fail("Expected JiraRemoteMessageException");
		} catch (JiraRemoteMessageException e) {
			assertThat(e.getMessage(), either(containsString(resolveMsg)).or(equalTo("Workflow Action Invalid")));
		}

		try {
			client.advanceIssueWorkflow(issue, closeOperation, "comment", null);
			fail("Expected JiraRemoteMessageException");
		} catch (JiraException e) {
			assertThat(
					e.getMessage(),
					either(
							containsString("It seems that you have tried to perform a workflow operation (Close Issue) that is not valid for the current state of this issue ")).or(
							equalTo("Workflow Action Invalid")));
		}

		String reopenOperation = JiraTestUtil.getOperation(client, issue.getKey(), "reopen");

		client.advanceIssueWorkflow(issue, reopenOperation, "comment", null);
		issue = client.getIssueByKey(issue.getKey(), null);
		assertEquals("Reopened", issue.getStatus().getName());
	}

	public void testGetIdFromKey() throws Exception {
		JiraIssue issue = JiraTestUtil.createIssue(client, "getIdFromKey");

		String key = client.getKeyFromId(issue.getId(), null);
		assertEquals(issue.getKey(), key);

		try {
			key = client.getKeyFromId("invalid", null);
			fail("Expected JiraException, got: " + key);
		} catch (JiraException e) {
		}

		try {
			key = client.getKeyFromId("1", null);
			fail("Expected JiraException, got: " + key);
		} catch (JiraException e) {
		}

	}

	public void testReassign() throws Exception {
		JiraIssue issue = JiraTestUtil.createIssue(client, "testReassign");

		issue.setAssignee("nonexistantuser");
		try {
			client.updateIssue(issue, "comment", null);
			fail("Expected JiraException");
		} catch (JiraRemoteMessageException e) {
			assertEquals("User 'nonexistantuser' cannot be assigned issues.", e.getHtmlMessage());
		}

		try {
			client.assignIssueTo(issue, JiraClient.ASSIGNEE_NONE, "", "", null);
			fail("Expected JiraException");
		} catch (JiraRemoteMessageException e) {
			assertEquals("Issues must be assigned.", e.getHtmlMessage());
		}

		try {
			client.assignIssueTo(issue, JiraClient.ASSIGNEE_SELF, "", "", null);
		} catch (JiraRemoteMessageException e) {
			assertEquals("Issue already assigned to Developer (" + client.getUserName() + ").", e.getHtmlMessage());
		}

		String guestUsername = TestUtil.readCredentials(PrivilegeLevel.GUEST).username;
		try {
			client.assignIssueTo(issue, JiraClient.ASSIGNEE_USER, guestUsername, "", null);
		} catch (JiraRemoteMessageException e) {
			assertEquals("User 'guest@mylyn.eclipse.org' cannot be assigned issues.", e.getHtmlMessage());
		}

		client.assignIssueTo(issue, JiraClient.ASSIGNEE_DEFAULT, "", "", null);
		issue = client.getIssueByKey(issue.getKey(), null);
		assertEquals("admin@mylyn.eclipse.org", issue.getAssignee());

		client.assignIssueTo(issue, JiraClient.ASSIGNEE_SELF, "", "", null);
		issue = client.getIssueByKey(issue.getKey(), null);
		assertEquals(client.getUserName(), issue.getAssignee());

		client = JiraFixture.current().client(PrivilegeLevel.GUEST);
		try {
			client.assignIssueTo(issue, JiraClient.ASSIGNEE_SELF, "", "", null);
			fail("Expected JiraException");
		} catch (JiraException e) {
		}
	}

	public void testFindIssues() throws Exception {
		FilterDefinition filter = new FilterDefinition();
		MockIssueCollector collector = new MockIssueCollector();
		client.search(filter, collector, null);
		assertTrue(collector.done);
	}

	public void testAddComment() throws Exception {
		JiraIssue issue = JiraTestUtil.createIssue(client, "testAddComment");

		client.addCommentToIssue(issue, "comment 1", null);
		issue = client.getIssueByKey(issue.getKey(), null);
		Comment comment = getComment(issue, "comment 1");
		assertNotNull(comment);
		assertEquals(client.getUserName(), comment.getAuthor());

		// test with other privileges
		client = JiraFixture.current().client(PrivilegeLevel.GUEST);

		client.addCommentToIssue(issue, "comment guest", null);
		issue = client.getIssueByKey(issue.getKey(), null);
		comment = getComment(issue, "comment guest");
		assertNotNull(comment);
		assertEquals(client.getUserName(), comment.getAuthor());
	}

	private Comment getComment(JiraIssue issue, String text) {
		for (Comment comment : issue.getComments()) {
			if (text.equals(comment.getComment())) {
				return comment;
			}
		}
		return null;
	}

	public void testAttachFile() throws Exception {
		File file = File.createTempFile("mylyn", null);
		file.deleteOnExit();

		JiraIssue issue = JiraTestUtil.createIssue(client, "testAttachFile");

		// test attaching an empty file
		try {
			client.addAttachment(issue, "", file.getName(), file, "application/binary", null);
			fail("Expected JiraException");
		} catch (JiraRemoteMessageException e) {
		}

		client.addAttachment(issue, "comment", "my.filename.1", new byte[] { 'M', 'y', 'l', 'y', 'n' },
				"application/binary", null);
		issue = client.getIssueByKey(issue.getKey(), null);
		Attachment attachment = getAttachment(issue, "my.filename.1");
		assertNotNull(attachment);
		assertEquals(client.getUserName(), attachment.getAuthor());
		assertEquals(5, attachment.getSize());
		assertNotNull(attachment.getCreated());

		// spaces in filename
		client.addAttachment(issue, "", "file name with spaces", new byte[] { '1' }, "text/html", null);
		issue = client.getIssueByKey(issue.getKey(), null);
		attachment = getAttachment(issue, "file name with spaces");
		assertNotNull(attachment);
		assertEquals(client.getUserName(), attachment.getAuthor());
		assertEquals(1, attachment.getSize());
		assertNotNull(attachment.getCreated());

		// non-USASCII in filename
		// upload is rejected by JIRA: bug 203663
//		client.attachFile(issue, "", "filename\u00C4\u00D6\u00DC", new byte[] { '1' }, "text/plain");
//		issue = client.getIssueByKey(issue.getKey());
//		attachment = JiraClient(issue, "filename\u00C4\u00D6\u00DC");
//		assertNotNull(attachment);
//		assertEquals(client.getUserName(), attachment.getAuthor());
//		assertEquals(1, attachment.getSize());
//		assertNotNull(attachment.getCreated());
	}

	private Attachment getAttachment(JiraIssue issue, String filename) {
		for (Attachment attachment : issue.getAttachments()) {
			if (filename.equals(attachment.getName())) {
				return attachment;
			}
		}
		return null;
	}

	public void testCreateIssue() throws Exception {
		JiraIssue issue = new JiraIssue();
		issue.setProject(client.getCache().getProjects()[0]);
		issue.setType(client.getCache().getIssueTypes()[0]);
		issue.setSummary("testCreateIssue");
		issue.setAssignee(client.getUserName());

		JiraIssue createdIssue = JiraTestUtil.createIssue(client, issue);
		assertEquals(issue.getProject(), createdIssue.getProject());
		assertEquals(issue.getType(), createdIssue.getType());
		assertEquals(issue.getSummary(), createdIssue.getSummary());
		assertEquals(issue.getAssignee(), createdIssue.getAssignee());
		assertEquals(client.getUserName(), createdIssue.getReporter());
		// TODO why are these null?
		// assertNotNull(issue.getCreated());
		// assertNotNull(issue.getUpdated());

		// change privilege level
		client = JiraFixture.current().client(PrivilegeLevel.GUEST);

		createdIssue = JiraTestUtil.createIssue(client, issue);
		assertEquals(issue.getProject(), createdIssue.getProject());
		assertEquals(issue.getType(), createdIssue.getType());
		assertEquals(issue.getSummary(), createdIssue.getSummary());
		assertEquals("admin@mylyn.eclipse.org", createdIssue.getAssignee());
		assertEquals(client.getUserName(), createdIssue.getReporter());
	}

	public void testCreateSubTask() throws Exception {
		JiraIssue issue = JiraTestUtil.newIssue(client, "testCreateSubTaskParent");
		JiraIssue parentIssue = JiraTestUtil.createIssue(client, issue);

		issue = JiraTestUtil.newSubTask(client, parentIssue, "testCreateSubTaskParent");
		JiraIssue childIssue = client.createSubTask(issue, null);
		assertEquals(parentIssue.getId(), childIssue.getParentId());

		parentIssue = client.getIssueByKey(parentIssue.getKey(), null);
		assertNotNull(parentIssue.getSubtasks());
		assertEquals(1, parentIssue.getSubtasks().length);
		assertEquals(childIssue.getId(), parentIssue.getSubtasks()[0].getIssueId());
	}

	public void testGetIssueLeadingSpaces() throws Exception {
		String summary = "  testCreateIssueLeadingSpaces";
		String description = "  leading spaces\n  more spaces";

		JiraIssue issue = JiraTestUtil.newIssue(client, summary);
		issue.setSummary(summary);
		issue.setDescription(description);
		issue.setAssignee(client.getUserName());

		issue = JiraTestUtil.createIssue(client, issue);
		issue = client.getIssueByKey(issue.getKey(), null);
		assertEquals(description, issue.getDescription());
		assertEquals(summary, issue.getSummary());

		issue.setDescription(issue.getDescription());
		client.updateIssue(issue, "", null);
		issue = client.getIssueByKey(issue.getKey(), null);
		assertEquals(description, issue.getDescription());
	}

	public void testUpdateIssue() throws Exception {
		Project project = JiraTestUtil.getProject(client, "EDITABLEREPORTER");

		JiraIssue issue = new JiraIssue();
		issue.setProject(project);
		issue.setType(client.getCache().getIssueTypes()[0]);
		issue.setSummary("testUpdateIssue");
		issue.setAssignee(client.getUserName());

		issue = JiraTestUtil.createIssue(client, issue);
		issue.setSummary("testUpdateIssueChanged");
		client.updateIssue(issue, "", null);
		issue = client.getIssueByKey(issue.getKey(), null);
		assertEquals("testUpdateIssueChanged", issue.getSummary());
		assertNotNull(issue.getUpdated());

		String operation = JiraTestUtil.getOperation(client, issue.getKey(), "custom");
		assertNotNull("Unable to find Custom workflow action", operation);

		// change privilege level
		client = JiraFixture.current().client(PrivilegeLevel.GUEST);
		try {
			client.updateIssue(issue, "", null);
			fail("Expected JiraException");
		} catch (JiraRemoteMessageException e) {
		}

		issue.setSummary("testUpdateIssueGuest");
		issue = JiraTestUtil.createIssue(client, issue);
		issue.setSummary("testUpdateIssueGuestChanged");
		try {
			client.updateIssue(issue, "", null);
			fail("Expected JiraException");
		} catch (JiraRemoteMessageException e) {
		}

		// change privilege level
		client = JiraFixture.current().client(PrivilegeLevel.USER);
		client.advanceIssueWorkflow(issue, operation, "custom action test", null);
	}

	public void testUpdateIssueNonAscii() throws Exception {
		String summary = "\u00C4\u00D6\u00DC\nnewline";
		String description = "\"&\n\u00A9\\ ',><br/>&nbsp; ";

		JiraIssue issue = JiraTestUtil.createIssue(client, summary);
		issue.setDescription(description);
		assertEquals(summary, issue.getSummary());

		client.updateIssue(issue, "comment: \u00C4\u00D6\u00DC", null);
		issue = client.getIssueByKey(issue.getKey(), null);
		assertEquals(summary, issue.getSummary());
		assertEquals(description, issue.getDescription());
		assertEquals(1, issue.getComments().length);
		assertEquals("comment: \u00C4\u00D6\u00DC", issue.getComments()[0].getComment());
	}

	public void testUpdateIssueMultipleLinesOfText() throws Exception {
		String summary = "line1\nline2";
		String description = "\nline2\n\nline4\n";

		JiraIssue issue = JiraTestUtil.createIssue(client, summary);
		issue.setDescription(description);
		assertEquals(summary, issue.getSummary());

		client.updateIssue(issue, "", null);
		issue = client.getIssueByKey(issue.getKey(), null);
		assertEquals(summary, issue.getSummary());
		assertEquals(description, issue.getDescription());
	}

	public void testUpdateIssueWithLinkInDescription() throws Exception {
		String summary = "updateIssueWithLinkInDescriptoin";
		String description = "Link:\n\nhttp://mylyn.eclipse.org/";

		JiraIssue issue = JiraTestUtil.createIssue(client, summary);
		issue.setDescription(description);
		assertEquals(summary, issue.getSummary());

		client.updateIssue(issue, "", null);
		issue = client.getIssueByKey(issue.getKey(), null);
		assertEquals(description, issue.getDescription());
	}

	public void testUpdateIssueHtmlTag() throws Exception {
		String summary = "<b>bold</b>";
		String description = "<head>123\n<pre>line1\nline2\n\nline4</pre>  &nbsp;&lt;&gt; ";

		JiraIssue issue = JiraTestUtil.createIssue(client, summary);
		issue.setDescription(description);
		assertEquals(summary, issue.getSummary());

		client.updateIssue(issue, "", null);
		issue = client.getIssueByKey(issue.getKey(), null);
		assertEquals(summary, issue.getSummary());
		assertEquals(description, issue.getDescription());
	}

	public void testWatchUnwatchIssue() throws Exception {
		JiraIssue issue = JiraTestUtil.createIssue(client, "testWatchUnwatch");

		assertFalse(issue.isWatched());
		client.watchIssue(issue, null);
		issue = client.getIssueByKey(issue.getKey(), null);
		// flag is never set
//		assertTrue(issue.isWatched());

		client.unwatchIssue(issue, null);
		issue = client.getIssueByKey(issue.getKey(), null);
		assertFalse(issue.isWatched());
		client.unwatchIssue(issue, null);
		issue = client.getIssueByKey(issue.getKey(), null);
		assertFalse(issue.isWatched());
	}

	public void testVoteUnvoteIssue() throws Exception {
		JiraIssue issue = JiraTestUtil.createIssue(client, "testVoteUnvote");

		assertEquals(0, issue.getVotes());
		assertFalse(issue.canUserVote(client.getUserName()));

		client = JiraFixture.current().client(PrivilegeLevel.GUEST);
		client.voteIssue(issue, null);
		issue = client.getIssueByKey(issue.getKey(), null);
		assertEquals(1, issue.getVotes());
		client.unvoteIssue(issue, null);
		issue = client.getIssueByKey(issue.getKey(), null);
		assertEquals(0, issue.getVotes());
	}

	public void testBasicAuth() throws Exception {
		basicAuth(JiraFixture.ENTERPRISE_3_13_BASIC_AUTH.getRepositoryUrl());
	}

	private void basicAuth(String url) throws Exception {
		Credentials credentials = TestUtil.readCredentials(PrivilegeLevel.GUEST);
		Credentials httpCredentials = TestUtil.readCredentials(PrivilegeLevel.USER);
		WebLocation location = new WebLocation(url, credentials.username, credentials.password);
		location.setCredentials(AuthenticationType.HTTP, httpCredentials.username, httpCredentials.password);
		client = new JiraClient(location);
		assertNotNull(client.getCache().getServerInfo(null));

		client = new JiraClient(new WebLocation(url, credentials.username, credentials.password));
		try {
			assertNotNull(client.getCache().getServerInfo(null));
			fail("Expected JiraServiceUnavailableException");
		} catch (JiraServiceUnavailableException expected) {
		}
	}

	public void testCharacterEncoding() throws Exception {
		assertEquals("ISO-8859-1", client.getCharacterEncoding(new NullProgressMonitor()));
		client.getConfiguration().setCharacterEncoding("UTF-8");
		assertEquals("UTF-8", client.getCharacterEncoding(new NullProgressMonitor()));
	}

	public void testGetServerInfo() throws Exception {
		ServerInfo serverInfo = client.getCache().getServerInfo(null);
		assertEquals(JiraFixture.current().getVersion(), serverInfo.getVersion());
		assertEquals(JiraFixture.current().getBuildNumber(), serverInfo.getBuildNumber());
		assertEquals("ISO-8859-1", serverInfo.getCharacterEncoding());
		//assertEquals(JiraFixture.current().getRepositoryUrl(), serverInfo.getBaseUrl());
	}

	public void testGetEditableFields() throws Exception {
		JiraIssue issue = JiraTestUtil.createIssue(client, "getEditableFields");

		IssueField[] fields = client.getEditableAttributes(issue.getKey(), null);
		Set<String> ids = new HashSet<String>();
		for (IssueField field : fields) {
			ids.add(field.getId());
		}
		assertFalse(ids.isEmpty());
		assertTrue("Missing 'versions': " + ids, ids.contains("versions"));
		assertTrue("Missing 'fixVersions': " + ids, ids.contains("fixVersions"));
	}

	public void testGetWorkLogs() throws Exception {
		JiraIssue issue = JiraTestUtil.createIssue(client, "getWorklogs");

		JiraWorkLog[] logs = client.getWorklogs(issue.getKey(), null);
		assertEquals(0, logs.length);

		JiraWorkLog log = new JiraWorkLog();
		log.setTimeSpent(5287);
		log.setStartDate(new Date());
		client.addWorkLog(issue.getKey(), log, null);

		logs = client.getWorklogs(issue.getKey(), null);
		assertEquals(1, logs.length);
		assertEquals(5280, logs[0].getTimeSpent());
	}

	/**
	 * Tests soap retrieval of the SecurityLevels
	 */
	public void testAvailableGetSecurityLevels() throws Exception {
		JiraIssue issue = JiraTestUtil.newIssue(client, "testAvailableGetSecurityLevels");
		issue.setProject(client.getCache().getProjectByKey(("SECURITY")));
		issue = JiraTestUtil.createIssue(client, issue);

		SecurityLevel[] securityLevels = client.getAvailableSecurityLevels(issue.getProject().getKey(),
				new NullProgressMonitor());
		assertNotNull(securityLevels);

		assertEquals(3, securityLevels.length);
		assertEquals("Developers", securityLevels[0].getName());
		assertEquals("10000", securityLevels[0].getId());
		assertEquals("Reporter", securityLevels[1].getName());
		assertEquals("10010", securityLevels[1].getId());
		assertEquals("Users", securityLevels[2].getName());
		assertEquals("10001", securityLevels[2].getId());
	}

	public void testAddWorkLog() throws Exception {
		JiraIssue issue = JiraTestUtil.createIssue(client, "getWorklogs");
		issue.setEstimate(1200);
		client.updateIssue(issue, "", null);
		issue = client.getIssueByKey(issue.getKey(), null);

		assertEquals(1200, issue.getEstimate());

		JiraWorkLog worklog1 = new JiraWorkLog();
		worklog1.setAutoAdjustEstimate(false);
		worklog1.setComment("comment");
		Date time = new GregorianCalendar().getTime();
		worklog1.setStartDate(time);
		worklog1.setTimeSpent(120);

		JiraWorkLog receivedWorklog = client.addWorkLog(issue.getKey(), worklog1, null);
		client.updateIssue(issue, "", null);
		issue = client.getIssueByKey(issue.getKey(), null);

		assertEquals(1200, issue.getEstimate());
		assertEquals("comment", receivedWorklog.getComment());
		assertEquals(120, receivedWorklog.getTimeSpent());
		assertEquals(time, receivedWorklog.getStartDate());

		worklog1 = new JiraWorkLog();
		worklog1.setAutoAdjustEstimate(true);
		worklog1.setComment("comment2");
		time = new GregorianCalendar().getTime();
		worklog1.setStartDate(time);
		worklog1.setTimeSpent(240);

		receivedWorklog = client.addWorkLog(issue.getKey(), worklog1, null);
		issue = client.getIssueByKey(issue.getKey(), null);

		assertEquals(1200 - 240, issue.getEstimate());
		assertEquals("comment2", receivedWorklog.getComment());
		assertEquals(240, receivedWorklog.getTimeSpent());
		assertEquals(time, receivedWorklog.getStartDate());
	}

	// FIXME re-enable test
//	public void testProjectSecurityLevelNotAccessible() throws Exception {
//		init(JiraTestConstants.JIRA_LATEST_URL, PrivilegeLevel.GUEST);
//		Project project = client.getCache().getProjectById("10050");
//		assertNull(project.getSecurityLevels());
//	}

	public void testProjectSecurityLevelAccessible() throws Exception {
		Project project = client.getCache().getProjectById("10050");
		assertNotNull(project.getSecurityLevels());
	}
}
