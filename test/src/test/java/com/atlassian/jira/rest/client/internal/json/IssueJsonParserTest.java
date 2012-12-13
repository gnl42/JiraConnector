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
import com.atlassian.jira.rest.client.domain.*;
import com.google.common.base.Function;
import com.google.common.collect.ImmutableList;
import org.apache.commons.lang.StringUtils;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.hamcrest.collection.IsEmptyCollection;
import org.hamcrest.collection.IsEmptyIterable;
import org.hamcrest.collection.IsIterableContainingInAnyOrder;
import org.joda.time.format.ISODateTimeFormat;
import org.junit.Assert;
import org.junit.Test;

import java.util.Iterator;

import static com.atlassian.jira.rest.client.TestUtil.*;
import static com.atlassian.jira.rest.client.domain.EntityHelper.findAttachmentByFileName;
import static com.google.common.collect.Iterables.*;
import static org.junit.Assert.assertEquals;

// Ignore "May produce NPE" warnings, as we know what we are doing in tests
@SuppressWarnings("ConstantConditions")
public class IssueJsonParserTest {
	@Test
	public void testParseIssue() throws Exception {
        final Issue issue = parseIssue("/json/issue/valid-all-expanded.json");

        Assert.assertEquals("Testing attachem2", issue.getSummary());
        Assert.assertEquals("TST-2", issue.getKey());
        Assert.assertEquals("my description", issue.getDescription());

        final BasicProject expectedProject = new BasicProject(toUri("http://localhost:8090/jira/rest/api/2/project/TST"), "TST", "Test Project");
        Assert.assertEquals(expectedProject, issue.getProject());

        assertEquals("Major", issue.getPriority().getName());
        Assert.assertNull(issue.getResolution());
        Assert.assertEquals(toDateTime("2010-07-26T13:29:18.262+0200"), issue.getCreationDate());
        Assert.assertEquals(toDateTime("2012-12-07T14:52:52.570+01:00"), issue.getUpdateDate());
        Assert.assertEquals(null, issue.getDueDate());

        final BasicIssueType expectedIssueType = new BasicIssueType(toUri("http://localhost:8090/jira/rest/api/2/issuetype/1"), 1L, "Bug", false);
        Assert.assertEquals(expectedIssueType, issue.getIssueType());

        Assert.assertEquals(TestConstants.USER_ADMIN, issue.getReporter());
        Assert.assertEquals(TestConstants.USER1, issue.getAssignee());

        // issue links
        Assert.assertThat(issue.getIssueLinks(), IsIterableContainingInAnyOrder.containsInAnyOrder(
                new IssueLink("TST-1", toUri("http://localhost:8090/jira/rest/api/2/issue/10000"),
                        new IssueLinkType("Duplicate", "duplicates", IssueLinkType.Direction.OUTBOUND)),
                new IssueLink("TST-1", toUri("http://localhost:8090/jira/rest/api/2/issue/10000"),
                        new IssueLinkType("Duplicate", "is duplicated by", IssueLinkType.Direction.INBOUND))
        ));

        // watchers
        final BasicWatchers watchers = issue.getWatchers();
        Assert.assertFalse(watchers.isWatching());
        Assert.assertEquals(toUri("http://localhost:8090/jira/rest/api/2/issue/TST-2/watchers"), watchers.getSelf());
        Assert.assertEquals(1, watchers.getNumWatchers());

        // time tracking
        Assert.assertEquals(new TimeTracking(0, 0, 145), issue.getTimeTracking());

        // attachments
        final Iterable<Attachment> attachments = issue.getAttachments();
        Assert.assertEquals(7, size(attachments));
        final Attachment attachment = findAttachmentByFileName(attachments, "avatar1.png");
        Assert.assertEquals(TestConstants.USER_ADMIN_BASIC, attachment.getAuthor());
        Assert.assertEquals(359345, attachment.getSize());
        Assert.assertEquals(toUri("http://localhost:8090/jira/secure/thumbnail/10070/_thumb_10070.png"), attachment.getThumbnailUri());
        Assert.assertEquals(toUri("http://localhost:8090/jira/secure/attachment/10070/avatar1.png"), attachment.getContentUri());
        Iterable<String> attachmentsNames = transform(attachments, new Function<Attachment, String>() {
            @Override
            public String apply(Attachment a) {
                return a.getFilename();
            }
        });
        Assert.assertThat(attachmentsNames, IsIterableContainingInAnyOrder.containsInAnyOrder("10000_thumb_snipe.jpg", "Admal pompa ciepła.pdf",
                "apache-tomcat-5.5.30.zip", "avatar1.png", "jira_logo.gif", "snipe.png", "transparent-png.png"));

        // worklogs
        final Iterable<Worklog> worklogs = issue.getWorklogs();
        Assert.assertEquals(5, size(worklogs));
        final Worklog expectedWorklog1 = new Worklog(
                toUri("http://localhost:8090/jira/rest/api/2/issue/10010/worklog/10011"),
                toUri("http://localhost:8090/jira/rest/api/latest/issue/10010"), TestConstants.USER1_BASIC,
                TestConstants.USER1_BASIC, "another piece of work",
                toDateTime("2010-08-17T16:38:00.013+02:00"), toDateTime("2010-08-17T16:38:24.948+02:00"),
                toDateTime("2010-08-17T16:37:00.000+02:00"), 15, Visibility.role("Developers"));
        final Worklog worklog1 = get(worklogs, 1);
        Assert.assertEquals(expectedWorklog1, worklog1);

        final Worklog worklog2 = get(worklogs, 2);
        Assert.assertEquals(Visibility.group("jira-users"), worklog2.getVisibility());

        final Worklog worklog3 = get(worklogs, 3);
        Assert.assertEquals(StringUtils.EMPTY, worklog3.getComment());

        // comments
        Assert.assertEquals(4, size(issue.getComments()));
        final Comment comment = issue.getComments().iterator().next();
        Assert.assertEquals(Visibility.Type.ROLE, comment.getVisibility().getType());
        Assert.assertEquals(TestConstants.USER_ADMIN_BASIC, comment.getAuthor());
        Assert.assertEquals(TestConstants.USER_ADMIN_BASIC, comment.getUpdateAuthor());

        // components
        final Iterable<String> componentsNames = EntityHelper.toNamesList(issue.getComponents());
        Assert.assertThat(componentsNames, IsIterableContainingInAnyOrder.containsInAnyOrder("Component A", "Component B"));
	}

