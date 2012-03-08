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
import com.atlassian.jira.rest.client.domain.Project;
import com.google.common.collect.Iterables;
import org.codehaus.jettison.json.JSONException;
import org.joda.time.DateMidnight;
import org.junit.Test;

import static com.atlassian.jira.rest.client.IterableMatcher.hasOnlyElements;
import static org.junit.Assert.*;

public class ProjectJsonParserTest {
	private final ProjectJsonParser parser = new ProjectJsonParser();

	@Test
	public void testParse() throws Exception {
		final Project project = parser.parse(ResourceUtil.getJsonObjectFromResource("/json/project/valid.json"));
		assertEquals(TestUtil.toUri("http://localhost:8090/jira/rest/api/latest/project/TST"), project.getSelf());
		assertEquals("This is my description here.\r\nAnother line.", project.getDescription());
		assertEquals(TestConstants.USER_ADMIN, project.getLead());
		assertEquals("http://example.com", project.getUri().toString());
		assertEquals("TST", project.getKey());
		assertThat(project.getVersions(), hasOnlyElements(TestConstants.VERSION_1, TestConstants.VERSION_1_1));
		assertThat(project.getComponents(), hasOnlyElements(TestConstants.BCOMPONENT_A, TestConstants.BCOMPONENT_B));
        assertNull(project.getName());
	}

	@Test
	public void testParseProjectWithNoUrl() throws JSONException {
		final Project project = parser.parse(ResourceUtil.getJsonObjectFromResource("/json/project/project-no-url.json"));
		assertEquals("MYT", project.getKey());
		assertNull(project.getUri());
		assertNull(project.getDescription());
	}

	@Test
	public void testParseProjectInJira4x4() throws JSONException {
		final Project project = parser.parse(ResourceUtil.getJsonObjectFromResource("/json/project/project-jira-4-4.json"));
		assertEquals("TST", project.getKey()); //2010-08-25
		assertEquals(new DateMidnight(2010, 8, 25).toInstant(), Iterables.getLast(project.getVersions()).getReleaseDate().toInstant());
        assertEquals("Test Project", project.getName());
	}
}
