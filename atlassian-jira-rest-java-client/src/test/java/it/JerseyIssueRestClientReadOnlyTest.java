/*
 * Copyright (C) 2012 Atlassian
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
import com.atlassian.jira.rest.client.GetCreateIssueMetadataOptionsBuilder;
import com.atlassian.jira.rest.client.IntegrationTestUtil;
import com.atlassian.jira.rest.client.IssueRestClient;
import com.atlassian.jira.rest.client.NullProgressMonitor;
import com.atlassian.jira.rest.client.domain.Attachment;
import com.atlassian.jira.rest.client.domain.BasicUser;
import com.atlassian.jira.rest.client.domain.ChangelogGroup;
import com.atlassian.jira.rest.client.domain.ChangelogItem;
import com.atlassian.jira.rest.client.domain.CimIssueType;
import com.atlassian.jira.rest.client.domain.CimProject;
import com.atlassian.jira.rest.client.domain.Comment;
import com.atlassian.jira.rest.client.domain.Issue;
import com.atlassian.jira.rest.client.domain.TimeTracking;
import com.atlassian.jira.rest.client.domain.Transition;
import com.atlassian.jira.rest.client.domain.Visibility;
import com.atlassian.jira.rest.client.domain.Votes;
import com.atlassian.jira.rest.client.domain.Watchers;
import com.atlassian.jira.rest.client.domain.Worklog;
import com.atlassian.jira.rest.client.domain.input.FieldInput;
import com.atlassian.jira.rest.client.domain.input.TransitionInput;
import com.atlassian.jira.rest.client.internal.json.TestConstants;
import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import org.joda.time.DateTime;
import org.joda.time.format.ISODateTimeFormat;
import org.junit.Test;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.List;

import static com.atlassian.jira.rest.client.IntegrationTestUtil.USER1;
import static com.atlassian.jira.rest.client.IntegrationTestUtil.USER2;
import static com.atlassian.jira.rest.client.TestUtil.assertErrorCode;
import static com.atlassian.jira.rest.client.internal.ServerVersionConstants.BN_JIRA_5;
import static com.google.common.collect.Iterables.toArray;
import static org.hamcrest.collection.IsIterableContainingInAnyOrder.containsInAnyOrder;
import static org.junit.Assert.*;

/**
 * Those tests mustn't change anything on server side, as jira is restored only once
 */
@SuppressWarnings("ConstantConditions") // To ignore "May produce NPE" warnings
@RestoreOnce(TestConstants.DEFAULT_JIRA_DUMP_FILE)
public class JerseyIssueRestClientReadOnlyTest extends AbstractJerseyRestClientTest {

	// no timezone here, as JIRA does not store timezone information in its dump file
	private final DateTime dateTime = ISODateTimeFormat.dateTimeParser().parseDateTime("2010-08-04T17:46:45.454");

	@Test
	public void testGetWatchers() throws Exception {
		final Issue issue = client.getIssueClient().getIssue("TST-1", new NullProgressMonitor());
		final Watchers watchers = client.getIssueClient().getWatchers(issue.getWatchers().getSelf(), new NullProgressMonitor());
		assertEquals(1, watchers.getNumWatchers());
		assertFalse(watchers.isWatching());
		assertThat(watchers.getUsers(), containsInAnyOrder(USER1));
	}

	@Test
	public void testGetWatcherForAnonymouslyAccessibleIssue() {
		setAnonymousMode();
		final Issue issue = client.getIssueClient().getIssue("ANNON-1", new NullProgressMonitor());
		final Watchers watchers = client.getIssueClient().getWatchers(issue.getWatchers().getSelf(), pm);
		assertEquals(1, watchers.getNumWatchers());
		assertFalse(watchers.isWatching());
		assertTrue("JRADEV-3594 bug!!!", Iterables.isEmpty(watchers.getUsers()));
		// to save time
		assertEquals(new TimeTracking(2700, 2400, null), issue.getTimeTracking());
	}

	@Test
	public void testGetIssueWithAnonymouslyCreatedAttachment() {
		setAnonymousMode();
		final Issue issue = client.getIssueClient().getIssue("ANONEDIT-1", new NullProgressMonitor());
		final Iterator<Attachment> attachmentIterator = issue.getAttachments().iterator();
		assertTrue(attachmentIterator.hasNext());
		assertNull(attachmentIterator.next().getAuthor());
	}

