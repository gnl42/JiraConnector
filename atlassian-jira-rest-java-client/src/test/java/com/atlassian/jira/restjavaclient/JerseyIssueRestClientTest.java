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

package com.atlassian.jira.restjavaclient;

import com.atlassian.jira.restjavaclient.domain.Attachment;
import com.atlassian.jira.restjavaclient.domain.Issue;
import com.atlassian.jira.restjavaclient.domain.User;
import com.atlassian.jira.restjavaclient.domain.Watchers;
import com.atlassian.jira.restjavaclient.json.TestConstants;
import com.google.common.collect.Iterables;
import org.joda.time.DateTime;
import org.joda.time.format.ISODateTimeFormat;
import org.junit.Test;

import java.net.URI;

import static org.junit.Assert.*;
import static org.junit.Assert.assertEquals;


/**
 * TODO: Document this class / interface here
 *
 * @since v0.1
 */
public class JerseyIssueRestClientTest extends AbstractJerseyRestClientTest {

    private final DateTime dateTime = ISODateTimeFormat.dateTime().parseDateTime("2010-08-04T17:46:45.454+0200");

    @Test
    public void testGetWatchers() throws Exception {
        final Issue issue = client.getIssueClient().getIssue(new IssueArgsBuilder("TST-1").build(), new NullProgressMonitor());
        final Watchers watchers = client.getIssueClient().getWatchers(issue, new NullProgressMonitor());
        assertEquals(1, watchers.getNumWatchers());
        assertFalse(watchers.isWatching());
        assertThat(watchers.getWatchers(), IsIterableOf.hasOnlyElements(TestConstants.USER1));
    }

    @Test
    public void testGetIssue() throws Exception {
        final Issue issue = client.getIssueClient().getIssue(
				new IssueArgsBuilder("TST-1").withAttachments(true).withComments(true).withWorklogs(true).withWatchers(true).build(),
                new NullProgressMonitor());
        assertEquals("TST-1", issue.getKey());
        assertTrue(issue.getSelf().toString().startsWith(jiraUri.toString()));

        assertEquals(3, Iterables.size(issue.getComments()));
        assertThat(issue.getExpandos(), IsIterableOf.hasOnlyElements("html"));

        assertEquals(4, Iterables.size(issue.getAttachments()));
        final Iterable<Attachment> items = issue.getAttachments();
        assertNotNull(items);
        final User user = new User(new URI("http://localhost:8090/jira/rest/api/latest/user/admin"),
                "admin", "Administrator");
        Attachment attachment1 = new Attachment(new URI("http://localhost:8090/jira/rest/api/latest/attachment/10040"),
                "dla Paw\u0142a.txt", user, dateTime, 643, "text/plain",
                new URI("http://localhost:8090/jira/secure/attachment/10040/dla+Paw%C5%82a.txt"), null);

        assertEquals(attachment1, items.iterator().next());

		System.out.println(issue);

    }
    
}
