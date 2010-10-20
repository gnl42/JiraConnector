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

import com.atlassian.jira.restjavaclient.IntegrationTestUtil;
import com.atlassian.jira.restjavaclient.IssueRestClient;
import com.atlassian.jira.restjavaclient.IterableMatcher;
import com.atlassian.jira.restjavaclient.NullProgressMonitor;
import com.atlassian.jira.restjavaclient.domain.Attachment;
import com.atlassian.jira.restjavaclient.domain.Comment;
import com.atlassian.jira.restjavaclient.domain.FieldInput;
import com.atlassian.jira.restjavaclient.domain.Issue;
import com.atlassian.jira.restjavaclient.domain.Transition;
import com.atlassian.jira.restjavaclient.domain.TransitionInput;
import com.atlassian.jira.restjavaclient.domain.Votes;
import com.atlassian.jira.restjavaclient.domain.Watchers;
import com.google.common.collect.Iterables;
import org.hamcrest.Matchers;
import org.joda.time.DateTime;
import org.joda.time.format.ISODateTimeFormat;
import org.junit.Assert;
import org.junit.Test;

import javax.ws.rs.core.Response;
import java.text.NumberFormat;
import java.util.Arrays;
import java.util.Collections;
import java.util.Locale;

import static com.atlassian.jira.restjavaclient.IntegrationTestUtil.*;
import static com.atlassian.jira.restjavaclient.TestUtil.assertErrorCode;
import static com.atlassian.jira.restjavaclient.json.TestConstants.USER1_USERNAME;
import static com.atlassian.jira.restjavaclient.json.TestConstants.USER2_USERNAME;
import static org.junit.Assert.assertThat;


