/*******************************************************************************
 * Copyright (c) 2004, 2008 Tasktop Technologies and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Tasktop Technologies - initial API and implementation
 *     Eugene Kuleshov - improvements
 *******************************************************************************/

package org.eclipse.mylyn.jira.tests.client;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

import junit.framework.TestCase;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.mylyn.commons.net.AuthenticationType;
import org.eclipse.mylyn.commons.net.WebLocation;
import org.eclipse.mylyn.context.tests.support.TestUtil;
import org.eclipse.mylyn.context.tests.support.TestUtil.Credentials;
import org.eclipse.mylyn.context.tests.support.TestUtil.PrivilegeLevel;
import org.eclipse.mylyn.internal.jira.core.model.Attachment;
import org.eclipse.mylyn.internal.jira.core.model.Comment;
import org.eclipse.mylyn.internal.jira.core.model.IssueField;
import org.eclipse.mylyn.internal.jira.core.model.JiraIssue;
import org.eclipse.mylyn.internal.jira.core.model.JiraWorkLog;
import org.eclipse.mylyn.internal.jira.core.model.Project;
import org.eclipse.mylyn.internal.jira.core.model.Resolution;
import org.eclipse.mylyn.internal.jira.core.model.ServerInfo;
import org.eclipse.mylyn.internal.jira.core.model.Version;
import org.eclipse.mylyn.internal.jira.core.model.filter.FilterDefinition;
import org.eclipse.mylyn.internal.jira.core.service.JiraClient;
import org.eclipse.mylyn.internal.jira.core.service.JiraException;
import org.eclipse.mylyn.internal.jira.core.service.JiraRemoteMessageException;
import org.eclipse.mylyn.internal.jira.core.service.JiraServiceUnavailableException;
import org.eclipse.mylyn.jira.tests.util.JiraTestConstants;
import org.eclipse.mylyn.jira.tests.util.JiraTestUtil;
import org.eclipse.mylyn.jira.tests.util.MockIssueCollector;

/**
 * @author Steffen Pingel
 * @author Eugene Kuleshov
 */
public class JiraClientTest extends TestCase {

	private JiraClient client;

	@Override
	protected void tearDown() throws Exception {
		JiraTestUtil.tearDown();
	}

	protected void init(String url, PrivilegeLevel level) throws Exception {
		Credentials credentials = TestUtil.readCredentials(level);
		client = new JiraClient(new WebLocation(url, credentials.username, credentials.password));

		JiraTestUtil.refreshDetails(client);
	}

	public void testLogin381() throws Exception {
		login(JiraTestConstants.JIRA_LATEST_URL);
	}

	private void login(String url) throws Exception {
		init(url, PrivilegeLevel.USER);

		client.login(null);
		client.logout(null);
		// should automatically login
		client.getCache().refreshDetails(new NullProgressMonitor());

		init(url, PrivilegeLevel.GUEST);
		client.login(null);
	}

	public void testStartStopIssue() throws Exception {
		startStopIssue(JiraTestConstants.JIRA_LATEST_URL);
	}

	private void startStopIssue(String url) throws Exception {
		init(url, PrivilegeLevel.USER);

		JiraIssue issue = JiraTestUtil.createIssue(client, "testStartStopIssue");

		String startOperation = JiraTestUtil.getOperation(client, issue.getKey(), "start");

		client.advanceIssueWorkflow(issue, startOperation, null, null);
		try {
			client.advanceIssueWorkflow(issue, startOperation, null, null);
			fail("Expected JiraRemoteMessageException");
		} catch (JiraRemoteMessageException e) {
			assertEquals("Workflow Action Invalid", e.getMessage());
		}

		String stopOperation = JiraTestUtil.getOperation(client, issue.getKey(), "stop");

		client.advanceIssueWorkflow(issue, stopOperation, null, null);
		try {
			client.advanceIssueWorkflow(issue, stopOperation, null, null);
			fail("Expected JiraRemoteMessageException");
		} catch (JiraException e) {
			assertEquals("Workflow Action Invalid", e.getMessage());
		}
		client.advanceIssueWorkflow(issue, startOperation, null, null);
	}

	public void testResolveCloseReopenIssue() throws Exception {
		resolveCloseReopenIssue(JiraTestConstants.JIRA_LATEST_URL);
	}

	private void resolveCloseReopenIssue(String url) throws Exception {
		init(url, PrivilegeLevel.USER);

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
			assertEquals("Workflow Action Invalid", e.getMessage());
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
			assertEquals("Workflow Action Invalid", e.getMessage());
		}

		try {
			client.advanceIssueWorkflow(issue, closeOperation, "comment", null);
			fail("Expected JiraRemoteMessageException");
		} catch (JiraException e) {
			assertEquals("Workflow Action Invalid", e.getMessage());
		}

		String reopenOperation = JiraTestUtil.getOperation(client, issue.getKey(), "reopen");