    @Test
    public void testParseIssueWithCustomFieldsValues() throws Exception {
        final Issue issue = parseIssue("/json/issue/valid-all-expanded.json");

        // test float value: number, com.atlassian.jira.plugin.system.customfieldtypes:float
        Assert.assertEquals(1.457, issue.getField("customfield_10000").getValue());

        // TODO: add assertions for more custom field types after fixing JRJC-122
    }

	private Issue parseIssue(final String resourcePath) throws JSONException {
		final JSONObject issueJson = ResourceUtil.getJsonObjectFromResource(resourcePath);
		final IssueJsonParser parser = new IssueJsonParser();
		return parser.parse(issueJson);
	}

	@Test
	public void testParseIssueWithResolution() throws JSONException {
		final Issue issue = parseIssue("/json/issue/valid-all-expanded-with-resolution.json");
		Assert.assertEquals("Incomplete", issue.getResolution().getName());

	}

	@Test
	public void testParseIssueWhenWatchersAndVotersAreSwitchedOff() throws JSONException {
		final Issue issue = parseIssue("/json/issue/valid-no-votes-no-watchers.json");
		Assert.assertNull(issue.getWatchers());
		Assert.assertNull(issue.getVotes());
	}

	@Test
	public void testParseUnassignedIssue() throws JSONException {
		final Issue issue = parseIssue("/json/issue/valid-unassigned-no-time-tracking.json");
		Assert.assertNull(issue.getAssignee());
	}

	@Test
	public void testParseNoTimeTrackingInfo() throws JSONException {
		final Issue issue = parseIssue("/json/issue/valid-unassigned-no-time-tracking.json");
		Assert.assertNull(issue.getTimeTracking());
	}

	@Test
	public void testParseIssueWithAnonymousComment() throws JSONException {
		final Issue issue = parseIssue("/json/issue/valid-anonymous-comment.json");
		Assert.assertEquals(1, size(issue.getComments()));
		final Comment comment = issue.getComments().iterator().next();
		Assert.assertEquals("Comment from anonymous user", comment.getBody());
		Assert.assertNull(comment.getAuthor());

	}

