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

import com.atlassian.jira.functest.framework.UserProfile;
import com.atlassian.jira.nimblefunctests.annotation.JiraBuildNumberDependent;
import com.atlassian.jira.nimblefunctests.annotation.LongCondition;
import com.atlassian.jira.nimblefunctests.annotation.Restore;
import com.atlassian.jira.rest.client.IntegrationTestUtil;
import com.atlassian.jira.rest.client.TestUtil;
import com.atlassian.jira.rest.client.api.GetCreateIssueMetadataOptionsBuilder;
import com.atlassian.jira.rest.client.api.IssueRestClient;
import com.atlassian.jira.rest.client.api.RestClientException;
import com.atlassian.jira.rest.client.api.domain.Attachment;
import com.atlassian.jira.rest.client.api.domain.BasicIssue;
import com.atlassian.jira.rest.client.api.domain.CimIssueType;
import com.atlassian.jira.rest.client.api.domain.CimProject;
import com.atlassian.jira.rest.client.api.domain.Comment;
import com.atlassian.jira.rest.client.api.domain.Issue;
import com.atlassian.jira.rest.client.api.domain.IssueLink;
import com.atlassian.jira.rest.client.api.domain.IssueLinkType;
import com.atlassian.jira.rest.client.api.domain.Transition;
import com.atlassian.jira.rest.client.api.domain.input.AttachmentInput;
import com.atlassian.jira.rest.client.api.domain.input.ComplexIssueInputFieldValue;
import com.atlassian.jira.rest.client.api.domain.input.FieldInput;
import com.atlassian.jira.rest.client.api.domain.input.IssueInput;
import com.atlassian.jira.rest.client.api.domain.input.IssueInputBuilder;
import com.atlassian.jira.rest.client.api.domain.input.LinkIssuesInput;
import com.atlassian.jira.rest.client.api.domain.input.TransitionInput;
import com.atlassian.jira.rest.client.api.domain.util.ErrorCollection;
import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import org.apache.commons.io.IOUtils;
import org.joda.time.DateTime;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import javax.annotation.Nullable;
import javax.ws.rs.core.Response;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.text.NumberFormat;
import java.util.Arrays;
import java.util.Collections;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.atlassian.jira.rest.client.IntegrationTestUtil.NUMERIC_CUSTOMFIELD_ID;
import static com.atlassian.jira.rest.client.IntegrationTestUtil.NUMERIC_CUSTOMFIELD_TYPE;
import static com.atlassian.jira.rest.client.IntegrationTestUtil.NUMERIC_CUSTOMFIELD_TYPE_V5;
import static com.atlassian.jira.rest.client.IntegrationTestUtil.TESTING_JIRA_5_OR_NEWER;
import static com.atlassian.jira.rest.client.IntegrationTestUtil.TEXT_CUSTOMFIELD_ID;
import static com.atlassian.jira.rest.client.IntegrationTestUtil.USER2;
import static com.atlassian.jira.rest.client.TestUtil.assertErrorCode;
import static com.atlassian.jira.rest.client.TestUtil.assertExpectedErrorCollection;
import static com.atlassian.jira.rest.client.api.domain.EntityHelper.findEntityByName;
import static com.atlassian.jira.rest.client.internal.ServerVersionConstants.BN_JIRA_4_3;
import static com.atlassian.jira.rest.client.internal.ServerVersionConstants.BN_JIRA_5;
import static com.atlassian.jira.rest.client.internal.json.TestConstants.ADMIN_PASSWORD;
import static com.atlassian.jira.rest.client.internal.json.TestConstants.ADMIN_USERNAME;
import static com.atlassian.jira.rest.client.internal.json.TestConstants.DEFAULT_JIRA_DUMP_FILE;
import static com.atlassian.jira.rest.client.internal.json.TestConstants.USER1_USERNAME;
import static com.atlassian.jira.rest.client.internal.json.TestConstants.USER2_USERNAME;
import static com.atlassian.jira.rest.client.test.matchers.RestClientExceptionMatchers.rceWithSingleError;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.collection.IsIterableContainingInAnyOrder.containsInAnyOrder;
import static org.junit.Assert.*;


// Ignore "May produce NPE" warnings, as we know what we are doing in tests
@SuppressWarnings("ConstantConditions")
@Restore(DEFAULT_JIRA_DUMP_FILE)
public class AsynchronousIssueRestClientTest extends AbstractAsynchronousRestClientTest {

	@Rule
	public ExpectedException expectedException = ExpectedException.none();

	public static final String UTF8_FILE_BODY = "File body encoded in utf8: Ka\u017a\u0144 i \u017c\u00f3\u0142to\u015b\u0107 b\u0119d\u0105! | \u1f55\u03b1\u03bb\u03bf\u03bd \u03d5\u03b1\u03b3\u03b5\u1fd6\u03bd \u03b4\u1f7b\u03bd\u03b1\u03bc\u03b1\u03b9\u0387 \u03c4\u03bf\u1fe6\u03c4\u03bf \u03bf\u1f54 \u03bc\u03b5 \u03b2\u03bb\u1f71\u03c0\u03c4\u03b5\u03b9 \u0411\u0438 \u0448\u0438\u043b \u0438\u0434\u044d\u0439 \u0447\u0430\u0434\u043d\u0430, \u043d\u0430\u0434\u0430\u0434 \u0445\u043e\u0440\u0442\u043e\u0439 \u0431\u0438\u0448., or 2\u03c0R";
	public static final String UTF8_FILE_NAME = "utf8 file name Ka\u017a\u0144 i \u017c\u00f3\u0142to\u015b\u0107 b\u0119d\u0105! | \u1f55\u03b1\u03bb\u03bf\u03bd \u03d5\u03b1\u03b3\u03b5\u1fd6\u03bd \u03b4\u1f7b\u03bd\u03b1\u03bc\u03b1\u03b9\u0387 \u03c4\u03bf\u1fe6\u03c4\u03bf \u03bf\u1f54 \u03bc\u03b5 \u03b2\u03bb\u1f71\u03c0\u03c4\u03b5\u03b9 \u0411\u0438 \u0448\u0438\u043b \u0438\u0434\u044d\u0439 \u0447\u0430\u0434\u043d\u0430, \u043d\u0430\u0434\u0430\u0434 \u0445\u043e\u0440\u0442\u043e\u0439 \u0431\u0438\u0448., or 2\u03c0R";

