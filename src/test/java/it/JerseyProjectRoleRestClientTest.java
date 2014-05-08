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
import com.atlassian.jira.nimblefunctests.annotation.RestoreOnce;
import com.atlassian.jira.rest.client.RestClientException;
import com.atlassian.jira.rest.client.domain.EntityHelper;
import com.atlassian.jira.rest.client.domain.Project;
import com.atlassian.jira.rest.client.domain.ProjectRole;
import com.atlassian.jira.rest.client.domain.RoleActor;
import com.atlassian.jira.rest.client.internal.ServerVersionConstants;
import com.atlassian.jira.rest.client.internal.json.TestConstants;
import com.google.common.base.Function;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.net.URI;

import static org.hamcrest.collection.IsIterableContainingInAnyOrder.containsInAnyOrder;
import static org.junit.Assert.*;

@RestoreOnce(TestConstants.DEFAULT_JIRA_DUMP_FILE)
public class JerseyProjectRoleRestClientTest extends AbstractJerseyRestClientTest {

	private static final String ANONYMOUS_PROJECT_KEY = "ANNON";
	private static final String RESTRICTED_PROJECT_KEY = "RST";

	@Rule
	public final ExpectedException exception = ExpectedException.none();

	@JiraBuildNumberDependent(ServerVersionConstants.BN_JIRA_4_4)
	@Test
	public void testGetProjectRoleWithRoleKeyFromAnonymousProject() {
		final Project anonProject = client.getProjectClient().getProject(ANONYMOUS_PROJECT_KEY, pm);
		final ProjectRole role = client.getProjectRolesRestClient().getRole(anonProject.getSelf(), 10000l, pm);
		assertNotNull(role);
		assertEquals("Users", role.getName());
		assertEquals("A project role that represents users in a project", role.getDescription());
		final RoleActor actor = Iterables.getOnlyElement(role.getActors());
		assertEquals("jira-users", actor.getDisplayName());
		assertEquals("atlassian-group-role-actor", actor.getType());
		assertEquals("jira-users", actor.getName());
		assertEquals(jiraUri.resolve("/jira/secure/useravatar?size=small&avatarId=10083"), actor.getAvatarUri());
	}

	@JiraBuildNumberDependent(ServerVersionConstants.BN_JIRA_4_4)
	@Test
	public void testGetProjectRoleWithRoleKeyFromRestrictedProject() {
		final Project restrictedProject = client.getProjectClient().getProject(RESTRICTED_PROJECT_KEY, pm);
		final ProjectRole role = client.getProjectRolesRestClient().getRole(restrictedProject.getSelf(), 10000l, pm);
		assertNotNull(role);
		assertEquals("Users", role.getName());
		assertEquals("A project role that represents users in a project", role.getDescription());
		final RoleActor actor = Iterables.getOnlyElement(role.getActors());
		assertEquals("Administrator", actor.getDisplayName());
		assertEquals("atlassian-user-role-actor", actor.getType());
		assertEquals("admin", actor.getName());
		assertEquals(jiraUri.resolve("/jira/secure/useravatar?size=small&ownerId=admin&avatarId=10054"), actor.getAvatarUri());
	}

	@JiraBuildNumberDependent(ServerVersionConstants.BN_JIRA_4_4)
	@Test
	public void testGetProjectRoleWithRoleKeyFromRestrictedProjectWithoutPermission() {
		final Project restrictedProject = client.getProjectClient().getProject(RESTRICTED_PROJECT_KEY, pm);
		setAnonymousMode();
		exception.expect(RestClientException.class);
		if (isJira5xOrNewer()) {
			exception.expectMessage(String.format("No project could be found with key '%s'", RESTRICTED_PROJECT_KEY));
		}
		else {
			exception.expectMessage("You cannot edit the configuration of this project.");
		}
		client.getProjectRolesRestClient().getRole(restrictedProject.getSelf(), 10000l, pm);
	}

