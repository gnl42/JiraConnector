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
import com.atlassian.jira.rest.client.TestUtil;
import com.atlassian.jira.rest.client.domain.Project;
import com.atlassian.jira.rest.client.domain.ProjectRole;
import com.atlassian.jira.rest.client.domain.RoleActor;
import com.atlassian.jira.rest.client.internal.ServerVersionConstants;
import com.atlassian.jira.rest.client.internal.json.TestConstants;
import com.google.common.base.Function;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.net.URI;

import static org.hamcrest.collection.IsIterableContainingInAnyOrder.containsInAnyOrder;
import static org.junit.Assert.*;

@RestoreOnce(TestConstants.DEFAULT_JIRA_DUMP_FILE)
public class JerseyProjectRoleRestClientTest extends AbstractJerseyRestClientTest {

	private Project anonProjectMock;
	private Project restrictedProjectMock;

	@Before
	public void setup() {
		this.anonProjectMock = Mockito.mock(Project.class);
		Mockito.when(anonProjectMock.getKey()).thenReturn("ANNON");
		this.restrictedProjectMock = Mockito.mock(Project.class);
		Mockito.when(restrictedProjectMock.getKey()).thenReturn("RST");
	}

	@JiraBuildNumberDependent(ServerVersionConstants.BN_JIRA_4_4)
	@Test
	public void testGetProjectRoleWithRoleKeyFromAnonymousProject() {
		final Project anonProject = client.getProjectClient().getProject(anonProjectMock.getKey(), pm);
		final ProjectRole role = client.getProjectRolesRestClient().getRole(anonProject.getSelf(), 10000l, pm);
		Assert.assertNotNull(role);
		Assert.assertEquals("Users", role.getName());
		Assert.assertEquals("A project role that represents users in a project", role.getDescription());
		final RoleActor actor = Iterables.getOnlyElement(role.getActors());
		Assert.assertEquals("jira-users", actor.getDisplayName());
		Assert.assertEquals("atlassian-group-role-actor", actor.getType());
		Assert.assertEquals("jira-users", actor.getName());
		Assert.assertEquals(TestUtil.buildURI(jiraUri, "jira/secure/useravatar?size=small&avatarId=10083"),
				actor.getAvatarUri());
	}

	@JiraBuildNumberDependent(ServerVersionConstants.BN_JIRA_4_4)
	@Test
	public void testGetProjectRoleWithRoleKeyFromRestrictedProject() {
		final Project restrictedProject = client.getProjectClient().getProject(restrictedProjectMock.getKey(), pm);
		final ProjectRole role = client.getProjectRolesRestClient().getRole(restrictedProject.getSelf(), 10000l, pm);
		Assert.assertNotNull(role);
		Assert.assertEquals("Users", role.getName());
		Assert.assertEquals("A project role that represents users in a project", role.getDescription());
		final RoleActor actor = Iterables.getOnlyElement(role.getActors());
		Assert.assertEquals("Administrator", actor.getDisplayName());
		Assert.assertEquals("atlassian-user-role-actor", actor.getType());
		Assert.assertEquals("admin", actor.getName());
		Assert.assertEquals(TestUtil.buildURI(jiraUri, "jira/secure/useravatar?size=small&ownerId=admin&avatarId=10054"), actor.getAvatarUri());
	}

	@JiraBuildNumberDependent(ServerVersionConstants.BN_JIRA_4_4)
	@Test(expected = RestClientException.class)
	public void testGetProjectRoleWithRoleKeyFromRestrictedProjectWithoutPermission() {
		final Project restrictedProject = client.getProjectClient().getProject(restrictedProjectMock.getSelf(), pm);
		setAnonymousMode();
		client.getProjectRolesRestClient().getRole(restrictedProject.getUri(), 10000l, pm);
	}

	@JiraBuildNumberDependent(ServerVersionConstants.BN_JIRA_4_4)
	@Test
	public void testGetProjectRoleWithFullURI() {
		final Project anonProject = client.getProjectClient().getProject(anonProjectMock.getKey(), pm);
		final URI roleURI = client.getProjectRolesRestClient().getRole(anonProject.getSelf(), 10000l, pm).getSelf();
		final ProjectRole role = client.getProjectRolesRestClient().getRole(roleURI, pm);
		Assert.assertNotNull(role);
		Assert.assertEquals("Users", role.getName());
		Assert.assertEquals("A project role that represents users in a project", role.getDescription());
		final RoleActor actor = Iterables.getOnlyElement(role.getActors());
		Assert.assertEquals("jira-users", actor.getDisplayName());
		Assert.assertEquals("atlassian-group-role-actor", actor.getType());
		Assert.assertEquals("jira-users", actor.getName());
		Assert.assertEquals(TestUtil.buildURI(jiraUri, "jira/secure/useravatar?size=small&avatarId=10083"), actor.getAvatarUri());
	}

	@JiraBuildNumberDependent(ServerVersionConstants.BN_JIRA_4_4)
	@Test
	public void testGetAllRolesForProject() {
		final Project anonymousProject = client.getProjectClient().getProject(anonProjectMock.getKey(), pm);

		// don't want to test if ProjectRole.self fields are equal.
		final Iterable<ProjectRole> projectRoles = Iterables.transform(
				client.getProjectRolesRestClient().getRoles(anonymousProject.getSelf(), pm),
				new Function<ProjectRole, ProjectRole>() {
					@Override
					public ProjectRole apply(final ProjectRole role) {
						return new ProjectRole(0l, null, role.getName(), role.getDescription(), Lists.newArrayList(role.getActors()));
					}
				}
		);

		assertThat(projectRoles, containsInAnyOrder(
				new ProjectRole(0l, null, "Users", "A project role that represents users in a project",
						ImmutableList.<RoleActor>of(
								new RoleActor(0l, "jira-users", "atlassian-group-role-actor", "jira-users",
										TestUtil.buildURI(jiraUri, "jira/secure/useravatar?size=small&avatarId=10083"))
						)),
				new ProjectRole(0l, null, "Developers", "A project role that represents developers in a project",
						ImmutableList.<RoleActor>of(
								new RoleActor(0l, "jira-developers", "atlassian-group-role-actor", "jira-developers",
										TestUtil.buildURI(jiraUri, "jira/secure/useravatar?size=small&avatarId=10083")),
								new RoleActor(0l, "My Test User", "atlassian-user-role-actor", "user",
										TestUtil.buildURI(jiraUri, "jira/secure/useravatar?size=small&avatarId=10082"))
						)),
				new ProjectRole(0l, null, "Administrators", "A project role that represents administrators in a project",
						ImmutableList.<RoleActor>of(
								new RoleActor(0l, "jira-administrators", "atlassian-group-role-actor", "jira-administrators",
										TestUtil.buildURI(jiraUri, "jira/secure/useravatar?size=small&avatarId=10083"))
						))
		));
	}

	@JiraBuildNumberDependent(ServerVersionConstants.BN_JIRA_4_4)
	@Test(expected = RestClientException.class)
	public void testGetProjectRoleWithRoleKeyErrorCode() {
		final Project anonProject = client.getProjectClient().getProject(anonProjectMock.getKey(), pm);
		client.getProjectRolesRestClient().getRole(anonProject.getSelf(), -1l, pm);
	}

}
