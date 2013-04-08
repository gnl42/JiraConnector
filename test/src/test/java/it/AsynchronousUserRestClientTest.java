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

import com.atlassian.jira.functest.framework.admin.GeneralConfiguration;
import com.atlassian.jira.nimblefunctests.annotation.JiraBuildNumberDependent;
import com.atlassian.jira.nimblefunctests.annotation.RestoreOnce;
import com.atlassian.jira.rest.client.api.ExpandableProperty;
import com.atlassian.jira.rest.client.IntegrationTestUtil;
import com.atlassian.jira.rest.client.TestUtil;
import com.atlassian.jira.rest.client.api.domain.User;
import com.atlassian.jira.rest.client.internal.json.TestConstants;
import com.google.common.collect.ImmutableList;
import org.codehaus.jettison.json.JSONException;
import org.junit.Test;

import javax.ws.rs.core.Response;

import static com.atlassian.jira.rest.client.IntegrationTestUtil.USER_SLASH;
import static com.atlassian.jira.rest.client.IntegrationTestUtil.USER_SLASH_60;
import static com.atlassian.jira.rest.client.internal.ServerVersionConstants.BN_JIRA_4_3;
import static com.atlassian.jira.rest.client.internal.json.TestConstants.ADMIN_USERNAME;
import static org.junit.Assert.*;

@RestoreOnce(TestConstants.DEFAULT_JIRA_DUMP_FILE)
public class AsynchronousUserRestClientTest extends AbstractAsynchronousRestClientTest {

	@Test
	public void testGetUser() throws JSONException {
		final User user = client.getUserClient().getUser(ADMIN_USERNAME).claim();
		assertEquals("wojciech.seliga@spartez.com", user.getEmailAddress());
		assertEquals("admin", user.getName());
		assertEquals("Administrator", user.getDisplayName());
		assertEquals(new ExpandableProperty<String>(3, ImmutableList
				.of("jira-administrators", "jira-developers", "jira-users")), user.getGroups());
		assertEquals(IntegrationTestUtil.USER_ADMIN_60.getSelf(), user.getSelf());
		assertTrue(user.getAvatarUri().toString().contains("ownerId=" + user.getName()));

		final User user2 = client.getUserClient().getUser(TestConstants.USER1_USERNAME).claim();
		assertEquals(new ExpandableProperty<String>(ImmutableList.of("jira-users")), user2.getGroups());
	}

	@Test
	public void testGetUserWithSlash() {
		final User user = client.getUserClient().getUser(USER_SLASH.getName()).claim();
		assertEquals(USER_SLASH_60.getSelf(), user.getSelf());
		assertEquals(USER_SLASH_60.getDisplayName(), user.getDisplayName());
	}

	@Test
	public void testGetNonExistingUser() {
		final String username = "same-fake-user-which-does-not-exist";
		TestUtil.assertErrorCode(Response.Status.NOT_FOUND, "The user named '" + username + "' does not exist",
				new Runnable() {
					@Override
					public void run() {
						client.getUserClient().getUser(username).claim();
					}
				});
	}

	@Test
	public void testGetUserAnonymously() {
		TestUtil.assertErrorCode(Response.Status.UNAUTHORIZED, new Runnable() {
			@Override
			public void run() {
				setAnonymousMode();
				client.getUserClient().getUser(TestConstants.USER1_USERNAME).claim();
			}
		});

	}

	// Email Visibility is respected in REST since 4.3
	@JiraBuildNumberDependent(BN_JIRA_4_3)
	@Test
	public void testGetUserWhenEmailVisibilityIsHidden() throws JSONException {
		administration.generalConfiguration().setUserEmailVisibility(GeneralConfiguration.EmailVisibility.HIDDEN);

		try {
			final User user = client.getUserClient().getUser(ADMIN_USERNAME).claim();
			assertNull(user.getEmailAddress());
			assertEquals("admin", user.getName());
			assertEquals("Administrator", user.getDisplayName());
			assertEquals(new ExpandableProperty<String>(3, ImmutableList
					.of("jira-administrators", "jira-developers", "jira-users")), user.getGroups());
			assertEquals(IntegrationTestUtil.USER_ADMIN_60.getSelf(), user.getSelf());
			assertTrue(user.getAvatarUri().toString().contains("ownerId=" + user.getName()));

			final User user2 = client.getUserClient().getUser(TestConstants.USER1_USERNAME).claim();
			assertEquals(new ExpandableProperty<String>(ImmutableList.of("jira-users")), user2.getGroups());

		} finally {
			// Restore e-mail visibility configuration
			administration.generalConfiguration().setUserEmailVisibility(GeneralConfiguration.EmailVisibility.PUBLIC);
		}
	}

	// Email Visibility is respected in REST since 4.3
	@JiraBuildNumberDependent(BN_JIRA_4_3)
	@Test
	public void testGetUserWhenEmailVisibilityIsMasked() throws JSONException {
		administration.generalConfiguration().setUserEmailVisibility(GeneralConfiguration.EmailVisibility.MASKED);

		try {
			final User user = client.getUserClient().getUser(ADMIN_USERNAME).claim();
			assertEquals("wojciech dot seliga at spartez dot com", user.getEmailAddress());
			assertEquals("admin", user.getName());
			assertEquals("Administrator", user.getDisplayName());
			assertEquals(new ExpandableProperty<String>(3, ImmutableList
					.of("jira-administrators", "jira-developers", "jira-users")), user.getGroups());
			assertEquals(IntegrationTestUtil.USER_ADMIN_60.getSelf(), user.getSelf());
			assertTrue(user.getAvatarUri().toString().contains("ownerId=" + user.getName()));

			final User user2 = client.getUserClient().getUser(TestConstants.USER1_USERNAME).claim();
			assertEquals(new ExpandableProperty<String>(ImmutableList.of("jira-users")), user2.getGroups());
		} finally {
			// Restore e-mail visibility configuration
			administration.generalConfiguration().setUserEmailVisibility(GeneralConfiguration.EmailVisibility.PUBLIC);
		}
	}
}
