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

import com.atlassian.jira.rest.client.api.domain.RoleActor;
import org.codehaus.jettison.json.JSONException;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.net.URI;

import static com.atlassian.jira.rest.client.TestUtil.toUri;
import static com.atlassian.jira.rest.client.internal.json.ResourceUtil.getJsonObjectFromResource;

public class RoleActorJsonParserTest {

	private URI baseJiraURI = toUri("http://localhost:2990");
	private final RoleActorJsonParser roleActorJsonParser = new RoleActorJsonParser(baseJiraURI);

	@Rule
	public final ExpectedException exception = ExpectedException.none();

	@Test
	public void testParseValidActorWithOptionalParam() throws Exception {
		final RoleActor actor = roleActorJsonParser.parse(getJsonObjectFromResource("/json/actor/valid-actor.json"));
		Assert.assertEquals(10020l, actor.getId().longValue());
		Assert.assertEquals("jira-users", actor.getName());
		Assert.assertEquals("jira-users", actor.getDisplayName());
		Assert.assertEquals("atlassian-group-role-actor", actor.getType());
		Assert.assertEquals(toUri(baseJiraURI.toString() + "/jira/secure/useravatar?size=small&avatarId=10083"), actor
				.getAvatarUri());
	}

	@Test
	public void testParseValidActorWithoutOptionalParams() throws JSONException {
		final RoleActor actor = roleActorJsonParser
				.parse(getJsonObjectFromResource("/json/actor/valid-actor-without-avatar.json"));
		Assert.assertEquals(10020l, actor.getId().longValue());
		Assert.assertEquals("jira-users", actor.getName());
		Assert.assertEquals("jira-users", actor.getDisplayName());
		Assert.assertEquals("atlassian-group-role-actor", actor.getType());
		Assert.assertNull(actor.getAvatarUri());
	}

	@Test
	public void testParseInvalidActor() throws Exception {
		exception.expect(JSONException.class);
		exception.expectMessage("JSONObject[\"type\"] not found.");
		roleActorJsonParser.parse(getJsonObjectFromResource("/json/actor/invalid-actor-without-required-fields.json"));
	}
}
