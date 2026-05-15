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

package me.glindholm.connector.eclipse.jira.tests.client;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.either;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.GregorianCalendar;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.mylyn.commons.net.AuthenticationType;
import org.eclipse.mylyn.commons.net.WebLocation;
import org.eclipse.mylyn.commons.sdk.util.CommonTestUtil;
import org.eclipse.mylyn.commons.sdk.util.CommonTestUtil.PrivilegeLevel;
import org.eclipse.osgi.util.NLS;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import me.glindholm.connector.eclipse.internal.jira.core.model.JiraAttachment;
import me.glindholm.connector.eclipse.internal.jira.core.model.JiraComment;
import me.glindholm.connector.eclipse.internal.jira.core.model.JiraIssue;
import me.glindholm.connector.eclipse.internal.jira.core.model.JiraIssueField;
import me.glindholm.connector.eclipse.internal.jira.core.model.JiraServerVersion;
import me.glindholm.connector.eclipse.internal.jira.core.model.JiraVersion;
import me.glindholm.connector.eclipse.internal.jira.core.model.JiraWorkLog;
import me.glindholm.connector.eclipse.internal.jira.core.model.JiraWorkLog.AdjustEstimateMethod;
import me.glindholm.connector.eclipse.internal.jira.core.model.filter.FilterDefinition;
import me.glindholm.connector.eclipse.internal.jira.core.service.JiraAuthenticationException;
import me.glindholm.connector.eclipse.internal.jira.core.service.JiraClient;
import me.glindholm.connector.eclipse.internal.jira.core.service.JiraException;
import me.glindholm.connector.eclipse.internal.jira.core.service.JiraRemoteMessageException;
import me.glindholm.connector.eclipse.jira.tests.util.JiraFixture;
import me.glindholm.connector.eclipse.jira.tests.util.JiraTestUtil;
import me.glindholm.connector.eclipse.jira.tests.util.MockIssueCollector;
import me.glindholm.jira.rest.client.api.domain.BasicUser;

/**
 * @author Steffen Pingel
 * @author Eugene Kuleshov
 * @author Thomas Ehrnhoefer
 */
public class JiraClientTest  {

	private final static String MESSAGE = "It seems that you have tried to perform a workflow operation ({0}) "
			+ "that is not valid for the current state of this issue ({1}). "
			+ "The likely cause is that somebody has changed the issue recently, please look at the issue history for details.";

	private JiraClient client;

	@BeforeEach
	protected void setUp() throws Exception {
		client = JiraFixture.current().client();
	}

	@AfterEach
	protected void tearDown() throws Exception {
		JiraTestUtil.tearDown();
	}

	//	public void testLogin() throws Exception {
	//		client.login(null);
	//		client.logout(null);
	//		// should automatically login
	//		client.getCache().refreshDetails(new NullProgressMonitor());
	//
	//		client = JiraFixture.current().client(PrivilegeLevel.GUEST);
	//		client.login(null);
	//	}

	@Test
	public void testStartStopIssue() throws Exception {
		final var issue = JiraTestUtil.createIssue(client, "testStartStopIssue");

		final var startOperation = JiraTestUtil.getOperation(client, issue.getKey(), "start");

		client.advanceIssueWorkflow(issue, startOperation, null, null);
		JiraException e  = assertThrows(JiraRemoteMessageException.class, () ->    client.advanceIssueWorkflow(issue, startOperation, null, null));
		assertTrue(e.getMessage().contains(NLS.bind(MESSAGE, "Start Progress", issue.getKey())));
		//			assertThat(e.getMessage(), containsString());

		final var stopOperation = JiraTestUtil.getOperation(client, issue.getKey(), "stop");

		client.advanceIssueWorkflow(issue, stopOperation, null, null);
		e = assertThrows(JiraRemoteMessageException.class, () -> client.advanceIssueWorkflow(issue, stopOperation, null, null));
		assertTrue(e.getMessage().contains(NLS.bind(MESSAGE, "Stop Progress", issue.getKey())));
		//			assertThat(e.getMessage(), containsString("Action 301 is invalid"));
		client.advanceIssueWorkflow(issue, startOperation, null, null);
	}

