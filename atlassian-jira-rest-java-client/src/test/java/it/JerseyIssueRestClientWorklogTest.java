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

import com.atlassian.jira.nimblefunctests.annotation.JiraBuildNumberDependent;
import com.atlassian.jira.nimblefunctests.annotation.RestoreOnce;
import com.atlassian.jira.rest.client.IssueRestClient;
import com.atlassian.jira.rest.client.RestClientException;
import com.atlassian.jira.rest.client.domain.Issue;
import com.atlassian.jira.rest.client.domain.TimeTracking;
import com.atlassian.jira.rest.client.domain.Visibility;
import com.atlassian.jira.rest.client.domain.Worklog;
import com.atlassian.jira.rest.client.domain.input.WorklogInput;
import com.atlassian.jira.rest.client.domain.input.WorklogInputBuilder;
import com.atlassian.jira.rest.client.internal.json.TestConstants;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import org.joda.time.DateTime;
import org.junit.Test;

import javax.annotation.Nullable;
import java.util.Set;

import static com.atlassian.jira.rest.client.IntegrationTestUtil.GROUP_JIRA_ADMINISTRATORS;
import static com.atlassian.jira.rest.client.internal.ServerVersionConstants.BN_JIRA_5;
import static org.hamcrest.collection.IsIterableContainingInAnyOrder.containsInAnyOrder;
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
			assertThat(ex.getErrorMessages(),
					containsInAnyOrder("You do not have the permission to see the specified issue.", "Login Required"));
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

	@JiraBuildNumberDependent(BN_JIRA_5)
	@Test
	public void testAddWorklogsWithEstimateAdjustment() {		
		final String issueKey = ISSUE_KEY;
		
		// set estimate in issue
		navigation.issue().setEstimates(ISSUE_KEY, "20", "20");
		
		final WorklogInputBuilder worklogInputBuilder = createDefaulWorklogInputBuilder();
		final IssueRestClient issueClient = client.getIssueClient();

		// get issue
		final Issue initialIssue = issueClient.getIssue(issueKey, pm);

		// # First change - test auto
		final WorklogInput worklogInput = worklogInputBuilder
				.setIssueUri(initialIssue.getSelf())
				.setMinutesSpent(2)
				.build();
		issueClient.addWorklog(initialIssue.getWorklogUri(), worklogInput, pm);

		// check if estimate nad logged has changed
		final Issue issueAfterFirstChange = issueClient.getIssue(issueKey, pm);
		final Integer actualTimeSpentAfterChange = getTimeSpentMinutesNotNull(issueAfterFirstChange.getTimeTracking());
		final Integer expectedTimeSpentAfterChange = getTimeSpentMinutesNotNull(initialIssue.getTimeTracking()) + worklogInput.getMinutesSpent();
		assertEquals(expectedTimeSpentAfterChange, actualTimeSpentAfterChange);

		final int actualRemainingEstimate = getRemainingEstimateMinutesNotNull(issueAfterFirstChange.getTimeTracking());
		final int expectedRemaningEstimate = getRemainingEstimateMinutesNotNull(initialIssue.getTimeTracking()) - worklogInput.getMinutesSpent();
		assertEquals(expectedRemaningEstimate, actualRemainingEstimate);

		// # Second change - test new; also we want to be sure that logged time are added, and not set to given value
		final Integer newEstimateValue = 15;
		final WorklogInput worklogInput2 = worklogInputBuilder
				.setIssueUri(initialIssue.getSelf())
				.setMinutesSpent(2)
				.setAdjustEstimateNew(newEstimateValue.toString())
				.build();
		issueClient.addWorklog(initialIssue.getWorklogUri(), worklogInput2, pm);

		// check if logged time has changed
		final Issue issueAfterSecondChange = issueClient.getIssue(issueKey, pm);
		final Integer actualTimeSpentAfterChange2 = getTimeSpentMinutesNotNull(issueAfterSecondChange.getTimeTracking());
		final Integer expectedTimeSpentAfterChange2 = getTimeSpentMinutesNotNull(issueAfterFirstChange.getTimeTracking()) + worklogInput2.getMinutesSpent();
		assertEquals(expectedTimeSpentAfterChange2, actualTimeSpentAfterChange2);

		// check if estimate has changed
		final Integer actualRemainingEstimate2 = getRemainingEstimateMinutesNotNull(issueAfterSecondChange.getTimeTracking());
		assertEquals(newEstimateValue, actualRemainingEstimate2);

		// # Third change - test leave
		final WorklogInput worklogInput3 = worklogInputBuilder
				.setIssueUri(initialIssue.getSelf())
				.setMinutesSpent(2)
				.setAdjustEstimateLeave()
				.build();
		issueClient.addWorklog(initialIssue.getWorklogUri(), worklogInput3, pm);

		// check if logged time has changed
		final Issue issueAfterThirdChange = issueClient.getIssue(issueKey, pm);
		final Integer actualTimeSpentAfterChange3 = getTimeSpentMinutesNotNull(issueAfterThirdChange.getTimeTracking());
		final Integer expectedTimeSpentAfterChange3 = getTimeSpentMinutesNotNull(issueAfterSecondChange.getTimeTracking()) + worklogInput3.getMinutesSpent();
		assertEquals(expectedTimeSpentAfterChange3, actualTimeSpentAfterChange3);

		// check if estimate has NOT changed
		final Integer actualRemainingEstimate3 = getRemainingEstimateMinutesNotNull(issueAfterThirdChange.getTimeTracking());
		final Integer expectedRemainingEstimate3 = getRemainingEstimateMinutesNotNull(issueAfterSecondChange.getTimeTracking());
		assertEquals(expectedRemainingEstimate3, actualRemainingEstimate3);

		// # Fourth change - test manual
		final Integer reduceByValueManual = 7;
		final WorklogInput worklogInput4 = worklogInputBuilder
				.setIssueUri(initialIssue.getSelf())
				.setMinutesSpent(2)
				.setAdjustEstimateManual(reduceByValueManual.toString())
				.build();

		issueClient.addWorklog(initialIssue.getWorklogUri(), worklogInput4, pm);

		// check if logged time has changed
		final Issue issueAfterFourthChange = issueClient.getIssue(issueKey, pm);
		final Integer actualTimeSpentAfterChange4 = getTimeSpentMinutesNotNull(issueAfterFourthChange.getTimeTracking());
		final Integer expectedTimeSpentAfterChange4 = getTimeSpentMinutesNotNull(issueAfterThirdChange.getTimeTracking()) + worklogInput4.getMinutesSpent();
		assertEquals(expectedTimeSpentAfterChange4, actualTimeSpentAfterChange4);

		// check if estimate has NOT changed
		final Integer actualRemainingEstimate4 = getRemainingEstimateMinutesNotNull(issueAfterFourthChange.getTimeTracking());
		final Integer expectedRemainingEstimate4 = getRemainingEstimateMinutesNotNull(issueAfterThirdChange.getTimeTracking()) - reduceByValueManual;
		assertEquals(expectedRemainingEstimate4, actualRemainingEstimate4);
	}

	private int getTimeSpentMinutesNotNull(@Nullable TimeTracking timeTracking) {
		if (timeTracking == null) {
			return 0;
		}

		Integer timeSpentMinutes = timeTracking.getTimeSpentMinutes();
		return timeSpentMinutes == null ? 0 : timeSpentMinutes; 
	}
	
	private int getRemainingEstimateMinutesNotNull(@Nullable TimeTracking timeTracking) {
		if (timeTracking == null) {
			return 0;
		}

		Integer remainingEstimateMinutes = timeTracking.getRemainingEstimateMinutes();
		return remainingEstimateMinutes == null ? 0 : remainingEstimateMinutes; 
	}


	private Worklog getAddedWorklog(final Set<Worklog> initialWorklogs, Issue issue) {
		final Set<Worklog> worklogs = Sets.newHashSet(issue.getWorklogs());
		worklogs.removeAll(initialWorklogs);
		assertEquals(1, worklogs.size());
		return worklogs.iterator().next();
	}

	private void testAddWorklogImpl(String issueKey, WorklogInputBuilder worklogInputBuilder) {
		final IssueRestClient issueClient = client.getIssueClient();

		// get initial worklogs
		final Issue issue = issueClient.getIssue(issueKey, pm);
		final Set<Worklog> initialWorklogs = ImmutableSet.copyOf(issue.getWorklogs());

		// create and add new
		final WorklogInput worklogInput = worklogInputBuilder.setIssueUri(issue.getSelf()).build();
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
