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

package it;

import com.atlassian.jira.nimblefunctests.annotation.JiraBuildNumberDependent;
import com.atlassian.jira.nimblefunctests.annotation.RestoreOnce;
import com.atlassian.jira.rest.client.TestUtil;
import com.atlassian.jira.rest.client.api.domain.BasicPriority;
import com.atlassian.jira.rest.client.api.domain.EntityHelper;
import com.atlassian.jira.rest.client.api.domain.Field;
import com.atlassian.jira.rest.client.api.domain.FieldSchema;
import com.atlassian.jira.rest.client.api.domain.FieldType;
import com.atlassian.jira.rest.client.api.domain.Issue;
import com.atlassian.jira.rest.client.api.domain.IssueType;
import com.atlassian.jira.rest.client.api.domain.IssuelinksType;
import com.atlassian.jira.rest.client.api.domain.Priority;
import com.atlassian.jira.rest.client.api.domain.Resolution;
import com.atlassian.jira.rest.client.api.domain.ServerInfo;
import com.atlassian.jira.rest.client.api.domain.Status;
import com.atlassian.jira.rest.client.api.domain.Transition;
import com.atlassian.jira.rest.client.api.domain.input.TransitionInput;
import com.atlassian.jira.rest.client.internal.json.TestConstants;
import com.atlassian.jira.rest.client.test.matchers.RegularExpressionMatcher;
import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;
import org.hamcrest.Matchers;
import org.joda.time.DateTime;
import org.junit.Test;

import java.util.Map;
import javax.ws.rs.core.Response;

import static com.atlassian.jira.rest.client.internal.ServerVersionConstants.BN_JIRA_4_3;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.core.StringEndsWith.endsWith;
import static org.hamcrest.core.StringStartsWith.startsWith;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

/**
 * Those tests mustn't change anything on server side, as jira is restored only once
 */
// Ignore "May produce NPE" warnings, as we know what we are doing in tests
@SuppressWarnings ("ConstantConditions")
@RestoreOnce (TestConstants.DEFAULT_JIRA_DUMP_FILE)
public class AsynchronousMetadataRestClientReadOnlyTest extends AbstractAsynchronousRestClientTest {

	@Test
	public void testGetServerInfo() throws Exception {
		final ServerInfo serverInfo = client.getMetadataClient().getServerInfo().claim();
		assertEquals("Your Company JIRA", serverInfo.getServerTitle());
		assertTrue(serverInfo.getBuildDate().isBeforeNow());
		assertTrue(serverInfo.getServerTime().isAfter(new DateTime().minusMinutes(5)));
		assertTrue(serverInfo.getServerTime().isBefore(new DateTime().plusMinutes(5)));
	}

	@Test
	public void testGetIssueTypeNonExisting() throws Exception {
		final IssueType issueType = client.getIssueClient().getIssue("TST-1").claim().getIssueType();
		TestUtil.assertErrorCode(Response.Status.NOT_FOUND, new Runnable() {
			@Override
			public void run() {
				client.getMetadataClient().getIssueType(TestUtil.toUri(issueType.getSelf() + "fake")).claim();
			}
		});
	}

	@Test
	public void testGetIssueType() {
		final IssueType getIssueIssueType = client.getIssueClient().getIssue("TST-1").claim().getIssueType();
		final IssueType issueType = client.getMetadataClient().getIssueType(getIssueIssueType.getSelf()).claim();
		assertEquals("Bug", issueType.getName());
		assertEquals("A problem which impairs or prevents the functions of the product.", issueType.getDescription());
		Long expectedId = isJira5xOrNewer() ? 1L : null;
		assertEquals(expectedId, issueType.getId());
		assertThat(issueType.getIconUri().toString(), Matchers.anyOf(
				endsWith("bug.png"),
				endsWith("bug.gif"),
				endsWith("viewavatar?size=xsmall&avatarId=10163&avatarType=issuetype")));
	}

	@JiraBuildNumberDependent (BN_JIRA_4_3)
	@Test
	public void testGetIssueTypes() {
		final Iterable<IssuelinksType> issueTypes = client.getMetadataClient().getIssueLinkTypes().claim();
		assertEquals(1, Iterables.size(issueTypes));
		final IssuelinksType issueType = Iterables.getOnlyElement(issueTypes);
		assertEquals("Duplicate", issueType.getName());
		assertEquals("is duplicated by", issueType.getInward());
		assertEquals("duplicates", issueType.getOutward());
	}

	@Test
	public void testGetStatuses() {
		final Iterable<Status> statuses = client.getMetadataClient().getStatuses().claim();
		final Map<String, Status> statusMap = Maps.uniqueIndex(statuses, EntityHelper.GET_ENTITY_NAME_FUNCTION);
		assertThat(statusMap.keySet(), containsInAnyOrder("Open", "In Progress", "Reopened", "Resolved", "Closed"));

		final Status status = statusMap.get("Open");
		assertThat(status.getSelf().toString(), Matchers.endsWith("status/1"));
		assertThat(status.getId(), is(1L));
		assertThat(status.getName(), is("Open"));
		assertThat(status.getDescription(), is("The issue is open and ready for the assignee to start work on it."));
		assertThat(status.getIconUrl().toString(), RegularExpressionMatcher.matchesRegexp(".*open\\.(png|gif)$"));
	}

