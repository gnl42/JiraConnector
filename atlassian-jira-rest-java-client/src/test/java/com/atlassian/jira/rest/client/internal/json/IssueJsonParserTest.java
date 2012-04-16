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

import com.atlassian.jira.rest.client.BasicComponentNameExtractionFunction;
import com.atlassian.jira.rest.client.IterableMatcher;
import com.atlassian.jira.rest.client.domain.Attachment;
import com.atlassian.jira.rest.client.domain.BasicIssueType;
import com.atlassian.jira.rest.client.domain.BasicPriority;
import com.atlassian.jira.rest.client.domain.BasicProject;
import com.atlassian.jira.rest.client.domain.BasicUser;
import com.atlassian.jira.rest.client.domain.BasicWatchers;
import com.atlassian.jira.rest.client.domain.ChangelogGroup;
import com.atlassian.jira.rest.client.domain.ChangelogItem;
import com.atlassian.jira.rest.client.domain.Comment;
import com.atlassian.jira.rest.client.domain.Field;
import com.atlassian.jira.rest.client.domain.Issue;
import com.atlassian.jira.rest.client.domain.IssueLink;
import com.atlassian.jira.rest.client.domain.IssueLinkType;
import com.atlassian.jira.rest.client.domain.Subtask;
import com.atlassian.jira.rest.client.domain.TimeTracking;
import com.atlassian.jira.rest.client.domain.Visibility;
import com.atlassian.jira.rest.client.domain.Worklog;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.joda.time.format.ISODateTimeFormat;
import org.junit.Assert;
import org.junit.Test;

import java.util.Iterator;

