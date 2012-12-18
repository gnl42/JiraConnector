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

package com.atlassian.jira.rest.client.internal.json;

import com.atlassian.jira.rest.client.api.domain.BasicIssue;
import com.atlassian.jira.rest.client.api.domain.BasicIssueType;
import com.atlassian.jira.rest.client.api.domain.BasicPriority;
import com.atlassian.jira.rest.client.api.domain.BasicProject;
import com.atlassian.jira.rest.client.api.domain.BasicStatus;
import com.atlassian.jira.rest.client.api.domain.BasicVotes;
import com.atlassian.jira.rest.client.api.domain.BasicWatchers;
import com.atlassian.jira.rest.client.api.domain.EntityHelper;
import com.atlassian.jira.rest.client.api.domain.Issue;
import com.atlassian.jira.rest.client.api.domain.SearchResult;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import org.codehaus.jettison.json.JSONException;
import org.hamcrest.FeatureMatcher;
import org.hamcrest.Matcher;
import org.hamcrest.Matchers;
import org.hamcrest.collection.IsIterableContainingInAnyOrder;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.Collection;
import java.util.List;

import static com.atlassian.jira.rest.client.TestUtil.toDateTime;
import static com.atlassian.jira.rest.client.TestUtil.toUri;
import static org.junit.Assert.*;

public class SearchResultJsonParserTest {

	@Rule
	public final ExpectedException exception = ExpectedException.none();

	final SearchResultJsonParser parser = new SearchResultJsonParser();

	// TODO: REFACTOR AFTER MERGE
	public static class SearchResultMatchers {
		public static Matcher<? super SearchResult> withStartIndex(final int startIndex) {
			return new FeatureMatcher<SearchResult, Integer>(Matchers.is(startIndex),
					"search result with start index that", "startIndex") {

				@Override
				protected Integer featureValueOf(SearchResult searchResult) {
					return searchResult.getStartIndex();
				}
			};
		}

		public static Matcher<? super SearchResult> withMaxResults(final int maxResults) {
			return new FeatureMatcher<SearchResult, Integer>(Matchers.is(maxResults),
					"search result with max results that", "maxResults") {

				@Override
				protected Integer featureValueOf(SearchResult searchResult) {
					return searchResult.getMaxResults();
				}
			};
		}

		public static Matcher<? super SearchResult> withTotal(final int total) {
			return new FeatureMatcher<SearchResult, Integer>(Matchers.is(total),
					"search result with total that", "total") {

				@Override
				protected Integer featureValueOf(SearchResult searchResult) {
					return searchResult.getTotal();
				}
			};
		}

		public static Matcher<? super SearchResult> withIssueCount(final int issueCount) {
			return new FeatureMatcher<SearchResult, Integer>(Matchers.is(issueCount),
					"search result with issue count that", "issue count") {

				@Override
				protected Integer featureValueOf(SearchResult searchResult) {
					final Iterable<Issue> issues = searchResult.getIssues();
					return (issues == null) ? 0 : Iterables.size(issues);
				}
			};
		}

//		public static Matcher<? super SearchResult> withParams(final int startIndex, final int maxResults, final int total) {
//			return Matchers.allOf(withStartIndex(startIndex), withMaxResults(maxResults), withTotal(total));
//		}

		public static Matcher<? super SearchResult> withParamsAndIssueCount(final int startIndex, final int maxResults,
				final int total, final int issueCount) {
			return Matchers.allOf(withStartIndex(startIndex), withMaxResults(maxResults), withTotal(total),
					withIssueCount(issueCount));
		}
	}

	// TODO: REFACTOR AFTER MERGE
	public static class IssueMatchers {
		public static Matcher<? super BasicIssue> withIssueKey(String issueKey) {
			return new FeatureMatcher<BasicIssue, String>(Matchers.is(issueKey), "issue with key that", "key") {

				@Override
				protected String featureValueOf(BasicIssue basicIssue) {
					return basicIssue.getKey();
				}
			};
		}

