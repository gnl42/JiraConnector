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
import com.atlassian.jira.restjavaclient.domain.Worklog;
import org.junit.Test;

import static com.atlassian.jira.restjavaclient.TestUtil.toUri;
import static org.junit.Assert.assertEquals;

public class WorklogJsonParserTest {
    @Test
    public void testParse() throws Exception {
        final WorklogJsonParser parser = new WorklogJsonParser();
        final Worklog worklog = parser.parse(ResourceUtil.getJsonObjectFromResource("/json/worklog/valid.json"));
        assertEquals(toUri("http://localhost:8090/jira/rest/api/latest/worklog/10010"), worklog.getSelf());
        assertEquals(toUri("http://localhost:8090/jira/rest/api/latest/issue/TST-2"), worklog.getIssueUri());
        assertEquals(TestConstants.USER_ADMIN, worklog.getAuthor());
        assertEquals(TestConstants.USER_ADMIN, worklog.getUpdateAuthor());
        assertEquals("my first work", worklog.getComment());
        assertEquals(TestUtil.toDateTime("2010-08-17T16:35:47.466+0200"), worklog.getCreationDate());
        assertEquals(TestUtil.toDateTime("2010-08-17T16:35:47.466+0200"), worklog.getUpdateDate());
        assertEquals(TestUtil.toDateTime("2010-08-15T16:35:00.000+0200"), worklog.getStartDate());
        assertEquals(60, worklog.getMinutesSpent());
    }
}
