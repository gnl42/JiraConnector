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

import com.atlassian.jira.rest.client.IntegrationTestUtil;
import com.atlassian.jira.rest.client.IssueRestClient;
import com.atlassian.jira.rest.client.IterableMatcher;
import com.atlassian.jira.rest.client.NullProgressMonitor;
import com.atlassian.jira.rest.client.domain.Attachment;
import com.atlassian.jira.rest.client.domain.BasicUser;
import com.atlassian.jira.rest.client.domain.Comment;
import com.atlassian.jira.rest.client.domain.Issue;
import com.atlassian.jira.rest.client.domain.IssueLink;
import com.atlassian.jira.rest.client.domain.IssueLinkType;
import com.atlassian.jira.rest.client.domain.TimeTracking;
import com.atlassian.jira.rest.client.domain.Transition;
import com.atlassian.jira.rest.client.domain.Visibility;
import com.atlassian.jira.rest.client.domain.Votes;
import com.atlassian.jira.rest.client.domain.Watchers;
import com.atlassian.jira.rest.client.domain.input.AttachmentInput;
import com.atlassian.jira.rest.client.domain.input.FieldInput;
import com.atlassian.jira.rest.client.domain.input.LinkIssuesInput;
import com.atlassian.jira.rest.client.domain.input.TransitionInput;
import com.atlassian.jira.rest.client.internal.ServerVersionConstants;
import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import org.apache.commons.io.IOUtils;
import org.hamcrest.Matchers;
import org.joda.time.DateTime;
import org.joda.time.format.ISODateTimeFormat;
import org.junit.Assert;
import org.junit.Test;

import javax.annotation.Nullable;
import javax.ws.rs.core.Response;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.text.NumberFormat;
import java.util.Arrays;
import java.util.Collections;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.atlassian.jira.rest.client.IntegrationTestUtil.*;
import static com.atlassian.jira.rest.client.TestUtil.assertErrorCode;
import static com.atlassian.jira.rest.client.internal.json.TestConstants.USER1_USERNAME;
import static com.atlassian.jira.rest.client.internal.json.TestConstants.USER2_USERNAME;
import static org.junit.Assert.assertThat;


public class JerseyIssueRestClientTest extends AbstractRestoringJiraStateJerseyRestClientTest {

	// no timezone here, as JIRA does not store timezone information in its dump file
	private final DateTime dateTime = ISODateTimeFormat.dateTimeParser().parseDateTime("2010-08-04T17:46:45.454");

	@Test
	public void testGetWatchers() throws Exception {
		final Issue issue = client.getIssueClient().getIssue("TST-1", new NullProgressMonitor());
		final Watchers watchers = client.getIssueClient().getWatchers(issue.getWatchers().getSelf(), new NullProgressMonitor());
		assertEquals(1, watchers.getNumWatchers());
		assertFalse(watchers.isWatching());
		assertThat(watchers.getUsers(), IterableMatcher.hasOnlyElements(USER1));
	}

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

		assertEquals(3, Iterables.size(issue.getComments()));
		final Iterable<String> expectedExpandos = isJira5xOrNewer()
				? ImmutableList.of("renderedFields", "names", "schema", "transitions", "editmeta", "changelog") : ImmutableList.of("html");
		assertThat(ImmutableList.copyOf(issue.getExpandos()), IterableMatcher.hasOnlyElements(expectedExpandos));
		assertEquals(new TimeTracking(null, 0, 190), issue.getTimeTracking());
		assertTrue(Iterables.size(issue.getFields()) > 0);

		assertEquals(IntegrationTestUtil.START_PROGRESS_TRANSITION_ID, Iterables.size(issue.getAttachments()));
		final Iterable<Attachment> items = issue.getAttachments();
		assertNotNull(items);
		Attachment attachment1 = new Attachment(IntegrationTestUtil.concat(jiraRestRootUri, "/attachment/10040"),
				"dla Paw\u0142a.txt", IntegrationTestUtil.USER_ADMIN, dateTime, 643, "text/plain",
				IntegrationTestUtil.concat(jiraUri, "/secure/attachment/10040/dla+Paw%C5%82a.txt"), null);