	@Test
	public void testGetStatus() {
		final Status basicStatus = client.getIssueClient().getIssue("TST-1").claim().getStatus();
		final Status status = client.getMetadataClient().getStatus(basicStatus.getSelf()).claim();
		assertEquals("The issue is open and ready for the assignee to start work on it.", status.getDescription());
		assertThat(status.getIconUrl().toString(), Matchers.anyOf(endsWith("status_open.gif"), endsWith("open.png")));
		assertEquals("Open", status.getName());
	}

	@Test
	public void testGetStatusNonExisting() throws Exception {
		final Status status = client.getIssueClient().getIssue("TST-1").claim().getStatus();
		TestUtil.assertErrorCode(Response.Status.NOT_FOUND, "The status with id '" +
				TestUtil.getLastPathSegment(status.getSelf()) + "fake" +
				"' does not exist", new Runnable() {
			@Override
			public void run() {
				client.getMetadataClient().getStatus(TestUtil.toUri(status.getSelf() + "fake")).claim();
			}
		});
	}

	@Test
	public void testGetPriority() {
		final BasicPriority basicPriority = client.getIssueClient().getIssue("TST-2").claim().getPriority();
		final Priority priority = client.getMetadataClient().getPriority(basicPriority.getSelf()).claim();
		assertEquals(basicPriority.getSelf(), priority.getSelf());
		assertEquals("Major", priority.getName());
		assertEquals("#009900", priority.getStatusColor());
		assertEquals("Major loss of function.", priority.getDescription());
		final Long expectedId = isJira5xOrNewer() ? 3L : null;
		assertEquals(expectedId, priority.getId());
		assertThat(priority.getIconUri().toString(), startsWith(jiraUri.toString()));
		assertThat(priority.getIconUri().toString(),
				Matchers.anyOf(
						endsWith("images/icons/priority_major.gif"),
						endsWith("images/icons/priorities/major.png"),
						endsWith("images/icons/priorities/major.svg"))
		);
	}

	@Test
	public void testGetResolution() {
		final Issue issue = client.getIssueClient().getIssue("TST-2").claim();
		assertNull(issue.getResolution());
		final Iterable<Transition> transitions = client.getIssueClient().getTransitions(issue).claim();
		final Transition resolveTransition = TestUtil.getTransitionByName(transitions, "Resolve Issue");

		client.getIssueClient().transition(issue, new TransitionInput(resolveTransition.getId())).claim();

		final Issue resolvedIssue = client.getIssueClient().getIssue("TST-2").claim();
		final Resolution basicResolution = resolvedIssue.getResolution();
		assertNotNull(basicResolution);

		final Resolution resolution = client.getMetadataClient().getResolution(basicResolution.getSelf()).claim();
		assertEquals(basicResolution.getName(), resolution.getName());
		assertEquals(basicResolution.getSelf(), resolution.getSelf());
		assertEquals("A fix for this issue is checked into the tree and tested.", resolution.getDescription());
	}

	@Test
	public void testGetAllFieldsAtOnce() {

		// the declared schema of "votes" field has been corrected in JIRA 7.1
		Field votesField = isJira7_1_OrNewer() ?
				new Field("votes", "Votes", FieldType.JIRA, false, true, false, new FieldSchema("votes", null, "votes", null, null)) :
				new Field("votes", "Votes", FieldType.JIRA, false, true, false, new FieldSchema("array", "votes", "votes", null, null));

		final Iterable<Field> fields = client.getMetadataClient().getFields().claim();
		assertThat(fields, hasItems(
				new Field("progress", "Progress", FieldType.JIRA, false, true, false,
						new FieldSchema("progress", null, "progress", null, null)),
				new Field("summary", "Summary", FieldType.JIRA, true, true, true,
						new FieldSchema("string", null, "summary", null, null)),
				new Field("timetracking", "Time Tracking", FieldType.JIRA, true, false, true,
						new FieldSchema("timetracking", null, "timetracking", null, null)),
				new Field("issuekey", "Key", FieldType.JIRA, false, true, false, null),
				new Field("issuetype", "Issue Type", FieldType.JIRA, true, true, true,
						new FieldSchema("issuetype", null, "issuetype", null, null)),
				votesField,
				new Field("components", "Component/s", FieldType.JIRA, true, true, true,
						new FieldSchema("array", "component", "components", null, null)),
				new Field("aggregatetimespent", "Σ Time Spent", FieldType.JIRA, false, true, false,
						new FieldSchema("number", null, "aggregatetimespent", null, null)),
				new Field("thumbnail", "Images", FieldType.JIRA, false, true, false, null),
				new Field("customfield_10000", "My Number Field New", FieldType.CUSTOM, true, true, true,
						new FieldSchema("number", null, null, "com.atlassian.jira.plugin.system.customfieldtypes:float", 10000l)),
				new Field("customfield_10011", "project2", FieldType.CUSTOM, true, true, true,
						new FieldSchema("string", null, null, "com.atlassian.jira.plugin.system.customfieldtypes:textarea", 10011l)),
				new Field("workratio", "Work Ratio", FieldType.JIRA, false, true, true,
						new FieldSchema("number", null, "workratio", null, null))
		));
	}
}
