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

package com.atlassian.jira.rest.client.internal.json;

import com.atlassian.jira.rest.client.api.domain.Filter;
import org.apache.commons.lang.StringUtils;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.junit.Test;

import static org.hamcrest.Matchers.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static com.atlassian.jira.rest.client.TestUtil.toUri;
import static com.atlassian.jira.rest.client.internal.json.TestConstants.USER_ADMIN_BASIC_LATEST;

public class FilterJsonParserTest {

	@Test
	public void testParseWithoutShares() throws Exception {
		final Filter actual = parseFilter("/json/filter/valid-no-shares.json");
		final Filter expectedOld = new Filter(toUri("http://localhost:8090/jira/rest/api/latest/filter/10000"), 10000L,
				"Bugs in Test project", StringUtils.EMPTY, "project = TST AND issuetype = Bug",
				toUri("http://localhost:8090/jira/secure/IssueNavigator.jspa?mode=hide&requestId=10000"),
				toUri("http://localhost:8090/jira/rest/api/latest/search?jql=project+%3D+TST+AND+issuetype+%3D+Bug"),
				USER_ADMIN_BASIC_LATEST, true);
		final Filter expectedNew = new Filter(toUri("http://localhost:8090/jira/rest/api/latest/filter/10000"), 10000L,
				"Bugs in Test project", StringUtils.EMPTY, "project = TST AND issuetype = Bug",
				toUri("http://localhost:8090/jira/secure/issues/?filter=10000"),
				toUri("http://localhost:8090/jira/rest/api/latest/search?jql=project+%3D+TST+AND+issuetype+%3D+Bug"),
				USER_ADMIN_BASIC_LATEST, true);
		assertThat(actual, anyOf(is(expectedOld), is(expectedNew)));
	}

	@Test
	public void testParseWithShares() throws Exception {
		final Filter actual = parseFilter("/json/filter/valid-with-shares.json");
		final Filter expectedOld = new Filter(toUri("http://localhost:8090/jira/rest/api/latest/filter/10003"), 10003L,
				"Resolved bugs", "For testing shares.", "issuetype = Bug AND status = Resolved",
				toUri("http://localhost:8090/jira/secure/IssueNavigator.jspa?mode=hide&requestId=10003"),
				toUri("http://localhost:8090/jira/rest/api/latest/search?jql=issuetype+%3D+Bug+AND+status+%3D+Resolved"),
				USER_ADMIN_BASIC_LATEST, true);
		final Filter expectedNew = new Filter(toUri("http://localhost:8090/jira/rest/api/latest/filter/10003"), 10003L,
				"Resolved bugs", "For testing shares.", "issuetype = Bug AND status = Resolved",
				toUri("http://localhost:8090/jira/secure/issues/?filter=10003"),
				toUri("http://localhost:8090/jira/rest/api/latest/search?jql=issuetype+%3D+Bug+AND+status+%3D+Resolved"),
				USER_ADMIN_BASIC_LATEST, true);
		assertThat(actual, anyOf(is(expectedOld), is(expectedNew)));
	}

	@Test
	public void testParseNotFavourite() throws Exception {
		final Filter actual = parseFilter("/json/filter/valid-not-favourite.json");
		final Filter expectedOld = new Filter(toUri("http://localhost:8090/jira/rest/api/latest/filter/10001"), 10001L,
				"Tasks in Test project - not favuorite filter", StringUtils.EMPTY, "project = TST AND issuetype = Task",
				toUri("http://localhost:8090/jira/secure/IssueNavigator.jspa?mode=hide&requestId=10001"),
				toUri("http://localhost:8090/jira/rest/api/latest/search?jql=project+%3D+TST+AND+issuetype+%3D+Task"),
				USER_ADMIN_BASIC_LATEST, false);
		final Filter expectedNew = new Filter(toUri("http://localhost:8090/jira/rest/api/latest/filter/10001"), 10001L,
				"Tasks in Test project - not favuorite filter", StringUtils.EMPTY, "project = TST AND issuetype = Task",
				toUri("http://localhost:8090/jira/issues/?filter=10001"),
				toUri("http://localhost:8090/jira/rest/api/latest/search?jql=project+%3D+TST+AND+issuetype+%3D+Task"),
				USER_ADMIN_BASIC_LATEST, false);
		assertThat(actual, anyOf(is(expectedOld), is(expectedNew)));
	}

	@Test
	public void testParseWitSubscriptionsBugJRA30958() throws Exception {
		final Filter actual = parseFilter("/json/filter/valid-with-subscriptions-bug-JRA-30958-subscription-have-no-elements.json");
		final Filter expectedOld = new Filter(toUri("http://localhost:8090/jira/rest/api/latest/filter/10004"), 10004L,
				"All in project Test", "For testing subscriptions.", "project = TST",
				toUri("http://localhost:8090/jira/secure/IssueNavigator.jspa?mode=hide&requestId=10004"),
				toUri("http://localhost:8090/jira/rest/api/latest/search?jql=project+%3D+TST"),
				USER_ADMIN_BASIC_LATEST, true);
		final Filter expectedNew = new Filter(toUri("http://localhost:8090/jira/rest/api/latest/filter/10004"), 10004L,
				"All in project Test", "For testing subscriptions.", "project = TST",
				toUri("http://localhost:8090/jira/issues/?filter=10004"),
				toUri("http://localhost:8090/jira/rest/api/latest/search?jql=project+%3D+TST"),
				USER_ADMIN_BASIC_LATEST, true);
		assertThat(actual, anyOf(is(expectedOld), is(expectedNew)));
	}

	private Filter parseFilter(String resourcePath) throws JSONException {
		final JSONObject issueJson = ResourceUtil.getJsonObjectFromResource(resourcePath);
		final FilterJsonParser parser = new FilterJsonParser();
		return parser.parse(issueJson);
	}
}
