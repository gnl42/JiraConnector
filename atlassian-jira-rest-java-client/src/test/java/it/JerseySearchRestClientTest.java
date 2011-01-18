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

import com.atlassian.jira.rest.client.TestUtil;
import com.atlassian.jira.rest.client.domain.SearchResult;
import org.junit.Test;

import javax.ws.rs.core.Response;

public class JerseySearchRestClientTest extends AbstractRestoringJiraStateJerseyRestClientTest {
	@Test
	public void testJqlSearch() {
		final SearchResult searchResultForNull = client.getSearchClient().searchJql(null, pm);
		assertEquals(9, searchResultForNull.getSize());

		final SearchResult searchResultForReporterWseliga = client.getSearchClient().searchJql("reporter=wseliga", pm);
		assertEquals(1, searchResultForReporterWseliga.getSize());

		setAnonymousMode();
		final SearchResult searchResultAsAnonymous = client.getSearchClient().searchJql(null, pm);
		assertEquals(1, searchResultAsAnonymous.getSize());

		final SearchResult searchResultForReporterWseligaAsAnonymous = client.getSearchClient().searchJql("reporter=wseliga", pm);
		assertEquals(0, searchResultForReporterWseligaAsAnonymous.getSize());
	}

	@Test
	public void testJqlSearchUnescapedCharacter() {
		TestUtil.assertErrorCode(Response.Status.BAD_REQUEST,
				"Error in the JQL Query: The character '/' is a reserved JQL character. You must enclose it in a string or use the escape '\\u002f' instead. (line 1, character 11)", new Runnable() {
			@Override
			public void run() {
				client.getSearchClient().searchJql("reporter=a/user/with/slash", pm);
			}
		});
	}

}