		client.advanceIssueWorkflow(issue, reopenOperation, "comment", null);
		issue = client.getIssueByKey(issue.getKey(), null);
		assertEquals("Reopened", issue.getStatus().getName());
	}

	public void testGetIdFromKey() throws Exception {
		getIdFromKey(JiraTestConstants.JIRA_LATEST_URL);
	}

	private void getIdFromKey(String url) throws Exception {
		init(url, PrivilegeLevel.GUEST);

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
		reassign(JiraTestConstants.JIRA_LATEST_URL);
	}

	private void reassign(String url) throws Exception {
		init(url, PrivilegeLevel.USER);

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

		init(url, PrivilegeLevel.GUEST);
		try {
			client.assignIssueTo(issue, JiraClient.ASSIGNEE_SELF, "", "", null);
			fail("Expected JiraException");
		} catch (JiraException e) {
		}
	}

	public void testFindIssues() throws Exception {
		findIssues(JiraTestConstants.JIRA_LATEST_URL);
	}

	private void findIssues(String url) throws Exception {
		init(url, PrivilegeLevel.USER);

		FilterDefinition filter = new FilterDefinition();
		MockIssueCollector collector = new MockIssueCollector();
		client.search(filter, collector, null);
		assertTrue(collector.done);
	}

	public void testAddComment() throws Exception {
		addComment(JiraTestConstants.JIRA_LATEST_URL);
	}

	private void addComment(String url) throws Exception {
		init(url, PrivilegeLevel.USER);

		JiraIssue issue = JiraTestUtil.createIssue(client, "testAddComment");

		client.addCommentToIssue(issue, "comment 1", null);
		issue = client.getIssueByKey(issue.getKey(), null);
		Comment comment = getComment(issue, "comment 1");
		assertNotNull(comment);
		assertEquals(client.getUserName(), comment.getAuthor());

		init(url, PrivilegeLevel.GUEST);

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
		attachFile(JiraTestConstants.JIRA_LATEST_URL);
	}

	private void attachFile(String url) throws Exception {
		init(url, PrivilegeLevel.USER);

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
		createIssue(JiraTestConstants.JIRA_LATEST_URL);
	}

	private void createIssue(String url) throws Exception {
		init(url, PrivilegeLevel.USER);

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

		init(url, PrivilegeLevel.GUEST);

		createdIssue = JiraTestUtil.createIssue(client, issue);
		assertEquals(issue.getProject(), createdIssue.getProject());
		assertEquals(issue.getType(), createdIssue.getType());
		assertEquals(issue.getSummary(), createdIssue.getSummary());
		assertEquals("admin@mylyn.eclipse.org", createdIssue.getAssignee());
		assertEquals(client.getUserName(), createdIssue.getReporter());
	}

	public void testCreateSubTask() throws Exception {
		createSubTask(JiraTestConstants.JIRA_LATEST_URL);
	}

	private void createSubTask(String url) throws Exception {
		init(url, PrivilegeLevel.USER);

		JiraIssue issue = new JiraIssue();
		issue.setProject(client.getCache().getProjects()[0]);
		issue.setType(client.getCache().getIssueTypes()[0]);
		issue.setSummary("testCreateSubTaskParent");

		JiraIssue parentIssue = JiraTestUtil.createIssue(client, issue);

		issue = new JiraIssue();
		issue.setProject(client.getCache().getProjects()[0]);
		issue.setType(client.getCache().getIssueTypes()[1]);
		issue.setParentId(parentIssue.getId());
		issue.setSummary("testCreateSubTaskChild");

		JiraIssue childIssue = client.createSubTask(issue, null);
		assertEquals(parentIssue.getId(), childIssue.getParentId());

		parentIssue = client.getIssueByKey(parentIssue.getKey(), null);
		assertNotNull(parentIssue.getSubtasks());
		assertEquals(1, parentIssue.getSubtasks().length);
		assertEquals(childIssue.getId(), parentIssue.getSubtasks()[0].getIssueId());
	}

	public void testGetIssueLeadingSpaces() throws Exception {
		getIssueLeadingSpaces(JiraTestConstants.JIRA_LATEST_URL);
	}

	private void getIssueLeadingSpaces(String url) throws Exception {
		init(url, PrivilegeLevel.USER);

		String summary = "  testCreateIssueLeadingSpaces";
		String description = "  leading spaces\n  more spaces";

		JiraIssue issue = new JiraIssue();
		issue.setProject(client.getCache().getProjects()[0]);
		issue.setType(client.getCache().getIssueTypes()[0]);
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
		updateIssue(JiraTestConstants.JIRA_LATEST_URL, "CUSTOMFIELDS");
	}

	public void testUpdateIssueCustomOperation() throws Exception {
		JiraIssue issue = updateIssue(JiraTestConstants.JIRA_LATEST_URL, "EDITABLEREPORTER");

		String operation = JiraTestUtil.getOperation(client, issue.getKey(), "custom");
		assertNotNull("Unable to find Custom workflow action", operation);

		init(JiraTestConstants.JIRA_LATEST_URL, PrivilegeLevel.USER);
		client.advanceIssueWorkflow(issue, operation, "custom action test", null);
	}

	private JiraIssue updateIssue(String url, String projectKey) throws Exception {
		init(url, PrivilegeLevel.USER);

		Project project = JiraTestUtil.getProject(client, projectKey);

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

		init(url, PrivilegeLevel.GUEST);
		try {
			client.updateIssue(issue, "", null);
			fail("Expected JiraException");
		} catch (JiraRemoteMessageException e) {
		}

		init(url, PrivilegeLevel.GUEST);

		issue.setSummary("testUpdateIssueGuest");
		issue = JiraTestUtil.createIssue(client, issue);
		issue.setSummary("testUpdateIssueGuestChanged");
		try {
			client.updateIssue(issue, "", null);
			fail("Expected JiraException");
		} catch (JiraRemoteMessageException e) {
		}

		return issue;
	}

	public void testUpdateIssueNonAscii() throws Exception {
		updateIssueNonAscii(JiraTestConstants.JIRA_LATEST_URL);
	}

	private void updateIssueNonAscii(String url) throws Exception {
		init(url, PrivilegeLevel.USER);

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
		updateIssueMultipleLinesOfText(JiraTestConstants.JIRA_LATEST_URL);
	}

	private void updateIssueMultipleLinesOfText(String url) throws Exception {
		init(url, PrivilegeLevel.USER);

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
		updateIssueWithLinkInDescriptoin(JiraTestConstants.JIRA_LATEST_URL);
	}

	private void updateIssueWithLinkInDescriptoin(String url) throws Exception {
		init(url, PrivilegeLevel.USER);

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
		updateIssueHtmlTags(JiraTestConstants.JIRA_LATEST_URL);
	}

	private void updateIssueHtmlTags(String url) throws Exception {
		init(url, PrivilegeLevel.USER);

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
		watchUnwatchIssue(JiraTestConstants.JIRA_LATEST_URL);
	}

	private void watchUnwatchIssue(String url) throws Exception {
		init(url, PrivilegeLevel.USER);

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

	public void testBasicAuth() throws Exception {
		basicAuth(JiraTestConstants.JIRA_39_BASIC_AUTH_URL);
	}

	private void basicAuth(String url) throws Exception {
		Credentials credentials = TestUtil.readCredentials(PrivilegeLevel.GUEST);
		Credentials httpCredentials = TestUtil.readCredentials(PrivilegeLevel.USER);
		WebLocation location = new WebLocation(url, credentials.username, credentials.password);
		location.setCredentials(AuthenticationType.HTTP, httpCredentials.username, httpCredentials.password);
		client = new JiraClient(location);
		assertNotNull(client.getCache().getServerInfo());

		client = new JiraClient(new WebLocation(url, credentials.username, credentials.password));
		try {
			assertNotNull(client.getCache().getServerInfo());
			fail("Expected JiraServiceUnavailableException");
		} catch (JiraServiceUnavailableException expected) {
		}
	}

	public void testCharacterEncoding() throws Exception {
		characterEncoding(JiraTestConstants.JIRA_LATEST_URL);
	}

	private void characterEncoding(String url) throws Exception {
		init(url, PrivilegeLevel.USER);
		assertEquals("ISO-8859-1", client.getCharacterEncoding());
		client.getConfiguration().setCharacterEncoding("UTF-8");
		assertEquals("UTF-8", client.getCharacterEncoding());
	}

//	public void testGetServerInfo3_9() throws Exception {
//		getServerInfo(JiraTestConstants.JIRA_39_URL, "3.9", "233");
//	}

	public void testGetServerInfo3_13_1() throws Exception {
		getServerInfo(JiraTestConstants.JIRA_3_13_1_URL, "3.13.1", "333");
	}

	private void getServerInfo(String url, String version, String buildNumber) throws Exception {
		init(url, PrivilegeLevel.USER);
		ServerInfo serverInfo = client.getCache().getServerInfo();
		assertEquals(version, serverInfo.getVersion());
		assertEquals(buildNumber, serverInfo.getBuildNumber());
		assertEquals("ISO-8859-1", serverInfo.getCharacterEncoding());
		assertEquals(url, serverInfo.getBaseUrl());
	}

	public void testGetEditableFields() throws Exception {
		getEditableFields(JiraTestConstants.JIRA_LATEST_URL);
	}

	private void getEditableFields(String url) throws Exception {
		init(url, PrivilegeLevel.USER);

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

	public void testGetWorklogs() throws Exception {
		getWorklogs(JiraTestConstants.JIRA_LATEST_URL);
	}

	private void getWorklogs(String url) throws Exception {
		init(url, PrivilegeLevel.USER);

		JiraIssue issue = JiraTestUtil.createIssue(client, "getWorklogs");

		JiraWorkLog[] logs = client.getWorklogs(issue.getKey(), null);
		assertEquals(0, logs.length);
//		JiraWorkLog log = new JiraWorkLog();
	}

}