/**
 * TODO: Document this class / interface here
 *
 * @since v0.1
 */
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
	}


	@Test
	public void testGetIssue() throws Exception {
		final Issue issue = client.getIssueClient().getIssue("TST-1", pm);
		assertEquals("TST-1", issue.getKey());
		assertTrue(issue.getSelf().toString().startsWith(jiraUri.toString()));
		assertEquals(IntegrationTestUtil.USER_ADMIN, issue.getReporter());
		assertEquals(IntegrationTestUtil.USER_ADMIN, issue.getAssignee());

		assertEquals(3, Iterables.size(issue.getComments()));
		assertThat(issue.getExpandos(), IterableMatcher.hasOnlyElements("html"));
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
		assertEquals("Administrators", c1.getRoleLevel());
		assertNull(c1.getGroupLevel());

		final Comment c3 = Iterables.get(comments, 2);
		assertEquals("jira-users", c3.getGroupLevel());
		assertNull(c3.getRoleLevel());

	}

	public void testGetIssueWithNoViewWatchersPermission() {
		setUser1();
		assertTrue(client.getIssueClient().getIssue("TST-1", pm).getWatchers().isWatching());

		setUser2();
		final Issue issue = client.getIssueClient().getIssue("TST-1", pm);
		assertFalse(issue.getWatchers().isWatching());
		client.getIssueClient().watch(issue, pm);
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
		assertErrorCode(Response.Status.FORBIDDEN, "You do not have the permission to see the specified issue", new Runnable() {
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
		final Iterable<Transition> transitions = client.getIssueClient().getTransitions(issue.getTransitionsUri(), pm);
		assertEquals(4, Iterables.size(transitions));
		assertTrue(Iterables.contains(transitions, new Transition("Start Progress", IntegrationTestUtil.START_PROGRESS_TRANSITION_ID, Collections.<Transition.Field>emptyList())));
	}

	@Test
	public void testTransition() throws Exception {
		final Issue issue = client.getIssueClient().getIssue("TST-1", new NullProgressMonitor());
		final Iterable<Transition> transitions = client.getIssueClient().getTransitions(issue.getTransitionsUri(), pm);
		assertEquals(4, Iterables.size(transitions));
		final Transition startProgressTransition = new Transition("Start Progress", IntegrationTestUtil.START_PROGRESS_TRANSITION_ID, Collections.<Transition.Field>emptyList());
		assertTrue(Iterables.contains(transitions, startProgressTransition));

		client.getIssueClient().transition(issue.getTransitionsUri(), new TransitionInput(IntegrationTestUtil.START_PROGRESS_TRANSITION_ID,
				Collections.<FieldInput>emptyList(), Comment.valueOf("My test comment")), new NullProgressMonitor()) ;
		final Issue transitionedIssue = client.getIssueClient().getIssue("TST-1", new NullProgressMonitor());
		assertEquals("In Progress", transitionedIssue.getStatus().getName());
		final Iterable<Transition> transitionsAfterTransition = client.getIssueClient().getTransitions(issue.getTransitionsUri(), pm);
		assertFalse(Iterables.contains(transitionsAfterTransition, startProgressTransition));
		final Transition stopProgressTransition = new Transition("Stop Progress", IntegrationTestUtil.STOP_PROGRESS_TRANSITION_ID, Collections.<Transition.Field>emptyList());
		assertTrue(Iterables.contains(transitionsAfterTransition, stopProgressTransition));
	}


	@Test
	public void testTransitionWithNumericCustomFieldPolishLocale() throws Exception {
		final double newValue = 123.45;
		final FieldInput fieldInput = new FieldInput(NUMERIC_CUSTOMFIELD_ID,
				NumberFormat.getNumberInstance(new Locale("pl")).format(newValue));
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
		final Iterable<Transition> transitions = client.getIssueClient().getTransitions(issue.getTransitionsUri(), pm);

		final Transition transitionFound = getTransitionByName(transitions, "Estimate");
		assertNotNull(transitionFound);
		assertTrue(Iterables.contains(transitionFound.getFields(),
				new Transition.Field(NUMERIC_CUSTOMFIELD_ID, false, NUMERIC_CUSTOMFIELD_TYPE)));
		client.getIssueClient().transition(issue.getTransitionsUri(), new TransitionInput(transitionFound.getId(), Arrays.asList(fieldInput),
				Comment.valueOf("My test comment")), new NullProgressMonitor());
		final Issue changedIssue = client.getIssueClient().getIssue("TST-1", pm);
		assertTrue(changedIssue.getField(NUMERIC_CUSTOMFIELD_ID).getValue().equals(expectedValue));
	}

	@Test
	public void testTransitionWithNumericCustomFieldAndInteger() throws Exception {
		final Issue issue = client.getIssueClient().getIssue("TST-1", pm);
		assertNull(issue.getField(NUMERIC_CUSTOMFIELD_ID).getValue());
		final Iterable<Transition> transitions = client.getIssueClient().getTransitions(issue.getTransitionsUri(), pm);
		Transition transitionFound = getTransitionByName(transitions, "Estimate");

		assertNotNull(transitionFound);
		assertTrue(Iterables.contains(transitionFound.getFields(),
				new Transition.Field(NUMERIC_CUSTOMFIELD_ID, false, NUMERIC_CUSTOMFIELD_TYPE)));
		final double newValue = 123;
		final FieldInput fieldInput = new FieldInput(NUMERIC_CUSTOMFIELD_ID, newValue);
		client.getIssueClient().transition(issue.getTransitionsUri(), new TransitionInput(transitionFound.getId(), Arrays.asList(fieldInput),
				Comment.valueOf("My test comment")), pm);
		final Issue changedIssue = client.getIssueClient().getIssue("TST-1", pm);
		assertEquals(newValue, changedIssue.getField(NUMERIC_CUSTOMFIELD_ID).getValue());
	}

	@Test
	public void testTransitionWithInvalidNumericField() throws Exception {
		final Issue issue = client.getIssueClient().getIssue("TST-1", pm);
		assertNull(issue.getField(NUMERIC_CUSTOMFIELD_ID).getValue());
		final Iterable<Transition> transitions = client.getIssueClient().getTransitions(issue.getTransitionsUri(), pm);
		final Transition transitionFound = getTransitionByName(transitions, "Estimate");

		assertNotNull(transitionFound);
		assertTrue(Iterables.contains(transitionFound.getFields(),
				new Transition.Field(NUMERIC_CUSTOMFIELD_ID, false, NUMERIC_CUSTOMFIELD_TYPE)));
		final FieldInput fieldInput = new FieldInput(NUMERIC_CUSTOMFIELD_ID, "]432jl");
		// warning: Polish language here - I am asserting if the messages are indeed localized
		assertErrorCode(Response.Status.BAD_REQUEST, "']432jl' nie jest prawid\u0142ow\u0105 liczb\u0105", new Runnable() {
			@Override
			public void run() {
				client.getIssueClient().transition(issue.getTransitionsUri(), new TransitionInput(transitionFound.getId(), Arrays.asList(fieldInput),
						Comment.valueOf("My test comment")), pm);
			}
		});
	}


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
		final Issue issue = client.getIssueClient().getIssue("TST-1", pm);
		final Iterable<Transition> transitions = client.getIssueClient().getTransitions(issue.getTransitionsUri(), pm);
		final Transition transitionFound = getTransitionByName(transitions, "Estimate");
		// @todo restore asserting for error message when JRA-22516 is fixed
		assertErrorCode(Response.Status.BAD_REQUEST, /*"Invalid role [some-fake-role]", */new Runnable() {
			@Override
			public void run() {
				client.getIssueClient().transition(issue.getTransitionsUri(), new TransitionInput(transitionFound.getId(), comment), pm);
			}
		});
	}

	private void testTransitionImpl(Comment comment) {
		final Issue issue = client.getIssueClient().getIssue("TST-1", pm);
		final Iterable<Transition> transitions = client.getIssueClient().getTransitions(issue.getTransitionsUri(), pm);
		Transition transitionFound = getTransitionByName(transitions, "Estimate");
		DateTime now = new DateTime();
		client.getIssueClient().transition(issue.getTransitionsUri(), new TransitionInput(transitionFound.getId(), comment), pm);

		final Issue changedIssue = client.getIssueClient().getIssue("TST-1", pm);
		final Comment lastComment = Iterables.getLast(changedIssue.getComments());
		assertEquals(comment.getBody(), lastComment.getBody());
		assertEquals(USER_ADMIN, lastComment.getAuthor());
		assertEquals(USER_ADMIN, lastComment.getUpdateAuthor());
		assertEquals(lastComment.getCreationDate(), lastComment.getUpdateDate());
		assertTrue(lastComment.getCreationDate().isAfter(now) || lastComment.getCreationDate().isEqual(now));
		assertEquals(comment.getGroupLevel(), lastComment.getGroupLevel());
		assertEquals(comment.getRoleLevel(), lastComment.getRoleLevel());
	}

	@Test
	public void testVoteUnvote() {
		final Issue issue1 = client.getIssueClient().getIssue("TST-1", pm);
		assertFalse(issue1.getVotes().hasVoted());
		assertEquals(1, issue1.getVotes().getVotes()); // the other user has voted

		// I hope that such Polish special characters (for better testing local specific behaviour of REST
		assertErrorCode(Response.Status.NOT_FOUND, "Nie mo\u017cesz g\u0142osowa\u0107 na zadanie kt\u00f3re utworzy\u0142e\u015b.", new Runnable() {
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
		assertErrorCode(Response.Status.NOT_FOUND, "Cannot remove a vote for an issue that the user has not already voted for.", new Runnable() {
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

		issueClient.watch(issue1, pm);
		Assert.assertThat(issueClient.getWatchers(issue1.getWatchers().getSelf(), pm).getUsers(), IterableMatcher.contains(USER_ADMIN));

		issueClient.unwatch(issue1, pm);
		Assert.assertThat(issueClient.getWatchers(issue1.getWatchers().getSelf(), pm).getUsers(), Matchers.not(IterableMatcher.contains(USER_ADMIN)));

		Assert.assertThat(issueClient.getWatchers(issue1.getWatchers().getSelf(), pm).getUsers(), IterableMatcher.contains(USER1));
		issueClient.removeWatcher(issue1, USER1.getName(), pm);
		Assert.assertThat(issueClient.getWatchers(issue1.getWatchers().getSelf(), pm).getUsers(), Matchers.not(IterableMatcher.contains(USER1)));
		issueClient.addWatcher(issue1, USER1.getName(), pm);
		Assert.assertThat(issueClient.getWatchers(issue1.getWatchers().getSelf(), pm).getUsers(), IterableMatcher.contains(USER1));
	}

	@Test
	public void testRemoveWatcherUnauthorized() {
		final IssueRestClient issueClient = client.getIssueClient();
		final Issue issue1 = issueClient.getIssue("TST-1", pm);
		issueClient.watch(issue1, pm);

		setUser1();
		final IssueRestClient issueClient2 = client.getIssueClient();
		assertErrorCode(Response.Status.UNAUTHORIZED,
				"User 'wseliga' is not allowed to remove watchers from issue 'TST-1'", new Runnable() {
			@Override
			public void run() {
				issueClient2.removeWatcher(issue1, ADMIN_USERNAME, pm);
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
		issueClient.watch(issue, pm);
		Assert.assertThat(client.getIssueClient().getWatchers(issue.getWatchers().getSelf(), pm).getUsers(), IterableMatcher.contains(USER1));
	}

	@Test
	public void testAddWatcherUnauthorized() {
		final IssueRestClient issueClient = client.getIssueClient();
		final Issue issue1 = issueClient.getIssue("TST-1", pm);
		issueClient.addWatcher(issue1, USER1_USERNAME, pm);
		assertThat(client.getIssueClient().getWatchers(issue1.getWatchers().getSelf(), pm).getUsers(), IterableMatcher.contains(USER1));

		setUser1();
		assertTrue(client.getIssueClient().getIssue("TST-1", pm).getWatchers().isWatching());

		// @todo restore assertion for the message when JRADEV-3516 is fixed
		assertErrorCode(Response.Status.UNAUTHORIZED/*, "User '" + USER1_USERNAME
				+ "' is not allowed to add watchers to issue 'TST-1'"*/, new Runnable() {
			@Override
			public void run() {
				client.getIssueClient().addWatcher(issue1, ADMIN_USERNAME, pm);
			}
		});
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
						issueClient.addWatcher(issue1, USER2_USERNAME, pm);
					}
				});

	}
}
