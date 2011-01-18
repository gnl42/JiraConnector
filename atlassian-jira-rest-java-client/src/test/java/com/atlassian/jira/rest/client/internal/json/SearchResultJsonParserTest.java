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

import com.atlassian.jira.rest.client.domain.BasicIssue;
import com.atlassian.jira.rest.client.domain.SearchResult;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import org.codehaus.jettison.json.JSONException;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.ArrayList;

import static com.atlassian.jira.rest.client.TestUtil.toUri;
import static org.junit.Assert.assertEquals;

public class SearchResultJsonParserTest {

	@Rule
	public final ExpectedException exception = ExpectedException.none();

	final SearchResultJsonParser parser = new SearchResultJsonParser();

	@Test
	public void testParse() throws Exception {
		final SearchResult searchResult = parser.parse(ResourceUtil.getJsonObjectFromResource("/json/search/issues1.json"));
		final ArrayList<BasicIssue> issues = Lists.newArrayList(new BasicIssue(toUri("http://localhost:8090/jira/rest/api/latest/issue/TST-7"), "TST-7"));

		assertEquals(new SearchResult(0, 50, 1, issues), searchResult);
	}

	@Test
	public void testParseMany() throws Exception {
		final SearchResult searchResult = parser.parse(ResourceUtil.getJsonObjectFromResource("/json/search/many-issues.json"));

		assertEquals(9, searchResult.getSize());
		assertEquals(50, searchResult.getMaxResults());
		assertEquals(0, searchResult.getStartIndex());
		assertEquals(9, Iterables.size(searchResult.getIssues()));
		assertEquals(new BasicIssue(toUri("http://localhost:8090/jira/rest/api/latest/issue/ANNON-1"), "ANNON-1"), Iterables.getLast(searchResult.getIssues()));
	}

	@Test
	public void testParseInvalidTotal() throws Exception {
		exception.expect(JSONException.class);
		exception.expectMessage("The number of elements of issues JSON array does not match the value of 'total' attribute");

		parser.parse(ResourceUtil.getJsonObjectFromResource("/json/search/issues-invalid-total.json"));
	}

}
