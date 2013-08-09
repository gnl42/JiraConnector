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

import com.atlassian.jira.nimblefunctests.annotation.RestoreOnce;
import com.atlassian.jira.rest.client.TestUtil;
import com.atlassian.jira.rest.client.api.domain.EntityHelper;
import com.atlassian.jira.rest.client.api.domain.Filter;
import com.atlassian.jira.rest.client.api.domain.util.ErrorCollection;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import org.apache.commons.lang.StringUtils;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.List;

import static com.atlassian.jira.rest.client.IntegrationTestUtil.USER_ADMIN_60;
import static com.atlassian.jira.rest.client.IntegrationTestUtil.resolveURI;
import static org.junit.Assert.*;

@RestoreOnce("jira-dump-with-filters.xml")
public class AsynchronousSearchRestClientFiltersTest extends AbstractAsynchronousRestClientTest {

	public static final Filter FILTER_10000 = new Filter(resolveURI("rest/api/latest/filter/10000"), 10000L,
			"Bugs in Test project", StringUtils.EMPTY, "project = TST AND issuetype = Bug",
			resolveURI("secure/IssueNavigator.jspa?mode=hide&requestId=10000"),
			resolveURI("rest/api/latest/search?jql=project+%3D+TST+AND+issuetype+%3D+Bug"),
			USER_ADMIN_60, true);

	public static final Filter FILTER_10001 = new Filter(resolveURI("rest/api/latest/filter/10001"), 10001L,
			"Tasks in Test project - not favuorite filter", StringUtils.EMPTY, "project = TST AND issuetype = Task",
			resolveURI("secure/IssueNavigator.jspa?mode=hide&requestId=10001"),
			resolveURI("rest/api/latest/search?jql=project+%3D+TST+AND+issuetype+%3D+Task"),
			USER_ADMIN_60, false);

	public static final Filter FILTER_10002 = new Filter(resolveURI("rest/api/latest/filter/10002"), 10002L,
			"All new features! (shared with everyone)", "This filter returns all issues of type \"New Fature\".", "issuetype = \"New Feature\"",
			resolveURI("secure/IssueNavigator.jspa?mode=hide&requestId=10002"),
			resolveURI("rest/api/latest/search?jql=issuetype+%3D+%22New+Feature%22"),
			USER_ADMIN_60, true);

	public static final Filter FILTER_10003 = new Filter(resolveURI("rest/api/latest/filter/10003"), 10003L,
			"Resolved bugs", "For testing shares.", "issuetype = Bug AND status = Resolved",
			resolveURI("secure/IssueNavigator.jspa?mode=hide&requestId=10003"),
			resolveURI("rest/api/latest/search?jql=issuetype+%3D+Bug+AND+status+%3D+Resolved"),
			USER_ADMIN_60, true);

	public static final Filter FILTER_10004 = new Filter(resolveURI("rest/api/latest/filter/10004"), 10004L,
			"All in project Test", "For testing subscriptions.", "project = TST",
			resolveURI("secure/IssueNavigator.jspa?mode=hide&requestId=10004"),
			resolveURI("rest/api/latest/search?jql=project+%3D+TST"),
			USER_ADMIN_60, true);


	@Rule
	public ExpectedException thrown = ExpectedException.none();

	@Test
	public void testGetFavouriteFilters() throws Exception {
		final Iterable<Filter> filters = client.getSearchClient().getFavouriteFilters().claim();

		final List<Filter> expectedFilters = ImmutableList.of(FILTER_10000, FILTER_10002, FILTER_10003, FILTER_10004);
		assertEquals(expectedFilters.size(), Iterables.size(filters));

		for (Filter expectedFilter : expectedFilters) {
			final Filter actualFilter = EntityHelper.findEntityById(filters, expectedFilter.getId());
			assertEquals(expectedFilter, actualFilter);
		}
	}

	@Test
	public void testGetFilterByUrl() throws Exception {
		final List<Filter> expectedFilters = ImmutableList.of(FILTER_10000, FILTER_10001, FILTER_10002, FILTER_10003,
				FILTER_10004);

		for (Filter expectedFilter : expectedFilters) {
			final Filter actualFilter = client.getSearchClient().getFilter(expectedFilter.getSelf()).claim();
			assertEquals(expectedFilter, actualFilter);
		}
	}

	@Test
	public void testGetFilterById() throws Exception {
		final List<Filter> expectedFilters = ImmutableList.of(FILTER_10000, FILTER_10001, FILTER_10002, FILTER_10003,
				FILTER_10004);

		for (Filter expectedFilter : expectedFilters) {
			final Filter actualFilter = client.getSearchClient().getFilter(expectedFilter.getId()).claim();
			assertEquals(expectedFilter, actualFilter);
		}
	}


	@Test
	public void testGetNotExistent() throws Exception {
		final ErrorCollection.Builder ecb = ErrorCollection.builder();
		ecb.errorMessage("The selected filter is not available to you, perhaps it has been deleted or had its permissions changed.")
				.status(400);

		TestUtil.assertExpectedErrorCollection(ImmutableList.of(ecb.build()), new Runnable() {
			@Override
			public void run() {
				client.getSearchClient().getFilter(resolveURI("rest/api/latest/filter/999999")).claim();
			}
		});
	}

	@Test
	public void testGetNotExistentById() throws Exception {
		final ErrorCollection.Builder ecb = ErrorCollection.builder();
		ecb.errorMessage("The selected filter is not available to you, perhaps it has been deleted or had its permissions changed.")
				.status(400);

		TestUtil.assertExpectedErrorCollection(ImmutableList.of(ecb.build()), new Runnable() {
			@Override
			public void run() {
				client.getSearchClient().getFilter(999999).claim();
			}
		});

	}

}
