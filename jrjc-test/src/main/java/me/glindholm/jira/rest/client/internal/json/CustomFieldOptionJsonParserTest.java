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

package me.glindholm.jira.rest.client.internal.json;

import java.util.Collections;

import org.junit.Test;

import com.google.common.collect.ImmutableList;

import junit.framework.Assert;
import me.glindholm.jira.rest.client.TestUtil;
import me.glindholm.jira.rest.client.api.domain.CustomFieldOption;

/**
 * @since v1.0
 */
public class CustomFieldOptionJsonParserTest {

    @Test
    public void testParseMinimal() throws Exception {
        CustomFieldOptionJsonParser parser = new CustomFieldOptionJsonParser();
        final CustomFieldOption customFieldOption = parser.parse(ResourceUtil
                .getJsonObjectFromResource("/json/customFieldOption/valid-minimal.json"));

        final CustomFieldOption expected = new CustomFieldOption(10017L,
                TestUtil.toUri("http://localhost:2990/jira/rest/api/2/customFieldOption/10017"), "colors",
                Collections.emptyList(), null);
        Assert.assertEquals(expected, customFieldOption);
    }

    @Test
    public void testParseWithChildren() throws Exception {
        CustomFieldOptionJsonParser parser = new CustomFieldOptionJsonParser();
        final CustomFieldOption customFieldOption = parser.parse(ResourceUtil
                .getJsonObjectFromResource("/json/customFieldOption/valid-with-children.json"));

        final CustomFieldOption expected = new CustomFieldOption(10017L,
                TestUtil.toUri("http://localhost:2990/jira/rest/api/2/customFieldOption/10017"), "colors",
                ImmutableList.of(
                        new CustomFieldOption(10019L,
                                TestUtil.toUri("http://localhost:2990/jira/rest/api/2/customFieldOption/10019"), "red",
                                Collections.emptyList(), null),
                        new CustomFieldOption(10020L,
                                TestUtil.toUri("http://localhost:2990/jira/rest/api/2/customFieldOption/10020"), "blue",
                                Collections.emptyList(), null),
                        new CustomFieldOption(10021L,
                                TestUtil.toUri("http://localhost:2990/jira/rest/api/2/customFieldOption/10021"), "green",
                                Collections.emptyList(), null)
                        ), null);
        Assert.assertEquals(expected, customFieldOption);
    }

    @Test
    public void testParseWithChild() throws Exception {
        CustomFieldOptionJsonParser parser = new CustomFieldOptionJsonParser();
        final CustomFieldOption customFieldOption = parser.parse(ResourceUtil
                .getJsonObjectFromResource("/json/customFieldOption/valid-with-child.json"));

        final CustomFieldOption child = new CustomFieldOption(10019L,
                TestUtil.toUri("http://localhost:2990/jira/rest/api/2/customFieldOption/10019"), "red",
                Collections.emptyList(), null);

        final CustomFieldOption expected = new CustomFieldOption(10017L,
                TestUtil.toUri("http://localhost:2990/jira/rest/api/2/customFieldOption/10017"), "colors",
                Collections.emptyList(), child);
        Assert.assertEquals(expected, customFieldOption);
    }
}
