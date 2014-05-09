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

import com.atlassian.jira.nimblefunctests.annotation.JiraBuildNumberDependent;
import com.atlassian.jira.nimblefunctests.annotation.RestoreOnce;
import com.atlassian.jira.rest.client.IntegrationTestUtil;
import com.atlassian.jira.rest.client.api.AddressableEntity;
import com.atlassian.jira.rest.client.api.OptionalIterable;
import com.atlassian.jira.rest.client.TestUtil;
import com.atlassian.jira.rest.client.api.domain.BasicProject;
import com.atlassian.jira.rest.client.api.domain.IssueType;
import com.atlassian.jira.rest.client.api.domain.Priority;
import com.atlassian.jira.rest.client.api.domain.Project;
import com.atlassian.jira.rest.client.api.domain.Resolution;
import com.atlassian.jira.rest.client.internal.ServerVersionConstants;
import com.atlassian.jira.rest.client.internal.json.TestConstants;
import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import org.junit.Assert;
import org.junit.Test;

import javax.annotation.Nullable;
import javax.ws.rs.core.Response;
import java.net.URISyntaxException;
import java.util.Iterator;

import static com.atlassian.jira.rest.client.internal.ServerVersionConstants.BN_JIRA_5;
import static org.hamcrest.collection.IsIterableContainingInAnyOrder.containsInAnyOrder;
import static org.junit.Assert.*;

/**
 * Those tests mustn't change anything on server side, as jira is restored only once
 */
// Ignore "May produce NPE" warnings, as we know what we are doing in tests
@SuppressWarnings("ConstantConditions")
@RestoreOnce(TestConstants.DEFAULT_JIRA_DUMP_FILE)
public class AsynchronousProjectRestClientReadOnlyTest extends AbstractAsynchronousRestClientTest {

	@Test
	public void testGetNonExistingProject() throws Exception {
		final String nonExistingProjectKey = "NONEXISTINGPROJECTKEY";
		TestUtil.assertErrorCode(Response.Status.NOT_FOUND, "No project could be found with key '" +
				nonExistingProjectKey + "'.", new Runnable() {
			@Override
			public void run() {
				client.getProjectClient().getProject(nonExistingProjectKey).claim();
			}
		});
	}

	@Test
	public void testGetProject() throws URISyntaxException {
		final Project project = client.getProjectClient().getProject("TST").claim();
		assertEquals("TST", project.getKey());
		assertEquals(Long.valueOf(10000), project.getId());
		assertEquals(IntegrationTestUtil.USER_ADMIN_60, project.getLead());
		assertEquals(2, Iterables.size(project.getVersions()));
		assertEquals(2, Iterables.size(project.getComponents()));
		final OptionalIterable<IssueType> issueTypes = project.getIssueTypes();
		if (isJira4x4OrNewer()) {
			assertTrue(issueTypes.isSupported());
			final Iterator<IssueType> issueTypesIterator = issueTypes.iterator();
			assertTrue(issueTypesIterator.hasNext());
			final IssueType it = issueTypesIterator.next();
			if (isJira5xOrNewer()) {
				assertEquals(Long.valueOf(1), it.getId());
			} else {
				assertNull(it.getId());
			}
			assertEquals(it.getName(), "Bug");
		} else {
			assertFalse(issueTypes.isSupported());
		}
	}

	@Test
	public void testGetRestrictedProject() {
		final Project project = client.getProjectClient().getProject("RST").claim();
		assertEquals("RST", project.getKey());

		setClient(TestConstants.USER1_USERNAME, TestConstants.USER1_PASSWORD);
		client.getProjectClient().getProject("TST").claim();
		// @todo when JRADEV-3519 - instead of NOT_FOUND, FORBIDDEN code should be returned by JIRA
		final String message = getCannotViewProjectErrorMessage("RST");
		TestUtil.assertErrorCode(Response.Status.NOT_FOUND, message, new Runnable() {
			@Override
			public void run() {
				client.getProjectClient().getProject("RST").claim();
			}
		});
	}

	private String getCannotViewProjectErrorMessage(String key) {
		return isJira4x4OrNewer()
				? (isJira5xOrNewer() ? ("No project could be found with key '" + key + "'.") : "You cannot view this project.")
				: "You must have the browse project permission to view this project.";
	}

