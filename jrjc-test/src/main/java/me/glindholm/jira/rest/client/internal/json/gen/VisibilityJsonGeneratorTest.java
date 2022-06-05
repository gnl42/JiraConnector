/*
 * Copyright (C) 2012 Atlassian
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package me.glindholm.jira.rest.client.internal.json.gen;

import static org.hamcrest.MatcherAssert.assertThat;

import org.junit.Test;

import me.glindholm.jira.rest.client.api.domain.Visibility;
import me.glindholm.jira.rest.client.internal.json.ResourceUtil;
import me.glindholm.jira.rest.client.test.matchers.JSONObjectMatcher;

public class VisibilityJsonGeneratorTest {

    private final VisibilityJsonGenerator generator = new VisibilityJsonGenerator();

    @Test
    public void testGenerateWithGroupType() throws Exception {
        final Visibility visibility = Visibility.group("jira-users");
        assertThat(generator.generate(visibility), JSONObjectMatcher.isEqual(
                ResourceUtil.getJsonObjectFromResource("/json/visibility/group.json")
                ));
    }

    @Test
    public void testGenerateWithRoleType() throws Exception {
        final Visibility visibility = Visibility.role("Developers");
        assertThat(generator.generate(visibility), JSONObjectMatcher.isEqual(
                ResourceUtil.getJsonObjectFromResource("/json/visibility/role.json")
                ));
    }

}