	@Test
	public void testGetIssueWithAnonymouslyCreatedWorklogEntry() {
		setAnonymousMode();
		final Issue issue = client.getIssueClient().getIssue("ANONEDIT-2", new NullProgressMonitor());
		final Iterator<Worklog> worklogIterator = issue.getWorklogs().iterator();
		assertTrue(worklogIterator.hasNext());
		assertNull(worklogIterator.next().getAuthor());
	}

	// URIs are broken in 5.0 - https://jdog.atlassian.com/browse/JRADEV-7691
	private void assertEqualsNoUri(BasicUser expected, BasicUser actual) {
		assertEquals(expected.getName(), actual.getName());
		assertEquals(expected.getDisplayName(), actual.getDisplayName());
	}


	@Test
	public void testGetIssue() throws Exception {
		final Issue issue = client.getIssueClient().getIssue("TST-1", pm);
		assertEquals("TST-1", issue.getKey());
		assertTrue(issue.getSelf().toString().startsWith(jiraUri.toString()));
		assertEqualsNoUri(IntegrationTestUtil.USER_ADMIN, issue.getReporter());
		assertEqualsNoUri(IntegrationTestUtil.USER_ADMIN, issue.getAssignee());

		assertThat(issue.getLabels(), containsInAnyOrder("a", "bcds"));

		assertEquals(3, Iterables.size(issue.getComments()));
		final Iterable<String> expectedExpandos = isJira5xOrNewer()
				? ImmutableList.of("renderedFields", "names", "schema", "transitions", "operations", "editmeta", "changelog") : ImmutableList.of("html");
		assertThat(ImmutableList.copyOf(issue.getExpandos()), containsInAnyOrder(toArray(expectedExpandos, String.class)));
		assertEquals(new TimeTracking(null, 0, 190), issue.getTimeTracking());
		assertTrue(Iterables.size(issue.getFields()) > 0);

		assertEquals(IntegrationTestUtil.START_PROGRESS_TRANSITION_ID, Iterables.size(issue.getAttachments()));
		final Iterable<Attachment> items = issue.getAttachments();
		assertNotNull(items);
		Attachment attachment1 = new Attachment(IntegrationTestUtil.concat(
				IntegrationTestUtil.TESTING_JIRA_5_OR_NEWER ? UriBuilder.fromUri(jiraUri).path("/rest/api/2/").build()
						: jiraRestRootUri, "/attachment/10040"),
				"dla Paw\u0142a.txt", IntegrationTestUtil.USER_ADMIN, dateTime, 643, "text/plain",
				IntegrationTestUtil.concat(jiraUri, "/secure/attachment/10040/dla+Paw%C5%82a.txt"), null);

		assertEquals(attachment1, items.iterator().next());

		// test for changelog
		assertNull(issue.getChangelog());

		final Issue issueWithChangelog = client.getIssueClient().getIssue("TST-2", EnumSet.of(IssueRestClient.Expandos.CHANGELOG), pm);
		final Iterable<ChangelogGroup> changelog = issueWithChangelog.getChangelog();
		if (isJira5xOrNewer()) {
			assertNotNull(changelog);
			final ChangelogGroup chg1 = Iterables.get(changelog, 18);
			assertEquals("admin", chg1.getAuthor().getName());
			assertEquals("Administrator", chg1.getAuthor().getDisplayName());
			assertEquals(new DateTime(2010, 8, 17, 16, 40, 34, 924).toInstant(), chg1.getCreated().toInstant());

			assertEquals(Collections
					.singletonList(new ChangelogItem(ChangelogItem.FieldType.JIRA, "status", "1", "Open", "3", "In Progress")), chg1
					.getItems());

			final ChangelogGroup chg2 = Iterables.get(changelog, 20);
			assertEquals("admin", chg2.getAuthor().getName());
			assertEquals("Administrator", chg2.getAuthor().getDisplayName());
			assertEquals(new DateTime(2010, 8, 24, 16, 10, 23, 468).toInstant(), chg2.getCreated().toInstant());

			final List<ChangelogItem> expected = ImmutableList.of(
					new ChangelogItem(ChangelogItem.FieldType.JIRA, "timeoriginalestimate", null, null, "0", "0"),
					new ChangelogItem(ChangelogItem.FieldType.CUSTOM, "My Radio buttons", null, null, null, "Another"),
					new ChangelogItem(ChangelogItem.FieldType.CUSTOM, "project3", null, null, "10000", "Test Project"),
					new ChangelogItem(ChangelogItem.FieldType.CUSTOM, "My Number Field New", null, null, null, "1.45")
			);
			assertEquals(expected, chg2.getItems());
		}
	}