	@Test
	public void testGetAnonymouslyProject() {
		// @todo when JRADEV-3519 - instead of NOT_FOUND, UNAUTHORIZED code should be returned by JIRA
		setAnonymousMode();
		TestUtil.assertErrorCode(Response.Status.NOT_FOUND, getCannotViewProjectErrorMessage("RST"), new Runnable() {
			@Override
			public void run() {
				client.getProjectClient().getProject("RST").claim();
			}
		});

		TestUtil.assertErrorCode(Response.Status.NOT_FOUND, getCannotViewProjectErrorMessage("TST"), new Runnable() {
			@Override
			public void run() {
				client.getProjectClient().getProject("TST").claim();
			}
		});

		final Project project = client.getProjectClient().getProject("ANNON").claim();
		assertEquals("ANNON", project.getKey());

	}

	@Test
	public void testGetAllProject() {
		if (!isGetAllProjectsSupported()) {
			return;
		}

		final Iterable<BasicProject> projects = client.getProjectClient().getAllProjects().claim();
		assertEquals(4, Iterables.size(projects));
		final BasicProject tst = Iterables.find(projects, new Predicate<BasicProject>() {
			@Override
			public boolean apply(@Nullable BasicProject input) {
				return input.getKey().equals("TST");
			}
		});
		assertTrue(tst.getSelf().toString().contains(jiraRestRootUri.toString()));

		setAnonymousMode();
		final Iterable<BasicProject> anonymouslyAccessibleProjects = client.getProjectClient().getAllProjects().claim();
		assertEquals(2, Iterables.size(anonymouslyAccessibleProjects));

		final Iterable<String> projectsKeys = Iterables
				.transform(anonymouslyAccessibleProjects, new Function<BasicProject, String>() {
					@Override
					public String apply(BasicProject project) {
						return project.getKey();
					}
				});
		Assert.assertThat(projectsKeys, containsInAnyOrder("ANNON", "ANONEDIT"));

		setUser1();
		assertEquals(3, Iterables.size(client.getProjectClient().getAllProjects().claim()));
	}

	private boolean isGetAllProjectsSupported() {
		return client.getMetadataClient().getServerInfo().claim().getBuildNumber() >= ServerVersionConstants.BN_JIRA_4_3;
	}

	@Test
	@JiraBuildNumberDependent(BN_JIRA_5)
	public void testGetPriorities() {
		final Iterable<Priority> priorities = client.getMetadataClient().getPriorities().claim();
		assertEquals(5, Iterables.size(priorities));

		final Priority priority = findEntityBySelfAddressSuffix(priorities, "/1");
		assertEquals(Long.valueOf(1), priority.getId());
		assertEquals("Blocker", priority.getName());
		assertEquals("Blocks development and/or testing work, production could not run.", priority.getDescription());
		assertNotNull(priority.getSelf());
	}

	@Test
	@JiraBuildNumberDependent(BN_JIRA_5)
	public void testGetIssueTypes() {
		final Iterable<IssueType> issueTypes = client.getMetadataClient().getIssueTypes().claim();
		assertEquals(5, Iterables.size(issueTypes));

		final IssueType issueType = findEntityBySelfAddressSuffix(issueTypes, "/5");
		assertEquals("Sub-task", issueType.getName());
		assertEquals("The sub-task of the issue", issueType.getDescription());
		assertEquals(Long.valueOf(5), issueType.getId());
		assertTrue(issueType.isSubtask());
		assertNotNull(issueType.getSelf());
	}

	@Test
	@JiraBuildNumberDependent(BN_JIRA_5)
	public void testGetResolutions() {
		final Iterable<Resolution> resolutions = client.getMetadataClient().getResolutions().claim();
		assertEquals(5, Iterables.size(resolutions));
		final Resolution resolution = findEntityBySelfAddressSuffix(resolutions, "/1");
		assertEquals("Fixed", resolution.getName());
		assertEquals("A fix for this issue is checked into the tree and tested.", resolution.getDescription());
		assertNotNull(resolution.getSelf());
	}

	private <T extends AddressableEntity> T findEntityBySelfAddressSuffix(final Iterable<T> entities, final String suffix) {
		return Iterables.find(entities, new Predicate<T>() {
			@Override
			public boolean apply(T input) {
				return input.getSelf().toString().endsWith(suffix);
			}
		});
	}

}
