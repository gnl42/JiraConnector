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
import org.junit.Test;

import static com.atlassian.jira.rest.client.IterableMatcher.hasOnlyElements;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

/**
 * TODO: Document this class / interface here
 *
 * @since v0.1
 */
public class ProjectJsonParserTest {
	@Test
	public void testParse() throws Exception {
		ProjectJsonParser parser = new ProjectJsonParser();
		final Project project = parser.parse(ResourceUtil.getJsonObjectFromResource("/json/project/valid.json"));
		assertEquals(TestUtil.toUri("http://localhost:8090/jira/rest/api/latest/project/TST"), project.getSelf());
		assertEquals("This is my description here.\r\nAnother line.", project.getDescription());
		assertEquals(TestConstants.USER_ADMIN, project.getLead());
		assertEquals("TST", project.getKey());
		assertThat(project.getVersions(), hasOnlyElements(TestConstants.VERSION_1, TestConstants.VERSION_1_1));
		assertThat(project.getComponents(), hasOnlyElements(TestConstants.BCOMPONENT_A, TestConstants.BCOMPONENT_B));
	}
}