	@Test
	public void testResolveCloseReopenIssue() throws Exception {

		final var resolution = JiraTestUtil.getFixedResolution(client);
		var issue = JiraTestUtil.createIssue(client, "testStartStopIssue");

		issue.setResolution(resolution);
		issue.setFixVersions(new JiraVersion[0]);

		final var resolveOperation = JiraTestUtil.getOperation(client, issue.getKey(), "resolve");

		client.advanceIssueWorkflow(issue, resolveOperation, "comment", null);

		issue = client.getIssueByKey(issue.getKey(), null);
		assertEquals("Resolved", issue.getStatus().getName());

		try {
			client.advanceIssueWorkflow(issue, resolveOperation, "comment", null);
			fail("Expected JiraRemoteMessageException");
		} catch (final JiraException e) {
			assertTrue(e.getMessage().contains(NLS.bind(MESSAGE, "Resolve Issue", issue.getKey())));
			//			assertThat(e.getMessage(), containsString("Action 5 is invalid"));
		}

		// have to get "close" operation after resolving issue
		final var closeOperation = JiraTestUtil.getOperation(client, issue.getKey(), "close");

		client.advanceIssueWorkflow(issue, closeOperation, "comment", null);
		issue = client.getIssueByKey(issue.getKey(), null);
		assertEquals("Closed", issue.getStatus().getName());

		try {
			client.advanceIssueWorkflow(issue, resolveOperation, "comment", null);
			fail("Expected JiraRemoteMessageException");
		} catch (final JiraException e) {
			assertTrue(e.getMessage().contains(NLS.bind(MESSAGE, "Resolve Issue", issue.getKey())));
			//			assertThat(e.getMessage(), containsString("Action 5 is invalid"));
		}

		try {
			client.advanceIssueWorkflow(issue, closeOperation, "comment", null);
			fail("Expected JiraRemoteMessageException");
		} catch (final JiraException e) {
			assertTrue(e.getMessage().contains(NLS.bind(MESSAGE, "Close Issue", issue.getKey())));
			//			assertThat(e.getMessage(), containsString("Action 701 is invalid"));
		}

		final var reopenOperation = JiraTestUtil.getOperation(client, issue.getKey(), "reopen");

		client.advanceIssueWorkflow(issue, reopenOperation, "comment", null);
		issue = client.getIssueByKey(issue.getKey(), null);
		assertEquals("Reopened", issue.getStatus().getName());
	}

	//	public void testGetIdFromKey() throws Exception {
	//		JiraIssue issue = JiraTestUtil.createIssue(client, "getIdFromKey");
	//
	//		String key = client.getKeyFromId(issue.getId(), null);
	//		assertEquals(issue.getKey(), key);
	//
	//		try {
	//			key = client.getKeyFromId("invalid", null);
	//			fail("Expected JiraException, got: " + key);
	//		} catch (JiraException e) {
	//		}
	//
	//		try {
	//			key = client.getKeyFromId("1", null);
	//			fail("Expected JiraException, got: " + key);
	//		} catch (JiraException e) {
	//		}
	//
	//	}

	@Test
	public void testReassign() throws Exception {
		final var issue = JiraTestUtil.createIssue(client, "testReassign");

		issue.setAssignee(createUser("nonexistantuser"));
		try {
			client.updateIssue(issue, "comment", null, null);
			fail("Expected JiraException");
		} catch (final JiraException e) {
			assertThat(
					e.getMessage(),
					either(containsString("User 'nonexistantuser' cannot be assigned issues.")).or(
							equalTo("User &#39;nonexistantuser&#39; cannot be assigned issues.")).or(
									containsString("User 'nonexistantuser' does not exist.")));
		}

		try {
			client.assignIssueTo(issue, "", "", null);
			fail("Expected JiraException");
		} catch (final JiraRemoteMessageException e) {
			assertThat(e.getHtmlMessage(), containsString("Issues must be assigned."));
		} catch (final JiraException e) {
			assertThat(e.getMessage(), containsString("Issues must be assigned."));
		}

		try {
			client.assignIssueTo(issue, client.getUserName(), "", null);
		} catch (final JiraRemoteMessageException e) {
			assertEquals("Issue already assigned to Developer (" + client.getUserName() + ").", e.getHtmlMessage());
		} catch (final JiraException e) {
			assertEquals("Issue already assigned to (" + client.getUserName() + ").", e.getMessage());
		}

		final var guestUsername = CommonTestUtil.getCredentials(PrivilegeLevel.GUEST).getUserName();
		try {
			client.assignIssueTo(issue, guestUsername, "", null);
		} catch (final JiraRemoteMessageException e) {
			assertThat(
					e.getHtmlMessage(),
					either(containsString("User 'guest@mylyn.eclipse.org' cannot be assigned issues.")).or(
							equalTo("User &#39;guest@mylyn.eclipse.org&#39; cannot be assigned issues.")));
		} catch (final JiraException e) {
			assertThat(
					e.getMessage(),
					either(containsString("User 'guest@mylyn.eclipse.org' cannot be assigned issues.")).or(
							equalTo("User &#39;guest@mylyn.eclipse.org&#39; cannot be assigned issues.")));
		}

		client.assignIssueTo(issue, "admin@mylyn.eclipse.org", "", null);
		final var  issue2 = client.getIssueByKey(issue.getKey(), null);
		assertEquals("admin@mylyn.eclipse.org", issue2.getAssignee());

		client.assignIssueTo(issue2, client.getUserName(), "", null);
		final var issue3 = client.getIssueByKey(issue2.getKey(), null);
		assertEquals(client.getUserName(), issue3.getAssignee());

		client = JiraFixture.current().client(PrivilegeLevel.GUEST);
		assertThrows(JiraException.class, () ->
		client.assignIssueTo(issue3, issue3.getAssignee().getAccountId(), "", null));
	}

