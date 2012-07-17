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
import com.atlassian.jira.nimblefunctests.annotation.Restore;
import com.atlassian.jira.rest.client.GetCreateIssueMetadataOptionsBuilder;
import com.atlassian.jira.rest.client.IntegrationTestUtil;
import com.atlassian.jira.rest.client.IssueRestClient;
import com.atlassian.jira.rest.client.IterableMatcher;
import com.atlassian.jira.rest.client.NullProgressMonitor;
import com.atlassian.jira.rest.client.RestClientException;
import com.atlassian.jira.rest.client.TestUtil;
import com.atlassian.jira.rest.client.domain.Attachment;
import com.atlassian.jira.rest.client.domain.BasicComponent;
import com.atlassian.jira.rest.client.domain.BasicIssue;
import com.atlassian.jira.rest.client.domain.BasicPriority;
import com.atlassian.jira.rest.client.domain.BasicUser;
import com.atlassian.jira.rest.client.domain.CimFieldInfo;
import com.atlassian.jira.rest.client.domain.CimIssueType;
import com.atlassian.jira.rest.client.domain.CimProject;
import com.atlassian.jira.rest.client.domain.Comment;
import com.atlassian.jira.rest.client.domain.EntityHelper;
import com.atlassian.jira.rest.client.domain.Issue;
import com.atlassian.jira.rest.client.domain.IssueFieldId;
import com.atlassian.jira.rest.client.domain.IssueLink;
import com.atlassian.jira.rest.client.domain.IssueLinkType;
import com.atlassian.jira.rest.client.domain.Transition;
import com.atlassian.jira.rest.client.domain.input.AttachmentInput;
import com.atlassian.jira.rest.client.domain.input.CannotTransformValueException;
import com.atlassian.jira.rest.client.domain.input.FieldInput;
import com.atlassian.jira.rest.client.domain.input.IssueInput;
import com.atlassian.jira.rest.client.domain.input.IssueInputBuilder;
import com.atlassian.jira.rest.client.domain.input.LinkIssuesInput;
import com.atlassian.jira.rest.client.domain.input.TransitionInput;
import com.atlassian.jira.rest.client.internal.ServerVersionConstants;
import com.atlassian.jira.rest.client.internal.json.JsonParseUtil;
import com.atlassian.jira.rest.client.internal.json.TestConstants;
import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import org.apache.commons.io.IOUtils;
import org.hamcrest.Matchers;
import org.hamcrest.collection.IsCollectionContaining;
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
import java.text.MessageFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.atlassian.jira.rest.client.IntegrationTestUtil.NUMERIC_CUSTOMFIELD_ID;
import static com.atlassian.jira.rest.client.IntegrationTestUtil.NUMERIC_CUSTOMFIELD_TYPE;
import static com.atlassian.jira.rest.client.IntegrationTestUtil.NUMERIC_CUSTOMFIELD_TYPE_V5;
import static com.atlassian.jira.rest.client.IntegrationTestUtil.TESTING_JIRA_5_OR_NEWER;
import static com.atlassian.jira.rest.client.IntegrationTestUtil.USER1;
import static com.atlassian.jira.rest.client.IntegrationTestUtil.USER_ADMIN;
import static com.atlassian.jira.rest.client.TestUtil.assertErrorCode;
import static com.atlassian.jira.rest.client.internal.ServerVersionConstants.BN_JIRA_5;
import static com.atlassian.jira.rest.client.internal.json.TestConstants.ADMIN_PASSWORD;
import static com.atlassian.jira.rest.client.internal.json.TestConstants.ADMIN_USERNAME;
import static com.atlassian.jira.rest.client.internal.json.TestConstants.USER1_USERNAME;
import static com.atlassian.jira.rest.client.internal.json.TestConstants.USER2_USERNAME;
import static org.junit.Assert.*;


// Ignore "May produce NPE" warnings, as we know what we are doing in tests
@SuppressWarnings("ConstantConditions")
@Restore(TestConstants.DEFAULT_JIRA_DUMP_FILE)
public class JerseyIssueRestClientTest extends AbstractJerseyRestClientTest {

	@Rule
	public ExpectedException thrown = ExpectedException.none();

