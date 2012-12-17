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
import com.atlassian.jira.rest.client.IntegrationTestUtil;
import com.atlassian.jira.rest.client.api.IssueRestClient;
import com.atlassian.jira.rest.client.api.domain.Comment;
import com.atlassian.jira.rest.client.api.domain.Issue;
import com.atlassian.jira.rest.client.internal.json.TestConstants;
import com.google.common.base.Objects;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import org.junit.Test;

import java.util.List;

import static com.atlassian.jira.rest.client.internal.ServerVersionConstants.BN_JIRA_5;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

/**
 * Those tests mustn't change anything on server side, as jira is restored only once
 */
@RestoreOnce(TestConstants.DEFAULT_JIRA_DUMP_FILE)
public class AsynchronousIssueRestClientCommentTest extends AbstractAsynchronousRestClientTest {

	@Test
	@JiraBuildNumberDependent(BN_JIRA_5)
	public void testAddCommentToIssue() {
		testAddCommentToIssueImpl("TST-5", Comment.valueOf("Simple test comment."));
	}

	@Test
	@JiraBuildNumberDependent(BN_JIRA_5)
	public void testAddCommentToIssueAsAnonymousUser() {
		setAnonymousMode();
		testAddCommentToIssueImpl("ANONEDIT-1", Comment.valueOf("Simple test comment."));
	}

	@Test
	@JiraBuildNumberDependent(BN_JIRA_5)
	public void testAddCommentToIssueWithGroupLevelVisibility() {
		final Comment comment = Comment.createWithGroupLevel("Simple test comment restricted for admins.",
				IntegrationTestUtil.GROUP_JIRA_ADMINISTRATORS);
		final String issueKey = "ANONEDIT-1";
		final Comment addedComment = testAddCommentToIssueImpl(issueKey, comment);

		// try to get as anonymous user
		setAnonymousMode();

		final IssueRestClient issueClient = client.getIssueClient();
		final Issue issue = issueClient.getIssue(issueKey).claim();

		// test if we can see added comment
		assertFalse(hasComment(issue.getComments(), addedComment.getId()));
	}

	@Test
	@JiraBuildNumberDependent(BN_JIRA_5)
	public void testAddCommentToIssueWithRoleLevelVisibility() {
		final Comment comment = Comment.createWithRoleLevel("Simple test comment restricted for role Administrators.",
				IntegrationTestUtil.ROLE_ADMINISTRATORS);
		final String issueKey = "ANONEDIT-1";
		final Comment addedComment = testAddCommentToIssueImpl(issueKey, comment);

		// try to get as anonymous user
		setAnonymousMode();

		final IssueRestClient issueClient = client.getIssueClient();
		final Issue issue = issueClient.getIssue(issueKey).claim();

		// test if we can see added comment
		assertFalse(hasComment(issue.getComments(), addedComment.getId()));
	}

	private boolean hasComment(final Iterable<Comment> comments, final Long id) {
		return Iterables.filter(comments, new Predicate<Comment>() {
			@Override
			public boolean apply(Comment input) {
				return Objects.equal(input.getId(), id);
			}
		}).iterator().hasNext();
	}

	private Comment testAddCommentToIssueImpl(final String issueKey, final Comment comment) {
		final IssueRestClient issueClient = client.getIssueClient();
		final Issue issue = issueClient.getIssue(issueKey).claim();
		final List<Comment> initialComments = Lists.newArrayList(issue.getComments());

		issueClient.addComment(issue.getCommentsUri(), comment).claim();

		final Issue issueWithComments = issueClient.getIssue(issueKey).claim();
		final List<Comment> newComments = Lists.newArrayList(issueWithComments.getComments());
		newComments.removeAll(initialComments);
		assertEquals(1, Iterables.size(newComments));
		Comment addedComment = newComments.get(0);
		assertEquals(comment.getBody(), addedComment.getBody());
		assertEquals(comment.getVisibility(), addedComment.getVisibility());
		return addedComment;
	}
}
