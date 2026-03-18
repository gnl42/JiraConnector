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
package me.glindholm.jira.rest.client.internal.json;

import static me.glindholm.jira.rest.client.TestUtil.toUri;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

import me.glindholm.jira.rest.client.TestUtil;
import me.glindholm.jira.rest.client.api.domain.ProjectRole;
import me.glindholm.jira.rest.client.api.domain.RoleActor;
import me.glindholm.jira.rest.client.shim.jettison.json.JSONException;

public class ProjectRoleJsonParserTest {

    private final URI baseJiraURI = TestUtil.toUri("http://localhost:2990");
    private final ProjectRoleJsonParser parser = new ProjectRoleJsonParser(baseJiraURI);

    @Test
    public void testParseRoleDetail() throws Exception {
        final ProjectRole role = parser.parse(ResourceUtil.getJsonObjectFromResource("/json/role/valid-role-single-actor.json"));
        assertEquals(TestUtil.toUri("http://www.example.com/jira/rest/api/2/project/MKY/role/10360"), role.getSelf());
        assertEquals("Developers", role.getName());
        assertEquals("A project role that represents developers in a project", role.getDescription());
        assertNotNull(role.getActors());
        final RoleActor actor = role.getActors().get(0);
        assertEquals("jira-developers", actor.getDisplayName());
        assertEquals("atlassian-group-role-actor", actor.getType());
        assertEquals("jira-developers", actor.getName());
    }

    @Test
    public void testParseRoleWithMultipleActors() throws Exception {
        final ProjectRole role = parser.parse(ResourceUtil
                .getJsonObjectFromResource("/json/role/valid-role-multiple-actors.json"));
        assertEquals(TestUtil.toUri("http://localhost:2990/jira/rest/api/2/project/TST/role/10000"), role.getSelf());
        assertEquals("Users", role.getName());
        assertEquals("A project role that represents users in a project", role.getDescription());
        assertNotNull(role.getActors());
        assertThat(role.getActors(),
                containsInAnyOrder(new RoleActor(10020l, "jira-users", "atlassian-group-role-actor", "jira-users",
                        toUri("http://localhost:2990/jira/secure/useravatar?size=small&avatarId=10083")
                        ),
                        new RoleActor(10030l, "jira-superuser", "atlassian-user-role-actor", "superuser", null)
                        )
                );
    }

    @Test
    public void testParseRoleWithNoActors() throws Exception {
        final ProjectRole role = parser.parse(ResourceUtil.getJsonObjectFromResource("/json/role/valid-role-no-actors.json"));
        assertEquals(toUri("http://localhost:2990/jira/rest/api/2/project/TST/role/10000"), role.getSelf());
        assertEquals("Users", role.getName());
        assertEquals("A project role that represents users in a project", role.getDescription());
        assertNotNull(role.getActors());
    }

    @Test
    public void testInvalidRole() {
        final JSONException e = assertThrows(JSONException.class,
                () -> parser.parse(ResourceUtil.getJsonObjectFromResource("/json/role/invalid-role.json")));
        assertTrue(e.getMessage().contains("JSONObject[\"self\"] not found."));
    }

    // This test checks the special "admin" case.
    // Id field should not be optional, unfortunately it is not returned for an admin role actor.
    @Test
    public void testParseProjectRoleContainingActorWithoutIdField() throws JSONException, MalformedURLException, URISyntaxException {
        final ProjectRole role = parser.parse(ResourceUtil
                .getJsonObjectFromResource("/json/role/valid-role-without-user-actor-id.json"));
        assertNotNull(role);
        assertEquals("Users", role.getName());
        assertEquals(TestUtil.toUri("http://localhost:2990/jira/rest/api/2/project/TST/role/10000"), role.getSelf());
        assertEquals(10000, role.getId().longValue());
        assertEquals("A project role that represents users in a project", role.getDescription());
        assertThat(
                role.getActors(),
                containsInAnyOrder(
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