	@Test
	public void testTransitionWithNumericCustomFieldPolishLocale() throws Exception {
		final double newValue = 123.45;
		final FieldInput fieldInput;
		if (IntegrationTestUtil.TESTING_JIRA_5_OR_NEWER) {
			fieldInput = new FieldInput(NUMERIC_CUSTOMFIELD_ID, newValue);
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
		final Issue issue = client.getIssueClient().getIssue("TST-1", new NullProgressMonitor());
		assertNull(issue.getField(NUMERIC_CUSTOMFIELD_ID).getValue());
		final Iterable<Transition> transitions = client.getIssueClient().getTransitions(issue, pm);

		final Transition transitionFound = TestUtil.getTransitionByName(transitions, "Estimate");
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
		Transition transitionFound = TestUtil.getTransitionByName(transitions, "Estimate");

		assertNotNull(transitionFound);
		assertTrue(Iterables.contains(transitionFound.getFields(),
				new Transition.Field(NUMERIC_CUSTOMFIELD_ID, false, IntegrationTestUtil.TESTING_JIRA_5_OR_NEWER ? NUMERIC_CUSTOMFIELD_TYPE_V5 : NUMERIC_CUSTOMFIELD_TYPE)));
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
		final Transition transitionFound = TestUtil.getTransitionByName(transitions, "Estimate");

		assertNotNull(transitionFound);
		assertTrue(Iterables.contains(transitionFound.getFields(),
				new Transition.Field(NUMERIC_CUSTOMFIELD_ID, false, TESTING_JIRA_5_OR_NEWER ? NUMERIC_CUSTOMFIELD_TYPE_V5 : NUMERIC_CUSTOMFIELD_TYPE)));
		final FieldInput fieldInput = new FieldInput(NUMERIC_CUSTOMFIELD_ID, "]432jl");
		// warning: Polish language here - I am asserting if the messages are indeed localized
		// since 5.0 messages are changed and not localized
		assertErrorCode(Response.Status.BAD_REQUEST, TESTING_JIRA_5_OR_NEWER
				? "Operation value must be a number" : "']432jl' nie jest prawid\u0142ow\u0105 liczb\u0105", new Runnable() {
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
		if (IntegrationTestUtil.TESTING_JIRA_5_OR_NEWER) {
			assertInvalidCommentInput(comment, "Invalid role level specified.");
		} else {
			assertInvalidCommentInput(comment, "Invalid role [some-fake-role]");
		}
	}

	@Test
	public void testTransitionWithInvalidGroup() {
		final Comment comment = Comment.createWithGroupLevel("My text which I am just adding " + new DateTime(), "some-fake-group");
		assertInvalidCommentInput(comment, "Group: some-fake-group does not exist.");
	}

	private void assertInvalidCommentInput(final Comment comment, String expectedErrorMsg) {
		final Issue issue = client.getIssueClient().getIssue("TST-1", pm);
		final Iterable<Transition> transitions = client.getIssueClient().getTransitions(issue, pm);
		final Transition transitionFound = TestUtil.getTransitionByName(transitions, "Estimate");
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
		Transition transitionFound = TestUtil.getTransitionByName(transitions, "Estimate");
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

	@Test
	public void testAddWatcherWhoDoesNotHaveViewIssuePermissions() {
		final IssueRestClient issueClient = client.getIssueClient();
		final String issueKey = "RST-1";
		final Issue issue1 = issueClient.getIssue(issueKey, pm);
		final String expectedErrorMessage;

		if (isJira5xOrNewer()) {
			expectedErrorMessage = "The user \"" + USER2_USERNAME +"\" does not have permission to view this issue."
					+ " This user will not be added to the watch list.";
		}
		else if (isJira4x3OrNewer()) {
				expectedErrorMessage = "User '" + ADMIN_USERNAME + "' is not allowed to add watchers to issue '" + issueKey + "'";
		}
		else {
				expectedErrorMessage = "com.sun.jersey.api.client.UniformInterfaceException: Client response status: 401";
		}

		assertErrorCode(Response.Status.UNAUTHORIZED, expectedErrorMessage,
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
		assertErrorCode(Response.Status.NOT_FOUND,
				IntegrationTestUtil.TESTING_JIRA_5_OR_NEWER ? "Issue Does Not Exist" : "The issue no longer exists.", new Runnable() {
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
			assertEquals(USER_ADMIN, comment.getAuthor());
			assertEquals(commentInput.getVisibility(), comment.getVisibility());
		} else {
			assertFalse(linkedIssue.getComments().iterator().hasNext());
		}


		final Issue targetIssue = issueClient.getIssue("TST-6", pm);
		final IssueLink targetLink = targetIssue.getIssueLinks().iterator().next();
		assertEquals(IssueLinkType.Direction.INBOUND, targetLink.getIssueLinkType().getDirection());
		assertEquals("Duplicate", targetLink.getIssueLinkType().getName());
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
		assertEquals(USER_ADMIN, client.getIssueClient().getIssue("TST-5", pm).getAssignee());

		changeUserLanguageByValueOrName("en_UK", "angielski (UK)");
		navigation.issue().unassignIssue("TST-5", "unassigning issue");
		// this single line does instead of 2 above - func test suck with non-English locale
		// but it does not work yet with JIRA 5.0-resthack...
		//navigation.issue().assignIssue("TST-5", "unassigning issue", "Nieprzydzielone");

		assertNull(client.getIssueClient().getIssue("TST-5", pm).getAssignee());
	}

	@Test
	public void testFetchingIssueWithAnonymousComment() {
		changeUserLanguageByValueOrName("en_UK", "angielski (UK)");
		administration.permissionSchemes().scheme("Anonymous Permission Scheme").grantPermissionToGroup(15, "");
		assertEquals(USER_ADMIN, client.getIssueClient().getIssue("TST-5", pm).getAssignee());
		navigation.logout();
		navigation.issue().addComment("ANNON-1", "my nice comment");
		final Issue issue = client.getIssueClient().getIssue("ANNON-1", pm);
		assertEquals(1, Iterables.size(issue.getComments()));
		final Comment comment = issue.getComments().iterator().next();
		assertEquals("my nice comment", comment.getBody());
		if (isJira5xOrNewer()) {
			assertNotNull(comment.getId());
		}
		else {
			assertNull(comment.getId());
		}
		assertNull(comment.getAuthor());
		assertNull(comment.getUpdateAuthor());

	}

	@JiraBuildNumberDependent(BN_JIRA_5)
	@Test
	public void testCreateIssue() {
		// collect CreateIssueMetadata for project with key TST
		final IssueRestClient issueClient = client.getIssueClient();
		final Iterable<CimProject> metadataProjects = issueClient.getCreateIssueMetadata(
				new GetCreateIssueMetadataOptionsBuilder().withProjectKeys("TST").withExpandedIssueTypesFields().build(),
				pm
		);

		// select project and issue
		assertEquals(1, Iterables.size(metadataProjects));
		final CimProject project = metadataProjects.iterator().next();
		final CimIssueType issueType = EntityHelper.findEntityByName(project.getIssueTypes(), "Bug");

		// grab the first component
		final Iterable<Object> allowedValuesForComponents = issueType.getField(IssueFieldId.COMPONENTS_FIELD).getAllowedValues();
		assertNotNull(allowedValuesForComponents);
		assertTrue(allowedValuesForComponents.iterator().hasNext());
		final BasicComponent component = (BasicComponent) allowedValuesForComponents.iterator().next();
		
		// grab the first priority
		final Iterable<Object> allowedValuesForPriority = issueType.getField(IssueFieldId.PRIORITY_FIELD).getAllowedValues();
		assertNotNull(allowedValuesForPriority);
		assertTrue(allowedValuesForPriority.iterator().hasNext());
		final BasicPriority priority = (BasicPriority) allowedValuesForPriority.iterator().next();

		// build issue input
		final String summary = "My new issue!";
		final String description = "Some description";
		final BasicUser assignee = IntegrationTestUtil.USER1;
		final List<String> affectedVersionsNames = Collections.emptyList();
		final DateTime dueDate = new DateTime(new Date().getTime());
		final ArrayList<String> fixVersionsNames = Lists.newArrayList("1.1");

		// prepare IssueInput
		final IssueInputBuilder issueInputBuilder = new IssueInputBuilder(project, issueType, summary)
				.setDescription(description)
				.setAssignee(assignee)
				.setAffectedVersionsNames(affectedVersionsNames)
				.setFixVersionsNames(fixVersionsNames)
				.setComponents(component)
				.setDueDate(dueDate)
				.setPriority(priority);

		// create
		final BasicIssue basicCreatedIssue = issueClient.createIssue(issueInputBuilder.build(), pm);
		assertNotNull(basicCreatedIssue.getKey());

		// get issue and check if everything was set as we expected
		final Issue createdIssue = issueClient.getIssue(basicCreatedIssue.getKey(), pm);
		assertNotNull(createdIssue);

		assertEquals(basicCreatedIssue.getKey(), createdIssue.getKey());
		assertEquals(project.getKey(), createdIssue.getProject().getKey());
		assertEquals(issueType.getId(), createdIssue.getIssueType().getId());
		assertEquals(summary, createdIssue.getSummary());
		assertEquals(description, createdIssue.getDescription());

		final BasicUser actualAssignee = createdIssue.getAssignee();
		assertNotNull(actualAssignee);
		assertEquals(assignee.getSelf(), actualAssignee.getSelf());

		final Iterable<String> actualAffectedVersionsNames = EntityHelper.toNamesList(createdIssue.getAffectedVersions());
		assertThat(affectedVersionsNames, IterableMatcher.hasOnlyElements(actualAffectedVersionsNames));

		final Iterable<String> actualFixVersionsNames = EntityHelper.toNamesList(createdIssue.getFixVersions());
		assertThat(fixVersionsNames, IterableMatcher.hasOnlyElements(actualFixVersionsNames));

		assertTrue(createdIssue.getComponents().iterator().hasNext());
		assertEquals(component.getId(), createdIssue.getComponents().iterator().next().getId());

		// strip time from dueDate
		final DateTime expectedDueDate = JsonParseUtil.parseDate(JsonParseUtil.formatDate(dueDate));
		assertEquals(expectedDueDate, createdIssue.getDueDate());

		final BasicPriority actualPriority = createdIssue.getPriority();
		assertNotNull(actualPriority);
		assertEquals(priority.getId(), actualPriority.getId());
	}

	@JiraBuildNumberDependent(BN_JIRA_5)
	@Test
	public void testCreateIssueWithOnlyRequiredFields() {
		// collect CreateIssueMetadata for project with key TST
		final IssueRestClient issueClient = client.getIssueClient();
		final Iterable<CimProject> metadataProjects = issueClient.getCreateIssueMetadata(
				new GetCreateIssueMetadataOptionsBuilder().withProjectKeys("TST").withExpandedIssueTypesFields().build(),
				pm
		);

		// select project and issue
		assertEquals(1, Iterables.size(metadataProjects));
		final CimProject project = metadataProjects.iterator().next();
		final CimIssueType issueType = EntityHelper.findEntityByName(project.getIssueTypes(), "Bug");

		// build issue input
		final String summary = "My new issue!";

		// create
		final IssueInput issueInput = new IssueInputBuilder(project, issueType, summary).build();
		final BasicIssue basicCreatedIssue = issueClient.createIssue(issueInput, pm);
		assertNotNull(basicCreatedIssue.getKey());

		// get issue and check if everything was set as we expected
		final Issue createdIssue = issueClient.getIssue(basicCreatedIssue.getKey(), pm);
		assertNotNull(createdIssue);

		assertEquals(basicCreatedIssue.getKey(), createdIssue.getKey());
		assertEquals(project.getKey(), createdIssue.getProject().getKey());
		assertEquals(issueType.getId(), createdIssue.getIssueType().getId());
		assertEquals(summary, createdIssue.getSummary());
	}

	@JiraBuildNumberDependent(BN_JIRA_5)
	@Test
	public void testCreateIssueWithoutSummary() {
		final IssueRestClient issueClient = client.getIssueClient();

		thrown.expect(RestClientException.class);
		thrown.expectMessage("You must specify a summary of the issue.");

		final IssueInput issueInput = new IssueInputBuilder("TST", 1L).build();
		issueClient.createIssue(issueInput, pm);
	}

	@JiraBuildNumberDependent(BN_JIRA_5)
	@Test
	public void testCreateIssueWithNotExistentProject() {
		final IssueRestClient issueClient = client.getIssueClient();

		thrown.expect(RestClientException.class);
		thrown.expectMessage("project is required");

		final IssueInput issueInput = new IssueInputBuilder("BAD", 1L, "Should fail").build();
		issueClient.createIssue(issueInput, pm);
	}

	@JiraBuildNumberDependent(BN_JIRA_5)
	@Test
	public void testCreateIssueWithNotExistentIssueType() {
		final IssueRestClient issueClient = client.getIssueClient();

		thrown.expect(RestClientException.class);
		thrown.expectMessage("valid issue type is required");

		final IssueInput issueInput = new IssueInputBuilder("TST", 666L, "Should fail").build();
		issueClient.createIssue(issueInput, pm);
	}

	@JiraBuildNumberDependent(BN_JIRA_5)
	@Test
	public void interactiveUseCase() throws CannotTransformValueException {
		final IssueRestClient issueClient = client.getIssueClient();

		// get project list with fields expanded
		final Iterable<CimProject> metadataProjects = issueClient.getCreateIssueMetadata(
				new GetCreateIssueMetadataOptionsBuilder().withExpandedIssueTypesFields().build(),
				pm
		);
		System.out.println("Available projects: ");
		for (CimProject p : metadataProjects) {
			System.out.println(MessageFormat.format("\t* [{0}] {1}", p.getKey(), p.getName()));
		}
		System.out.println("");
		assertTrue("There is no project to select!", metadataProjects.iterator().hasNext());
		
		// select project
		final CimProject project = metadataProjects.iterator().next();
		System.out.println(MessageFormat.format("Selected project: [{0}] {1}\n", project.getKey(), project.getName()));
		
		// select issue type
		System.out.println("Available issue types for selected project:");
		for (CimIssueType t : project.getIssueTypes()) {
			System.out.println(MessageFormat.format("\t* [{0}] {1}", t.getId(), t.getName()));
		}
		System.out.println("");

		final CimIssueType issueType = project.getIssueTypes().iterator().next();
		System.out.println(MessageFormat.format("Selected issue type: [{0}] {1}\n", issueType.getId(), issueType.getName()));

		final IssueInputBuilder builder = new IssueInputBuilder(project.getKey(), issueType.getId());

		// fill fields
		System.out.println("Filling fields:");
		for (Map.Entry<String, CimFieldInfo> entry : issueType.getFields().entrySet()) {
			final CimFieldInfo fieldInfo = entry.getValue();

			if ("project".equals(fieldInfo.getId()) || "issuetype".equals(fieldInfo.getId())) {
				// this field was already set by IssueInputBuilder constructor - skip it
				continue;
			}

			System.out.println(MessageFormat.format("\t* [{0}] {1}\n\t\t| schema: {2}\n\t\t| required: {3}", fieldInfo.getId(), fieldInfo.getName(), fieldInfo.getSchema(), fieldInfo.isRequired()));

			// choose value for this field
			Object value = null;
			final Iterable<Object> allowedValues = fieldInfo.getAllowedValues();
			if (allowedValues != null) {
				System.out.println("\t\t| field only accepts those values:");
				for (Object val : allowedValues) {
					System.out.println("\t\t\t* " + val);
				}
				if (allowedValues.iterator().hasNext()) {
					final Object singleValue = allowedValues.iterator().next();
					value = "array".equals(fieldInfo.getSchema().getType()) ? Collections.singletonList(singleValue) : singleValue;
					System.out.println("\t\t| selecting value: " + value);
				}
				else {
					System.out.println("\t\t| there is no allowed value - leaving field blank");
				}
			}
			else {
				if ("string".equals(fieldInfo.getSchema().getType())) {
					value = "This is simple string value for field " + fieldInfo.getId() + " named " + fieldInfo.getName() + ".";
				}
				else if ("user".equals(fieldInfo.getSchema().getType())) {
					value = IntegrationTestUtil.USER_ADMIN;
				}
				else {
					if (fieldInfo.isRequired()) {
						fail("I don't know how to fill that required field, sorry.");
					}
					else {
						System.out.println("\t\t| field value is not required, leaving blank");
					}
				}
			}
			if (value == null) {
				System.out.println("\t\t| value is null, skipping filed");
			}
			else {
				System.out.println(MessageFormat.format("\t\t| setting value => {0}", value));
				builder.setFieldValue(fieldInfo.getId(), value);
			}
		}
		System.out.println("");

		// all required data is provided, let's create issue
		final IssueInput issueInput = builder.build();

		final BasicIssue basicCreatedIssue = issueClient.createIssue(issueInput, pm);
		assertNotNull(basicCreatedIssue);

		final Issue createdIssue = issueClient.getIssue(basicCreatedIssue.getKey(), pm);
		assertNotNull(createdIssue);

		System.out.println("Created new issue successfully, key: " + basicCreatedIssue.getKey());

		// assert few fields
		IssueInputBuilder actualBuilder = new IssueInputBuilder(createdIssue.getProject(), createdIssue.getIssueType(), createdIssue.getSummary())
				.setPriority(createdIssue.getPriority())
				.setReporter(createdIssue.getReporter())
				.setAssignee(createdIssue.getAssignee())
				.setDescription(createdIssue.getDescription());

		final Collection<FieldInput> actualValues = actualBuilder.build().getFields().values();
		final Collection<FieldInput> expectedValues = issueInput.getFields().values();

		assertThat(expectedValues, IsCollectionContaining.hasItems(actualValues.toArray(new FieldInput[actualValues.size()])));
	}

}