	@Test
	public void testGetIssueWithNonTrivialComments() {
		final Issue issue = client.getIssueClient().getIssue("TST-2", pm);
		final Iterable<Comment> comments = issue.getComments();
		assertEquals(3, Iterables.size(comments));
		final Comment c1 = Iterables.get(comments, 0);
		assertEquals(Visibility.role("Administrators"), c1.getVisibility());

		final Comment c3 = Iterables.get(comments, 2);
		assertEquals(Visibility.group("jira-users"), c3.getVisibility());

	}

	@Test
	public void testGetIssueWithNoViewWatchersPermission() {
		setUser1();
		assertTrue(client.getIssueClient().getIssue("TST-1", pm).getWatchers().isWatching());

		setUser2();
		final Issue issue = client.getIssueClient().getIssue("TST-1", pm);
		assertFalse(issue.getWatchers().isWatching());
		client.getIssueClient().watch(issue.getWatchers().getSelf(), pm);
		final Issue watchedIssue = client.getIssueClient().getIssue("TST-1", pm);
		assertTrue(watchedIssue.getWatchers().isWatching());
		assertEquals(2, watchedIssue.getWatchers().getNumWatchers());

		// although there are 2 watchers, only one is listed with details - the caller itself, as the caller does not
		// have view watchers and voters permission
		assertThat(client.getIssueClient().getWatchers(watchedIssue.getWatchers().getSelf(), pm).getUsers(), containsInAnyOrder(USER2));
	}

	@Test
	public void testGetVoter() {
		final Issue issue = client.getIssueClient().getIssue("TST-1", pm);
		final Votes votes = client.getIssueClient().getVotes(issue.getVotes().getSelf(), pm);
		assertFalse(votes.hasVoted());
		assertThat(votes.getUsers(), containsInAnyOrder(USER1));
	}

	@Test
	public void testGetVotersWithoutViewIssuePermission() {
		final Issue issue = client.getIssueClient().getIssue("RST-1", pm);
		setUser2();
		final String optionalDot = isJira5xOrNewer() ? "." : "";
		assertErrorCode(Response.Status.FORBIDDEN, "You do not have the permission to see the specified issue" + optionalDot, new Runnable() {
			@Override
			public void run() {
				client.getIssueClient().getVotes(issue.getVotes().getSelf(), pm);
			}
		});
	}

	@Test
	public void testGetVotersWithoutViewVotersPermission() {
		setUser2();
		assertNumVotesAndNoVotersDetails("TST-1", 1);
	}

	@Test
	public void testGetVotersAnonymously() {
		setAnonymousMode();
		assertNumVotesAndNoVotersDetails("ANNON-1", 0);
	}


	private void assertNumVotesAndNoVotersDetails(final String issueKey, final int numVotes) {
		final Issue issue = client.getIssueClient().getIssue(issueKey, pm);
		assertEquals(numVotes, issue.getVotes().getVotes());
		assertFalse(issue.getVotes().hasVoted());
		final Votes votes = client.getIssueClient().getVotes(issue.getVotes().getSelf(), pm);
		assertFalse(votes.hasVoted());
		assertEquals(numVotes, votes.getVotes());
		assertTrue(Iterables.isEmpty(votes.getUsers()));
	}


	@Test
	public void testGetTransitions() throws Exception {
		final Issue issue = client.getIssueClient().getIssue("TST-1", new NullProgressMonitor());
		final Iterable<Transition> transitions = client.getIssueClient().getTransitions(issue, pm);
		assertEquals(4, Iterables.size(transitions));
		assertTrue(Iterables.contains(transitions, new Transition("Start Progress", IntegrationTestUtil.START_PROGRESS_TRANSITION_ID, Collections.<Transition.Field>emptyList())));
	}

