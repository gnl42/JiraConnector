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

import com.atlassian.jira.rest.client.IntegrationTestUtil;
import com.atlassian.jira.rest.client.TestUtil;
import com.atlassian.jira.rest.client.domain.SearchResult;
import com.atlassian.jira.rest.client.internal.ServerVersionConstants;
import com.google.common.collect.Iterables;
import org.junit.Test;

import javax.ws.rs.core.Response;

public class JerseySearchRestClientTest extends AbstractRestoringJiraStateJerseyRestClientTest {
	@Test
	public void testJqlSearch() {
		if (!isJqlSupportedByRest()) {
			return;
		}
		final SearchResult searchResultForNull = client.getSearchClient().searchJql(null, pm);
		assertEquals(9, searchResultForNull.getTotal());

		final SearchResult searchResultForReporterWseliga = client.getSearchClient().searchJql("reporter=wseliga", pm);
		assertEquals(1, searchResultForReporterWseliga.getTotal());

		setAnonymousMode();
		final SearchResult searchResultAsAnonymous = client.getSearchClient().searchJql(null, pm);
		assertEquals(1, searchResultAsAnonymous.getTotal());

		final SearchResult searchResultForReporterWseligaAsAnonymous = client.getSearchClient().searchJql("reporter=wseliga", pm);
		assertEquals(0, searchResultForReporterWseligaAsAnonymous.getTotal());
	}

	@Test
	public void testJqlSearchWithPaging() {
		if (!isJqlSupportedByRest()) {
			return;
		}
		final SearchResult searchResultForNull = client.getSearchClient().searchJql(null, 3, 3, pm);
		assertEquals(9, searchResultForNull.getTotal());
		assertEquals(3, Iterables.size(searchResultForNull.getIssues()));
		assertEquals(3, searchResultForNull.getStartIndex());
		assertEquals(3, searchResultForNull.getMaxResults());

		// seems pagination works differently between 4.4 and 5.0
		// check the rationale https://jdog.atlassian.com/browse/JRADEV-8889
		final SearchResult search2 = client.getSearchClient().searchJql("assignee is not EMPTY", 2, 1, pm);
		assertEquals(9, search2.getTotal());
		assertEquals(2, Iterables.size(search2.getIssues()));
		assertEquals("TST-6", Iterables.get(search2.getIssues(), 0).getKey());
		assertEquals("TST-5", Iterables.get(search2.getIssues(), 1).getKey());
		assertEquals(IntegrationTestUtil.TESTING_JIRA_5_OR_NEWER ? 1 : 0, search2.getStartIndex());
		assertEquals(2, search2.getMaxResults());

		setUser1();
		final SearchResult search3 = client.getSearchClient().searchJql("assignee is not EMPTY", 10, 5, pm);
		assertEquals(8, search3.getTotal());
		assertEquals(IntegrationTestUtil.TESTING_JIRA_5_OR_NEWER ? 3 : 8, Iterables.size(search3.getIssues()));
		assertEquals(IntegrationTestUtil.TESTING_JIRA_5_OR_NEWER ? 5 : 0, search3.getStartIndex());
		assertEquals(10, search3.getMaxResults());
	}

	@Test
	public void testVeryLongJqlWhichWillBePost() {
		if (!isJqlSupportedByRest()) {
			return;
		}
		final String coreJql = "summary ~ fsdsfdfds";
		StringBuilder sb = new StringBuilder(coreJql);
		for (int i = 0; i < 500; i++) {
			sb.append(" and (reporter is not empty)"); // building very long JQL query
		}
		sb.append(" or summary is not empty"); // so that effectively all issues are returned;
		final SearchResult searchResultForNull = client.getSearchClient().searchJql(sb.toString(), 3, 6, pm);
		assertEquals(9, searchResultForNull.getTotal());
		assertEquals(3, Iterables.size(searchResultForNull.getIssues()));
		assertEquals(6, searchResultForNull.getStartIndex());
		assertEquals(3, searchResultForNull.getMaxResults());
	}


	@Test
	public void testJqlSearchUnescapedCharacter() {
		if (!isJqlSupportedByRest()) {
			return;
		}
		TestUtil.assertErrorCode(Response.Status.BAD_REQUEST,
				"Error in the JQL Query: The character '/' is a reserved JQL character. You must enclose it in a string or use the escape '\\u002f' instead. (line 1, character 11)", new Runnable() {
			@Override
			public void run() {
				client.getSearchClient().searchJql("reporter=a/user/with/slash", pm);
			}
		});
	}


	private boolean isJqlSupportedByRest() {
		return client.getMetadataClient().getServerInfo(pm).getBuildNumber() >= ServerVersionConstants.BN_JIRA_4_3;
	}


}
