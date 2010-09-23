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

import com.atlassian.jira.restjavaclient.*;
import com.atlassian.jira.restjavaclient.domain.*;
import com.atlassian.jira.restjavaclient.json.TestConstants;
import com.google.common.collect.Iterables;
import org.joda.time.DateTime;
import org.joda.time.format.ISODateTimeFormat;
import org.junit.Ignore;
import org.junit.Test;

import javax.ws.rs.core.UriBuilder;
import java.net.URI;
import java.text.NumberFormat;
import java.util.Arrays;
import java.util.Collections;
import java.util.Locale;

import static com.atlassian.jira.restjavaclient.IntegrationTestUtil.NUMERIC_CUSTOMFIELD_ID;
import static com.atlassian.jira.restjavaclient.IntegrationTestUtil.NUMERIC_CUSTOMFIELD_TYPE;
import static org.junit.Assert.assertThat;


/**
 * TODO: Document this class / interface here
 *
 * @since v0.1
 */
public class JerseyIssueRestClientTest extends AbstractJerseyRestClientTest {

	// no timezone here, as JIRA does not store timezone information in its dump file
	private final DateTime dateTime = ISODateTimeFormat.dateTimeParser().parseDateTime("2010-08-04T17:46:45.454");

	@Test
	public void testGetWatchers() throws Exception {
		final Issue issue = client.getIssueClient().getIssue(new IssueArgsBuilder("TST-1").build(), new NullProgressMonitor());
		final Watchers watchers = client.getIssueClient().getWatchers(issue, new NullProgressMonitor());
		assertEquals(1, watchers.getNumWatchers());
		assertFalse(watchers.isWatching());
		assertThat(watchers.getWatchers(), IterableMatcher.hasOnlyElements(IntegrationTestUtil.USER1));
	}

	public URI jiraRestUri(String path) {
		return UriBuilder.fromUri(jiraRestRootUri).path(path).build();
	}

	@Test
	public void testGetIssue() throws Exception {
		final Issue issue = client.getIssueClient().getIssue(
				new IssueArgsBuilder("TST-1").withAttachments(true).withComments(true).withWorklogs(true).withWatchers(true).build(),
				new NullProgressMonitor());
		assertEquals("TST-1", issue.getKey());
		assertTrue(issue.getSelf().toString().startsWith(jiraUri.toString()));

		assertEquals(3, Iterables.size(issue.getComments()));
		assertThat(issue.getExpandos(), IterableMatcher.hasOnlyElements("html"));

		assertEquals(IntegrationTestUtil.START_PROGRESS_TRANSITION_ID, Iterables.size(issue.getAttachments()));
		final Iterable<Attachment> items = issue.getAttachments();
		assertNotNull(items);
		final User user = new User(jiraRestUri("/user/admin"),
				"admin", "Administrator");
		Attachment attachment1 = new Attachment(IntegrationTestUtil.concat(jiraRestRootUri, "/attachment/10040"),
				"dla Paw\u0142a.txt", user, dateTime, 643, "text/plain",
				IntegrationTestUtil.concat(jiraUri, "/secure/attachment/10040/dla+Paw%C5%82a.txt"), null);

		assertEquals(attachment1, items.iterator().next());

		System.out.println(issue);

	}

	@Test
	public void testGetTransitions() throws Exception {
		final Issue issue = client.getIssueClient().getIssue(new IssueArgsBuilder("TST-1").build(), new NullProgressMonitor());
		final Iterable<Transition> transitions = client.getIssueClient().getTransitions(issue, new NullProgressMonitor());
		assertEquals(4, Iterables.size(transitions));
		assertTrue(Iterables.contains(transitions, new Transition("Start Progress", IntegrationTestUtil.START_PROGRESS_TRANSITION_ID, Collections.<Transition.Field>emptyList())));
	}