	@Test
	public void testParseIssueWithVisibility() throws JSONException {
		final Issue issue = parseIssue("/json/issue/valid-visibility.json");
		Assert.assertEquals(Visibility.role("Administrators"), issue.getComments().iterator().next().getVisibility());
		Assert.assertEquals(Visibility.role("Developers"), get(issue.getWorklogs(), 1).getVisibility());
		Assert.assertEquals(Visibility.group("jira-users"), get(issue.getWorklogs(), 2).getVisibility());
	}

    // TODO: temporary disabled as we want to run integration tests. Fix JRJC-122 and re-enable this test
//	@Test
//	public void testParseIssueWithUserPickerCustomFieldFilledOut() throws JSONException {
//		final Issue issue = parseIssue("/json/issue/valid-user-picker-custom-field-filled-out.json");
//		final Field extraUserField = issue.getFieldByName("Extra User");
//		assertNotNull(extraUserField);
//		assertEquals(BasicUser.class, extraUserField.getValue().getClass());
//		assertEquals(TestConstants.USER1, extraUserField.getValue());
//	}

	@Test
	public void testParseIssueWithUserPickerCustomFieldEmpty() throws JSONException {
		final Issue issue = parseIssue("/json/issue/valid-user-picker-custom-field-empty.json");
		final Field extraUserField = issue.getFieldByName("Extra User");
		Assert.assertNotNull(extraUserField);
		Assert.assertNull(extraUserField.getValue());
	}

	@Test
	public void testParseIssueJira5x0Representation() throws JSONException {
		final Issue issue = parseIssue("/json/issue/valid-5.0.json");
		Assert.assertEquals(3, size(issue.getComments()));
		final BasicPriority priority = issue.getPriority();
		Assert.assertNotNull(priority);
		assertEquals("Major", priority.getName());
		Assert.assertEquals("my description", issue.getDescription());
		Assert.assertEquals("TST", issue.getProject().getKey());
		Assert.assertNotNull(issue.getDueDate());
		Assert.assertEquals(toDateTimeFromIsoDate("2010-07-05"), issue.getDueDate());
		Assert.assertEquals(4, size(issue.getAttachments()));
		Assert.assertEquals(1, size(issue.getIssueLinks()));
		Assert.assertEquals(1.457, issue.getField("customfield_10000").getValue());
		Assert.assertThat(transform(issue.getComponents(), new BasicComponentNameExtractionFunction()), IsIterableContainingInAnyOrder.containsInAnyOrder("Component A", "Component B"));
		Assert.assertEquals(2, size(issue.getWorklogs()));
		Assert.assertEquals(1, issue.getWatchers().getNumWatchers());
		Assert.assertFalse(issue.getWatchers().isWatching());
		Assert.assertEquals(new TimeTracking(2700, 2220, 180), issue.getTimeTracking());

		Assert.assertEquals(Visibility.role("Developers"), issue.getWorklogs().iterator().next().getVisibility());
		Assert.assertEquals(Visibility.group("jira-users"), get(issue.getWorklogs(), 1).getVisibility());

	}

	@Test
	public void testParseIssueJira50Representation() throws JSONException {
		final Issue issue = parseIssue("/json/issue/valid-5.0-1.json");
		Assert.assertEquals(0, size(issue.getComments()));
		final BasicPriority priority = issue.getPriority();
		Assert.assertNull(priority);
		Assert.assertEquals("Pivotal Tracker provides time tracking information on the project level.\n"
                + "JIRA stores time tracking information on issue level, so this issue has been created to store imported time tracking information.", issue.getDescription());
		Assert.assertEquals("TIMETRACKING", issue.getProject().getKey());
		Assert.assertNull(issue.getDueDate());
		Assert.assertEquals(0, size(issue.getAttachments()));
		Assert.assertNull(issue.getIssueLinks());
		Assert.assertNull(issue.getField("customfield_10000").getValue());
		Assert.assertThat(issue.getComponents(), IsEmptyIterable.<BasicComponent>emptyIterable());
		Assert.assertEquals(2, size(issue.getWorklogs()));
		Assert.assertEquals(0, issue.getWatchers().getNumWatchers());
		Assert.assertFalse(issue.getWatchers().isWatching());
		Assert.assertEquals(new TimeTracking(null, null, 840), issue.getTimeTracking());

		Assert.assertNull(issue.getWorklogs().iterator().next().getVisibility());
		Assert.assertNull(get(issue.getWorklogs(), 1).getVisibility());
	}

