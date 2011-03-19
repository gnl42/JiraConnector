/*
 * Copyright (C) 2010 Atlassian
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.atlassian.jira.rest.client.internal.json;

import com.atlassian.jira.rest.client.IterableMatcher;
import com.atlassian.jira.rest.client.domain.Attachment;
import com.atlassian.jira.rest.client.domain.BasicIssueType;
import com.atlassian.jira.rest.client.domain.BasicProject;
import com.atlassian.jira.rest.client.domain.BasicWatchers;
import com.atlassian.jira.rest.client.domain.Comment;
import com.atlassian.jira.rest.client.domain.Issue;
import com.atlassian.jira.rest.client.domain.IssueLink;
import com.atlassian.jira.rest.client.domain.IssueLinkType;
import com.atlassian.jira.rest.client.domain.Visibility;
import com.atlassian.jira.rest.client.domain.Worklog;
import com.google.common.collect.Iterables;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.junit.Assert;
import org.junit.Test;

import java.util.Iterator;

import static com.atlassian.jira.rest.client.TestUtil.toDateTime;
import static com.atlassian.jira.rest.client.TestUtil.toUri;
import static org.junit.Assert.*;

/**
 * @since v0.1
 */
public class IssueJsonParserTest {
	@Test
	public void testParseIssue() throws Exception {
		final Issue issue = parseIssue("/json/issue/valid-all-expanded.json");
		assertExpectedIssue(issue);
	}

	@Test
	public void testParseIssueJira4x2() throws Exception {
		final Issue issue = parseIssue("/json/issue/valid-all-expanded-jira-4-2.json");
		assertExpectedIssue(issue);
	}

	private void assertExpectedIssue(Issue issue) {
		assertEquals("Testing issue", issue.getSummary());
		assertEquals("TST-2", issue.getKey());
		assertEquals(new BasicIssueType(toUri("http://localhost:8090/jira/rest/api/latest/issueType/1"), "Bug", false),
				issue.getIssueType());
		assertEquals(new BasicProject(toUri("http://localhost:8090/jira/rest/api/latest/project/TST"), "TST"), issue.getProject());
		assertEquals("Major", issue.getPriority().getName());
		assertNull(issue.getResolution());
		assertEquals(toDateTime("2010-07-26T13:29:18.262+0200"), issue.getCreationDate());
		assertEquals(toDateTime("2010-08-27T15:00:02.107+0200"), issue.getUpdateDate());

		assertEquals(TestConstants.USER_ADMIN, issue.getReporter());
		assertEquals(TestConstants.USER1, issue.getAssignee());

		// issue links
		Assert.assertThat(issue.getIssueLinks(), IterableMatcher.hasOnlyElements(
				new IssueLink("TST-1", toUri("http://localhost:8090/jira/rest/api/latest/issue/TST-1"),
						new IssueLinkType("Duplicate", "duplicates", IssueLinkType.Direction.OUTBOUND)),
				new IssueLink("TST-1", toUri("http://localhost:8090/jira/rest/api/latest/issue/TST-1"),
						new IssueLinkType("Duplicate", "is duplicated by", IssueLinkType.Direction.INBOUND))
		));


		// watchers
		final BasicWatchers watchers = issue.getWatchers();
		assertFalse(watchers.isWatching());
		assertEquals(toUri("http://localhost:8090/jira/rest/api/latest/issue/TST-2/watchers"), watchers.getSelf());
		assertEquals(1, watchers.getNumWatchers());

		// attachments
		final Iterable<Attachment> attachments = issue.getAttachments();
		assertEquals(3, Iterables.size(attachments));
		final Attachment attachment = attachments.iterator().next();
		assertEquals("jira_logo.gif", attachment.getFilename());
		assertEquals(TestConstants.USER_ADMIN, attachment.getAuthor());
		assertEquals(2517, attachment.getSize());
		assertEquals(toUri("http://localhost:8090/jira/secure/thumbnail/10036/10036_jira_logo.gif"), attachment.getThumbnailUri());
		final Iterator<Attachment> attachmentIt = attachments.iterator();
		attachmentIt.next();
		attachmentIt.next();
		final Attachment lastAttachment = attachmentIt.next();
		assertEquals("transparent-png.png", lastAttachment.getFilename());

		// worklogs
		final Iterable<Worklog> worklogs = issue.getWorklogs();
		assertEquals(5, Iterables.size(worklogs));
		final Worklog worklog = Iterables.get(worklogs, 2);
		assertEquals(new Worklog(toUri("http://localhost:8090/jira/rest/api/latest/worklog/10012"),
				toUri("http://localhost:8090/jira/rest/api/latest/issue/TST-2"), TestConstants.USER1,
				TestConstants.USER1, "a worklog viewable just by jira-users",
				toDateTime("2010-08-17T16:53:15.848+0200"), toDateTime("2010-08-17T16:53:15.848+0200"),
				toDateTime("2010-08-11T16:52:00.000+0200"), 3, Visibility.group("jira-users")), worklog);

		final Worklog worklog3 = Iterables.get(worklogs, 3);
		assertEquals("", worklog3.getComment());

		// comments
		assertEquals(3, Iterables.size(issue.getComments()));
		final Comment comment = issue.getComments().iterator().next();
		assertEquals(Visibility.Type.ROLE, comment.getVisibility().getType());
		assertEquals(TestConstants.USER_ADMIN, comment.getAuthor());
		assertEquals(TestConstants.USER_ADMIN, comment.getUpdateAuthor());
	}

	private Issue parseIssue(final String resourcePath) throws JSONException {
		final JSONObject issueJson = ResourceUtil.getJsonObjectFromResource(resourcePath);
		final IssueJsonParser parser = new IssueJsonParser();
		return parser.parse(issueJson);
	}

	@Test
	public void testParseIssueWithResolution() throws JSONException {
		final Issue issue = parseIssue("/json/issue/valid-all-expanded-with-resolution.json");
		assertEquals("Incomplete", issue.getResolution().getName());

	}

	@Test
	public void testParseIssueWhenWatchersAndVotersAreSwitchedOff() throws JSONException {
		final Issue issue = parseIssue("/json/issue/valid-no-votes-no-watchers.json");
		assertNull(issue.getWatchers());
		assertNull(issue.getVotes());
	}

	@Test
	public void testParseUnassignedIssue() throws JSONException {
		final Issue issue = parseIssue("/json/issue/valid-unassigned.json");
		assertNull(issue.getAssignee());
	}

	@Test
	public void testParseUnassignedIssueJira4x3() throws JSONException {
		final Issue issue = parseIssue("/json/issue/valid-unassigned-jira-4.3.json");
		assertNull(issue.getAssignee());
	}

	@Test
	public void testParseIssueWithAnonymousComment() throws JSONException {
		final Issue issue = parseIssue("/json/issue/valid-anonymous-comment-jira-4.3.json");
		assertEquals(1, Iterables.size(issue.getComments()));
		final Comment comment = issue.getComments().iterator().next();
		assertEquals("A comment from anonymous user", comment.getBody());
		assertNull(comment.getAuthor());

	}

	@Test
	public void testParseIssueWithVisibilityJira4x3() throws JSONException {
		final Issue issue = parseIssue("/json/issue/valid-visibility-jira-4.3.json");
		assertEquals(Visibility.role("Administrators"), issue.getComments().iterator().next().getVisibility());
		assertEquals(Visibility.role("Developers"), Iterables.get(issue.getWorklogs(), 1).getVisibility());
		assertEquals(Visibility.group("jira-users"), Iterables.get(issue.getWorklogs(), 2).getVisibility());
	}

}
