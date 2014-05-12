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

import com.atlassian.jira.nimblefunctests.annotation.JiraBuildNumberDependent;
import com.atlassian.jira.nimblefunctests.annotation.LongCondition;
import com.atlassian.jira.nimblefunctests.annotation.RestoreOnce;
import com.atlassian.jira.rest.client.api.RestClientException;
import com.atlassian.jira.rest.client.api.domain.EntityHelper;
import com.atlassian.jira.rest.client.api.domain.Project;
import com.atlassian.jira.rest.client.api.domain.ProjectRole;
import com.atlassian.jira.rest.client.api.domain.RoleActor;
import com.atlassian.jira.rest.client.internal.ServerVersionConstants;
import com.atlassian.jira.rest.client.internal.json.TestConstants;
import com.google.common.base.Function;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.net.URI;

import static com.atlassian.jira.rest.client.IntegrationTestUtil.buildUserAvatarUri;
import static com.atlassian.jira.rest.client.test.matchers.RestClientExceptionMatchers.rceWithSingleError;
import static org.hamcrest.collection.IsIterableContainingInAnyOrder.containsInAnyOrder;
import static org.junit.Assert.*;

@RestoreOnce(TestConstants.DEFAULT_JIRA_DUMP_FILE)
public class AsynchronousProjectRoleRestClientTest extends AbstractAsynchronousRestClientTest {

	private static final String ANONYMOUS_PROJECT_KEY = "ANNON";
	private static final String RESTRICTED_PROJECT_KEY = "RST";

	@Rule
	public final ExpectedException exception = ExpectedException.none();

	@JiraBuildNumberDependent(ServerVersionConstants.BN_JIRA_4_4)
	@Test
	public void testGetProjectRoleWithRoleKeyFromAnonymousProject() {
		final Project anonProject = client.getProjectClient().getProject(ANONYMOUS_PROJECT_KEY).claim();
		final ProjectRole role = client.getProjectRolesRestClient().getRole(anonProject.getSelf(), 10000l).claim();
		assertNotNull(role);
		assertEquals("Users", role.getName());
		assertEquals("A project role that represents users in a project", role.getDescription());
		final RoleActor actor = Iterables.getOnlyElement(role.getActors());
		assertEquals("jira-users", actor.getDisplayName());
		assertEquals("atlassian-group-role-actor", actor.getType());
		assertEquals("jira-users", actor.getName());
		assertEquals(actor.getAvatarUri(), buildUserAvatarUri(null, 10083L, "16x16"));
	}

	@JiraBuildNumberDependent(ServerVersionConstants.BN_JIRA_4_4)
	@Test
	public void testGetProjectRoleWithRoleKeyFromRestrictedProject() {
		final Project restrictedProject = client.getProjectClient().getProject(RESTRICTED_PROJECT_KEY).claim();
		final ProjectRole role = client.getProjectRolesRestClient().getRole(restrictedProject.getSelf(), 10000l).claim();
		assertNotNull(role);
		assertEquals("Users", role.getName());
		assertEquals("A project role that represents users in a project", role.getDescription());
		final RoleActor actor = Iterables.getOnlyElement(role.getActors());
		assertEquals("Administrator", actor.getDisplayName());
		assertEquals("atlassian-user-role-actor", actor.getType());
		assertEquals("admin", actor.getName());
		assertEquals(actor.getAvatarUri(), buildUserAvatarUri("admin", 10054L, "16x16"));
	}

	@JiraBuildNumberDependent(ServerVersionConstants.BN_JIRA_4_4)
	@Test
	public void testGetProjectRoleWithRoleKeyFromRestrictedProjectWithoutPermission() {
		final Project restrictedProject = client.getProjectClient().getProject(RESTRICTED_PROJECT_KEY).claim();
		setAnonymousMode();
		exception.expect(RestClientException.class);
		if (isJira61xOrNewer()) {
			final String expectedError = String.format("No project could be found with id '%s'.", restrictedProject.getId());
			exception.expect(rceWithSingleError(404, expectedError));
		}
		else {
			exception.expectMessage(String.format("No project could be found with key '%s'", RESTRICTED_PROJECT_KEY));
		}
		client.getProjectRolesRestClient().getRole(restrictedProject.getSelf(), 10000l).claim();
	}

