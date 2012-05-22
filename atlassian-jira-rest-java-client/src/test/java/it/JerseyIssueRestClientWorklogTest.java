/*
 * Copyright (C) 2012 Atlassian
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package it;

import com.atlassian.jira.rest.client.IssueRestClient;
import com.atlassian.jira.rest.client.IterableMatcher;
import com.atlassian.jira.rest.client.RestClientException;
import com.atlassian.jira.rest.client.annotation.JiraBuildNumberDependent;
import com.atlassian.jira.rest.client.annotation.RestoreOnce;
import com.atlassian.jira.rest.client.domain.Issue;
import com.atlassian.jira.rest.client.domain.Visibility;
import com.atlassian.jira.rest.client.domain.Worklog;
import com.atlassian.jira.rest.client.domain.input.WorklogInput;
import com.atlassian.jira.rest.client.domain.input.WorklogInputBuilder;
import com.atlassian.jira.rest.client.internal.json.TestConstants;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import org.joda.time.DateTime;
import org.junit.Test;

import java.util.Set;

import static com.atlassian.jira.rest.client.IntegrationTestUtil.GROUP_JIRA_ADMINISTRATORS;
import static com.atlassian.jira.rest.client.internal.ServerVersionConstants.BN_JIRA_5;
import static org.junit.Assert.*;

@RestoreOnce(TestConstants.DEFAULT_JIRA_DUMP_FILE)
public class JerseyIssueRestClientWorklogTest extends AbstractJerseyRestClientTest {

	public static final String ISSUE_KEY = "TST-5";
	public static final String ISSUE_KEY_ANONYMOUS = "ANONEDIT-2";

	@JiraBuildNumberDependent(BN_JIRA_5)
	@Test
	public void testAddWorklogAsLoggedUser() {
		testAddWorklogImpl(ISSUE_KEY, createDefaulWorklogInputBuilder());
	}

	@JiraBuildNumberDependent(BN_JIRA_5)
	@Test
	public void testAddWorklogAsAnonymousWithoutPermissions() {
		setAnonymousMode();
		try {
			testAddWorklogImpl(ISSUE_KEY, createDefaulWorklogInputBuilder());
		} catch (RestClientException ex) {
			assertThat(ex.getErrorMessages(), IterableMatcher
					.hasOnlyElements("You do not have the permission to see the specified issue.", "Login Required"));
		}
	}

	@JiraBuildNumberDependent(BN_JIRA_5)
	@Test
	public void testAddWorklogAsAnonymous() {
		setAnonymousMode();
		testAddWorklogImpl(ISSUE_KEY_ANONYMOUS, createDefaulWorklogInputBuilder());
	}

	@JiraBuildNumberDependent(BN_JIRA_5)
	@Test
	public void testAddWorklogWithEmptyComment() {
		testAddWorklogImpl(ISSUE_KEY, createDefaulWorklogInputBuilder().setComment(""));
	}

	@JiraBuildNumberDependent(BN_JIRA_5)
	@Test
	public void testAddWorklogWithVisibility() {
		Visibility visibility = Visibility.group(GROUP_JIRA_ADMINISTRATORS);
		testAddWorklogImpl(ISSUE_KEY, createDefaulWorklogInputBuilder().setVisibility(visibility));
	}

	private Worklog getAddedWorklog(final Set<Worklog> initialWorklogs, Issue issue) {
		final Set<Worklog> worklogs = Sets.newHashSet(issue.getWorklogs());
		worklogs.removeAll(initialWorklogs);
		assertEquals(1, worklogs.size());
		return worklogs.iterator().next();
	}

	private void testAddWorklogImpl(String issueKey, WorklogInputBuilder worklogBuilder) {
		final IssueRestClient issueClient = client.getIssueClient();

		// get initial worklogs
		final Issue issue = issueClient.getIssue(issueKey, pm);
		final Set<Worklog> initialWorklogs = ImmutableSet.copyOf(issue.getWorklogs());

		// create and add new
		final WorklogInput worklogInput = worklogBuilder.setIssueUri(issue.getSelf()).build();
		issueClient.addWorklog(issue.getWorklogUri(), worklogInput, pm);

		// check if added correctly
		final Issue issueWithWorklog = issueClient.getIssue(issueKey, pm);
		final Worklog addedWorklog = getAddedWorklog(initialWorklogs, issueWithWorklog);
		assertEquals(worklogInput.getStartDate(), addedWorklog.getStartDate());
		assertEquals(worklogInput.getMinutesSpent(), addedWorklog.getMinutesSpent());
		assertEquals(worklogInput.getIssueUri(), addedWorklog.getIssueUri());
		assertEquals(worklogInput.getComment(), addedWorklog.getComment());
		assertEquals(worklogInput.getVisibility(), worklogInput.getVisibility());
	}

	private WorklogInputBuilder createDefaulWorklogInputBuilder() {
		return new WorklogInputBuilder()
				.setComment("I created test for adding worklog.")
				.setStartDate(new DateTime())
				.setMinutesSpent(20);
	}

}
