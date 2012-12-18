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

import com.atlassian.jira.rest.client.api.ExpandableProperty;
import com.atlassian.jira.rest.client.TestUtil;
import com.atlassian.jira.rest.client.api.domain.User;
import com.google.common.collect.ImmutableList;
import org.junit.Assert;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;


public class UserJsonParserTest {
	@Test
	public void testParse() throws Exception {
		final UserJsonParser parser = new UserJsonParser();
		final User user = parser.parse(ResourceUtil.getJsonObjectFromResource("/json/user/valid.json"));
		Assert.assertEquals(TestUtil
				.toUri("http://localhost:8090/jira/secure/useravatar?size=large&ownerId=admin&avatarId=10054"), user
				.getAvatarUri());
		Assert.assertNull(user.getSmallAvatarUri());
		assertEquals("admin", user.getName());
		assertEquals("Administrator", user.getDisplayName());
		Assert.assertEquals("user@atlassian.com", user.getEmailAddress());
		Assert.assertEquals(new ExpandableProperty<String>(3, ImmutableList
				.of("jira-administrators", "jira-developers", "jira-users")), user.getGroups());
		Assert.assertNull(user.getTimezone());
	}

	@Test
	public void testParseJira5x0User() throws Exception {
		final UserJsonParser parser = new UserJsonParser();
		final User user = parser.parse(ResourceUtil.getJsonObjectFromResource("/json/user/valid-5.0.json"));
		Assert.assertEquals(TestUtil.toUri("http://localhost:2990/jira/secure/useravatar?avatarId=10082"), user.getAvatarUri());
		Assert.assertEquals(TestUtil.toUri("http://localhost:2990/jira/secure/useravatar?size=small&avatarId=10082"), user
				.getSmallAvatarUri());
		assertEquals("wseliga", user.getName());
		assertEquals("Wojciech Seliga", user.getDisplayName());
		Assert.assertEquals("wseliga@atlassian.com", user.getEmailAddress());
		Assert.assertEquals(1, user.getGroups().getSize());
		Assert.assertNull(user.getGroups().getItems());
		Assert.assertEquals("Europe/Warsaw", user.getTimezone());
	}

	@Test
	public void testParseWhenEmailHidden() throws Exception {
		final UserJsonParser parser = new UserJsonParser();
		final User user = parser.parse(ResourceUtil.getJsonObjectFromResource("/json/user/valid-with-hidden-email.json"));

		Assert.assertNull(user.getEmailAddress());
	}

	@Test
	public void testParseWhenEmailMasked() throws Exception {
		final UserJsonParser parser = new UserJsonParser();
		final User user = parser.parse(ResourceUtil.getJsonObjectFromResource("/json/user/valid-with-masked-email.json"));

		Assert.assertEquals("wojciech dot seliga at spartez dot com", user.getEmailAddress());
	}

}
