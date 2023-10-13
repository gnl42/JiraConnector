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

import me.glindholm.jira.rest.client.api.domain.Group;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * @since v5.1.0
 */
public class GroupJsonParserTest {
    @Test
    public void testParse() throws Exception {
        final GroupJsonParser parser = new GroupJsonParser();
        final Group group = parser.parse(ResourceUtil.getJsonObjectFromResource("/json/group/valid.json"));

        assertEquals("jira-users", group.getName());
    }
}