	@Test
	public void testTransitionWithNumericCustomFieldPolishLocale() throws Exception {
		final double newValue = 123.45;
		final FieldInput fieldInput;
		if (IntegrationTestUtil.TESTING_JIRA_5_OR_NEWER) {
			fieldInput = new FieldInput(NUMERIC_CUSTOMFIELD_ID, newValue);
		} else {
			fieldInput = new FieldInput(NUMERIC_CUSTOMFIELD_ID, NumberFormat.getNumberInstance(new Locale("pl"))
					.format(newValue));
		}
		assertTransitionWithNumericCustomField(fieldInput, newValue);
	}

	@Test
	public void testTransitionWithNumericCustomFieldEnglishLocale() throws Exception {
		setUser1();
		final double newValue = 123.45;
		final FieldInput fieldInput = new FieldInput(NUMERIC_CUSTOMFIELD_ID,
				NumberFormat.getNumberInstance(new Locale("pl")).format(newValue));

		assertErrorCode(Response.Status.BAD_REQUEST, IntegrationTestUtil.TESTING_JIRA_5_OR_NEWER
				? "Operation value must be a number" : ("'" + fieldInput.getValue() + "' is an invalid number"), new Runnable() {
			@Override
			public void run() {
				assertTransitionWithNumericCustomField(fieldInput, newValue);
			}
		});

		final FieldInput fieldInput2 = new FieldInput(NUMERIC_CUSTOMFIELD_ID, newValue); // this will be serialized always with "." according to JSL
		assertTransitionWithNumericCustomField(fieldInput2, newValue);
	}


	private void assertTransitionWithNumericCustomField(FieldInput fieldInput, Double expectedValue) {
		final Issue issue = client.getIssueClient().getIssue("TST-1").claim();
		assertNull(issue.getField(NUMERIC_CUSTOMFIELD_ID).getValue());
		final Iterable<Transition> transitions = client.getIssueClient().getTransitions(issue).claim();

		final Transition transitionFound = TestUtil.getTransitionByName(transitions, "Estimate");
		assertNotNull(transitionFound);
		assertTrue(Iterables.contains(transitionFound.getFields(),
				new Transition.Field(NUMERIC_CUSTOMFIELD_ID, false,
						IntegrationTestUtil.TESTING_JIRA_5_OR_NEWER ? NUMERIC_CUSTOMFIELD_TYPE_V5 : NUMERIC_CUSTOMFIELD_TYPE)));
		client.getIssueClient().transition(issue, new TransitionInput(transitionFound.getId(), Arrays.asList(fieldInput),
				Comment.valueOf("My test comment"))).claim();
		final Issue changedIssue = client.getIssueClient().getIssue("TST-1").claim();
		assertTrue(changedIssue.getField(NUMERIC_CUSTOMFIELD_ID).getValue().equals(expectedValue));
	}

	@Test
	public void testDeleteIssue() {
		final IssueRestClient issueClient = client.getIssueClient();

		// verify that issue exist
		final String issueKey = "TST-1";
		final Issue issue = issueClient.getIssue(issueKey).claim();
		assertEquals(issueKey, issue.getKey());

		// delete issue
		issueClient.deleteIssue(issueKey, false).claim();

		// verify
		assertThatIssueNotExists(issueKey);
	}

	@Test
	public void testDeleteIssueWithSubtasks() {
		final IssueRestClient issueClient = client.getIssueClient();

		// verify that issue exist and create subtask
		final String issueKey = "TST-1";
		final Issue issue = issueClient.getIssue(issueKey).claim();
		assertEquals(issueKey, issue.getKey());
		final BasicIssue subtask = addSubtaskToIssue(issue);
		System.out.println(subtask);

		// delete issue
		issueClient.deleteIssue(issueKey, true).claim();

		// verify
		assertThatIssueNotExists(issueKey);
		assertThatIssueNotExists(subtask.getKey());
	}

	@Test
	public void testDeleteIssueWithSubtasksWhenDeleteSubtasksIsFalse() {
		final IssueRestClient issueClient = client.getIssueClient();

		// verify that issue exist and create subtask
		final String issueKey = "TST-1";
		final Issue issue = issueClient.getIssue(issueKey).claim();
		assertEquals(issueKey, issue.getKey());
		BasicIssue subtask = addSubtaskToIssue(issue);
		System.out.println(subtask);

		// delete issue
		expectedException.expect(rceWithSingleError(400, String.format("The issue '%s' has subtasks.  "
				+ "You must specify the 'deleteSubtasks' parameter to delete this issue and all its subtasks.", issueKey)));
		issueClient.deleteIssue(issueKey, false).claim();
	}

	@Test
	public void testDeleteIssueWhenNoSuchIssue() {
		final IssueRestClient issueClient = client.getIssueClient();

		// verify that issue exist
		final String issueKey = "TST-999";
		assertThatIssueNotExists(issueKey);

		// delete issue should thrown 404
		expectedException.expect(rceWithSingleError(404, "Issue Does Not Exist"));
		issueClient.deleteIssue(issueKey, false).claim();
	}

	@Test
	public void testDeleteIssueWithoutDeletePermission() {
		setAnonymousMode();
		final IssueRestClient issueClient = client.getIssueClient();

		// verify that issue doesn't exist
		final String issueKey = "ANONEDIT-2";
		final Issue issue = issueClient.getIssue(issueKey).claim();
		assertEquals(issueKey, issue.getKey());

		// delete issue should thrown 401
		expectedException.expect(rceWithSingleError(401, "You do not have permission to delete issues in this project."));
		issueClient.deleteIssue(issueKey, false).claim();
	}

	@JiraBuildNumberDependent(BN_JIRA_5)
	@Test
	public void testUpdateField() {
		final Issue issue = client.getIssueClient().getIssue("TST-1").claim();
		assertNull(issue.getField(NUMERIC_CUSTOMFIELD_ID).getValue());
		final double newValue = 123;
		final FieldInput fieldInput = new FieldInput(NUMERIC_CUSTOMFIELD_ID, newValue);
		client.getIssueClient().updateIssue(issue.getKey(), IssueInput.createWithFields(fieldInput)).claim();
		final Issue changedIssue = client.getIssueClient().getIssue("TST-1").claim();
		assertEquals(newValue, changedIssue.getField(NUMERIC_CUSTOMFIELD_ID).getValue());
	}

