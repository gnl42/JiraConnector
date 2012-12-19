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

import com.atlassian.jira.nimblefunctests.annotation.RestoreOnce;
import com.atlassian.jira.rest.client.IntegrationTestUtil;
import com.atlassian.jira.rest.client.TestUtil;
import com.atlassian.jira.rest.client.api.domain.BasicIssueType;
import com.atlassian.jira.rest.client.api.domain.BasicPriority;
import com.atlassian.jira.rest.client.api.domain.BasicProject;
import com.atlassian.jira.rest.client.api.domain.BasicStatus;
import com.atlassian.jira.rest.client.api.domain.BasicVotes;
import com.atlassian.jira.rest.client.api.domain.BasicWatchers;
import com.atlassian.jira.rest.client.api.domain.Issue;
import com.atlassian.jira.rest.client.api.domain.SearchResult;
import com.atlassian.jira.rest.client.internal.json.TestConstants;
import com.google.common.collect.Iterables;
import org.hamcrest.Matchers;
import org.joda.time.DateTime;
import org.joda.time.format.ISODateTimeFormat;
import org.junit.Test;

import javax.ws.rs.core.Response;
import java.lang.reflect.InvocationTargetException;

import static com.atlassian.jira.rest.client.IntegrationTestUtil.resolveURI;
import static org.junit.Assert.*;

@RestoreOnce(TestConstants.DEFAULT_JIRA_DUMP_FILE)
public class AsynchronousSearchRestClientTest extends AbstractAsynchronousRestClientTest {

	@Test
	public void testJqlSearch() {
		final SearchResult searchResultForNull = client.getSearchClient().searchJql(null).claim();
		assertEquals(11, searchResultForNull.getTotal());

		final SearchResult searchResultForReporterWseliga = client.getSearchClient().searchJql("reporter=wseliga").claim();
		assertEquals(1, searchResultForReporterWseliga.getTotal());
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
		final SearchResult searchResultForNull = client.getSearchClient().searchJql(null, 3, 3).claim();
		assertEquals(11, searchResultForNull.getTotal());
		assertEquals(3, Iterables.size(searchResultForNull.getIssues()));
		assertEquals(3, searchResultForNull.getStartIndex());
		assertEquals(3, searchResultForNull.getMaxResults());

		final SearchResult search2 = client.getSearchClient().searchJql("assignee is not EMPTY", 2, 1).claim();
		assertEquals(11, search2.getTotal());
		assertEquals(2, Iterables.size(search2.getIssues()));
		assertEquals("TST-6", Iterables.get(search2.getIssues(), 0).getKey());
		assertEquals("TST-5", Iterables.get(search2.getIssues(), 1).getKey());
		assertEquals(1, search2.getStartIndex());
		assertEquals(2, search2.getMaxResults());

		setUser1();
		final SearchResult search3 = client.getSearchClient().searchJql("assignee is not EMPTY", 10, 5).claim();
		assertEquals(10, search3.getTotal());
		assertEquals(5, Iterables.size(search3.getIssues()));
		assertEquals(5, search3.getStartIndex());
		assertEquals(10, search3.getMaxResults());
	}

	@Test
	public void testVeryLongJqlWhichWillBePost() {
		final String coreJql = "summary ~ fsdsfdfds";
		StringBuilder sb = new StringBuilder(coreJql);
		for (int i = 0; i < 500; i++) {
			sb.append(" and (reporter is not empty)"); // building very long JQL query
		}
		sb.append(" or summary is not empty"); // so that effectively all issues are returned;
		final SearchResult searchResultForNull = client.getSearchClient().searchJql(sb.toString(), 3, 6).claim();
		assertEquals(11, searchResultForNull.getTotal());
		assertEquals(3, Iterables.size(searchResultForNull.getIssues()));
		assertEquals(6, searchResultForNull.getStartIndex());
		assertEquals(3, searchResultForNull.getMaxResults());
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

	// TODO: REFACTOR AFTER MERGE
	private static <K> void assertEmptyIterable(Iterable<K> iterable) {
		assertThat(iterable, Matchers.<K>emptyIterable());
	}

	@Test
	public void jqlSearchShouldReturnIssueWithDetails() throws InvocationTargetException, IllegalAccessException {
		final SearchResult searchResult = client.getSearchClient().searchJql("reporter=wseliga").claim();
		assertEquals(1, searchResult.getTotal());
		final Issue issue = Iterables.get(searchResult.getIssues(), 0);

		assertEquals("TST-7", issue.getKey());
		assertEquals(Long.valueOf(10040), issue.getId());
		assertEquals(resolveURI("rest/api/latest/issue/10040"), issue.getSelf());
		assertEquals("A task where someone will vote", issue.getSummary());
		assertNull(issue.getDescription()); // by default search doesn't retrieve description
		assertEquals(new BasicPriority(resolveURI("rest/api/2/priority/3"), 3L, "Major"), issue.getPriority());
		assertEquals(new BasicStatus(resolveURI("rest/api/2/status/1"), "Open"), issue.getStatus());
		assertEmptyIterable(issue.getComments());
		assertEmptyIterable(issue.getComments());
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
		assertEquals(toDateTimeIgnoringTimezone("2010-09-22T18:06:32.000+02:00"), issue.getUpdateDate());
		assertEquals(toDateTimeIgnoringTimezone("2010-09-22T18:06:32.000+02:00"), issue.getCreationDate());
		assertEquals(IntegrationTestUtil.USER1_FULL, issue.getReporter());
		assertEquals(IntegrationTestUtil.USER_ADMIN_FULL, issue.getAssignee());
		assertEquals(new BasicProject(resolveURI("rest/api/2/project/TST"), "TST", "Test Project"), issue.getProject());
		assertEquals(new BasicVotes(resolveURI("rest/api/2/issue/TST-7/votes"), 0, false), issue.getVotes());
		assertEquals(new BasicWatchers(resolveURI("rest/api/2/issue/TST-7/watchers"), false, 0), issue.getWatchers());
		assertEquals(new BasicIssueType(resolveURI("rest/api/2/issuetype/3"), 3L, "Task", false), issue.getIssueType());
	}

	// JIRA does not store timezone information in its dump file
	private DateTime toDateTimeIgnoringTimezone(final String date) {
		return ISODateTimeFormat.dateTimeParser().parseDateTime(date);
	}
}