	@JiraBuildNumberDependent(ServerVersionConstants.BN_JIRA_4_4)
	@Test
	public void testGetProjectRoleWithFullURI() {
		final Project anonProject = client.getProjectClient().getProject(ANONYMOUS_PROJECT_KEY).claim();
		final URI roleURI = client.getProjectRolesRestClient().getRole(anonProject.getSelf(), 10000l).claim().getSelf();
		final ProjectRole role = client.getProjectRolesRestClient().getRole(roleURI).claim();
		assertNotNull(role);
		assertEquals("Users", role.getName());
		assertEquals("A project role that represents users in a project", role.getDescription());
		final RoleActor actor = Iterables.getOnlyElement(role.getActors());
		assertEquals("jira-users", actor.getDisplayName());
		assertEquals("atlassian-group-role-actor", actor.getType());
		assertEquals("jira-users", actor.getName());
		assertEquals(actor.getAvatarUri(), buildUserAvatarUri(null, 10083L, "16x16"));
	}

	@JiraBuildNumberDependent(value = ServerVersionConstants.BN_JIRA_6_1, condition = LongCondition.LESS_THAN)
	@Test
	public void testGetAllRolesForProjectBefore6_1() {
		testGetAllRolesForProject(ANONYMOUS_PROJECT_KEY);
	}

	@JiraBuildNumberDependent(value = ServerVersionConstants.BN_JIRA_6_1)
	@Test
	public void testGetAllRolesForProject() {
		testGetAllRolesForProject("10020");
	}

	private void testGetAllRolesForProject(String projectIdOrKey) {
		final Project anonymousProject = client.getProjectClient().getProject(ANONYMOUS_PROJECT_KEY).claim();
		final Iterable<ProjectRole> projectRoles = client.getProjectRolesRestClient().getRoles(anonymousProject.getSelf())
				.claim();
		final Iterable<ProjectRole> projectRolesWithoutSelf = Iterables.transform(
				projectRoles,
				new Function<ProjectRole, ProjectRole>() {
					@Override
					public ProjectRole apply(final ProjectRole role) {
						return new ProjectRole(role.getId(), null, role.getName(), role.getDescription(), Lists.newArrayList(role
								.getActors()));
					}
				}
		);
		assertThat(projectRolesWithoutSelf, containsInAnyOrder(
				new ProjectRole(10000l, null, "Users", "A project role that represents users in a project",
						ImmutableList.<RoleActor>of(
								new RoleActor(10062l, "jira-users", "atlassian-group-role-actor", "jira-users", buildUserAvatarUri(null, 10083L, "16x16"))
						)),
				new ProjectRole(10001l, null, "Developers", "A project role that represents developers in a project",
						ImmutableList.<RoleActor>of(
								new RoleActor(10061l, "jira-developers", "atlassian-group-role-actor", "jira-developers", buildUserAvatarUri(null, 10083L, "16x16")),
								new RoleActor(10063l, "My Test User", "atlassian-user-role-actor", "user", buildUserAvatarUri(null, 10082L, "16x16"))
						)),
				new ProjectRole(10002l, null, "Administrators", "A project role that represents administrators in a project",
						ImmutableList.<RoleActor>of(
								new RoleActor(10060l, "jira-administrators", "atlassian-group-role-actor", "jira-administrators", buildUserAvatarUri(null, 10083L, "16x16"))
						))
		));

		//noinspection unchecked
		Assert.assertThat(projectRoles, Matchers.containsInAnyOrder(
				addressEndsWith("project/" + projectIdOrKey + "/role/10000"),
				addressEndsWith("project/" + projectIdOrKey + "/role/10001"),
				addressEndsWith("project/" + projectIdOrKey + "/role/10002")));
	}

	private Matcher<ProjectRole> addressEndsWith(final String addressEnding) {
		return new BaseMatcher<ProjectRole>() {
			@Override
			public boolean matches(Object o) {
				return o instanceof ProjectRole && new EntityHelper.AddressEndsWithPredicate(addressEnding).apply((ProjectRole) o);
			}

			@Override
			public void describeTo(Description description) {
				description.appendText("ProjectRole with self address ending with " + addressEnding);
			}
		};
	}

	@JiraBuildNumberDependent(ServerVersionConstants.BN_JIRA_4_4)
	@Test
	public void testGetProjectRoleWithRoleKeyErrorCode() {
		final Project anonProject = client.getProjectClient().getProject(ANONYMOUS_PROJECT_KEY).claim();
		exception.expect(RestClientException.class);
		exception.expectMessage("Can not retrieve a role actor for a null project role.");
		client.getProjectRolesRestClient().getRole(anonProject.getSelf(), -1l).claim();
	}
}
