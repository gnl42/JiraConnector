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

package com.atlassian.jira.restjavaclient.json;

import com.atlassian.jira.restjavaclient.TestUtil;
import com.atlassian.jira.restjavaclient.domain.Transition;
import com.google.common.collect.Iterables;
import org.junit.Assert;
import org.junit.Test;

import static com.atlassian.jira.restjavaclient.TestUtil.toUri;
import static org.junit.Assert.assertEquals;

/**
 * TODO: Document this class / interface here
 *
 * @since v0.1
 */
public class TransitionJsonParserTest {
    @Test
    public void testParse() throws Exception {
        final TransitionJsonParser parser = new TransitionJsonParser();

        final Transition transition = parser.parse(ResourceUtil.getJsonObjectFromResource("/json/transition/valid.json"), "5");
        assertEquals(4, Iterables.size(transition.getFields()));
        assertEquals(new Transition.Field("assignee", false, "com.opensymphony.user.User"), Iterables.getLast(transition.getFields()));
        assertEquals("5", transition.getId());
    }
}
