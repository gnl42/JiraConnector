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
package com.atlassian.jira.rest.client.internal.json;

import com.atlassian.jira.rest.client.TestUtil;
import com.atlassian.jira.rest.client.api.domain.ProjectRole;
import com.atlassian.jira.rest.client.api.domain.RoleActor;
import com.google.common.collect.Iterables;
import org.codehaus.jettison.json.JSONException;
import org.hamcrest.collection.IsIterableContainingInAnyOrder;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.net.MalformedURLException;
import java.net.URI;

import static com.atlassian.jira.rest.client.TestUtil.toUri;

public class ProjectRoleJsonParserTest {

	private final URI baseJiraURI = TestUtil.toUri("http://localhost:2990");
	private final ProjectRoleJsonParser parser = new ProjectRoleJsonParser(baseJiraURI);

	@Rule
	public final ExpectedException exception = ExpectedException.none();

	@Test
	public void testParseRoleDetail() throws Exception {
		final ProjectRole role = parser.parse(ResourceUtil.getJsonObjectFromResource("/json/role/valid-role-single-actor.json"));
		Assert.assertEquals(TestUtil.toUri("http://www.example.com/jira/rest/api/2/project/MKY/role/10360"), role.getSelf());
		Assert.assertEquals("Developers", role.getName());
		Assert.assertEquals("A project role that represents developers in a project", role.getDescription());
		Assert.assertNotNull(role.getActors());
		final RoleActor actor = Iterables.getOnlyElement(role.getActors());
		Assert.assertEquals("jira-developers", actor.getDisplayName());
		Assert.assertEquals("atlassian-group-role-actor", actor.getType());
		Assert.assertEquals("jira-developers", actor.getName());
	}

	@Test
	public void testParseRoleWithMultipleActors() throws Exception {
		final ProjectRole role = parser.parse(ResourceUtil
				.getJsonObjectFromResource("/json/role/valid-role-multiple-actors.json"));
		Assert.assertEquals(TestUtil.toUri("http://localhost:2990/jira/rest/api/2/project/TST/role/10000"), role.getSelf());
		Assert.assertEquals("Users", role.getName());
		Assert.assertEquals("A project role that represents users in a project", role.getDescription());
		Assert.assertNotNull(role.getActors());
		Assert.assertThat(role.getActors(),
				IsIterableContainingInAnyOrder
						.containsInAnyOrder(new RoleActor(10020l, "jira-users", "atlassian-group-role-actor", "jira-users",
								toUri("http://localhost:2990/jira/secure/useravatar?size=small&avatarId=10083")
						),
								new RoleActor(10030l, "jira-superuser", "atlassian-user-role-actor", "superuser", null)
						)
		);
	}

	@Test
	public void testParseRoleWithNoActors() throws Exception {
		final ProjectRole role = parser.parse(ResourceUtil.getJsonObjectFromResource("/json/role/valid-role-no-actors.json"));
		Assert.assertEquals(toUri("http://localhost:2990/jira/rest/api/2/project/TST/role/10000"), role.getSelf());
		Assert.assertEquals("Users", role.getName());
		Assert.assertEquals("A project role that represents users in a project", role.getDescription());
		Assert.assertNotNull(role.getActors());
	}

	@Test
	public void testInvalidRole() throws Exception {
		exception.expect(JSONException.class);
		exception.expectMessage("JSONObject[\"self\"] not found.");
		parser.parse(ResourceUtil.getJsonObjectFromResource("/json/role/invalid-role.json"));
	}

	// This test checks the special "admin" case.
	// Id field should not be optional, unfortunately it is not returned for an admin role actor.
	@Test
	public void testParseProjectRoleContainingActorWithoutIdField() throws JSONException, MalformedURLException {
		final ProjectRole role = parser.parse(ResourceUtil
				.getJsonObjectFromResource("/json/role/valid-role-without-user-actor-id.json"));
		Assert.assertNotNull(role);
		Assert.assertEquals("Users", role.getName());
		Assert.assertEquals(TestUtil.toUri("http://localhost:2990/jira/rest/api/2/project/TST/role/10000"), role.getSelf());
		Assert.assertEquals(10000, role.getId().longValue());
		Assert.assertEquals("A project role that represents users in a project", role.getDescription());
		Assert.assertThat(
				role.getActors(),
				IsIterableContainingInAnyOrder.containsInAnyOrder(
						new RoleActor(null, "Administrator", "atlassian-user-role-actor", "admin",
								baseJiraURI.resolve("/jira/secure/useravatar?size=small&ownerId=admin&avatarId=10054")
						),
						new RoleActor(10020l, "jira-users", "atlassian-group-role-actor", "jira-users",
								baseJiraURI.resolve("/jira/secure/useravatar?size=small&avatarId=10083")
						),
						new RoleActor(10030l, "Wojciech Seliga", "atlassian-user-role-actor", "wseliga",
								baseJiraURI.resolve("/jira/secure/useravatar?size=small&avatarId=10082"))
				)
		);
	}

}