	@JiraBuildNumberDependent(ServerVersionConstants.BN_JIRA_4_4)
	@Test
	public void testGetProjectRoleWithFullURI() {
		final Project anonProject = client.getProjectClient().getProject(ANONYMOUS_PROJECT_KEY, pm);
		final URI roleURI = client.getProjectRolesRestClient().getRole(anonProject.getSelf(), 10000l, pm).getSelf();
		final ProjectRole role = client.getProjectRolesRestClient().getRole(roleURI, pm);
		assertNotNull(role);
		assertEquals("Users", role.getName());
		assertEquals("A project role that represents users in a project", role.getDescription());
		final RoleActor actor = Iterables.getOnlyElement(role.getActors());
		assertEquals("jira-users", actor.getDisplayName());
		assertEquals("atlassian-group-role-actor", actor.getType());
		assertEquals("jira-users", actor.getName());
		assertEquals(jiraUri.resolve("/jira/secure/useravatar?size=small&avatarId=10083"), actor.getAvatarUri());
	}

	@JiraBuildNumberDependent(ServerVersionConstants.BN_JIRA_4_4)
	@Test
	public void testGetAllRolesForProject() {
		final Project anonymousProject = client.getProjectClient().getProject(ANONYMOUS_PROJECT_KEY, pm);
		final Iterable<ProjectRole> projectRoles = client.getProjectRolesRestClient().getRoles(anonymousProject.getSelf(), pm);
		final Iterable<ProjectRole> projectRolesWithoutSelf = Iterables.transform(
				projectRoles,
				new Function<ProjectRole, ProjectRole>() {
					@Override
					public ProjectRole apply(final ProjectRole role) {
						return new ProjectRole(role.getId(), null, role.getName(), role.getDescription(), Lists.newArrayList(role.getActors()));
					}
				}
		);
		assertThat(projectRolesWithoutSelf, containsInAnyOrder(
				new ProjectRole(10000l, null, "Users", "A project role that represents users in a project",
						ImmutableList.<RoleActor>of(
								new RoleActor(10062l, "jira-users", "atlassian-group-role-actor", "jira-users",
										jiraUri.resolve("/jira/secure/useravatar?size=small&avatarId=10083"))
						)),
				new ProjectRole(10001l, null, "Developers", "A project role that represents developers in a project",
						ImmutableList.<RoleActor>of(
								new RoleActor(10061l, "jira-developers", "atlassian-group-role-actor", "jira-developers",
										jiraUri.resolve("/jira/secure/useravatar?size=small&avatarId=10083")),
								new RoleActor(10063l, "My Test User", "atlassian-user-role-actor", "user",
										jiraUri.resolve("/jira/secure/useravatar?size=small&avatarId=10082"))
						)),
				new ProjectRole(10002l, null, "Administrators", "A project role that represents administrators in a project",
						ImmutableList.<RoleActor>of(
								new RoleActor(10060l, "jira-administrators", "atlassian-group-role-actor", "jira-administrators",
										jiraUri.resolve("/jira/secure/useravatar?size=small&avatarId=10083"))
						))
		));

		assertNotNull(Iterables.find(projectRoles, new EntityHelper.AddressEndsWithPredicate("project/ANNON/role/10000")));
		assertNotNull(Iterables.find(projectRoles, new EntityHelper.AddressEndsWithPredicate("project/ANNON/role/10001")));
		assertNotNull(Iterables.find(projectRoles, new EntityHelper.AddressEndsWithPredicate("project/ANNON/role/10002")));
	}

	@JiraBuildNumberDependent(ServerVersionConstants.BN_JIRA_4_4)
	@Test
	public void testGetProjectRoleWithRoleKeyErrorCode() {
		final Project anonProject = client.getProjectClient().getProject(ANONYMOUS_PROJECT_KEY, pm);
		exception.expect(RestClientException.class);
		exception.expectMessage("Can not retrieve a role actor for a null project role.");
		client.getProjectRolesRestClient().getRole(anonProject.getSelf(), -1l, pm);
	}

}