    @Test
    public void testParseIssueWithProjectNamePresentInRepresentation() throws JSONException {
        final Issue issue = parseIssue("/json/issue/issue-with-project-name-present.json");
        Assert.assertEquals("My Test Project", issue.getProject().getName());
    }

    @Test
    public void testParseIssueJiraRepresentationJrjc49() throws JSONException {
        final Issue issue = parseIssue("/json/issue/jrjc49.json");
        final Iterable<Worklog> worklogs = issue.getWorklogs();
        Assert.assertEquals(1, size(worklogs));
        final Worklog worklog = get(worklogs, 0);
        Assert.assertEquals("Worklog comment should be returned as empty string, when JIRA doesn't include it in reply",
                StringUtils.EMPTY, worklog.getComment());
        Assert.assertEquals(180, worklog.getMinutesSpent());
        Assert.assertEquals("deleteduser", worklog.getAuthor().getName());
    }

	@Test
	public void testParseIssueJira5x0RepresentationNullCustomField() throws JSONException {
		final Issue issue = parseIssue("/json/issue/valid-5.0-null-custom-field.json");
		Assert.assertEquals(null, issue.getField("customfield_10000").getValue());
		Assert.assertNull(issue.getIssueLinks());
	}

	@Test
	public void issueWithSubtasks() throws JSONException {
		final Issue issue = parseIssue("/json/issue/subtasks-5.json");
		Iterable<Subtask> subtasks = issue.getSubtasks();
		Assert.assertEquals(1, size(subtasks));
		Subtask subtask = get(subtasks, 0);
		Assert.assertNotNull(subtask);
		Assert.assertEquals("SAM-2", subtask.getIssueKey());
		assertEquals("Open", subtask.getStatus().getName());
		assertEquals("Subtask", subtask.getIssueType().getName());
	}

	@Test
	public void issueWithChangelog() throws JSONException {
		final Issue issue = parseIssue("/json/issue/valid-5.0-with-changelog.json");
		Assert.assertEquals("HST-1", issue.getKey());

		final Iterable<ChangelogGroup> changelog = issue.getChangelog();
		Assert.assertNotNull(changelog);

		Assert.assertEquals(4, size(changelog));
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

		verifyChangelog(iterator.next(),
				"2012-04-12T14:28:44.079+0200",
				null,
				ImmutableList.of(
						new ChangelogItem(ChangelogItem.FieldType.JIRA, "assignee", "user1", "User One", "user2", "User Two")
				));
	}

	private static void verifyChangelog(ChangelogGroup changelogGroup, String createdDate, BasicUser author, Iterable<ChangelogItem> expectedItems) {
		Assert.assertEquals(ISODateTimeFormat.dateTime().parseDateTime(createdDate), changelogGroup.getCreated());
		assertEquals(author, changelogGroup.getAuthor());
		Assert.assertEquals(expectedItems, changelogGroup.getItems());
	}

	@Test
	public void testParseIssueWithLabelsForJira5x0() throws JSONException {
		final Issue issue = parseIssue("/json/issue/valid-5.0-with-labels.json");
		Assert.assertThat(issue.getLabels(), IsIterableContainingInAnyOrder.containsInAnyOrder("a", "bcds"));
	}

	@Test
	public void testParseIssueWithLabels() throws JSONException {
		final Issue issue = parseIssue("/json/issue/valid-5.0-with-labels.json");
		Assert.assertThat(issue.getLabels(), IsIterableContainingInAnyOrder.containsInAnyOrder("a", "bcds"));
	}

	@Test
	public void testParseIssueWithoutLabelsForJira5x0() throws JSONException {
		final Issue issue = parseIssue("/json/issue/valid-5.0-without-labels.json");
		Assert.assertThat(issue.getLabels(), IsEmptyCollection.<String>empty());
	}

}
