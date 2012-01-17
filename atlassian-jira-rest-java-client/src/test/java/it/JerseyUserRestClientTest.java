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

import com.atlassian.jira.rest.client.ExpandableProperty;
import com.atlassian.jira.rest.client.IntegrationTestUtil;
import com.atlassian.jira.rest.client.TestUtil;
import com.atlassian.jira.rest.client.domain.User;
import com.atlassian.jira.rest.client.internal.json.TestConstants;
import com.google.common.collect.ImmutableList;
import org.codehaus.jettison.json.JSONException;
import org.junit.Test;

import javax.ws.rs.core.Response;

import static com.atlassian.jira.rest.client.IntegrationTestUtil.USER_SLASH;
import static com.atlassian.jira.rest.client.IntegrationTestUtil.USER_SLASH_LATEST;

public class JerseyUserRestClientTest extends AbstractRestoringJiraStateJerseyRestClientTest {

    @Test
    public void testGetUser() throws JSONException {
		final User user = client.getUserClient().getUser(ADMIN_USERNAME, pm);
		assertEquals("wojciech.seliga@spartez.com", user.getEmailAddress());
		assertEquals("admin", user.getName());
		assertEquals("Administrator", user.getDisplayName());
		assertEquals(new ExpandableProperty<String>(3, ImmutableList.of("jira-administrators", "jira-developers", "jira-users")), user.getGroups());
		assertEquals(IntegrationTestUtil.USER_ADMIN_LATEST.getSelf(), user.getSelf());
		assertTrue(user.getAvatarUri().toString().contains("ownerId=" + user.getName()));

		final User user2 = client.getUserClient().getUser(TestConstants.USER1_USERNAME, pm);
		assertEquals(new ExpandableProperty<String>(ImmutableList.of("jira-users")), user2.getGroups());
    }

	public void testGetUserWithSlash() {
		final User user = client.getUserClient().getUser(USER_SLASH.getName(), pm);
		assertEquals(USER_SLASH_LATEST.getSelf(), user.getSelf());
		assertEquals(USER_SLASH_LATEST.getDisplayName(), user.getDisplayName());
	}

	public void testGetNonExistingUser() {
		final String username = "same-fake-user-which-does-not-exist";
		TestUtil.assertErrorCode(Response.Status.NOT_FOUND, "The user named '" + username + "' does not exist",
				new Runnable() {
			@Override
			public void run() {
				client.getUserClient().getUser(username, pm);
			}
		});
	}

	public void testGetUserAnonymously() {
		TestUtil.assertErrorCode(Response.Status.UNAUTHORIZED, new Runnable() {
			@Override
			public void run() {
				setAnonymousMode();
				client.getUserClient().getUser(TestConstants.USER1_USERNAME, pm);
			}
		});

	}

}