	@Test
	public void testFindIssues() throws Exception {
		final var filter = new FilterDefinition();
		final var collector = new MockIssueCollector();
		client.search(filter, collector, null);
		assertTrue(collector.done);
	}

	@Test
	public void testAddComment() throws Exception {
		var issue = JiraTestUtil.createIssue(client, "testAddComment");

		client.addCommentToIssue(issue.getKey(), "comment 1", null);
		issue = client.getIssueByKey(issue.getKey(), null);
		var comment = getComment(issue, "comment 1");
		assertNotNull(comment);
		assertEquals(client.getUserName(), comment.getAuthor());

		// test with other privileges
		client = JiraFixture.current().client(PrivilegeLevel.GUEST);

		client.addCommentToIssue(issue.getKey(), "comment guest", null);
		issue = client.getIssueByKey(issue.getKey(), null);
		comment = getComment(issue, "comment guest");
		assertNotNull(comment);
		assertEquals(client.getUserName(), comment.getAuthor());
	}

	private JiraComment getComment(final JiraIssue issue, final String text) {
		for (final JiraComment comment : issue.getComments()) {
			if (text.equals(comment.getComment())) {
				return comment;
			}
		}
		return null;
	}

	@Test
	public void testAttachFile() throws Exception {
		var issue = JiraTestUtil.createIssue(client, "testAttachFile");

		// test attaching an empty file
		try {
			client.addAttachment(issue, "", "testAttachEmptyFile.txt", new byte[0], null);
			fail("Expected JiraException");
		} catch (final JiraException e) {
			assertTrue(e.getMessage().contains("Cannot attach empty file"));
		}

		client.addAttachment(issue, "comment", "my.filename.1", new byte[] { 'M', 'y', 'l', 'y', 'n' }, null);
		issue = client.getIssueByKey(issue.getKey(), null);
		var attachment = getAttachment(issue, "my.filename.1");
		assertNotNull(attachment);
		assertEquals(client.getUserName(), attachment.getAuthor());
		assertEquals(5, attachment.getSize());
		assertNotNull(attachment.getCreated());

		// spaces in filename
		client.addAttachment(issue, "", "file name with spaces", new byte[] { '1' }, null);
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

	private JiraAttachment getAttachment(final JiraIssue issue, final String filename) {
		for (final JiraAttachment attachment : issue.getAttachments()) {
			if (filename.equals(attachment.getName())) {
				return attachment;
			}
		}
		return null;
	}

	@Test
	public void testCreateIssue() throws Exception {
		final var issue = new JiraIssue();
		issue.setProject(client.getCache().getProjects()[0]);
		issue.setType(client.getCache().getIssueTypes()[0]);
		issue.setSummary("testCreateIssue");
		issue.setAssignee(createUser(client.getUserName()));
		issue.setPriority(client.getCache().getPriorities()[0]);

		var createdIssue = JiraTestUtil.createIssue(client, issue);
		assertEquals(issue.getProject(), createdIssue.getProject());
		assertEquals(issue.getType(), createdIssue.getType());
		assertEquals(issue.getSummary(), createdIssue.getSummary());
		assertEquals(issue.getAssignee(), createdIssue.getAssignee());
		assertEquals(client.getUserName(), createdIssue.getReporter());
		assertEquals(issue.getPriority(), createdIssue.getPriority());
		// TODO why are these null?
		// assertNotNull(issue.getCreated());
		// assertNotNull(issue.getUpdated());

		// change privilege level
		client = JiraFixture.current().client(PrivilegeLevel.GUEST);
		issue.setAssignee(null);
		issue.setFixVersions(null);

		createdIssue = JiraTestUtil.createIssue(client, issue);
		assertEquals(issue.getProject(), createdIssue.getProject());
		assertEquals(issue.getType(), createdIssue.getType());
		assertEquals(issue.getSummary(), createdIssue.getSummary());
		assertEquals("admin@mylyn.eclipse.org", createdIssue.getAssignee());
		assertEquals(client.getUserName(), createdIssue.getReporter());
	}

	private static BasicUser  createUser(final String name) {
		return new BasicUser(null, name, name);
	}

	@Test
	public void testCreateSubTask() throws Exception {
		var issue = JiraTestUtil.newIssue(client, "testCreateSubTaskParent");
		var parentIssue = JiraTestUtil.createIssue(client, issue);

		issue = JiraTestUtil.newSubTask(client, parentIssue, "testCreateSubTaskParent");
		final var childIssue = client.createIssue(issue, null);
		assertEquals(parentIssue.getId(), childIssue.getParentId());

		parentIssue = client.getIssueByKey(parentIssue.getKey(), null);
		assertNotNull(parentIssue.getSubtasks());
		assertEquals(1, parentIssue.getSubtasks().length);
		assertEquals(childIssue.getId(), parentIssue.getSubtasks()[0].getIssueId());
	}

	@Test
	public void testGetIssueLeadingSpaces() throws Exception {
		final var summary = "  testCreateIssueLeadingSpaces";
		final var description = "  leading spaces\n  more spaces";

		var issue = JiraTestUtil.newIssue(client, summary);
		issue.setSummary(summary);
		issue.setDescription(description);
		issue.setAssignee(createUser(client.getUserName()));

		issue = JiraTestUtil.createIssue(client, issue);
		issue = client.getIssueByKey(issue.getKey(), null);
		assertEquals(description, issue.getDescription());
		assertEquals(summary, issue.getSummary());

		issue.setDescription(issue.getDescription());
		client.updateIssue(issue, "", null, null);
		issue = client.getIssueByKey(issue.getKey(), null);
		assertEquals(description, issue.getDescription());
	}

	@Test
	public void testUpdateIssue() throws Exception {
		final var project = JiraTestUtil.getProject(client, "EDITABLEREPORTER");

		var issue = new JiraIssue();
		issue.setProject(project);
		issue.setType(client.getCache().getIssueTypes()[0]);
		issue.setSummary("testUpdateIssue");
		issue.setAssignee(createUser(client.getUserName()));
		issue.setPriority(client.getCache().getPriorities()[0]);

		issue = JiraTestUtil.createIssue(client, issue);
		issue.setSummary("testUpdateIssueChanged");
		client.updateIssue(issue, "", null, null);
		issue = client.getIssueByKey(issue.getKey(), null);
		assertEquals("testUpdateIssueChanged", issue.getSummary());
		assertNotNull(issue.getUpdated());

		final var operation = JiraTestUtil.getOperation(client, issue.getKey(), "custom");
		assertNotNull("Unable to find Custom workflow action", operation);

		// change privilege level
		client = JiraFixture.current().client(PrivilegeLevel.GUEST);
		try {
			client.updateIssue(issue, "", null, null);
			fail("Expected JiraException");
		} catch (final JiraException e) {
		}

		issue.setSummary("testUpdateIssueGuest");
		issue.setAssignee(null);
		issue.setFixVersions(null);
		issue = JiraTestUtil.createIssue(client, issue);
		issue.setSummary("testUpdateIssueGuestChanged");
		try {
			client.updateIssue(issue, "", null, null);
			fail("Expected JiraException");
		} catch (final JiraException e) {
		}

		// change privilege level
		client = JiraFixture.current().client(PrivilegeLevel.USER);
		client.advanceIssueWorkflow(issue, operation, "custom action test", null);
	}

	@Test
	public void testUpdateIssueNonAscii() throws Exception {
		final var summary = "\u00C4\u00D6\u00DC";
		final var description = "\"&\n\u00A9\\ ',><br/>&nbsp; ";

		var issue = JiraTestUtil.createIssue(client, summary);
		issue.setDescription(description);
		assertEquals(summary, issue.getSummary());

		client.updateIssue(issue, "comment: \u00C4\u00D6\u00DC", null, null);
		issue = client.getIssueByKey(issue.getKey(), null);
		assertEquals(summary, issue.getSummary());
		assertEquals(description, issue.getDescription());
		assertEquals(1, issue.getComments().length);
		assertEquals("comment: \u00C4\u00D6\u00DC", issue.getComments()[0].getComment());
	}

	@Test
	public void testIssueMultipleLinesOfSummary() throws Exception {
		final var summary = "line1\nline2";

		try {
			var issue = JiraTestUtil.createIssue(client, summary);
			assertEquals(summary, issue.getSummary());

			issue = client.getIssueByKey(issue.getKey(), null);
			assertEquals(summary, issue.getSummary());
		} catch (final JiraException e) {
			assertTrue(e.getMessage().contains("The summary is invalid because it contains newline characters."));
		}
	}

	@Test
	public void testUpdateIssueMultipleLinesOfDescription() throws Exception {
		final var summary = "summary";
		final var description = "\nline2\n\nline4\n";

		var issue = JiraTestUtil.createIssue(client, summary);
		issue.setDescription(description);
		assertEquals(summary, issue.getSummary());

		client.updateIssue(issue, "", null, null);
		issue = client.getIssueByKey(issue.getKey(), null);
		assertEquals(summary, issue.getSummary());
		assertEquals(description, issue.getDescription());
	}

	@Test
	public void testUpdateIssueWithLinkInDescription() throws Exception {
		final var summary = "updateIssueWithLinkInDescriptoin";
		final var description = "Link:\n\nhttp://mylyn.eclipse.org/";

		var issue = JiraTestUtil.createIssue(client, summary);
		issue.setDescription(description);
		assertEquals(summary, issue.getSummary());

		client.updateIssue(issue, "", null, null);
		issue = client.getIssueByKey(issue.getKey(), null);
		assertEquals(description, issue.getDescription());
	}

	@Test
	public void testUpdateIssueHtmlTag() throws Exception {
		final var summary = "<b>bold</b>";
		final var description = "<head>123\n<pre>line1\nline2\n\nline4</pre>  &nbsp;&lt;&gt; ";

		var issue = JiraTestUtil.createIssue(client, summary);
		issue.setDescription(description);
		assertEquals(summary, issue.getSummary());

		client.updateIssue(issue, "", null, null);
		issue = client.getIssueByKey(issue.getKey(), null);
		assertEquals(summary, issue.getSummary());
		assertEquals(description, issue.getDescription());
	}

	@Test
	public void testBasicAuth() throws Exception {
		basicAuth(JiraFixture.SNAPSHOT.getRepositoryUrl());
	}

	private void basicAuth(final String url) throws Exception {

		// test case that BasicAuth credentials by Mylyn does not override Repository Credentials (sent as BasicAuth by REST client)
		var credentials = CommonTestUtil.getCredentials(PrivilegeLevel.ANONYMOUS);
		final var httpCredentials = CommonTestUtil.getCredentials(PrivilegeLevel.USER);
		final var location = new WebLocation(url, credentials.getUserName(), credentials.getPassword());
		location.setCredentials(AuthenticationType.HTTP, httpCredentials.getUserName(), httpCredentials.getPassword());
		client = new JiraClient(location);

		try {
			assertNotNull(client.getCache().getServerInfo(null));
			fail("Expected JiraAuthenticationException");
		} catch (final JiraAuthenticationException expected) {
		}

		// test credentials to make sure that above exception does not happen
		credentials = CommonTestUtil.getCredentials(PrivilegeLevel.USER);
		client = new JiraClient(new WebLocation(url, credentials.getUserName(), credentials.getPassword()));
		assertNotNull(client.getCache().getServerInfo(null));
	}

	@Test
	public void testCharacterEncoding() throws Exception {
		client.getLocalConfiguration().setCharacterEncoding("UTF-8");
		assertEquals("UTF-8", client.getCharacterEncoding(new NullProgressMonitor()));
	}

	@Test
	public void testGetServerInfo() throws Exception {
		final var serverInfo = client.getCache().getServerInfo(null);
		assertEquals(JiraFixture.current().getVersion(), serverInfo.getVersion());
		// skip build number comparison for snapshot
		if (!JiraFixture.current().getVersion().contains("SNAPSHOT")) {
			assertEquals(JiraFixture.current().getBuildNumber(), serverInfo.getBuildNumber());
		}
		final var jira8x = serverInfo.getVersion().compareTo(JiraServerVersion.JIRA_8_0) >= 0;
		assertEquals(jira8x ? "UTF-8" : "ISO-8859-1", serverInfo.getCharacterEncoding());
		//assertEquals(JiraFixture.current().getRepositoryUrl(), serverInfo.getBaseUrl());
	}

	@Test
	public void testGetEditableFields() throws Exception {
		final var issue = JiraTestUtil.createIssue(client, "getEditableFields");

		//		IssueField[] fields = client.getEditableAttributes(issue.getKey(), null);
		final var fields = issue.getEditableFields();
		final Set<String> ids = new HashSet<>();
		for (final JiraIssueField field : fields) {
			ids.add(field.getId());
		}
		assertFalse(ids.isEmpty());
		assertTrue(ids.contains("versions"), "Missing 'versions': " + ids);
		assertTrue(ids.contains("fixVersions"), "Missing 'fixVersions': " + ids);
	}

	//	public void testGetWorkLogs() throws Exception {
	//		JiraIssue issue = JiraTestUtil.createIssue(client, "getWorklogs");
	//
	//		JiraWorkLog[] logs = client.getWorklogs(issue.getKey(), null);
	//		assertEquals(0, logs.length);
	//
	//		JiraWorkLog log = new JiraWorkLog();
	//		log.setTimeSpent(5287);
	//		log.setStartDate(new Date());
	//		client.addWorkLog(issue.getKey(), log, null);
	//
	//		logs = client.getWorklogs(issue.getKey(), null);
	//		assertEquals(1, logs.length);
	//		assertEquals(5280, logs[0].getTimeSpent());
	//	}

	/**
	 * Tests soap retrieval of the SecurityLevels
	 */
	@Test
	public void testAvailableGetSecurityLevels() throws Exception {
		var issue = JiraTestUtil.newIssue(client, "testAvailableGetSecurityLevels");
		issue.setProject(client.getCache().getProjectByKey("SECURITY"));
		issue = JiraTestUtil.createIssue(client, issue);

		final var securityLevels = client.getAvailableSecurityLevels(issue.getProject().getKey(),
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

	@Test
	public void testAddWorkLog() throws Exception {
		var issue = JiraTestUtil.createIssue(client, "getWorklogs");
		issue.setEstimate(1200);
		client.updateIssue(issue, "", null, null);
		issue = client.getIssueByKey(issue.getKey(), null);

		assertEquals(Long.valueOf(1200), issue.getEstimate());

		var worklog1 = new JiraWorkLog();
		worklog1.setAdjustEstimate(AdjustEstimateMethod.LEAVE);
		worklog1.setComment("comment");
		var time = new GregorianCalendar().getTime();
		worklog1.setStartDate(time.toInstant());
		worklog1.setTimeSpent(120);

		client.addWorkLog(issue.getKey(), worklog1, null);
		var receivedWorklog = client.getIssueByKey(issue.getKey(), null).getWorklogs()[0];
		client.updateIssue(issue, "", null, null);
		issue = client.getIssueByKey(issue.getKey(), null);

		assertEquals(Long.valueOf(1200), issue.getEstimate());
		assertEquals("comment", receivedWorklog.getComment());
		assertEquals(120, receivedWorklog.getTimeSpent());
		assertEquals(time, receivedWorklog.getStartDate());

		worklog1 = new JiraWorkLog();
		worklog1.setAdjustEstimate(AdjustEstimateMethod.AUTO);
		worklog1.setComment("comment2");
		time = new GregorianCalendar().getTime();
		worklog1.setStartDate(time.toInstant());
		worklog1.setTimeSpent(240);

		client.addWorkLog(issue.getKey(), worklog1, null);
		receivedWorklog = client.getIssueByKey(issue.getKey(), null).getWorklogs()[1];
		issue = client.getIssueByKey(issue.getKey(), null);

		assertEquals(Long.valueOf(1200 - 240), issue.getEstimate());
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

	@Test
	public void testProjectSecurityLevelAccessible() throws Exception {
		final var project = client.getCache().getProjectById("10050");
		if (!project.hasDetails()) {
			client.getCache().refreshProjectDetails(project, null);
		}
		assertNotNull(project.getSecurityLevels());
	}
}