import static com.atlassian.jira.rest.client.TestUtil.toDateTime;
import static com.atlassian.jira.rest.client.TestUtil.toUri;
import static org.junit.Assert.*;

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
		assertEquals(new BasicProject(toUri("http://localhost:8090/jira/rest/api/latest/project/TST"), "TST", null), issue.getProject());
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
		assertEquals(new TimeTracking(0, 0, 145), issue.getTimeTracking());

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
	public void testParseNoTimeTrackingInfo() throws JSONException {
		final Issue issue = parseIssue("/json/issue/valid-unassigned.json");
		assertNull(issue.getTimeTracking());
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

	@Test
	public void testParseIssueWithUserPickerCustomFieldFilledOut() throws JSONException {
		final Issue issue = parseIssue("/json/issue/valid-user-picker-custom-field-filled-out.json");
		final Field extraUserField = issue.getFieldByName("Extra User");
		assertNotNull(extraUserField);
		assertEquals(BasicUser.class, extraUserField.getValue().getClass());
		assertEquals(TestConstants.USER1, extraUserField.getValue());
	}

	@Test
	public void testParseIssueWithUserPickerCustomFieldEmpty() throws JSONException {
		final Issue issue = parseIssue("/json/issue/valid-user-picker-custom-field-empty.json");
		final Field extraUserField = issue.getFieldByName("Extra User");
		assertNotNull(extraUserField);
		assertNull(extraUserField.getValue());
	}

	@Test
	public void testParseIssueJira5x0Representation() throws JSONException {
		final Issue issue = parseIssue("/json/issue/valid-5.0.json");
		assertEquals(3, Iterables.size(issue.getComments()));
		final BasicPriority priority = issue.getPriority();
		assertNotNull(priority);
		assertEquals("Major", priority.getName());
		assertEquals("my description", issue.getDescription());
		assertEquals("TST", issue.getProject().getKey());
		assertEquals(4, Iterables.size(issue.getAttachments()));
		assertEquals(1, Iterables.size(issue.getIssueLinks()));
		assertEquals(1.457, issue.getField("customfield_10000").getValue());
		assertThat(Iterables.transform(issue.getComponents(), new BasicComponentNameExtractionFunction()), IterableMatcher.hasOnlyElements("Component A", "Component B"));
		assertEquals(2, Iterables.size(issue.getWorklogs()));
		assertEquals(1, issue.getWatchers().getNumWatchers());
		assertFalse(issue.getWatchers().isWatching());
		assertEquals(new TimeTracking(2700, 2220, 180), issue.getTimeTracking());

		assertEquals(Visibility.role("Developers"), issue.getWorklogs().iterator().next().getVisibility());
		assertEquals(Visibility.group("jira-users"), Iterables.get(issue.getWorklogs(), 1).getVisibility());

	}

	@Test
	public void testParseIssueJira50Representation() throws JSONException {
		final Issue issue = parseIssue("/json/issue/valid-5.0-1.json");
	}

    @Test
    public void testParseIssueWithProjectNamePresentInRepresentation() throws JSONException {
        final Issue issue = parseIssue("/json/issue/issue-with-project-name-present.json");
        assertEquals("My Test Project", issue.getProject().getName());
    }

    @Test
    public void testParseIssueJiraRepresentationJrjc49() throws JSONException {
        final Issue issue = parseIssue("/json/issue/jrjc49.json");
        final Iterable<Worklog> worklogs = issue.getWorklogs();
        assertEquals(1, Iterables.size(worklogs));
        final Worklog worklog = Iterables.get(worklogs, 0);
        assertNull(worklog.getComment());
        assertEquals(180, worklog.getMinutesSpent());
        assertEquals("Sample, User", worklog.getAuthor().getDisplayName());

    }

	@Test
	public void testParseIssueJira5x0RepresentationNullCustomField() throws JSONException {
		final Issue issue = parseIssue("/json/issue/valid-5.0-null-custom-field.json");
		assertEquals(null, issue.getField("customfield_10000").getValue());
		assertNull(issue.getIssueLinks());
	}

	@Test
	public void issueWithSubtasks() throws JSONException {
		final Issue issue = parseIssue("/json/issue/subtasks-5.json");
		Iterable<Subtask> subtasks = issue.getSubtasks();
		assertEquals(1, Iterables.size(subtasks));
		Subtask subtask = Iterables.get(subtasks, 0, null);
		assertNotNull(subtask);
		assertEquals("SAM-2", subtask.getIssueKey());
		assertEquals("Open", subtask.getStatus().getName());
		assertEquals("Subtask", subtask.getIssueType().getName());
	}

	@Test
	public void issueWithChangelog() throws JSONException {
		final Issue issue = parseIssue("/json/issue/valid-5.0-with-changelog.json");
		assertEquals("HST-1", issue.getKey());

		final Iterable<ChangelogGroup> changelog = issue.getChangelog();
		assertNotNull(changelog);

		assertEquals(3, Iterables.size(changelog));
		final Iterator<ChangelogGroup> iterator = changelog.iterator();

		final BasicUser user1 = new BasicUser(toUri("http://localhost:2990/jira/rest/api/2/user?username=user1"), "user1", "User One");
		final BasicUser user2 = new BasicUser(toUri("http://localhost:2990/jira/rest/api/2/user?username=user2"), "user2", "User Two");

		verifyChangelog(iterator.next(),
				"2012-04-12T14:28:28.255+0200",
				user1,
				ImmutableList.of(
						new ChangelogItem(ChangelogItem.FieldType.JIRA, "duedate", null, null, "2012-04-12", "2012-04-12 00:00:00.0"),
						new ChangelogItem(ChangelogItem.FieldType.CUSTOM, "Radio Field", null, null, "10000", "One")
				));

		verifyChangelog(iterator.next(),
				"2012-04-12T14:28:44.079+0200",
				user1,
				ImmutableList.of(
						new ChangelogItem(ChangelogItem.FieldType.JIRA, "assignee", "user1", "User One", "user2", "User Two")
				));

		verifyChangelog(iterator.next(),
				"2012-04-12T14:30:09.690+0200",
				user2,
				ImmutableList.of(
						new ChangelogItem(ChangelogItem.FieldType.JIRA, "summary", null, "Simple history test", null, "Simple history test - modified"),
						new ChangelogItem(ChangelogItem.FieldType.JIRA, "issuetype", "1", "Bug", "2", "New Feature"),
						new ChangelogItem(ChangelogItem.FieldType.JIRA, "priority", "3", "Major", "4", "Minor"),
						new ChangelogItem(ChangelogItem.FieldType.JIRA, "description", null, "Initial Description", null, "Modified Description"),
						new ChangelogItem(ChangelogItem.FieldType.CUSTOM, "Date Field", "2012-04-11T14:26+0200", "11/Apr/12 2:26 PM", "2012-04-12T14:26+0200", "12/Apr/12 2:26 PM"),
						new ChangelogItem(ChangelogItem.FieldType.JIRA, "duedate", "2012-04-12", "2012-04-12 00:00:00.0", "2012-04-13", "2012-04-13 00:00:00.0"),
						new ChangelogItem(ChangelogItem.FieldType.CUSTOM, "Radio Field", "10000", "One", "10001", "Two"),
						new ChangelogItem(ChangelogItem.FieldType.CUSTOM, "Text Field", null, "Initial text field value", null, "Modified text field value")
				));
	}

	private static void verifyChangelog(ChangelogGroup changelogGroup, String createdDate, BasicUser author, Iterable<ChangelogItem> expectedItems) {
		assertEquals(ISODateTimeFormat.dateTime().parseDateTime(createdDate), changelogGroup.getCreated());
		assertEquals(author, changelogGroup.getAuthor());
		assertEquals(expectedItems, changelogGroup.getItems());
	}


}