	@JiraBuildNumberDependent(BN_JIRA_5)
	@Test
	public void testUpdateMultipleFields() {
		final Issue issue = client.getIssueClient().getIssue("TST-1").claim();
		assertNull(issue.getField(NUMERIC_CUSTOMFIELD_ID).getValue());
		final double newNumericValue = 123;
		final String newTextValue = "my new text";

		final IssueInputBuilder issueInputBuilder = new IssueInputBuilder()
				.setFieldValue(NUMERIC_CUSTOMFIELD_ID, newNumericValue)
				.setFieldValue(TEXT_CUSTOMFIELD_ID, newTextValue);

		client.getIssueClient().updateIssue(issue.getKey(), issueInputBuilder.build()).claim();
		final Issue changedIssue = client.getIssueClient().getIssue("TST-1").claim();
		assertNotNull(changedIssue);
		assertEquals(newNumericValue, changedIssue.getField(NUMERIC_CUSTOMFIELD_ID).getValue());
		assertEquals(newTextValue, changedIssue.getField(TEXT_CUSTOMFIELD_ID).getValue());
	}

	@JiraBuildNumberDependent(BN_JIRA_5)
	@Test
	public void testUpdateIssueWithInvalidAdditionalField() {
		final Issue issue = client.getIssueClient().getIssue("TST-1").claim();
		final String fieldId = "invalidField";

		expectedException.expect(RestClientException.class);
		expectedException.expectMessage(String.format(
				"Field '%s' cannot be set. It is not on the appropriate screen, or unknown.", fieldId));
		final FieldInput fieldInput = new FieldInput(fieldId, "who cares?");
		client.getIssueClient().updateIssue(issue.getKey(), IssueInput.createWithFields(fieldInput)).claim();
	}

	@JiraBuildNumberDependent(BN_JIRA_5)
	@Test
	public void testUpdateIssueWithoutPermissions() {
		setUser2();

		expectedException.expect(RestClientException.class);
		expectedException.expectMessage(String.format(
				"Field '%s' cannot be set. It is not on the appropriate screen, or unknown.", NUMERIC_CUSTOMFIELD_ID));
		final FieldInput fieldInput = new FieldInput(NUMERIC_CUSTOMFIELD_ID, 1.23d);
		client.getIssueClient().updateIssue("TST-1", IssueInput.createWithFields(fieldInput)).claim();
	}
	
	@Test
	public void testTransitionWithNumericCustomFieldAndInteger() throws Exception {
		final Issue issue = client.getIssueClient().getIssue("TST-1").claim();
		assertNull(issue.getField(NUMERIC_CUSTOMFIELD_ID).getValue());
		final Iterable<Transition> transitions = client.getIssueClient().getTransitions(issue).claim();
		final Transition transitionFound = TestUtil.getTransitionByName(transitions, "Estimate");

		assertNotNull(transitionFound);
		assertTrue(Iterables.contains(transitionFound.getFields(),
				new Transition.Field(NUMERIC_CUSTOMFIELD_ID, false,
						IntegrationTestUtil.TESTING_JIRA_5_OR_NEWER ? NUMERIC_CUSTOMFIELD_TYPE_V5 : NUMERIC_CUSTOMFIELD_TYPE)));
		final double newValue = 123;
		final FieldInput fieldInput = new FieldInput(NUMERIC_CUSTOMFIELD_ID, newValue);
		client.getIssueClient().transition(issue.getTransitionsUri(), new TransitionInput(transitionFound.getId(), Arrays
				.asList(fieldInput),
				Comment.valueOf("My test comment"))).claim();
		final Issue changedIssue = client.getIssueClient().getIssue("TST-1").claim();
		assertEquals(newValue, changedIssue.getField(NUMERIC_CUSTOMFIELD_ID).getValue());
	}