		assertEquals(attachment1, items.iterator().next());

	}


	public void testGetIssueWithNonTrivialComments() {
		final Issue issue = client.getIssueClient().getIssue("TST-2", pm);
		final Iterable<Comment> comments = issue.getComments();
		assertEquals(3, Iterables.size(comments));
		final Comment c1 = Iterables.get(comments, 0);
		assertEquals(Visibility.role("Administrators"), c1.getVisibility());

		final Comment c3 = Iterables.get(comments, 2);
		assertEquals(Visibility.group("jira-users"), c3.getVisibility());

	}

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
		assertThat(client.getIssueClient().getWatchers(watchedIssue.getWatchers().getSelf(), pm).getUsers(), IterableMatcher.hasOnlyElements(USER2));
	}

	@Test
	public void testGetVoter() {
		final Issue issue = client.getIssueClient().getIssue("TST-1", pm);
		final Votes votes = client.getIssueClient().getVotes(issue.getVotes().getSelf(), pm);
		assertFalse(votes.hasVoted());
		assertThat(votes.getUsers(), IterableMatcher.hasOnlyElements(USER1));
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

	@Test
	public void testTransitionWithNumericCustomFieldPolishLocale() throws Exception {
		final double newValue = 123.45;
		final FieldInput fieldInput;
		if (IntegrationTestUtil.TESTING_JIRA_5_OR_NEWER) {
			fieldInput = new FieldInput(NUMERIC_CUSTOMFIELD_ID, Double.valueOf(newValue));
		} else {
			fieldInput = new FieldInput(NUMERIC_CUSTOMFIELD_ID, NumberFormat.getNumberInstance(new Locale("pl")).format(newValue));
		}
		assertTransitionWithNumericCustomField(fieldInput, newValue);
	}

	@Test
	public void testTransitionWithNumericCustomFieldEnglishLocale() throws Exception {
		setUser1();
		final double newValue = 123.45;
		final FieldInput fieldInput = new FieldInput(NUMERIC_CUSTOMFIELD_ID,
				NumberFormat.getNumberInstance(new Locale("pl")).format(newValue));

		assertErrorCode(Response.Status.BAD_REQUEST, "'" + fieldInput.getValue() + "' is an invalid number", new Runnable() {
			@Override
			public void run() {
				assertTransitionWithNumericCustomField(fieldInput, newValue);
			}
		});

		final FieldInput fieldInput2 = new FieldInput(NUMERIC_CUSTOMFIELD_ID, newValue); // this will be serialized always with "." according to JSL
		assertTransitionWithNumericCustomField(fieldInput2, newValue);

	}


	private void assertTransitionWithNumericCustomField(FieldInput fieldInput, Double expectedValue) {
		final Issue issue = client.getIssueClient().getIssue("TST-1", new NullProgressMonitor());
		assertNull(issue.getField(NUMERIC_CUSTOMFIELD_ID).getValue());
		final Iterable<Transition> transitions = client.getIssueClient().getTransitions(issue, pm);

		final Transition transitionFound = getTransitionByName(transitions, "Estimate");
		assertNotNull(transitionFound);
		assertTrue(Iterables.contains(transitionFound.getFields(),
				new Transition.Field(NUMERIC_CUSTOMFIELD_ID, false, IntegrationTestUtil.TESTING_JIRA_5_OR_NEWER ? NUMERIC_CUSTOMFIELD_TYPE_V5 : NUMERIC_CUSTOMFIELD_TYPE)));
		client.getIssueClient().transition(issue, new TransitionInput(transitionFound.getId(), Arrays.asList(fieldInput),
				Comment.valueOf("My test comment")), new NullProgressMonitor());
		final Issue changedIssue = client.getIssueClient().getIssue("TST-1", pm);
		assertTrue(changedIssue.getField(NUMERIC_CUSTOMFIELD_ID).getValue().equals(expectedValue));
	}

	@Test
	public void testTransitionWithNumericCustomFieldAndInteger() throws Exception {
		final Issue issue = client.getIssueClient().getIssue("TST-1", pm);
		assertNull(issue.getField(NUMERIC_CUSTOMFIELD_ID).getValue());
		final Iterable<Transition> transitions = client.getIssueClient().getTransitions(issue, pm);
		Transition transitionFound = getTransitionByName(transitions, "Estimate");

		assertNotNull(transitionFound);
		assertTrue(Iterables.contains(transitionFound.getFields(),
				new Transition.Field(NUMERIC_CUSTOMFIELD_ID, false, NUMERIC_CUSTOMFIELD_TYPE)));
		final double newValue = 123;
		final FieldInput fieldInput = new FieldInput(NUMERIC_CUSTOMFIELD_ID, newValue);
		client.getIssueClient().transition(issue, new TransitionInput(transitionFound.getId(), Arrays.asList(fieldInput),
				Comment.valueOf("My test comment")), pm);
		final Issue changedIssue = client.getIssueClient().getIssue("TST-1", pm);
		assertEquals(newValue, changedIssue.getField(NUMERIC_CUSTOMFIELD_ID).getValue());
	}

	@Test
	public void testTransitionWithInvalidNumericField() throws Exception {
		final Issue issue = client.getIssueClient().getIssue("TST-1", pm);
		assertNull(issue.getField(NUMERIC_CUSTOMFIELD_ID).getValue());
		final Iterable<Transition> transitions = client.getIssueClient().getTransitions(issue, pm);
		final Transition transitionFound = getTransitionByName(transitions, "Estimate");

		assertNotNull(transitionFound);
		assertTrue(Iterables.contains(transitionFound.getFields(),
				new Transition.Field(NUMERIC_CUSTOMFIELD_ID, false, NUMERIC_CUSTOMFIELD_TYPE)));
		final FieldInput fieldInput = new FieldInput(NUMERIC_CUSTOMFIELD_ID, "]432jl");
		// warning: Polish language here - I am asserting if the messages are indeed localized
		assertErrorCode(Response.Status.BAD_REQUEST, "']432jl' nie jest prawid\u0142ow\u0105 liczb\u0105", new Runnable() {
			@Override
			public void run() {
				client.getIssueClient().transition(issue, new TransitionInput(transitionFound.getId(), Arrays.asList(fieldInput),
						Comment.valueOf("My test comment")), pm);
			}
		});
	}

	@Test
	public void testTransitionWithNoRoleOrGroup() {
		Comment comment = Comment.valueOf("My text which I am just adding " + new DateTime());
		testTransitionImpl(comment);
	}

	@Test
	public void testTransitionWithRoleLevel() {
		Comment comment = Comment.createWithRoleLevel("My text which I am just adding " + new DateTime(), "Users");
		testTransitionImpl(comment);
	}

	@Test
	public void testTransitionWithGroupLevel() {
		Comment comment = Comment.createWithGroupLevel("My text which I am just adding " + new DateTime(), "jira-users");
		testTransitionImpl(comment);
	}

	@Test
	public void testTransitionWithInvalidRole() {
		final Comment comment = Comment.createWithRoleLevel("My text which I am just adding " + new DateTime(), "some-fake-role");
		assertInvalidCommentInput(comment, "Invalid role [some-fake-role]");
	}

	@Test
	public void testTransitionWithInvalidGroup() {
		final Comment comment = Comment.createWithGroupLevel("My text which I am just adding " + new DateTime(), "some-fake-group");
		assertInvalidCommentInput(comment, "Group: some-fake-group does not exist.");
	}

	private void assertInvalidCommentInput(final Comment comment, String expectedErrorMsg) {
		final Issue issue = client.getIssueClient().getIssue("TST-1", pm);
		final Iterable<Transition> transitions = client.getIssueClient().getTransitions(issue, pm);
		final Transition transitionFound = getTransitionByName(transitions, "Estimate");
		final String errorMsg = doesJiraServeCorrectlyErrorMessagesForBadRequestWhileTransitioningIssue()
				? expectedErrorMsg : null;
		assertErrorCode(Response.Status.BAD_REQUEST, errorMsg, new Runnable() {
			@Override
			public void run() {
				client.getIssueClient().transition(issue, new TransitionInput(transitionFound.getId(), comment), pm);
			}
		});
	}

	private void testTransitionImpl(Comment comment) {
		final Issue issue = client.getIssueClient().getIssue("TST-1", pm);
		final Iterable<Transition> transitions = client.getIssueClient().getTransitions(issue, pm);
		Transition transitionFound = getTransitionByName(transitions, "Estimate");
		DateTime now = new DateTime();
		client.getIssueClient().transition(issue, new TransitionInput(transitionFound.getId(), comment), pm);

		final Issue changedIssue = client.getIssueClient().getIssue("TST-1", pm);
		final Comment lastComment = Iterables.getLast(changedIssue.getComments());
		assertEquals(comment.getBody(), lastComment.getBody());
		assertEquals(USER_ADMIN, lastComment.getAuthor());
		assertEquals(USER_ADMIN, lastComment.getUpdateAuthor());
		assertEquals(lastComment.getCreationDate(), lastComment.getUpdateDate());
		assertTrue(lastComment.getCreationDate().isAfter(now) || lastComment.getCreationDate().isEqual(now));
		assertEquals(comment.getVisibility(), lastComment.getVisibility());
	}

	@Test
	public void testVoteUnvote() {
		final Issue issue1 = client.getIssueClient().getIssue("TST-1", pm);
		assertFalse(issue1.getVotes().hasVoted());
		assertEquals(1, issue1.getVotes().getVotes()); // the other user has voted
		final String expectedMessage = isJira5xOrNewer() // JIRA 5.0 comes without Polish translation OOB
				? "You cannot vote for an issue you have reported."
				: "Nie mo\u017cesz g\u0142osowa\u0107 na zadanie kt\u00f3re utworzy\u0142e\u015b.";

		// I hope that such Polish special characters (for better testing local specific behaviour of REST
		assertErrorCode(Response.Status.NOT_FOUND, expectedMessage, new Runnable() {
			@Override
			public void run() {
				client.getIssueClient().vote(issue1.getVotesUri(), pm);
			}
		});


		final String issueKey = "TST-7";
		Issue issue = client.getIssueClient().getIssue(issueKey, pm);
		assertFalse(issue.getVotes().hasVoted());
		assertEquals(0, issue.getVotes().getVotes());

		client.getIssueClient().vote(issue.getVotesUri(), pm);
		issue = client.getIssueClient().getIssue(issueKey, pm);
		assertTrue(issue.getVotes().hasVoted());
		assertEquals(1, issue.getVotes().getVotes());

		client.getIssueClient().unvote(issue.getVotesUri(), pm);
		issue = client.getIssueClient().getIssue(issueKey, pm);
		assertFalse(issue.getVotes().hasVoted());
		assertEquals(0, issue.getVotes().getVotes());

		setUser2();
		issue = client.getIssueClient().getIssue(issueKey, pm);
		assertFalse(issue.getVotes().hasVoted());
		assertEquals(0, issue.getVotes().getVotes());
		final Issue finalIssue = issue;
		assertErrorCode(Response.Status.NOT_FOUND, "Cannot remove a vote for an issue that the user has not already voted for.",
				new Runnable() {
			@Override
			public void run() {
				client.getIssueClient().unvote(finalIssue.getVotesUri(), pm);
			}
		});


		issue = client.getIssueClient().getIssue(issueKey, pm);
		assertFalse(issue.getVotes().hasVoted());
		assertEquals(0, issue.getVotes().getVotes());
		client.getIssueClient().vote(issue.getVotesUri(), pm);
		issue = client.getIssueClient().getIssue(issueKey, pm);
		assertTrue(issue.getVotes().hasVoted());
		assertEquals(1, issue.getVotes().getVotes());

		setClient(ADMIN_USERNAME, ADMIN_PASSWORD);
		client.getIssueClient().vote(issue.getVotesUri(), pm);
		issue = client.getIssueClient().getIssue(issueKey, pm);
		assertTrue(issue.getVotes().hasVoted());
		assertEquals(2, issue.getVotes().getVotes());
	}

	@Test
	public void testWatchUnwatch() {
		final IssueRestClient issueClient = client.getIssueClient();
		final Issue issue1 = issueClient.getIssue("TST-1", pm);

		Assert.assertThat(issueClient.getWatchers(issue1.getWatchers().getSelf(), pm).getUsers(),
				Matchers.not(IterableMatcher.contains(USER_ADMIN)));

		issueClient.watch(issue1.getWatchers().getSelf(), pm);
		Assert.assertThat(issueClient.getWatchers(issue1.getWatchers().getSelf(), pm).getUsers(), IterableMatcher.contains(USER_ADMIN));

		issueClient.unwatch(issue1.getWatchers().getSelf(), pm);
		Assert.assertThat(issueClient.getWatchers(issue1.getWatchers().getSelf(), pm).getUsers(), Matchers.not(IterableMatcher.contains(USER_ADMIN)));

		Assert.assertThat(issueClient.getWatchers(issue1.getWatchers().getSelf(), pm).getUsers(), IterableMatcher.contains(USER1));
		issueClient.removeWatcher(issue1.getWatchers().getSelf(), USER1.getName(), pm);
		Assert.assertThat(issueClient.getWatchers(issue1.getWatchers().getSelf(), pm).getUsers(), Matchers.not(IterableMatcher.contains(USER1)));
		issueClient.addWatcher(issue1.getWatchers().getSelf(), USER1.getName(), pm);
		Assert.assertThat(issueClient.getWatchers(issue1.getWatchers().getSelf(), pm).getUsers(), IterableMatcher.contains(USER1));
	}

	@Test
	public void testRemoveWatcherUnauthorized() {
		final IssueRestClient issueClient = client.getIssueClient();
		final Issue issue1 = issueClient.getIssue("TST-1", pm);
		issueClient.watch(issue1.getWatchers().getSelf(), pm);

		setUser1();
		final IssueRestClient issueClient2 = client.getIssueClient();
		assertErrorCode(Response.Status.UNAUTHORIZED,
				"User 'wseliga' is not allowed to remove watchers from issue 'TST-1'", new Runnable() {
			@Override
			public void run() {
				issueClient2.removeWatcher(issue1.getWatchers().getSelf(), ADMIN_USERNAME, pm);
			}
		});
	}


	@Test
	public void testWatchAlreadyWatched() {
		setUser1();
		final IssueRestClient issueClient = client.getIssueClient();
		final Issue issue = issueClient.getIssue("TST-1", pm);
		Assert.assertThat(client.getIssueClient().getWatchers(issue.getWatchers().getSelf(), pm).getUsers(), IterableMatcher.contains(USER1));
		// JIRA allows to watch already watched issue by you - such action effectively has no effect
		issueClient.watch(issue.getWatchers().getSelf(), pm);
		Assert.assertThat(client.getIssueClient().getWatchers(issue.getWatchers().getSelf(), pm).getUsers(), IterableMatcher.contains(USER1));
	}

	@Test
	public void testAddWatcherUnauthorized() {
		final IssueRestClient issueClient = client.getIssueClient();
		final Issue issue1 = issueClient.getIssue("TST-1", pm);
		issueClient.addWatcher(issue1.getWatchers().getSelf(), USER1_USERNAME, pm);
		assertThat(client.getIssueClient().getWatchers(issue1.getWatchers().getSelf(), pm).getUsers(), IterableMatcher.contains(USER1));

		setUser1();
		assertTrue(client.getIssueClient().getIssue("TST-1", pm).getWatchers().isWatching());
		String expectedErrorMsg = isJraDev3516Fixed() ? ("User '" + USER1_USERNAME
				+ "' is not allowed to add watchers to issue 'TST-1'") : null;
		assertErrorCode(Response.Status.UNAUTHORIZED, expectedErrorMsg, new Runnable() {
			@Override
			public void run() {
				client.getIssueClient().addWatcher(issue1.getWatchers().getSelf(), ADMIN_USERNAME, pm);
			}
		});
	}

	private boolean isJraDev3516Fixed() {
		return client.getMetadataClient().getServerInfo(pm).getBuildNumber() >= ServerVersionConstants.BN_JIRA_4_3;
	}

	//@Test restore when JRADEV-3666 is fixed (I don't want to pollute JRJC integration test results)
	public void xtestAddWatcherWhoDoesNotHaveViewIssuePermissions() {
		final IssueRestClient issueClient = client.getIssueClient();
		final Issue issue1 = issueClient.getIssue("RST-1", pm);
		assertErrorCode(Response.Status.BAD_REQUEST, "The user \"" + USER2_USERNAME
				+ "\" does not have permission to view this issue. This user will not be added to the watch list.",
				new Runnable() {
					@Override
					public void run() {
						issueClient.addWatcher(issue1.getWatchers().getSelf(), USER2_USERNAME, pm);
					}
				});

	}

	@Test
	public void testLinkIssuesWithRoleLevel() {
		testLinkIssuesImpl(Comment.createWithRoleLevel("A comment about linking", "Administrators"));
	}

	@Test
	public void testLinkIssuesWithGroupLevel() {
		testLinkIssuesImpl(Comment.createWithGroupLevel("A comment about linking", "jira-administrators"));
	}

	@Test
	public void testLinkIssuesWithSimpleComment() {
		testLinkIssuesImpl(Comment.valueOf("A comment about linking"));
	}

	@Test
	public void testLinkIssuesWithoutComment() {
		testLinkIssuesImpl(null);
	}

	@Test
	public void testLinkIssuesWithInvalidParams() {
		if (!doesJiraSupportRestIssueLinking()) {
			return;
		}
		assertErrorCode(Response.Status.NOT_FOUND, "The issue no longer exists.", new Runnable() {
			@Override
			public void run() {
				client.getIssueClient().linkIssue(new LinkIssuesInput("TST-7", "FAKEKEY-1", "Duplicate", null), pm);
			}
		});

		assertErrorCode(Response.Status.NOT_FOUND, "No issue link type with name 'NonExistingLinkType' found.", new Runnable() {
			@Override
			public void run() {
				client.getIssueClient().linkIssue(new LinkIssuesInput("TST-7", "TST-6", "NonExistingLinkType", null), pm);
			}
		});

		setUser1();
		final String optionalDot = isJira5xOrNewer() ? "." : "";
		assertErrorCode(Response.Status.NOT_FOUND, "You do not have the permission to see the specified issue" + optionalDot, new Runnable() {
			@Override
			public void run() {
				client.getIssueClient().linkIssue(new LinkIssuesInput("TST-7", "RST-1", "Duplicate", null), pm);
			}
		});
		assertErrorCode(Response.Status.BAD_REQUEST, "Failed to create comment for issue 'TST-6'\nYou are currently not a member of the project role: Administrators.", new Runnable() {
			@Override
			public void run() {
				client.getIssueClient().linkIssue(new LinkIssuesInput("TST-7", "TST-6", "Duplicate",
						Comment.createWithRoleLevel("my body", "Administrators")), pm);
			}
		});
		assertErrorCode(Response.Status.BAD_REQUEST, "You are currently not a member of the group: jira-administrators.", new Runnable() {
			@Override
			public void run() {
				client.getIssueClient().linkIssue(new LinkIssuesInput("TST-7", "TST-6", "Duplicate",
						Comment.createWithGroupLevel("my body", "jira-administrators")), pm);
			}
		});
		assertErrorCode(Response.Status.BAD_REQUEST, "Group: somefakegroup does not exist.", new Runnable() {
			@Override
			public void run() {
				client.getIssueClient().linkIssue(new LinkIssuesInput("TST-7", "TST-6", "Duplicate",
						Comment.createWithGroupLevel("my body", "somefakegroup")), pm);
			}
		});


		setUser2();
		assertErrorCode(Response.Status.UNAUTHORIZED, "No Link Issue Permission for issue 'TST-7'", new Runnable() {
			@Override
			public void run() {
				client.getIssueClient().linkIssue(new LinkIssuesInput("TST-7", "TST-6", "Duplicate", null), pm);
			}
		});

	}


	private void testLinkIssuesImpl(@Nullable Comment commentInput) {
		if (!doesJiraSupportRestIssueLinking()) {
			return;
		}

		final IssueRestClient issueClient = client.getIssueClient();
		final Issue originalIssue = issueClient.getIssue("TST-7", pm);
		int origNumComments = Iterables.size(originalIssue.getComments());
		assertFalse(originalIssue.getIssueLinks().iterator().hasNext());

		issueClient.linkIssue(new LinkIssuesInput("TST-7", "TST-6", "Duplicate", commentInput), pm);

		final Issue linkedIssue = issueClient.getIssue("TST-7", pm);
		assertEquals(1, Iterables.size(linkedIssue.getIssueLinks()));
		final IssueLink addedLink = linkedIssue.getIssueLinks().iterator().next();
		assertEquals("Duplicate", addedLink.getIssueLinkType().getName());
		assertEquals("TST-6", addedLink.getTargetIssueKey());
		assertEquals(IssueLinkType.Direction.OUTBOUND, addedLink.getIssueLinkType().getDirection());

		final int expectedNumComments = commentInput != null ? origNumComments + 1 : origNumComments;
		assertEquals(expectedNumComments, Iterables.size(linkedIssue.getComments()));
		if (commentInput != null) {
			final Comment comment = linkedIssue.getComments().iterator().next();
			assertEquals(commentInput.getBody(), comment.getBody());
			assertEquals(IntegrationTestUtil.USER_ADMIN, comment.getAuthor());
			assertEquals(commentInput.getVisibility(), comment.getVisibility());
		} else {
			assertFalse(linkedIssue.getComments().iterator().hasNext());
		}


		final Issue targetIssue = issueClient.getIssue("TST-6", pm);
		final IssueLink targetLink = targetIssue.getIssueLinks().iterator().next();
		assertEquals(IssueLinkType.Direction.INBOUND, targetLink.getIssueLinkType().getDirection());
		assertEquals("Duplicate", targetLink.getIssueLinkType().getName());
	}

	private boolean doesJiraSupportRestIssueLinking() {
		return client.getMetadataClient().getServerInfo(pm).getBuildNumber() >= ServerVersionConstants.BN_JIRA_4_3;
	}

	private boolean doesJiraSupportAddingAttachment() {
		return client.getMetadataClient().getServerInfo(pm).getBuildNumber() >= ServerVersionConstants.BN_JIRA_4_3;
	}

	private boolean doesJiraServeCorrectlyErrorMessagesForBadRequestWhileTransitioningIssue() {
		return client.getMetadataClient().getServerInfo(pm).getBuildNumber() >= ServerVersionConstants.BN_JIRA_4_3;
	}

	@Test
	public void testAddAttachment() throws IOException {
		if (!doesJiraSupportAddingAttachment()) {
			return;
		}
		final IssueRestClient issueClient = client.getIssueClient();
		final Issue issue = issueClient.getIssue("TST-3", pm);
		assertFalse(issue.getAttachments().iterator().hasNext());

		String str = "Wojtek";
		final String filename1 = "my-test-file";
		issueClient.addAttachment(pm, issue.getAttachmentsUri(), new ByteArrayInputStream(str.getBytes("UTF-8")), filename1);
		final String filename2 = "my-picture.png";
		issueClient.addAttachment(pm, issue.getAttachmentsUri(), JerseyIssueRestClientTest.class.getResourceAsStream("/attachment-test/transparent-png.png"), filename2);

		final Issue issueWithAttachments = issueClient.getIssue("TST-3", pm);
		final Iterable<Attachment> attachments = issueWithAttachments.getAttachments();
		assertEquals(2, Iterables.size(attachments));
		final Iterable<String> attachmentsNames = Iterables.transform(attachments, new Function<Attachment, String>() {
			@Override
			public String apply(@Nullable Attachment from) {
				return from.getFilename();
			}
		});
		assertThat(attachmentsNames, IterableMatcher.hasOnlyElements(filename1, filename2));
		final Attachment pictureAttachment = Iterables.find(attachments, new Predicate<Attachment>() {
			@Override
			public boolean apply(@Nullable Attachment input) {
				return filename2.equals(input.getFilename());
			}
		});

		// let's download it now and compare it's binary content

		assertTrue(
				IOUtils.contentEquals(JerseyIssueRestClientTest.class.getResourceAsStream("/attachment-test/transparent-png.png"),
						issueClient.getAttachment(pm, pictureAttachment.getContentUri())));
	}

	@Test
	public void testAddAttachments() throws IOException {
		if (!doesJiraSupportAddingAttachment()) {
			return;
		}
		final IssueRestClient issueClient = client.getIssueClient();
		final Issue issue = issueClient.getIssue("TST-4", pm);
		assertFalse(issue.getAttachments().iterator().hasNext());

		final AttachmentInput[] attachmentInputs = new AttachmentInput[3];
		for (int i = 1; i <= 3; i++) {
			attachmentInputs[i - 1] = new AttachmentInput("my-test-file-" + i + ".txt", new ByteArrayInputStream(("content-of-the-file-" + i).getBytes("UTF-8")));
		}
		issueClient.addAttachments(pm, issue.getAttachmentsUri(), attachmentInputs);

		final Issue issueWithAttachments = issueClient.getIssue("TST-4", pm);
		final Iterable<Attachment> attachments = issueWithAttachments.getAttachments();
		assertEquals(3, Iterables.size(attachments));
		Pattern pattern = Pattern.compile("my-test-file-(\\d)\\.txt");
		for (Attachment attachment : attachments) {
			assertTrue(pattern.matcher(attachment.getFilename()).matches());
			final Matcher matcher = pattern.matcher(attachment.getFilename());
			matcher.find();
			final String interfix = matcher.group(1);
			assertTrue(IOUtils.contentEquals(new ByteArrayInputStream(("content-of-the-file-" + interfix).getBytes("UTF-8")),
					issueClient.getAttachment(pm, attachment.getContentUri())));

		}
	}

	@Test
	public void testAddFileAttachments() throws IOException {
		if (!doesJiraSupportAddingAttachment()) {
			return;
		}
		final IssueRestClient issueClient = client.getIssueClient();
		final Issue issue = issueClient.getIssue("TST-5", pm);
		assertFalse(issue.getAttachments().iterator().hasNext());

		final File tempFile = File.createTempFile("jim-integration-test", ".txt");
		tempFile.deleteOnExit();
		FileWriter writer = new FileWriter(tempFile);
		writer.write("This is the content of my file which I am going to upload to JIRA for testing.");
		writer.close();
		issueClient.addAttachments(pm, issue.getAttachmentsUri(), tempFile);

		final Issue issueWithAttachments = issueClient.getIssue("TST-5", pm);
		final Iterable<Attachment> attachments = issueWithAttachments.getAttachments();
		assertEquals(1, Iterables.size(attachments));
		assertTrue(IOUtils.contentEquals(new FileInputStream(tempFile),
				issueClient.getAttachment(pm, attachments.iterator().next().getContentUri())));
	}


	@Test
	public void testFetchingUnassignedIssue() {
		administration.generalConfiguration().setAllowUnassignedIssues(true);
		assertEquals(IntegrationTestUtil.USER_ADMIN, client.getIssueClient().getIssue("TST-5", pm).getAssignee());

		navigation.userProfile().changeUserLanguage("angielski (UK)");
		navigation.issue().unassignIssue("TST-5", "unassigning issue");
		// this single line does instead of 2 above - func test suck with non-English locale
		// but it does not work yet with JIRA 5.0-resthack...
		//navigation.issue().assignIssue("TST-5", "unassigning issue", "Nieprzydzielone");

		assertNull(client.getIssueClient().getIssue("TST-5", pm).getAssignee());
	}

	@Test
	public void testFetchingIssueWithAnonymousComment() {
		navigation.userProfile().changeUserLanguage("angielski (UK)");
		administration.permissionSchemes().scheme("Anonymous Permission Scheme").grantPermissionToGroup(15, "");
		assertEquals(IntegrationTestUtil.USER_ADMIN, client.getIssueClient().getIssue("TST-5", pm).getAssignee());
		navigation.logout();
		navigation.issue().addComment("ANNON-1", "my nice comment");
		final Issue issue = client.getIssueClient().getIssue("ANNON-1", pm);
		assertEquals(1, Iterables.size(issue.getComments()));
		final Comment comment = issue.getComments().iterator().next();
		assertEquals("my nice comment", comment.getBody());
		assertNull(comment.getAuthor());
		assertNull(comment.getUpdateAuthor());

	}

}