	@Test
	public void testTransition() throws Exception {
		final Issue issue = client.getIssueClient().getIssue("TST-1", new NullProgressMonitor());
		final Iterable<Transition> transitions = client.getIssueClient().getTransitions(issue, pm);
		assertEquals(4, Iterables.size(transitions));
		final Transition startProgressTransition = new Transition("Start Progress", IntegrationTestUtil.START_PROGRESS_TRANSITION_ID, Collections.<Transition.Field>emptyList());
		assertTrue(Iterables.contains(transitions, startProgressTransition));

		client.getIssueClient().transition(issue, new TransitionInput(IntegrationTestUtil.START_PROGRESS_TRANSITION_ID,
				Collections.<FieldInput>emptyList(), Comment.valueOf("My test comment")), new NullProgressMonitor()) ;
		final Issue transitionedIssue = client.getIssueClient().getIssue("TST-1", new NullProgressMonitor());
		assertEquals("In Progress", transitionedIssue.getStatus().getName());
		final Iterable<Transition> transitionsAfterTransition = client.getIssueClient().getTransitions(issue, pm);
		assertFalse(Iterables.contains(transitionsAfterTransition, startProgressTransition));
		final Transition stopProgressTransition = new Transition("Stop Progress", IntegrationTestUtil.STOP_PROGRESS_TRANSITION_ID, Collections.<Transition.Field>emptyList());
		assertTrue(Iterables.contains(transitionsAfterTransition, stopProgressTransition));
	}

	@JiraBuildNumberDependent(BN_JIRA_5)
	@Test
	public void testGetCreateIssueMetadata() throws URISyntaxException {
		final Iterable<CimProject> metadataProjects = client
				.getIssueClient()
				.getCreateIssueMetadata(new GetCreateIssueMetadataOptionsBuilder().withExpandedIssueTypesFields().build(), pm);

		assertEquals(4, Iterables.size(metadataProjects));

		final CimProject project = Iterables.find(metadataProjects, new Predicate<CimProject>() {
			@Override
			public boolean apply(CimProject input) {
				return "ANONEDIT".equals(input.getKey());
			}
		});

		assertEquals(project.getName(), "Anonymous Editable Project");

		for (CimIssueType issueType : project.getIssueTypes()) {
			assertFalse(String.format("Issue type ('%s') fields are not empty!", issueType.getName()), issueType.getFields().isEmpty());
		}
	}

	@JiraBuildNumberDependent(BN_JIRA_5)
	@Test
	public void testGetCreateIssueMetadataWithFieldsNotExpanded() throws URISyntaxException {
		final Iterable<CimProject> metadataProjects = client
				.getIssueClient()
				.getCreateIssueMetadata(null, pm);

		assertEquals(4, Iterables.size(metadataProjects));

		final CimProject project = Iterables.find(metadataProjects, new Predicate<CimProject>() {
			@Override
			public boolean apply(CimProject input) {
				return "ANONEDIT".equals(input.getKey());
			}
		});

		assertEquals(project.getName(), "Anonymous Editable Project");
		assertEquals(5, Iterables.size(project.getIssueTypes()));

		for (CimIssueType issueType : project.getIssueTypes()) {
			assertTrue(issueType.getFields().isEmpty());
		}
	}

	@JiraBuildNumberDependent(BN_JIRA_5)
	@Test
	public void testGetCreateIssueMetadataWithProjectKeyFilter() throws URISyntaxException {
		final Iterable<CimProject> metadataProjects = client
				.getIssueClient()
				.getCreateIssueMetadata(
						new GetCreateIssueMetadataOptionsBuilder().withProjectKeys("ANONEDIT", "TST").withExpandedIssueTypesFields().build(),
						pm
				);

		assertEquals(2, Iterables.size(metadataProjects));

		final CimProject project = Iterables.find(metadataProjects, new Predicate<CimProject>() {
			@Override
			public boolean apply(CimProject input) {
				return "TST".equals(input.getKey());
			}
		});

		assertEquals(project.getName(), "Test Project");
		assertEquals(5, Iterables.size(project.getIssueTypes()));

		for (CimIssueType issueType : project.getIssueTypes()) {
			assertFalse(issueType.getFields().isEmpty());
		}
	}
}