	@Test
	public void testTransitionWithInvalidNumericField() throws Exception {
		final Issue issue = client.getIssueClient().getIssue("TST-1").claim();
		assertNull(issue.getField(NUMERIC_CUSTOMFIELD_ID).getValue());
		final Iterable<Transition> transitions = client.getIssueClient().getTransitions(issue).claim();
		final Transition transitionFound = TestUtil.getTransitionByName(transitions, "Estimate");

		assertNotNull(transitionFound);
		assertTrue(Iterables.contains(transitionFound.getFields(),
				new Transition.Field(NUMERIC_CUSTOMFIELD_ID, false,
						TESTING_JIRA_5_OR_NEWER ? NUMERIC_CUSTOMFIELD_TYPE_V5 : NUMERIC_CUSTOMFIELD_TYPE)));
		final FieldInput fieldInput = new FieldInput(NUMERIC_CUSTOMFIELD_ID, "]432jl");
		// warning: Polish language here - I am asserting if the messages are indeed localized
		// since 5.0 messages are changed and not localized
		assertErrorCode(Response.Status.BAD_REQUEST, TESTING_JIRA_5_OR_NEWER
				? "Operation value must be a number" : "']432jl' nie jest prawid\u0142ow\u0105 liczb\u0105", new Runnable() {
			@Override
			public void run() {
				client.getIssueClient().transition(issue, new TransitionInput(transitionFound.getId(), Arrays.asList(fieldInput),
						Comment.valueOf("My test comment"))).claim();
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
		if (IntegrationTestUtil.TESTING_JIRA_5_OR_NEWER) {
			assertInvalidCommentInput(comment, "Invalid role level specified.");
		} else {
			assertInvalidCommentInput(comment, "Invalid role [some-fake-role]");
		}
	}

	@Test
	public void testTransitionWithInvalidGroup() {
		final Comment comment = Comment.createWithGroupLevel(
				"My text which I am just adding " + new DateTime(), "some-fake-group");
		assertInvalidCommentInput(comment, "Group: some-fake-group does not exist.");
	}

	private void assertInvalidCommentInput(final Comment comment, String expectedErrorMsg) {
		final Issue issue = client.getIssueClient().getIssue("TST-1").claim();
		final Iterable<Transition> transitions = client.getIssueClient().getTransitions(issue).claim();
		final Transition transitionFound = TestUtil.getTransitionByName(transitions, "Estimate");
		final String errorMsg = doesJiraServeCorrectlyErrorMessagesForBadRequestWhileTransitioningIssue()
				? expectedErrorMsg : null;
		assertErrorCode(Response.Status.BAD_REQUEST, errorMsg, new Runnable() {
			@Override
			public void run() {
				client.getIssueClient().transition(issue, new TransitionInput(transitionFound.getId(), comment)).claim();
			}
		});
	}

	private void testTransitionImpl(Comment comment) {
		final Issue issue = client.getIssueClient().getIssue("TST-1").claim();
		final Iterable<Transition> transitions = client.getIssueClient().getTransitions(issue).claim();
		Transition transitionFound = TestUtil.getTransitionByName(transitions, "Estimate");
		DateTime now = new DateTime();
		client.getIssueClient().transition(issue, new TransitionInput(transitionFound.getId(), comment)).claim();

		final Issue changedIssue = client.getIssueClient().getIssue("TST-1").claim();
		final Comment lastComment = Iterables.getLast(changedIssue.getComments());
		assertEquals(comment.getBody(), lastComment.getBody());
		assertEquals(IntegrationTestUtil.USER_ADMIN, lastComment.getAuthor());
		assertEquals(IntegrationTestUtil.USER_ADMIN, lastComment.getUpdateAuthor());
		assertEquals(lastComment.getCreationDate(), lastComment.getUpdateDate());
		assertTrue(lastComment.getCreationDate().isAfter(now) || lastComment.getCreationDate().isEqual(now));
		assertEquals(comment.getVisibility(), lastComment.getVisibility());
	}

	@Test
	public void testVoteUnvote() {
		final Issue issue1 = client.getIssueClient().getIssue("TST-1").claim();
		assertFalse(issue1.getVotes().hasVoted());
		assertEquals(1, issue1.getVotes().getVotes()); // the other user has voted
		final String expectedMessage = isJira5xOrNewer() // JIRA 5.0 comes without Polish translation OOB
				? "You cannot vote for an issue you have reported."
				: "Nie mo\u017cesz g\u0142osowa\u0107 na zadanie kt\u00f3re utworzy\u0142e\u015b.";

		// I hope that such Polish special characters (for better testing local specific behaviour of REST
		assertErrorCode(Response.Status.NOT_FOUND, expectedMessage, new Runnable() {
			@Override
			public void run() {
				client.getIssueClient().vote(issue1.getVotesUri()).claim();
			}
		});


		final String issueKey = "TST-7";
		Issue issue = client.getIssueClient().getIssue(issueKey).claim();
		assertFalse(issue.getVotes().hasVoted());
		assertEquals(0, issue.getVotes().getVotes());

		client.getIssueClient().vote(issue.getVotesUri()).claim();
		issue = client.getIssueClient().getIssue(issueKey).claim();
		assertTrue(issue.getVotes().hasVoted());
		assertEquals(1, issue.getVotes().getVotes());

		client.getIssueClient().unvote(issue.getVotesUri()).claim();
		issue = client.getIssueClient().getIssue(issueKey).claim();
		assertFalse(issue.getVotes().hasVoted());
		assertEquals(0, issue.getVotes().getVotes());

		setUser2();
		issue = client.getIssueClient().getIssue(issueKey).claim();
		assertFalse(issue.getVotes().hasVoted());
		assertEquals(0, issue.getVotes().getVotes());
		final Issue finalIssue = issue;
		if (isJira6_3_7_OrNewer()) {
			client.getIssueClient().unvote(finalIssue.getVotesUri()).claim();
			issue = client.getIssueClient().getIssue(issueKey).claim();
			assertEquals(0, issue.getVotes().getVotes());
		} else {
			assertErrorCode(Response.Status.NOT_FOUND, "Cannot remove a vote for an issue that the user has not already voted for.",
					new Runnable() {
						@Override
						public void run() {
							client.getIssueClient().unvote(finalIssue.getVotesUri()).claim();
						}
					});
		}

		issue = client.getIssueClient().getIssue(issueKey).claim();
		assertFalse(issue.getVotes().hasVoted());
		assertEquals(0, issue.getVotes().getVotes());
		client.getIssueClient().vote(issue.getVotesUri()).claim();
		issue = client.getIssueClient().getIssue(issueKey).claim();
		assertTrue(issue.getVotes().hasVoted());
		assertEquals(1, issue.getVotes().getVotes());

		setClient(ADMIN_USERNAME, ADMIN_PASSWORD);
		client.getIssueClient().vote(issue.getVotesUri()).claim();
		issue = client.getIssueClient().getIssue(issueKey).claim();
		assertTrue(issue.getVotes().hasVoted());
		assertEquals(2, issue.getVotes().getVotes());
	}

	@Test
	public void testWatchUnwatch() {
		final IssueRestClient issueClient = client.getIssueClient();
		final Issue issue1 = issueClient.getIssue("TST-1").claim();

		Assert.assertThat(issueClient.getWatchers(issue1.getWatchers().getSelf()).claim()
				.getUsers(), not(hasItem(IntegrationTestUtil.USER_ADMIN)));

		issueClient.watch(issue1.getWatchers().getSelf()).claim();
		Assert.assertThat(issueClient.getWatchers(issue1.getWatchers().getSelf()).claim()
				.getUsers(), hasItem(IntegrationTestUtil.USER_ADMIN));

		issueClient.unwatch(issue1.getWatchers().getSelf()).claim();
		Assert.assertThat(issueClient.getWatchers(issue1.getWatchers().getSelf()).claim()
				.getUsers(), not(hasItem(IntegrationTestUtil.USER_ADMIN)));

		Assert.assertThat(issueClient.getWatchers(issue1.getWatchers().getSelf()).claim()
				.getUsers(), hasItem(IntegrationTestUtil.USER1));
		issueClient.removeWatcher(issue1.getWatchers().getSelf(), IntegrationTestUtil.USER1.getName()).claim();
		Assert.assertThat(issueClient.getWatchers(issue1.getWatchers().getSelf()).claim()
				.getUsers(), not(hasItem(IntegrationTestUtil.USER1)));
		issueClient.addWatcher(issue1.getWatchers().getSelf(), IntegrationTestUtil.USER1.getName()).claim();
		Assert.assertThat(issueClient.getWatchers(issue1.getWatchers().getSelf()).claim()
				.getUsers(), hasItem(IntegrationTestUtil.USER1));
	}

	@Test
	public void testRemoveWatcherUnauthorized() {
		final IssueRestClient issueClient = client.getIssueClient();
		final Issue issue1 = issueClient.getIssue("TST-1").claim();
		issueClient.watch(issue1.getWatchers().getSelf()).claim();

		setUser1();
		final IssueRestClient issueClient2 = client.getIssueClient();
		assertErrorCode(Response.Status.UNAUTHORIZED,
				"User 'wseliga' is not allowed to remove watchers from issue 'TST-1'", new Runnable() {
			@Override
			public void run() {
				issueClient2.removeWatcher(issue1.getWatchers().getSelf(), ADMIN_USERNAME).claim();
			}
		});
	}


	@Test
	public void testWatchAlreadyWatched() {
		setUser1();
		final IssueRestClient issueClient = client.getIssueClient();
		final Issue issue = issueClient.getIssue("TST-1").claim();
		Assert.assertThat(client.getIssueClient().getWatchers(issue.getWatchers().getSelf()).claim()
				.getUsers(), hasItem(IntegrationTestUtil.USER1));
		// JIRA allows to watch already watched issue by you - such action effectively has no effect
		issueClient.watch(issue.getWatchers().getSelf()).claim();
		Assert.assertThat(client.getIssueClient().getWatchers(issue.getWatchers().getSelf()).claim()
				.getUsers(), hasItem(IntegrationTestUtil.USER1));
	}

	@Test
	public void testAddWatcherUnauthorized() {
		final IssueRestClient issueClient = client.getIssueClient();
		final Issue issue1 = issueClient.getIssue("TST-1").claim();
		issueClient.addWatcher(issue1.getWatchers().getSelf(), USER1_USERNAME).claim();
		assertThat(client.getIssueClient().getWatchers(issue1.getWatchers().getSelf()).claim()
				.getUsers(), hasItem(IntegrationTestUtil.USER1));

		setUser1();
		assertTrue(client.getIssueClient().getIssue("TST-1").claim().getWatchers().isWatching());
		String expectedErrorMsg = isJraDev3516Fixed() ? ("User '" + USER1_USERNAME
				+ "' is not allowed to add watchers to issue 'TST-1'") : null;
		assertErrorCode(Response.Status.UNAUTHORIZED, expectedErrorMsg, new Runnable() {
			@Override
			public void run() {
				client.getIssueClient().addWatcher(issue1.getWatchers().getSelf(), ADMIN_USERNAME).claim();
			}
		});
	}

	private boolean isJraDev3516Fixed() {
		return client.getMetadataClient().getServerInfo().claim().getBuildNumber() >= BN_JIRA_4_3;
	}

	@Test
	public void testAddWatcherWhoDoesNotHaveViewIssuePermissions() {
		final IssueRestClient issueClient = client.getIssueClient();
		final String issueKey = "RST-1";
		final Issue issue1 = issueClient.getIssue(issueKey).claim();
		final String expectedErrorMessage;

		if (isJira5xOrNewer()) {
			expectedErrorMessage = "The user \"" + USER2_USERNAME + "\" does not have permission to view this issue."
					+ " This user will not be added to the watch list.";
		} else if (isJira43xOrNewer()) {
			expectedErrorMessage = "User '" + ADMIN_USERNAME + "' is not allowed to add watchers to issue '" + issueKey + "'";
		} else {
			expectedErrorMessage = "com.sun.jersey.api.client.UniformInterfaceException: Client response status: 401";
		}

		assertErrorCode(Response.Status.UNAUTHORIZED, expectedErrorMessage,
				new Runnable() {
					@Override
					public void run() {
						issueClient.addWatcher(issue1.getWatchers().getSelf(), USER2_USERNAME).claim();
					}
				});
	}

	@JiraBuildNumberDependent(BN_JIRA_4_3)
	@Test
	public void testLinkIssuesWithRoleLevel() {
		testLinkIssuesImpl(Comment.createWithRoleLevel("A comment about linking", "Administrators"));
	}

	@JiraBuildNumberDependent(BN_JIRA_4_3)
	@Test
	public void testLinkIssuesWithGroupLevel() {
		testLinkIssuesImpl(Comment.createWithGroupLevel("A comment about linking", "jira-administrators"));
	}

	@JiraBuildNumberDependent(BN_JIRA_4_3)
	@Test
	public void testLinkIssuesWithSimpleComment() {
		testLinkIssuesImpl(Comment.valueOf("A comment about linking"));
	}

	@JiraBuildNumberDependent(BN_JIRA_4_3)
	@Test
	public void testLinkIssuesWithoutComment() {
		testLinkIssuesImpl(null);
	}

	@JiraBuildNumberDependent(BN_JIRA_4_3)
	@Test
	public void testLinkIssuesWithInvalidParams() {
		assertErrorCode(Response.Status.NOT_FOUND,
				IntegrationTestUtil.TESTING_JIRA_5_OR_NEWER ? "Issue Does Not Exist"
						: "The issue no longer exists.", new Runnable() {
			@Override
			public void run() {
				client.getIssueClient().linkIssue(new LinkIssuesInput("TST-7", "FAKEKEY-1", "Duplicate", null)).claim();
			}
		});

		assertErrorCode(Response.Status.NOT_FOUND, "No issue link type with name 'NonExistingLinkType' found.", new Runnable() {
			@Override
			public void run() {
				client.getIssueClient().linkIssue(new LinkIssuesInput("TST-7", "TST-6", "NonExistingLinkType", null)).claim();
			}
		});

		setUser1();
		final String optionalDot = isJira5xOrNewer() ? "." : "";
		assertErrorCode(Response.Status.NOT_FOUND,
				"You do not have the permission to see the specified issue" + optionalDot, new Runnable() {
			@Override
			public void run() {
				client.getIssueClient().linkIssue(new LinkIssuesInput("TST-7", "RST-1", "Duplicate", null)).claim();
			}
		});
		final ErrorCollection.Builder ecb = ErrorCollection.builder();
		ecb.status(Response.Status.BAD_REQUEST.getStatusCode())
				.errorMessage("Failed to create comment for issue 'TST-6'")
				.error("commentLevel", "You are currently not a member of the project role: Administrators.");
		final ImmutableList<ErrorCollection> errorCollections = ImmutableList.of(ecb.build());

		assertExpectedErrorCollection(errorCollections, new Runnable() {
			@Override
			public void run() {
				client.getIssueClient().linkIssue(new LinkIssuesInput("TST-7", "TST-6", "Duplicate",
						Comment.createWithRoleLevel("my body", "Administrators"))).claim();
			}
		});
		assertErrorCode(Response.Status.BAD_REQUEST, "You are currently not a member of the group: jira-administrators.", new Runnable() {
			@Override
			public void run() {
				client.getIssueClient().linkIssue(new LinkIssuesInput("TST-7", "TST-6", "Duplicate",
						Comment.createWithGroupLevel("my body", "jira-administrators"))).claim();
			}
		});
		assertErrorCode(Response.Status.BAD_REQUEST, "Group: somefakegroup does not exist.", new Runnable() {
			@Override
			public void run() {
				client.getIssueClient().linkIssue(new LinkIssuesInput("TST-7", "TST-6", "Duplicate",
						Comment.createWithGroupLevel("my body", "somefakegroup"))).claim();
			}
		});
	}

    @JiraBuildNumberDependent(condition = LongCondition.LESS_THAN, value = 6211)
    @Test
    public void testLinkIssuesWithInvalidParamsBeforeUpgradeTask6211() {
        setUser2();
        assertErrorCode(Response.Status.UNAUTHORIZED, "No Link Issue Permission for issue 'TST-7'", new Runnable() {
            @Override
            public void run() {
                client.getIssueClient().linkIssue(new LinkIssuesInput("TST-7", "TST-6", "Duplicate", null)).claim();
            }
        });
    }

    @JiraBuildNumberDependent(6211)
    @Test
    public void testLinkIssuesForUserRoleLevelAfterUpgradeTask6211() {
        testLinkIssuesImpl(Comment.createWithRoleLevel("A comment about linking", "Users"));
    }

	private void testLinkIssuesImpl(@Nullable Comment commentInput) {
		final IssueRestClient issueClient = client.getIssueClient();
		final Issue originalIssue = issueClient.getIssue("TST-7").claim();
		int origNumComments = Iterables.size(originalIssue.getComments());
		assertFalse(originalIssue.getIssueLinks().iterator().hasNext());

		issueClient.linkIssue(new LinkIssuesInput("TST-7", "TST-6", "Duplicate", commentInput)).claim();

		final Issue linkedIssue = issueClient.getIssue("TST-7").claim();
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


		final Issue targetIssue = issueClient.getIssue("TST-6").claim();
		final IssueLink targetLink = targetIssue.getIssueLinks().iterator().next();
		assertEquals(IssueLinkType.Direction.INBOUND, targetLink.getIssueLinkType().getDirection());
		assertEquals("Duplicate", targetLink.getIssueLinkType().getName());
	}

	private boolean doesJiraSupportAddingAttachment() {
		return client.getMetadataClient().getServerInfo().claim().getBuildNumber() >= BN_JIRA_4_3;
	}

	private boolean doesJiraServeCorrectlyErrorMessagesForBadRequestWhileTransitioningIssue() {
		return client.getMetadataClient().getServerInfo().claim().getBuildNumber() >= BN_JIRA_4_3;
	}

	@Test
	// TODO: implement
	public void testAddAttachment() throws IOException {

		if (!doesJiraSupportAddingAttachment()) {
			return;
		}
		final IssueRestClient issueClient = client.getIssueClient();
		final Issue issue = issueClient.getIssue("TST-3").claim();
		assertFalse(issue.getAttachments().iterator().hasNext());

		String str = "Wojtek";
		final String filename1 = "my-test-file";
		issueClient.addAttachment(issue.getAttachmentsUri(), new ByteArrayInputStream(str.getBytes("UTF-8")), filename1).claim();
		final String filename2 = "my-picture.png";
		issueClient.addAttachment(issue.getAttachmentsUri(), AsynchronousIssueRestClientTest.class
				.getResourceAsStream("/attachment-test/transparent-png.png"), filename2).claim();

		final Issue issueWithAttachments = issueClient.getIssue("TST-3").claim();
		final Iterable<Attachment> attachments = issueWithAttachments.getAttachments();
		assertEquals(2, Iterables.size(attachments));
		final Iterable<String> attachmentsNames = Iterables.transform(attachments, new Function<Attachment, String>() {
			@Override
			public String apply(@Nullable Attachment from) {
				return from.getFilename();
			}
		});
		assertThat(attachmentsNames, containsInAnyOrder(filename1, filename2));
		final Attachment pictureAttachment = Iterables.find(attachments, new Predicate<Attachment>() {
			@Override
			public boolean apply(@Nullable Attachment input) {
				return filename2.equals(input.getFilename());
			}
		});

		// let's download it now and compare it's binary content

		assertTrue(
				IOUtils.contentEquals(AsynchronousIssueRestClientTest.class
						.getResourceAsStream("/attachment-test/transparent-png.png"),
						issueClient.getAttachment(pictureAttachment.getContentUri()).claim()));
	}

	@Test
	public void testAddAttachmentWithUtf8InNameAndBody() throws IOException {
		final IssueRestClient issueClient = client.getIssueClient();
		final Issue issue = issueClient.getIssue("TST-3").claim();
		assertFalse(issue.getAttachments().iterator().hasNext());

		final ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(UTF8_FILE_BODY.getBytes("UTF-8"));
		issueClient.addAttachment(issue.getAttachmentsUri(), byteArrayInputStream, UTF8_FILE_NAME).claim();

		final Issue issueWithAttachments = issueClient.getIssue("TST-3").claim();
		final Iterable<Attachment> attachments = issueWithAttachments.getAttachments();
		assertEquals(1, Iterables.size(attachments));
		final Attachment attachment = attachments.iterator().next();
		assertThat(attachment.getFilename(), equalTo(UTF8_FILE_NAME));

		assertTrue(IOUtils.contentEquals(new ByteArrayInputStream(UTF8_FILE_BODY.getBytes("UTF-8")),
				issueClient.getAttachment(attachment.getContentUri()).claim()));
	}

	@Test
	// TODO: implement
	public void testAddAttachments() throws IOException {
		if (!doesJiraSupportAddingAttachment()) {
			return;
		}
		final IssueRestClient issueClient = client.getIssueClient();
		final Issue issue = issueClient.getIssue("TST-4").claim();
		assertFalse(issue.getAttachments().iterator().hasNext());

		final AttachmentInput[] attachmentInputs = new AttachmentInput[3];
		for (int i = 1; i <= 3; i++) {
			attachmentInputs[i - 1] = new AttachmentInput("my-test-file-" + i + ".txt", new ByteArrayInputStream((
					"content-of-the-file-" + i).getBytes("UTF-8")));
		}
		issueClient.addAttachments(issue.getAttachmentsUri(), attachmentInputs).claim();

		final Issue issueWithAttachments = issueClient.getIssue("TST-4").claim();
		final Iterable<Attachment> attachments = issueWithAttachments.getAttachments();
		assertEquals(3, Iterables.size(attachments));
		Pattern pattern = Pattern.compile("my-test-file-(\\d)\\.txt");
		for (Attachment attachment : attachments) {
			assertTrue(pattern.matcher(attachment.getFilename()).matches());
			final Matcher matcher = pattern.matcher(attachment.getFilename());
			matcher.find();
			final String interfix = matcher.group(1);
			assertTrue(IOUtils.contentEquals(new ByteArrayInputStream(("content-of-the-file-" + interfix).getBytes("UTF-8")),
					issueClient.getAttachment(attachment.getContentUri()).claim()));

		}
	}

	@Test
	public void testAddAttachmentsWithUtf8InNameAndBody() throws IOException {
		final IssueRestClient issueClient = client.getIssueClient();
		final Issue issue = issueClient.getIssue("TST-4").claim();
		assertFalse(issue.getAttachments().iterator().hasNext());

		final AttachmentInput[] attachmentInputs = new AttachmentInput[3];
		final String[] names = new String[3];
		final String[] contents = new String[3];
		for (int i = 0; i < 3; i++) {
			names[i] = UTF8_FILE_NAME + "-" + i + ".txt";
			contents[i] = "content-of-the-file-" + i + " with some utf8: " + UTF8_FILE_BODY;
			attachmentInputs[i] = new AttachmentInput(names[i], new ByteArrayInputStream(contents[i].getBytes("UTF-8")));
		}
		issueClient.addAttachments(issue.getAttachmentsUri(), attachmentInputs).claim();

		final Issue issueWithAttachments = issueClient.getIssue("TST-4").claim();
		final Iterable<Attachment> attachments = issueWithAttachments.getAttachments();
		assertEquals(3, Iterables.size(attachments));
		Pattern pattern = Pattern.compile(".*-(\\d)\\.txt");
		for (Attachment attachment : attachments) {
			assertTrue(pattern.matcher(attachment.getFilename()).matches());
			final Matcher matcher = pattern.matcher(attachment.getFilename());
			matcher.find();
			final int attachmentNum = Integer.parseInt(matcher.group(1));
			assertThat(attachment.getFilename(), equalTo(names[attachmentNum]));
			assertTrue(IOUtils.contentEquals(new ByteArrayInputStream((contents[attachmentNum]).getBytes("UTF-8")),
					issueClient.getAttachment(attachment.getContentUri()).claim()));

		}
	}

	@Test
	// TODO: implement
	public void testAddFileAttachments() throws IOException {
		if (!doesJiraSupportAddingAttachment()) {
			return;
		}
		final IssueRestClient issueClient = client.getIssueClient();
		final Issue issue = issueClient.getIssue("TST-5").claim();
		assertFalse(issue.getAttachments().iterator().hasNext());

		final File tempFile = File.createTempFile("jim-integration-test", ".txt");
		tempFile.deleteOnExit();
		FileWriter writer = new FileWriter(tempFile);
		writer.write("This is the content of my file which I am going to upload to JIRA for testing.");
		writer.close();
		issueClient.addAttachments(issue.getAttachmentsUri(), tempFile).claim();

		final Issue issueWithAttachments = issueClient.getIssue("TST-5").claim();
		final Iterable<Attachment> attachments = issueWithAttachments.getAttachments();
		assertEquals(1, Iterables.size(attachments));
		assertTrue(IOUtils.contentEquals(new FileInputStream(tempFile),
				issueClient.getAttachment(attachments.iterator().next().getContentUri()).claim()));
	}

	@Test
	public void testAddFileAttachmentWithUtf8InNameAndBody() throws IOException {
		final IssueRestClient issueClient = client.getIssueClient();
		final Issue issue = issueClient.getIssue("TST-5").claim();
		assertFalse(issue.getAttachments().iterator().hasNext());

		final File tempFile = File.createTempFile(UTF8_FILE_NAME, ".txt");
		tempFile.deleteOnExit();
		FileWriter writer = new FileWriter(tempFile);
		writer.write(UTF8_FILE_BODY);
		writer.close();
		issueClient.addAttachments(issue.getAttachmentsUri(), tempFile).claim();

		final Issue issueWithAttachments = issueClient.getIssue("TST-5").claim();
		final Iterable<Attachment> attachments = issueWithAttachments.getAttachments();
		assertEquals(1, Iterables.size(attachments));
		final Attachment firstAttachment = attachments.iterator().next();
		assertTrue(IOUtils.contentEquals(new FileInputStream(tempFile),
				issueClient.getAttachment(firstAttachment.getContentUri()).claim()));
		assertThat(firstAttachment.getFilename(), equalTo(tempFile.getName()));
	}

	private void setUserLanguageToEnUk() {
		changeUserLanguageByValueOrName("en_UK", "angielski (UK)");
	}

	private void changeUserLanguageByValueOrName(String value, String name) {
		final UserProfile userProfile = navigation.userProfile();
		boolean fallbackToChangeByValue = false;
		try {
			Method changeUserLanguageByValue = userProfile.getClass().getMethod("changeUserLanguageByValue", String.class);
			changeUserLanguageByValue.invoke(userProfile, value);
		} catch (NoSuchMethodException e) {
			// fallbackToChangeByValue to value - for JIRA < 5.1
			fallbackToChangeByValue = true;
		} catch (InvocationTargetException e) {
			fallbackToChangeByValue = true;
		} catch (IllegalAccessException e) {
			fallbackToChangeByValue = true;
		}

		if (fallbackToChangeByValue) {
			userProfile.changeUserLanguage(name);
		}
	}

	@Test
	public void testFetchingUnassignedIssue() {
		administration.generalConfiguration().setAllowUnassignedIssues(true);
		assertEquals(IntegrationTestUtil.USER_ADMIN, client.getIssueClient().getIssue("TST-5").claim().getAssignee());

		setUserLanguageToEnUk();
		navigation.issue().unassignIssue("TST-5", "unassigning issue");
		// this single line does instead of 2 above - func test suck with non-English locale
		// but it does not work yet with JIRA 5.0-resthack...
		//navigation.issue().assignIssue("TST-5", "unassigning issue", "Nieprzydzielone");

		assertNull(client.getIssueClient().getIssue("TST-5").claim().getAssignee());
	}

	@Test
	public void testFetchingIssueWithAnonymousComment() {
		setUserLanguageToEnUk();
		final String commentText = "my nice comment";
		final String issueKey = "ANONEDIT-1";

		navigation.logout();
		navigation.issue().addComment(issueKey, commentText);

		final Issue issue = client.getIssueClient().getIssue(issueKey).claim();
		assertEquals(1, Iterables.size(issue.getComments()));
		final Comment comment = issue.getComments().iterator().next();
		assertEquals(commentText, comment.getBody());
		if (isJira5xOrNewer()) {
			assertNotNull(comment.getId());
		} else {
			assertNull(comment.getId());
		}
		assertNull(comment.getAuthor());
		assertNull(comment.getUpdateAuthor());
	}

	@Test
	public void testGetIssueWithNoViewWatchersPermission() {
		setUser1();
		assertTrue(client.getIssueClient().getIssue("TST-1").claim().getWatchers().isWatching());

		setUser2();
		final Issue issue = client.getIssueClient().getIssue("TST-1").claim();
		assertFalse(issue.getWatchers().isWatching());
		client.getIssueClient().watch(issue.getWatchers().getSelf()).claim();
		final Issue watchedIssue = client.getIssueClient().getIssue("TST-1").claim();
		assertTrue(watchedIssue.getWatchers().isWatching());
		assertEquals(2, watchedIssue.getWatchers().getNumWatchers());

		// although there are 2 watchers, only one is listed with details - the caller itself, as the caller does not
		// have view watchers and voters permission
		assertThat(client.getIssueClient().getWatchers(watchedIssue.getWatchers().getSelf()).claim().getUsers(),
				containsInAnyOrder(USER2));
	}

	@Test
	public void testTransition() throws Exception {
		final Issue issue = client.getIssueClient().getIssue("TST-1").claim();
		final Iterable<Transition> transitions = client.getIssueClient().getTransitions(issue).claim();
		assertEquals(4, Iterables.size(transitions));
		final Transition startProgressTransition = new Transition("Start Progress", IntegrationTestUtil.START_PROGRESS_TRANSITION_ID, Collections
				.<Transition.Field>emptyList());
		assertTrue(Iterables.contains(transitions, startProgressTransition));

		client.getIssueClient().transition(issue, new TransitionInput(IntegrationTestUtil.START_PROGRESS_TRANSITION_ID,
				Collections.<FieldInput>emptyList(), Comment.valueOf("My test comment"))).claim();
		final Issue transitionedIssue = client.getIssueClient().getIssue("TST-1").claim();
		assertEquals("In Progress", transitionedIssue.getStatus().getName());
		final Iterable<Transition> transitionsAfterTransition = client.getIssueClient().getTransitions(issue).claim();
		assertFalse(Iterables.contains(transitionsAfterTransition, startProgressTransition));
		final Transition stopProgressTransition = new Transition("Stop Progress", IntegrationTestUtil.STOP_PROGRESS_TRANSITION_ID, Collections
				.<Transition.Field>emptyList());
		assertTrue(Iterables.contains(transitionsAfterTransition, stopProgressTransition));
	}

	private void assertThatIssueNotExists(String issueKey) {
		try {
			final Issue issue = client.getIssueClient().getIssue(issueKey).claim();
			fail("It looks that issue exists, and it should not be here! issue = " + issue);
		} catch (RestClientException ex) {
			assertThat(ex, rceWithSingleError(404, "Issue Does Not Exist"));
		}
	}

	private BasicIssue addSubtaskToIssue(final Issue issue) {
		// collect CreateIssueMetadata for project with key TST
		final IssueRestClient issueClient = client.getIssueClient();
		final Iterable<CimProject> metadataProjects = issueClient.getCreateIssueMetadata(
				new GetCreateIssueMetadataOptionsBuilder().withProjectKeys(issue.getProject().getKey())
						.withExpandedIssueTypesFields().build()).claim();

		// select project and issue
		assertEquals(1, Iterables.size(metadataProjects));
		final CimProject project = metadataProjects.iterator().next();
		final CimIssueType issueType = findEntityByName(project.getIssueTypes(), "Sub-task");

		// build issue input
		final String summary = "Some subtask";
		final String description = "Some description for substask";

		// prepare IssueInput
		final IssueInputBuilder issueInputBuilder = new IssueInputBuilder(project, issueType, summary)
				.setDescription(description)
				.setFieldValue("parent", ComplexIssueInputFieldValue.with("key", issue.getKey()));

		// create
		final BasicIssue basicCreatedIssue = issueClient.createIssue(issueInputBuilder.build()).claim();
		assertNotNull(basicCreatedIssue.getKey());

		return basicCreatedIssue;
	}
}
