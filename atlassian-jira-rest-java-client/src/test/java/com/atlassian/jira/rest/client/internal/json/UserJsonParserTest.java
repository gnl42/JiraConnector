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

package com.atlassian.jira.rest.client.internal.json;

import com.atlassian.jira.rest.client.ExpandableProperty;
import com.atlassian.jira.rest.client.TestUtil;
import com.atlassian.jira.rest.client.domain.User;
import com.google.common.collect.ImmutableList;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;


public class UserJsonParserTest {
	@Test
	public void testParse() throws Exception {
		final UserJsonParser parser = new UserJsonParser();
		final User user = parser.parse(ResourceUtil.getJsonObjectFromResource("/json/user/valid.json"));
		assertEquals(TestUtil.toUri("http://localhost:8090/jira/secure/useravatar?size=large&ownerId=admin&avatarId=10054"), user.getAvatarUri());
		assertNull(user.getSmallAvatarUri());
		assertEquals("admin", user.getName());
		assertEquals("Administrator", user.getDisplayName());
		assertEquals("user@atlassian.com", user.getEmailAddress());
		assertEquals(new ExpandableProperty<String>(3, ImmutableList.of("jira-administrators", "jira-developers", "jira-users")), user.getGroups());
	}

	@Test
	public void testParseJira5x0User() throws Exception {
		final UserJsonParser parser = new UserJsonParser();
		final User user = parser.parse(ResourceUtil.getJsonObjectFromResource("/json/user/valid-5.0.json"));
		assertEquals(TestUtil.toUri("http://localhost:2990/jira/secure/useravatar?avatarId=10082"), user.getAvatarUri());
		assertEquals(TestUtil.toUri("http://localhost:2990/jira/secure/useravatar?size=small&avatarId=10082"), user.getSmallAvatarUri());
		assertEquals("wseliga", user.getName());
		assertEquals("Wojciech Seliga", user.getDisplayName());
		assertEquals("wseliga@atlassian.com", user.getEmailAddress());
		assertEquals(1, user.getGroups().getSize());
		assertNull(user.getGroups().getItems());
	}

}
