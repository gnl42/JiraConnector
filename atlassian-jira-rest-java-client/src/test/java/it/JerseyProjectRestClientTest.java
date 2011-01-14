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

package it;

import com.atlassian.jira.rest.client.IntegrationTestUtil;
import com.atlassian.jira.rest.client.TestUtil;
import com.atlassian.jira.rest.client.domain.BasicProject;
import com.atlassian.jira.rest.client.domain.Project;
import com.atlassian.jira.rest.client.internal.json.TestConstants;
import com.google.common.collect.Iterables;
import org.junit.Test;

import javax.ws.rs.core.Response;

public class JerseyProjectRestClientTest extends AbstractRestoringJiraStateJerseyRestClientTest {
	@Test
	public void testGetNonExistingProject() throws Exception {
		final String nonExistingProjectKey = "NONEXISTINGPROJECTKEY";
		TestUtil.assertErrorCode(Response.Status.NOT_FOUND, "No project could be found with key '" +
				nonExistingProjectKey + "'.", new Runnable() {
			@Override
			public void run() {
				client.getProjectClient().getProject(nonExistingProjectKey, pm);
			}
		});
	}

	@Test
	public void testGetProject() {
		final Project project = client.getProjectClient().getProject("TST", pm);
		assertEquals("TST", project.getKey());
		assertEquals(IntegrationTestUtil.USER_ADMIN, project.getLead());
		assertEquals(2, Iterables.size(project.getVersions()));
		assertEquals(2, Iterables.size(project.getComponents()));
	}

	@Test
	public void testGetRestrictedProject() {
		final Project project = client.getProjectClient().getProject("RST", pm);
		assertEquals("RST", project.getKey());

		setClient(TestConstants.USER1_USERNAME, TestConstants.USER1_PASSWORD);
		client.getProjectClient().getProject("TST", pm);
		// @todo when JRADEV-3519 - instead of NOT_FOUND, FORBIDDEN code should be returned by JIRA
		TestUtil.assertErrorCode(Response.Status.NOT_FOUND, "You must have the browse project permission to view this project.", new Runnable() {
			@Override
			public void run() {
				client.getProjectClient().getProject("RST", pm);
			}
		});
	}

	@Test
	public void testGetAnonymouslyProject() {
		// @todo when JRADEV-3519 - instead of NOT_FOUND, UNAUTHORIZED code should be returned by JIRA
		setAnonymousMode();
		TestUtil.assertErrorCode(Response.Status.NOT_FOUND, "You must have the browse project permission to view this project.", new Runnable() {
			@Override
			public void run() {
				client.getProjectClient().getProject("RST", pm);
			}
		});

		TestUtil.assertErrorCode(Response.Status.NOT_FOUND, "You must have the browse project permission to view this project.", new Runnable() {
			@Override
			public void run() {
				client.getProjectClient().getProject("TST", pm);
			}
		});

		final Project project = client.getProjectClient().getProject("ANNON", pm);
		assertEquals("ANNON", project.getKey());

	}

	@Test
	public void testGetAllProject() {
		final Iterable<BasicProject> projects = client.getProjectClient().getAllProjects(pm);
		assertEquals(3, Iterables.size(projects));
		assertEquals("TST", Iterables.get(projects, 0).getKey());
		assertTrue(Iterables.get(projects, 0).getSelf().toString().contains(jiraRestRootUri.toString()));

		setAnonymousMode();
		final Iterable<BasicProject> anonymouslyAccessibleProjects = client.getProjectClient().getAllProjects(pm);
		assertEquals(1, Iterables.size(anonymouslyAccessibleProjects));
		assertEquals("ANNON", Iterables.get(anonymouslyAccessibleProjects, 0).getKey());

		setUser1();
		assertEquals(2, Iterables.size(client.getProjectClient().getAllProjects(pm)));
	}
}
