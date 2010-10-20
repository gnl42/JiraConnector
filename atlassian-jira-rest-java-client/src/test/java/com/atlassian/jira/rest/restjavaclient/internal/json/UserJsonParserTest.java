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

package com.atlassian.jira.rest.restjavaclient.internal.json;

import com.atlassian.jira.rest.restjavaclient.IterableMatcher;
import com.atlassian.jira.rest.restjavaclient.TestUtil;
import com.atlassian.jira.rest.restjavaclient.domain.User;
import org.junit.Test;
import static org.junit.Assert.*;


/**
 * TODO: Document this class / interface here
 *
 * @since v0.1
 */
public class UserJsonParserTest {
	@Test
	public void testParse() throws Exception {
		final UserJsonParser parser = new UserJsonParser();
		final User user = parser.parse(ResourceUtil.getJsonObjectFromResource("/json/user/valid.json"));
		assertEquals(TestUtil.toUri("http://localhost:8090/jira/secure/useravatar?size=large&ownerId=admin&avatarId=10054"), user.getAvatarUri());
		assertEquals("admin", user.getName());
		assertEquals("Administrator", user.getDisplayName());
		assertEquals("user@atlassian.com", user.getEmailAddress());
		assertThat(user.getGroups(), IterableMatcher.hasOnlyElements("jira-administrators", "jira-developers", "jira-users"));
	}
}
