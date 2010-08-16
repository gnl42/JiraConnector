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
import com.atlassian.jira.restjavaclient.domain.Comment;
import com.atlassian.jira.restjavaclient.domain.Issue;
import com.atlassian.jira.restjavaclient.domain.User;
import org.joda.time.DateTime;
import org.joda.time.format.ISODateTimeFormat;
import org.junit.Test;

import java.net.URI;
import java.net.URISyntaxException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

/**
 * TODO: Document this class / interface here
 *
 * @since v0.1
 */
public class JerseyJiraRestClientTest {
    final URI jiraUri;
    private JerseyJiraRestClient client;
    private final DateTime dateTime = ISODateTimeFormat.dateTime().parseDateTime("2010-08-04T17:46:45.454+0200");

    public JerseyJiraRestClientTest() throws URISyntaxException {
        jiraUri = new URI("http://localhost:8090/jira/");
        client = new JerseyJiraRestClient(jiraUri);
    }

    @Test
    public void testGetIssue() throws Exception {
        final Issue issue = client.getIssue(
				new IssueArgsBuilder("TST-1").withAttachments(true).withComments(true).withWorklogs(true).withWatchers(true).build(),
                new NullProgressMonitor());
        assertEquals("TST-1", issue.getKey());
        assertTrue(issue.getSelf().toString().startsWith(jiraUri.toString()));

        assertEquals(3, issue.getComments().getSize());
        assertThat(issue.getExpandos(), IsIterableOf.hasOnlyElements("comments", "worklogs", "attachments", "watchers", "fields"));

        assertEquals(4, issue.getAttachments().getSize());
        final Iterable<Attachment> items = issue.getAttachments().getItems();
        assertNotNull(items);
        final User user = new User(new URI("http://localhost:8090/jira/rest/api/latest/user/admin"),
                "admin", "Administrator");
        Attachment attachment1 = new Attachment(new URI("http://localhost:8090/jira/rest/api/latest/attachment/10040"),
                "dla Paw\u0142a.txt", user, dateTime, 643, "text/plain",
                new URI("http://localhost:8090/jira/secure/attachment/10040/dla+Paw%C5%82a.txt"), null);

        assertEquals(attachment1, items.iterator().next());

		System.out.println(issue);

    }

    @Test
    public void testGetIssueNoAttachments() throws Exception {
        final Issue issue = client.getIssue(new IssueArgsBuilder("TST-1").withAttachments(false).withComments(true).build(),
                new NullProgressMonitor());
        assertEquals("TST-1", issue.getKey());
        assertTrue(issue.getSelf().toString().startsWith(jiraUri.toString()));

        assertEquals(3, issue.getComments().getSize());
        final Iterable<Comment> comments = issue.getComments().getItems();
        assertEquals(4, issue.getAttachments().getSize());
        assertNull(issue.getAttachments().getItems());
    }

	@Test
	public void temporaryOnly() throws Exception {
		final Issue issue = client.getIssue(new IssueArgsBuilder("TST-2").withAttachments(false).withComments(true).build(),
				new NullProgressMonitor());
		System.out.println(issue);
	}

}
