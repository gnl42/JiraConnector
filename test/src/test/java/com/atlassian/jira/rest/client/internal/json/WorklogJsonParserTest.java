/*
 * Copyright (C) 2010 Atlassian
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

import com.atlassian.jira.rest.client.TestUtil;
import com.atlassian.jira.rest.client.api.domain.Visibility;
import com.atlassian.jira.rest.client.api.domain.Worklog;
import org.junit.Test;

import static com.atlassian.jira.rest.client.TestUtil.toUri;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class WorklogJsonParserTest {
	@Test
	public void testParse() throws Exception {
		final WorklogJsonParser parser = new WorklogJsonParser();
		final Worklog worklog = parser.parse(ResourceUtil.getJsonObjectFromResource("/json/worklog/valid.json"));
		assertEquals(toUri("http://localhost:8090/jira/rest/api/latest/worklog/10010"), worklog.getSelf());
		assertEquals(toUri("http://localhost:8090/jira/rest/api/latest/issue/TST-2"), worklog.getIssueUri());
		assertEquals(TestConstants.USER_ADMIN_BASIC_DEPRECATED, worklog.getAuthor());
		assertEquals(TestConstants.USER_ADMIN_BASIC_DEPRECATED, worklog.getUpdateAuthor());
		assertEquals("my first work", worklog.getComment());
		assertEquals(TestUtil.toDateTime("2010-08-17T16:35:47.466+0200"), worklog.getCreationDate());
		assertEquals(TestUtil.toDateTime("2010-08-17T16:35:47.466+0200"), worklog.getUpdateDate());
		assertEquals(TestUtil.toDateTime("2010-08-15T16:35:00.000+0200"), worklog.getStartDate());
		assertEquals(60, worklog.getMinutesSpent());
		assertNull(worklog.getVisibility());
	}

	@Test
	public void testParseWithRoleLevel() throws Exception {
		final WorklogJsonParser parser = new WorklogJsonParser();
		final Worklog worklog = parser.parse(ResourceUtil.getJsonObjectFromResource("/json/worklog/valid-roleLevel.json"));
		assertEquals(toUri("http://localhost:8090/jira/rest/api/latest/worklog/10011"), worklog.getSelf());
		assertEquals(toUri("http://localhost:8090/jira/rest/api/latest/issue/TST-2"), worklog.getIssueUri());
		assertEquals(TestConstants.USER1_BASIC_DEPRECATED, worklog.getAuthor());
		assertEquals(TestConstants.USER1_BASIC_DEPRECATED, worklog.getUpdateAuthor());
		assertEquals("another piece of work", worklog.getComment());
		assertEquals(TestUtil.toDateTime("2010-08-17T16:38:00.013+0200"), worklog.getCreationDate());
		assertEquals(TestUtil.toDateTime("2010-08-17T16:38:24.948+0200"), worklog.getUpdateDate());
		assertEquals(TestUtil.toDateTime("2010-08-17T16:37:00.000+0200"), worklog.getStartDate());
		assertEquals(Visibility.role("Developers"), worklog.getVisibility());
		assertEquals(15, worklog.getMinutesSpent());
	}

	@Test
	public void testParseWithGroupLevel() throws Exception {
		final WorklogJsonParser parser = new WorklogJsonParser();
		final Worklog worklog = parser.parse(ResourceUtil.getJsonObjectFromResource("/json/worklog/valid-groupLevel.json"));
		assertEquals(Visibility.group("jira-users"), worklog.getVisibility());
	}

	@Test
	public void testParseWhenAuthorIsAnonymous() throws Exception {
		final WorklogJsonParser parser = new WorklogJsonParser();
		final Worklog worklog = parser.parse(ResourceUtil.getJsonObjectFromResource("/json/worklog/valid-anonymousAuthor.json"));
		assertNull(worklog.getAuthor());
		assertNull(worklog.getUpdateAuthor());
	}

	@Test
	public void testParseWhenAuthorIsAnonymousInOldRepresentation() throws Exception {
		final WorklogJsonParser parser = new WorklogJsonParser();
		final Worklog worklog = parser.parse(ResourceUtil
				.getJsonObjectFromResource("/json/worklog/valid-anonymousAuthor-oldRepresentation.json"));
		assertNull(worklog.getAuthor());
		assertNull(worklog.getUpdateAuthor());
	}

}