	@Test
	public void testTransition() throws Exception {
		configureJira();
		final Issue issue = client.getIssueClient().getIssue(new IssueArgsBuilder("TST-1").build(), new NullProgressMonitor());
		final Iterable<Transition> transitions = client.getIssueClient().getTransitions(issue, new NullProgressMonitor());
		assertEquals(4, Iterables.size(transitions));
		final Transition startProgressTransition = new Transition("Start Progress", IntegrationTestUtil.START_PROGRESS_TRANSITION_ID, Collections.<Transition.Field>emptyList());
		assertTrue(Iterables.contains(transitions, startProgressTransition));

		client.getIssueClient().transition(issue, new TransitionInput(IntegrationTestUtil.START_PROGRESS_TRANSITION_ID,
				Collections.<FieldInput>emptyList(), Comment.valueOf("My test comment")), new NullProgressMonitor()) ;
		final Issue transitionedIssue = client.getIssueClient().getIssue(new IssueArgsBuilder("TST-1").build(), new NullProgressMonitor());
		assertEquals("In Progress", transitionedIssue.getStatus().getName());
		final Iterable<Transition> transitionsAfterTransition = client.getIssueClient().getTransitions(issue, new NullProgressMonitor());
		assertFalse(Iterables.contains(transitionsAfterTransition, startProgressTransition));
		final Transition stopProgressTransition = new Transition("Stop Progress", IntegrationTestUtil.STOP_PROGRESS_TRANSITION_ID, Collections.<Transition.Field>emptyList());
		assertTrue(Iterables.contains(transitionsAfterTransition, stopProgressTransition));
	}


	@Test
	public void testTransitionWithNumericCustomField() throws Exception {
		final IssueArgs issueArgs = new IssueArgsBuilder("TST-1").build();
		final Issue issue = client.getIssueClient().getIssue(issueArgs, new NullProgressMonitor());
		assertNull(issue.getField(NUMERIC_CUSTOMFIELD_ID).getValue());
		final Iterable<Transition> transitions = client.getIssueClient().getTransitions(issue, new NullProgressMonitor());
		Transition transitionFound = getTransitionByName(transitions, "Estimate");

		assertNotNull(transitionFound);
		assertTrue(Iterables.contains(transitionFound.getFields(),
				new Transition.Field(NUMERIC_CUSTOMFIELD_ID, false, NUMERIC_CUSTOMFIELD_TYPE)));
		final double newValue = 123.45;
		// @todo put double directly here instead of formatting it for expected locale, when JIRA is fixed - 
		final FieldInput fieldInput = new FieldInput(NUMERIC_CUSTOMFIELD_ID,
				NumberFormat.getNumberInstance(new Locale("pl")).format(newValue));
		client.getIssueClient().transition(issue, new TransitionInput(transitionFound.getId(), Arrays.asList(fieldInput),
				Comment.valueOf("My test comment")), new NullProgressMonitor());
		final Issue changedIssue = client.getIssueClient().getIssue(issueArgs, new NullProgressMonitor());
		assertTrue(changedIssue.getField(NUMERIC_CUSTOMFIELD_ID).getValue().equals(newValue));
	}

