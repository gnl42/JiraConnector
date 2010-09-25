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

import com.atlassian.jira.restjavaclient.IntegrationTestUtil;
import com.atlassian.jira.restjavaclient.IterableMatcher;
import com.atlassian.jira.restjavaclient.TestUtil;
import com.atlassian.jira.restjavaclient.auth.AnonymousAuthenticationHandler;
import com.atlassian.jira.restjavaclient.domain.User;
import com.atlassian.jira.restjavaclient.jersey.JerseyJiraRestClient;
import com.atlassian.jira.restjavaclient.json.TestConstants;
import org.codehaus.jettison.json.JSONException;
import org.junit.Test;

import javax.ws.rs.core.Response;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

/**
 * TODO: Document this class / interface here
 *
 * @since v0.1
 */
public class JerseyUserRestClientTest extends AbstractJerseyRestClientTest {

    @Test
    public void testGetUser() throws JSONException {
		final User user = client.getUserClient().getUser(ADMIN_USERNAME, pm);
		assertEquals("wojciech.seliga@spartez.com", user.getEmailAddress());
		assertEquals("admin", user.getName());
		assertEquals("Administrator", user.getDisplayName());
		assertThat(user.getGroups(), IterableMatcher.hasOnlyElements("jira-administrators", "jira-developers", "jira-users"));
		assertEquals(IntegrationTestUtil.USER_ADMIN.getSelf(), user.getSelf());
		assertTrue(user.getAvatarUri().toString().contains("ownerId=" + user.getName()));

		final User user2 = client.getUserClient().getUser(TestConstants.USER1_USERNAME, pm);
		assertThat(user2.getGroups(), IterableMatcher.hasOnlyElements("jira-users"));
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
				client = new JerseyJiraRestClient(jiraUri, new AnonymousAuthenticationHandler());
				client.getUserClient().getUser(TestConstants.USER1_USERNAME, pm);
			}
		});

	}


}
