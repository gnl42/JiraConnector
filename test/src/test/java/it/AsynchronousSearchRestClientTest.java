/*
 * Copyright (C) 2011 Atlassian
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
import com.atlassian.jira.rest.client.IntegrationTestUtil;
import com.atlassian.jira.rest.client.TestUtil;
import com.atlassian.jira.rest.client.api.RestClientException;
import com.atlassian.jira.rest.client.api.SearchRestClient;
import com.atlassian.jira.rest.client.api.domain.BasicPriority;
import com.atlassian.jira.rest.client.api.domain.BasicProject;
import com.atlassian.jira.rest.client.api.domain.BasicVotes;
import com.atlassian.jira.rest.client.api.domain.BasicWatchers;
import com.atlassian.jira.rest.client.api.domain.Comment;
import com.atlassian.jira.rest.client.api.domain.EntityHelper;
import com.atlassian.jira.rest.client.api.domain.Issue;
import com.atlassian.jira.rest.client.api.domain.IssueLink;
import com.atlassian.jira.rest.client.api.domain.IssueLinkType;
import com.atlassian.jira.rest.client.api.domain.SearchResult;
import com.atlassian.jira.rest.client.api.domain.TimeTracking;
import com.atlassian.jira.rest.client.api.domain.Version;
import com.atlassian.jira.rest.client.api.domain.Worklog;
import com.atlassian.jira.rest.client.internal.json.TestConstants;
import com.atlassian.jira.rest.client.test.matchers.AddressableEntityMatchers;
import com.atlassian.jira.rest.client.test.matchers.NamedEntityMatchers;
import com.atlassian.jira.testkit.client.Backdoor;
import com.atlassian.jira.testkit.client.util.TestKitLocalEnvironmentData;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;
import org.hamcrest.collection.IsIterableContainingInOrder;
import org.junit.Before;
import org.junit.Test;

import javax.ws.rs.core.Response;
import java.lang.reflect.InvocationTargetException;
import java.util.Set;

import static com.atlassian.jira.nimblefunctests.annotation.LongCondition.LESS_THAN;
import static com.atlassian.jira.rest.client.IntegrationTestUtil.resolveURI;
import static com.atlassian.jira.rest.client.TestUtil.assertEmptyIterable;
import static com.atlassian.jira.rest.client.TestUtil.toDateTime;
import static com.atlassian.jira.rest.client.internal.ServerVersionConstants.BN_JIRA_6_1;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.anyOf;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.collection.IsIterableContainingInAnyOrder.containsInAnyOrder;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;

public class AsynchronousSearchRestClientTest extends AbstractAsynchronousRestClientTest {

	public static final Set<String> REQUIRED_ISSUE_FIELDS = ImmutableSet.of("summary", "issuetype", "created", "updated",
			"project", "status");

	@Before
	public void setup() {
		Backdoor backdoor = new Backdoor(new TestKitLocalEnvironmentData());
		backdoor.restoreDataFromResource(TestConstants.DEFAULT_JIRA_DUMP_FILE);
	}

	@Test
	public void testJqlSearch() {
		final SearchResult searchResultForNull = client.getSearchClient().searchJql(null).claim();
		assertEquals(11, searchResultForNull.getTotal());

		final SearchResult searchResultForReporterWseliga = client.getSearchClient().searchJql("reporter=wseliga").claim();
		assertEquals(1, searchResultForReporterWseliga.getTotal());
	}

	@Test
	public void testJqlSearchWithStartAt() {
		final int maxResults = 3;

		// returns: 0,1,2
		final SearchResult searchResultFrom0 = client.getSearchClient().searchJql(null, maxResults, 0, null).claim();
		final Issue secondIssueFromFirstSearch = Iterables.get(searchResultFrom0.getIssues(), 1);

		// returns: 1,2,3
		final SearchResult searchResultFrom1 = client.getSearchClient().searchJql(null, maxResults, 1, null).claim();
		final Issue firstIssueFromSecondSearch = Iterables.get(searchResultFrom1.getIssues(), 0);

		assertEquals(secondIssueFromFirstSearch, firstIssueFromSecondSearch);
	}

	@Test
	public void testJqlSearchWithNullStartAtShouldUseDefault0ForStartAtAndPreserveMaxResults() {
		final int maxResults = 21;
		final SearchResult searchResult = client.getSearchClient().searchJql(null, maxResults, null, null).claim();
		assertEquals(0, searchResult.getStartIndex());
		assertEquals(maxResults, searchResult.getMaxResults());
	}

	@Test
	public void testJqlSearchWithNullMaxResultsShouldUseDefault50ForMaxResultsAndPreserveStartAt() {
		final int startAt = 7;
		final SearchResult searchResult = client.getSearchClient().searchJql(null, null, startAt, null).claim();
		assertEquals(50, searchResult.getMaxResults());
		assertEquals(startAt, searchResult.getStartIndex());
	}

	@Test
	public void testJqlSearchWithNullStartAtAndMaxResultsShouldUseAsDefault0ForStartIndexAnd50ForMaxResults() {
		final SearchResult searchResult = client.getSearchClient().searchJql(null, null, null, null).claim();
		assertEquals(50, searchResult.getMaxResults());
		assertEquals(0, searchResult.getStartIndex());
	}

	@Test
	public void testJqlSearchAsAnonymous() {
		setAnonymousMode();
		final SearchResult searchResultForNull = client.getSearchClient().searchJql(null).claim();
		assertEquals(3, searchResultForNull.getTotal());

		final SearchResult searchResultForReporterWseliga = client.getSearchClient().searchJql("reporter=wseliga").claim();
		assertEquals(0, searchResultForReporterWseliga.getTotal());
	}

	@Test
	public void testJqlSearchWithPaging() {
		final SearchResult searchResultForNull = client.getSearchClient().searchJql(null, 3, 3, null).claim();
		assertEquals(11, searchResultForNull.getTotal());
		assertEquals(3, Iterables.size(searchResultForNull.getIssues()));
		assertEquals(3, searchResultForNull.getStartIndex());
		assertEquals(3, searchResultForNull.getMaxResults());

		final SearchResult search2 = client.getSearchClient().searchJql("assignee is not EMPTY", 2, 1, null).claim();
		assertEquals(11, search2.getTotal());
		assertEquals(2, Iterables.size(search2.getIssues()));
		assertEquals("TST-6", Iterables.get(search2.getIssues(), 0).getKey());
		assertEquals("TST-5", Iterables.get(search2.getIssues(), 1).getKey());
		assertEquals(1, search2.getStartIndex());
		assertEquals(2, search2.getMaxResults());

		setUser1();
		final SearchResult search3 = client.getSearchClient().searchJql("assignee is not EMPTY", 10, 5, null).claim();
		assertEquals(10, search3.getTotal());
		assertEquals(5, Iterables.size(search3.getIssues()));
		assertEquals(5, search3.getStartIndex());
		assertEquals(10, search3.getMaxResults());
	}

	@Test
	public void testVeryLongJqlWhichWillBePost() {
		final String longJql = generateVeryLongJql() + " or summary is not empty"; // so that effectively all issues are returned;
		final SearchResult searchResultForNull = client.getSearchClient().searchJql(longJql, 3, 6, null).claim();
		assertEquals(11, searchResultForNull.getTotal());
		assertEquals(3, Iterables.size(searchResultForNull.getIssues()));
		assertEquals(6, searchResultForNull.getStartIndex());
		assertEquals(3, searchResultForNull.getMaxResults());
	}

	private String generateVeryLongJql() {
		final String coreJql = "(reporter is not empty OR reporter is empty)";
		StringBuilder sb = new StringBuilder(coreJql);
		for (int i = 0; i < 500; i++) {
			sb.append(" and ").append(coreJql);
		}
		return sb.toString();
	}

	@Test
	public void testJqlSearchUnescapedCharacter() {
		TestUtil.assertErrorCode(Response.Status.BAD_REQUEST, new Runnable() {
			@Override
			public void run() {
				client.getSearchClient().searchJql("reporter=a/user/with/slash").claim();
			}
		});
	}

	@JiraBuildNumberDependent(value = BN_JIRA_6_1, condition = LESS_THAN)
	@Test
	public void jqlSearchShouldReturnIssueWithDetailsBefore6_1() throws InvocationTargetException, IllegalAccessException {
		jqlSearchShouldReturnIssueWithDetails("rest/api/2/project/TST");
	}

	@JiraBuildNumberDependent(value = BN_JIRA_6_1)
	@Test
	public void jqlSearchShouldReturnIssueWithDetails() throws InvocationTargetException, IllegalAccessException {
		jqlSearchShouldReturnIssueWithDetails("rest/api/2/project/10000");
	}

	private void jqlSearchShouldReturnIssueWithDetails(String projectSelf) {
		final SearchResult searchResult = client.getSearchClient().searchJql("reporter=wseliga").claim();
		final Issue issue = Iterables.getOnlyElement(searchResult.getIssues());

		assertEquals("TST-7", issue.getKey());
		assertEquals(Long.valueOf(10040), issue.getId());
		assertEquals(resolveURI("rest/api/latest/issue/10040"), issue.getSelf());
		assertEquals("A task where someone will vote", issue.getSummary());
		assertNull(issue.getDescription()); // by default search doesn't retrieve description
		assertEquals(new BasicPriority(resolveURI("rest/api/2/priority/3"), 3L, "Major"), issue.getPriority());
		assertThat(issue.getStatus(), allOf(
				hasProperty("self", is(resolveURI("rest/api/2/status/1"))),
				hasProperty("id", is(1L)),
				hasProperty("name", is("Open")),
				hasProperty("description", is("The issue is open and ready for the assignee to start work on it.")),
				anyOf(
						hasProperty("iconUrl", is(resolveURI("images/icons/statuses/open.png"))), // Jira >= 5.2
						hasProperty("iconUrl", is(resolveURI("images/icons/status_open.gif"))) // Jira < 5.2
				)));
		assertEmptyIterable(issue.getComments());  // not expanded by default
		assertEmptyIterable(issue.getComponents());
		assertEmptyIterable(issue.getWorklogs());
		assertEmptyIterable(issue.getSubtasks());
		assertEmptyIterable(issue.getIssueLinks());
		assertEmptyIterable(issue.getFixVersions());
		assertEmptyIterable(issue.getAffectedVersions());
		assertEmptyIterable(issue.getLabels());
		assertNull(issue.getDueDate());
		assertNull(issue.getTimeTracking());
		assertNull(issue.getResolution());
		assertNull(issue.getChangelog());
		assertNull(issue.getAttachments());
		// JIRA does not store timezone information in its dump file, so no timezone here
		assertEquals(toDateTime("2010-09-22T18:06:32.000"), issue.getUpdateDate());
		assertEquals(toDateTime("2010-09-22T18:06:32.000"), issue.getCreationDate());
		assertEquals(IntegrationTestUtil.USER1_FULL, issue.getReporter());
		assertEquals(IntegrationTestUtil.USER_ADMIN_FULL, issue.getAssignee());
		assertEquals(new BasicProject(resolveURI(projectSelf), "TST", 10000L, "Test Project"), issue.getProject());
		assertEquals(new BasicVotes(resolveURI("rest/api/2/issue/TST-7/votes"), 0, false), issue.getVotes());
		assertEquals(new BasicWatchers(resolveURI("rest/api/2/issue/TST-7/watchers"), false, 0), issue.getWatchers());
		assertThat(issue.getIssueType(), notNullValue());
		assertThat(issue.getIssueType().getSelf(), is(resolveURI("rest/api/2/issuetype/3")));
		assertThat(issue.getIssueType().getId(), is(3L));
		assertThat(issue.getIssueType().getName(), is("Task"));
		assertThat(issue.getIssueType().isSubtask(), is(false));
		assertThat(issue.getIssueType().getDescription(), is("A task that needs to be done."));
        assertThat(issue.getIssueType().getIconUri(), anyOf(
                is(resolveURI("images/icons/issuetypes/task.png")),
                is(resolveURI("images/icons/task.gif")),
                is(resolveURI("secure/viewavatar?size=xsmall&avatarId=10178&avatarType=issuetype"))
        ));
    }

	@Test
	public void jqlSearchWithAllFieldsRequestedShouldReturnIssueWithAllFields() throws Exception {
		jqlSearchWithAllFieldsImpl("key=TST-2");
	}

	@Test
	public void jqlSearchUsingPostWithAllFieldsRequestedShouldReturnIssueWithAllFields() throws Exception {
		jqlSearchWithAllFieldsImpl(generateVeryLongJql() + "and key=TST-2");
	}

	private void jqlSearchWithAllFieldsImpl(String jql) {
		final ImmutableSet<String> fields = ImmutableSet.of("*all");
		final SearchResult searchResult = client.getSearchClient().searchJql(jql, null, null, fields).claim();
		final Issue issue = Iterables.getOnlyElement(searchResult.getIssues());

		assertEquals("TST-2", issue.getKey());
		assertEquals("Testing attachem2", issue.getSummary());
		assertEquals(new TimeTracking(0, 0, 145), issue.getTimeTracking());
		assertThat(issue.getComponents(), NamedEntityMatchers.entitiesWithNames("Component A", "Component B"));

		// comments
		final Iterable<Comment> comments = issue.getComments();
		assertEquals(3, Iterables.size(comments));
		assertEquals("a comment viewable only by jira-users", Iterables.getLast(comments).getBody());

		// worklogs
		final Iterable<Worklog> worklogs = issue.getWorklogs();
		assertThat(worklogs, AddressableEntityMatchers.entitiesWithSelf(
				resolveURI("rest/api/2/issue/10010/worklog/10010"),
				resolveURI("rest/api/2/issue/10010/worklog/10011"),
				resolveURI("rest/api/2/issue/10010/worklog/10012"),
				resolveURI("rest/api/2/issue/10010/worklog/10020"),
				resolveURI("rest/api/2/issue/10010/worklog/10021")
		));

		final Worklog actualWorklog = Iterables.getLast(worklogs);
		final Worklog expectedWorklog = new Worklog(resolveURI("rest/api/2/issue/10010/worklog/10021"),
				resolveURI("rest/api/latest/issue/10010"), IntegrationTestUtil.USER_ADMIN, IntegrationTestUtil.USER_ADMIN,
				"Another work for 7 min", toDateTime("2010-08-27T15:00:02.104"), toDateTime("2010-08-27T15:00:02.104"),
				toDateTime("2010-08-27T14:59:00.000"), 7, null);
		assertEquals(expectedWorklog, actualWorklog);

		// issue links
		assertThat(issue.getIssueLinks(), IsIterableContainingInOrder.contains(
				new IssueLink("TST-1", resolveURI("rest/api/2/issue/10000"), new IssueLinkType("Duplicate", "duplicates", IssueLinkType.Direction.OUTBOUND)),
				new IssueLink("TST-1", resolveURI("rest/api/2/issue/10000"), new IssueLinkType("Duplicate", "is duplicated by", IssueLinkType.Direction.INBOUND))
		));

		// fix versions
		final Version actualFixVersion = Iterables.getOnlyElement(issue.getFixVersions());
		final Version expectedFixVersion = new Version(resolveURI("rest/api/2/version/10000"), 10000L, "1.1", "Some version", false, false, toDateTime("2010-08-25T00:00:00.000"));
		assertEquals(expectedFixVersion, actualFixVersion);

		// affected versions
		assertThat(issue.getAffectedVersions(), IsIterableContainingInOrder.contains(
				new Version(resolveURI("rest/api/2/version/10001"), 10001L, "1", "initial version", false, false, null),
				new Version(resolveURI("rest/api/2/version/10000"), 10000L, "1.1", "Some version", false, false, toDateTime("2010-08-25T00:00:00.000"))
		));

		// dates
		assertNull(issue.getDueDate());
		assertEquals(toDateTime("2010-08-30T10:49:33.000"), issue.getUpdateDate());
		assertEquals(toDateTime("2010-07-26T13:29:18.000"), issue.getCreationDate());

		// attachments
		final Iterable<String> attachmentsNames = EntityHelper.toFileNamesList(issue.getAttachments());
		assertThat(attachmentsNames, containsInAnyOrder("10000_thumb_snipe.jpg", "Admal pompa ciepła.pdf",
				"apache-tomcat-5.5.30.zip", "jira_logo.gif", "snipe.png", "transparent-png.png"));
	}

	@Test
	public void jqlSearchShouldReturnIssueWithLabelsAndDueDate() throws Exception {
		final SearchResult searchResult = client.getSearchClient().searchJql("key=TST-1").claim();
		final Issue issue = Iterables.getOnlyElement(searchResult.getIssues());
		assertEquals("TST-1", issue.getKey());
		assertThat(issue.getLabels(), containsInAnyOrder("a", "bcds"));
		assertEquals(toDateTime("2010-07-05T00:00:00.000"), issue.getDueDate());
	}

	@Test
	public void jqlSearchWithMinimalFieldSetShouldReturnParseableIssues() throws Exception {
		final SearchRestClient searchClient = client.getSearchClient();
		final SearchResult searchResult = searchClient.searchJql("key=TST-1", null, null, REQUIRED_ISSUE_FIELDS).claim();
		final Issue issue = Iterables.getOnlyElement(searchResult.getIssues());
		assertEquals("TST-1", issue.getKey());
		assertEquals("My sample test", issue.getSummary());
		assertEquals("Bug", issue.getIssueType().getName());
		assertEquals(toDateTime("2010-07-23T12:16:56.000"), issue.getCreationDate());
		assertEquals(toDateTime("2010-08-17T16:36:29.000"), issue.getUpdateDate());
		assertEquals("Test Project", issue.getProject().getName());
		assertEquals(Long.valueOf(10000), issue.getId());
		assertEquals("Open", issue.getStatus().getName());

		// this issue has labels, but they were not returned by JIRA REST API
		assertEmptyIterable(issue.getLabels());
		final Issue fullIssue = client.getIssueClient().getIssue(issue.getKey()).claim();
		assertThat(fullIssue.getLabels(), containsInAnyOrder("a", "bcds"));
	}

	/**
	 * If this test fails, then maybe we accept missing field in issue parser? If yes, then we need to update
	 * javadoc for {@link SearchRestClient#searchJql(String, Integer, Integer, Set)}
	 */
	@Test
	public void jqlSearchWithoutOneOfRequiredFieldsShouldCauseParserFailure() {
		final SearchRestClient searchClient = client.getSearchClient();

		for (final String missingField : REQUIRED_ISSUE_FIELDS) {
			final Set<String> fieldsToRetrieve = Sets.difference(REQUIRED_ISSUE_FIELDS, Sets.newHashSet(missingField));

			try {
				searchClient.searchJql(null, 1, 0, fieldsToRetrieve).claim();
				throw new java.lang.AssertionError(String.format(
						"The above statement should throw exception. fieldsToRetrieve = %s, fields = %s, requiredFields = %s",
						missingField, fieldsToRetrieve, REQUIRED_ISSUE_FIELDS));
			} catch (RestClientException e) {
				final String expectedMessage = String.format(
						"org.codehaus.jettison.json.JSONException: JSONObject[\"%s\"] not found.", missingField);
				assertEquals(expectedMessage, e.getMessage());
			}
		}
	}
}