	@Ignore
	//@Test
	public void xtestTransitionWithNumericCustomFieldAndInteger() throws Exception {
		configureJira();
		final IssueArgs issueArgs = new IssueArgsBuilder("TST-1").build();
		final Issue issue = client.getIssueClient().getIssue(issueArgs, new NullProgressMonitor());
		assertNull(issue.getField(NUMERIC_CUSTOMFIELD_ID).getValue());
		final Iterable<Transition> transitions = client.getIssueClient().getTransitions(issue, new NullProgressMonitor());
		Transition transitionFound = getTransitionByName(transitions, "Estimate");

		assertNotNull(transitionFound);
		assertTrue(Iterables.contains(transitionFound.getFields(),
				new Transition.Field(NUMERIC_CUSTOMFIELD_ID, false, NUMERIC_CUSTOMFIELD_TYPE)));
		final int newValue = 123;
		final FieldInput fieldInput = new FieldInput(NUMERIC_CUSTOMFIELD_ID, newValue);
		client.getIssueClient().transition(issue, new TransitionInput(transitionFound.getId(), Arrays.asList(fieldInput),
				Comment.valueOf("My test comment")), new NullProgressMonitor());
		final Issue changedIssue = client.getIssueClient().getIssue(issueArgs, new NullProgressMonitor());
		assertTrue(changedIssue.getField(NUMERIC_CUSTOMFIELD_ID).getValue().equals(newValue));
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

	private void testTransitionImpl(Comment comment) {
		final IssueArgs issueArgs = new IssueArgsBuilder("TST-1").build();
		final Issue issue = client.getIssueClient().getIssue(issueArgs, new NullProgressMonitor());
		final Iterable<Transition> transitions = client.getIssueClient().getTransitions(issue, new NullProgressMonitor());
		Transition transitionFound = getTransitionByName(transitions, "Estimate");
		DateTime now = new DateTime();
		client.getIssueClient().transition(issue, new TransitionInput(transitionFound.getId(), comment),
				new NullProgressMonitor());

		final Issue changedIssue = client.getIssueClient().getIssue(issueArgs, new NullProgressMonitor());
		final Comment lastComment = Iterables.getLast(changedIssue.getComments());
		assertEquals(comment.getBody(), lastComment.getBody());
		assertEquals(IntegrationTestUtil.USER_ADMIN, lastComment.getAuthor());
		assertEquals(IntegrationTestUtil.USER_ADMIN, lastComment.getUpdateAuthor());
		assertEquals(lastComment.getCreationDate(), lastComment.getUpdateDate());
		assertTrue(lastComment.getCreationDate().isAfter(now) || lastComment.getCreationDate().isEqual(now));
		assertEquals(comment.getGroupLevel(), lastComment.getGroupLevel());
		// @todo restore it when JIRA REST is fixed
//		assertEquals(comment.getRoleLevel(), lastComment.getRoleLevel());
	}

	@Test
	public void testVoteUnvote() {
		final Issue issue1 = client.getIssueClient().getIssue(new IssueArgsBuilder("TST-1").build(), new NullProgressMonitor());
		assertFalse(issue1.getVotes().hasVoted());
		assertEquals(1, issue1.getVotes().getVotes()); // the other user has voted

		// I hope that such Polish special characters (for better testing local specific behaviour of REST
		// will work correctly as this file is UTF-8 encoded
		TestUtil.assertErrorCode(404, "Nie możesz głosować na zadanie które utworzyłeś.", new Runnable() {
			@Override
			public void run() {
				client.getIssueClient().vote(issue1, new NullProgressMonitor());
			}
		});


		final IssueArgs issueArgs = new IssueArgsBuilder("TST-7").build();
		Issue issue = client.getIssueClient().getIssue(issueArgs, new NullProgressMonitor());
		assertFalse(issue.getVotes().hasVoted());
		assertEquals(0, issue.getVotes().getVotes());

		client.getIssueClient().vote(issue, new NullProgressMonitor());
		issue = client.getIssueClient().getIssue(issueArgs, new NullProgressMonitor());
		assertTrue(issue.getVotes().hasVoted());
		assertEquals(1, issue.getVotes().getVotes());

		client.getIssueClient().unvote(issue, new NullProgressMonitor());
		issue = client.getIssueClient().getIssue(issueArgs, new NullProgressMonitor());
		assertFalse(issue.getVotes().hasVoted());
		assertEquals(0, issue.getVotes().getVotes());

		setClient(TestConstants.USER2_USERNAME, TestConstants.USER2_PASSWORD);
		issue = client.getIssueClient().getIssue(issueArgs, new NullProgressMonitor());
		assertFalse(issue.getVotes().hasVoted());
		assertEquals(0, issue.getVotes().getVotes());
		final Issue finalIssue = issue;
		TestUtil.assertErrorCode(404, "Cannot remove a vote for an issue that the user has not already voted for.", new Runnable() {
			@Override
			public void run() {
				client.getIssueClient().unvote(finalIssue, new NullProgressMonitor());
			}
		});


		issue = client.getIssueClient().getIssue(issueArgs, new NullProgressMonitor());
		assertFalse(issue.getVotes().hasVoted());
		assertEquals(0, issue.getVotes().getVotes());
		client.getIssueClient().vote(issue, new NullProgressMonitor());
		issue = client.getIssueClient().getIssue(issueArgs, new NullProgressMonitor());
		assertTrue(issue.getVotes().hasVoted());
		assertEquals(1, issue.getVotes().getVotes());

		setClient(ADMIN_USERNAME, ADMIN_PASSWORD);
		client.getIssueClient().vote(issue, new NullProgressMonitor());
		issue = client.getIssueClient().getIssue(issueArgs, new NullProgressMonitor());
		assertTrue(issue.getVotes().hasVoted());
		assertEquals(2, issue.getVotes().getVotes());
	}


	private Transition getTransitionByName(Iterable<Transition> transitions, String transitionName) {
		Transition transitionFound = null;
		for (Transition transition : transitions) {
			if (transition.getName().equals(transitionName)) {
				transitionFound = transition;
				break;
			}
		}
		return transitionFound;
	}


	@Override
	protected void setUpTest() {
		super.setUpTest();
		configureJira();
	}
}