		public static Matcher<Iterable<? extends BasicIssue>> issuesWithKeys(String... keys) {
			final Collection<Matcher<? super BasicIssue>> matchers = Lists.newArrayListWithCapacity(keys.length);
			for (String key : keys) {
				matchers.add(withIssueKey(key));
			}
			return IsIterableContainingInAnyOrder.containsInAnyOrder(matchers);
		}
	}

	@Test
	public void testParse() throws Exception {
		final SearchResult searchResult = parser.parse(ResourceUtil.getJsonObjectFromResource("/json/search/issues1.json"));

		assertThat(searchResult, SearchResultMatchers.withParamsAndIssueCount(0, 50, 1, 1));

		final Issue foundIssue = Iterables.getLast(searchResult.getIssues());
		assertIssueIsTST7(foundIssue);
	}

	@Test
	public void testParseMany() throws Exception {
		final SearchResult searchResult = parser.parse(ResourceUtil.getJsonObjectFromResource("/json/search/many-issues.json"));

		assertThat(searchResult, SearchResultMatchers.withParamsAndIssueCount(0, 8, 15, 8));

		final Issue issue = EntityHelper.findEntityById(searchResult.getIssues(), 10040L);
		assertIssueIsTST7(issue);

		final String[] expectedIssuesKeys = {"TST-13", "TST-12", "TST-11", "TST-10", "TST-9", "TST-8", "TST-7", "TST-6"};
		assertThat(searchResult.getIssues(), IssueMatchers.issuesWithKeys(expectedIssuesKeys));
	}

	@Test
	public void testParseInvalidTotal() throws Exception {
		exception.expect(JSONException.class);
		exception.expectMessage("JSONObject[\"total\"] is not a number.");

		parser.parse(ResourceUtil.getJsonObjectFromResource("/json/search/issues-invalid-total.json"));
	}

	// TODO: REFACTOR AFTER MERGE, SEE ALSO AsynchronousSearchRestClientTest
	private static <K> void assertEmptyIterable(Iterable<K> iterable) {
		assertThat(iterable, Matchers.<K>emptyIterable());
	}

	private void assertIssueIsTST7(Issue issue) {
		assertEquals("TST-7", issue.getKey());
		assertEquals(Long.valueOf(10040), issue.getId());
		assertEquals(toUri("http://localhost:8090/jira/rest/api/latest/issue/10040"), issue.getSelf());
		assertEquals("A task where someone will vote", issue.getSummary());
		assertNull(issue.getDescription()); // by default search doesn't retrieve description
		assertEquals(new BasicPriority(toUri("http://localhost:8090/jira/rest/api/2/priority/3"), 3L, "Major"), issue
				.getPriority());
		assertEquals(new BasicStatus(toUri("http://localhost:8090/jira/rest/api/2/status/1"), "Open"), issue.getStatus());
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
		assertEquals(toDateTime("2010-09-22T18:06:32.000+02:00"), issue.getUpdateDate());
		assertEquals(toDateTime("2010-09-22T18:06:32.000+02:00"), issue.getCreationDate());
		assertEquals(TestConstants.USER1, issue.getReporter());
		assertEquals(TestConstants.USER_ADMIN, issue.getAssignee());
		assertEquals(new BasicProject(toUri("http://localhost:8090/jira/rest/api/2/project/TST"), "TST", "Test Project"), issue
				.getProject());
		assertEquals(new BasicVotes(toUri("http://localhost:8090/jira/rest/api/2/issue/TST-7/votes"), 0, false), issue
				.getVotes());
		assertEquals(new BasicWatchers(toUri("http://localhost:8090/jira/rest/api/2/issue/TST-7/watchers"), false, 0), issue
				.getWatchers());
		assertEquals(new BasicIssueType(toUri("http://localhost:8090/jira/rest/api/2/issuetype/3"), 3L, "Task", false), issue
				.getIssueType());
	}

}
