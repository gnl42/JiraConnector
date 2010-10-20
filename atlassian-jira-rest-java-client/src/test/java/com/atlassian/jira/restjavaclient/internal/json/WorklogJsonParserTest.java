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

package com.atlassian.jira.restjavaclient.internal.json;

import com.atlassian.jira.restjavaclient.TestUtil;
import com.atlassian.jira.restjavaclient.domain.Worklog;
import org.junit.Assert;
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
        Assert.assertNull(worklog.getRoleLevel());
        Assert.assertNull(worklog.getGroupLevel());
    }

    @Test
    public void testParseWithRoleLevel() throws Exception {
        final WorklogJsonParser parser = new WorklogJsonParser();
        final Worklog worklog = parser.parse(ResourceUtil.getJsonObjectFromResource("/json/worklog/valid-roleLevel.json"));
        assertEquals(toUri("http://localhost:8090/jira/rest/api/latest/worklog/10011"), worklog.getSelf());
        assertEquals(toUri("http://localhost:8090/jira/rest/api/latest/issue/TST-2"), worklog.getIssueUri());
        assertEquals(TestConstants.USER1, worklog.getAuthor());
        assertEquals(TestConstants.USER1, worklog.getUpdateAuthor());
        assertEquals("another piece of work", worklog.getComment());
        assertEquals(TestUtil.toDateTime("2010-08-17T16:38:00.013+0200"), worklog.getCreationDate());
        assertEquals(TestUtil.toDateTime("2010-08-17T16:38:24.948+0200"), worklog.getUpdateDate());
        assertEquals(TestUtil.toDateTime("2010-08-17T16:37:00.000+0200"), worklog.getStartDate());
        assertEquals("Developers", worklog.getRoleLevel());
        assertEquals(15, worklog.getMinutesSpent());
        Assert.assertNull(worklog.getGroupLevel());
    }

    @Test
    public void testParseWithGroupLevel() throws Exception {
        final WorklogJsonParser parser = new WorklogJsonParser();
        final Worklog worklog = parser.parse(ResourceUtil.getJsonObjectFromResource("/json/worklog/valid-groupLevel.json"));
        assertEquals("jira-users", worklog.getGroupLevel());
        Assert.assertNull(worklog.getRoleLevel());
    }

}
