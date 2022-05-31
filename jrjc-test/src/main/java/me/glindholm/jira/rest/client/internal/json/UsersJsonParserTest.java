/*
 * Copyright (C) 2018 Atlassian
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

import me.glindholm.jira.rest.client.TestUtil;
import me.glindholm.jira.rest.client.api.domain.User;
import com.google.common.collect.Iterables;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

/**
 * @since v5.1.0
 */
public class UsersJsonParserTest {

    @Test
    public void testParse() throws Exception {
        final UsersJsonParser parser = new UsersJsonParser();
        final Iterable<User> users = parser.parse(ResourceUtil.getJsonArrayFromResource("/json/users/valid.json"));

        assertEquals(2, Iterables.size(users));

        for (User user : users) {
            if (user.getName().equals("admin")) {
                assertEquals(TestUtil
                        .toUri("http://localhost:8090/jira/secure/useravatar?size=large&ownerId=admin&avatarId=10054"), user
                        .getAvatarUri());
                assertNull(user.getSmallAvatarUri());
                assertEquals("admin", user.getName());
                assertEquals("Administrator", user.getDisplayName());
                assertEquals("user@atlassian.com", user.getEmailAddress());
                assertNull(user.getGroups());
                assertNull(user.getTimezone());
            } else if (user.getName().equals("admin2")) {
                assertEquals(TestUtil
                        .toUri("http://localhost:8090/jira/secure/useravatar?size=large&ownerId=admin&avatarId=10055"), user
                        .getAvatarUri());
                assertNull(user.getSmallAvatarUri());
                assertEquals("admin2", user.getName());
                assertEquals("Administrator", user.getDisplayName());
                assertEquals("user2@atlassian.com", user.getEmailAddress());
                assertNull(user.getGroups());
                assertNull(user.getTimezone());

            } else {
                fail("Invalid item '" + user.getName());
            }
        }
    }
}
