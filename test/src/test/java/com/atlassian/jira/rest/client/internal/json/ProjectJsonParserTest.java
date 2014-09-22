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

import com.atlassian.jira.rest.client.api.OptionalIterable;
import com.atlassian.jira.rest.client.TestUtil;
import com.atlassian.jira.rest.client.api.domain.BasicProjectRole;
import com.atlassian.jira.rest.client.api.domain.IssueType;
import com.atlassian.jira.rest.client.api.domain.Project;
import com.google.common.collect.Iterables;
import org.codehaus.jettison.json.JSONException;
import org.hamcrest.collection.IsEmptyIterable;
import org.hamcrest.collection.IsIterableContainingInAnyOrder;
import org.joda.time.DateMidnight;
import org.junit.Assert;
import org.junit.Test;

import java.net.URISyntaxException;

import static org.junit.Assert.assertEquals;

// Ignore "May produce NPE" warnings, as we know what we are doing in tests
@SuppressWarnings("ConstantConditions")
public class ProjectJsonParserTest {
	private final ProjectJsonParser parser = new ProjectJsonParser();

	@Test
	public void testParse() throws Exception {
		final Project project = parser.parse(ResourceUtil.getJsonObjectFromResource("/json/project/valid.json"));
		Assert.assertEquals(TestUtil.toUri("http://localhost:8090/jira/rest/api/latest/project/TST"), project.getSelf());
		Assert.assertEquals("This is my description here.\r\nAnother line.", project.getDescription());
		assertEquals(TestConstants.USER_ADMIN_BASIC_DEPRECATED, project.getLead());
		Assert.assertEquals("http://example.com", project.getUri().toString());
		Assert.assertEquals("TST", project.getKey());
		Assert.assertEquals(Long.valueOf(10000), project.getId());
		Assert.assertThat(project.getVersions(), IsIterableContainingInAnyOrder
				.containsInAnyOrder(TestConstants.VERSION_1, TestConstants.VERSION_1_1));
		Assert.assertThat(project.getComponents(), IsIterableContainingInAnyOrder
				.containsInAnyOrder(TestConstants.BCOMPONENT_A, TestConstants.BCOMPONENT_B));
		Assert.assertNull(project.getName());
		final OptionalIterable<IssueType> issueTypes = project.getIssueTypes();
		Assert.assertFalse(issueTypes.isSupported());
		Assert.assertThat(issueTypes, IsEmptyIterable.<IssueType>emptyIterable());
	}

	@Test
	public void testParseWithoutId() throws Exception {
		final Project project = parser.parse(ResourceUtil.getJsonObjectFromResource("/json/project/valid-without-id.json"));
		Assert.assertEquals("TST", project.getKey());
		Assert.assertNull(project.getId());
	}

	@Test
	public void testParseProjectWithNoUrl() throws JSONException {
		final Project project = parser.parse(ResourceUtil.getJsonObjectFromResource("/json/project/project-no-url.json"));
		Assert.assertEquals("MYT", project.getKey());
		Assert.assertNull(project.getUri());
		Assert.assertNull(project.getDescription());
	}

	@Test
	public void testParseProjectInJira5x0() throws JSONException, URISyntaxException {
		final Project project = parser.parse(ResourceUtil.getJsonObjectFromResource("/json/project/project-jira-5-0.json"));
		Assert.assertEquals("TST", project.getKey());
		Assert.assertEquals(new DateMidnight(2010, 8, 25).toInstant(), Iterables.getLast(project.getVersions()).getReleaseDate()
				.toInstant());
		Assert.assertEquals("Test Project", project.getName());
		final OptionalIterable<IssueType> issueTypes = project.getIssueTypes();
		Assert.assertTrue(issueTypes.isSupported());
		Assert.assertThat(issueTypes, IsIterableContainingInAnyOrder.containsInAnyOrder(
				new IssueType(TestUtil
						.toUri("http://localhost:2990/jira/rest/api/latest/issuetype/1"), 1L, "Bug", false, "A problem which impairs or prevents the functions of the product.", TestUtil
						.toUri("http://localhost:2990/jira/images/icons/bug.gif")),
				new IssueType(TestUtil
						.toUri("http://localhost:2990/jira/rest/api/latest/issuetype/2"), 2L, "New Feature", false, "A new feature of the product, which has yet to be developed.", TestUtil
						.toUri("http://localhost:2990/jira/images/icons/newfeature.gif")),
				new IssueType(TestUtil
						.toUri("http://localhost:2990/jira/rest/api/latest/issuetype/3"), 3L, "Task", false, "A task that needs to be done.", TestUtil
						.toUri("http://localhost:2990/jira/images/icons/task.gif")),
				new IssueType(TestUtil
						.toUri("http://localhost:2990/jira/rest/api/latest/issuetype/4"), 4L, "Improvement", false, "An improvement or enhancement to an existing feature or task.", TestUtil
						.toUri("http://localhost:2990/jira/images/icons/improvement.gif")),
				new IssueType(TestUtil
						.toUri("http://localhost:2990/jira/rest/api/latest/issuetype/5"), 5L, "Sub-task", true, "The sub-task of the issue", TestUtil
						.toUri("http://localhost:2990/jira/images/icons/issue_subtask.gif"))
		));
	}

	@Test
	public void testParseProjectWithBasicRoles() throws JSONException, URISyntaxException {
		final Project project = parser.parse(ResourceUtil.getJsonObjectFromResource("/json/project/project-jira-5-0.json"));
		final Iterable<BasicProjectRole> projectRoles = project.getProjectRoles();
		Assert.assertThat(projectRoles, IsIterableContainingInAnyOrder.containsInAnyOrder(
				new BasicProjectRole(TestUtil
						.toUri("http://localhost:2990/jira/rest/api/latest/project/TST/role/10000"), "Users"),
				new BasicProjectRole(TestUtil
						.toUri("http://localhost:2990/jira/rest/api/latest/project/TST/role/10001"), "Developers"),
				new BasicProjectRole(TestUtil
						.toUri("http://localhost:2990/jira/rest/api/latest/project/TST/role/10002"), "Administrators")
		));
	}
}
