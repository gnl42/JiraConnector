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

package it;

import com.atlassian.jira.restjavaclient.IntegrationTestUtil;
import com.atlassian.jira.restjavaclient.IterableMatcher;
import com.atlassian.jira.restjavaclient.IssueArgsBuilder;
import com.atlassian.jira.restjavaclient.NullProgressMonitor;
import com.atlassian.jira.restjavaclient.domain.*;
import com.google.common.collect.Iterables;
import org.joda.time.DateTime;
import org.joda.time.format.ISODateTimeFormat;
import org.junit.Test;

import javax.ws.rs.core.UriBuilder;
import java.net.URI;
import java.util.Collections;

import static org.junit.Assert.assertThat;


/**
 * TODO: Document this class / interface here
 *
 * @since v0.1
 */
public class JerseyIssueRestClientTest extends AbstractJerseyRestClientTest {

    // no timezone here, as JIRA does not store timezone information in its dump file
    private final DateTime dateTime = ISODateTimeFormat.dateTimeParser().parseDateTime("2010-08-04T17:46:45.454");

    @Test
    public void testGetWatchers() throws Exception {
        configureJira();
        final Issue issue = client.getIssueClient().getIssue(new IssueArgsBuilder("TST-1").build(), new NullProgressMonitor());
        final Watchers watchers = client.getIssueClient().getWatchers(issue, new NullProgressMonitor());
        assertEquals(1, watchers.getNumWatchers());
        assertFalse(watchers.isWatching());
        assertThat(watchers.getWatchers(), IterableMatcher.hasOnlyElements(IntegrationTestUtil.USER1));
    }

    public URI jiraRestUri(String path) {
        return UriBuilder.fromUri(jiraRestRootUri).path(path).build();
    }

    @Test
    public void testGetIssue() throws Exception {
        configureJira();
        final Issue issue = client.getIssueClient().getIssue(
				new IssueArgsBuilder("TST-1").withAttachments(true).withComments(true).withWorklogs(true).withWatchers(true).build(),
                new NullProgressMonitor());
        assertEquals("TST-1", issue.getKey());
        assertTrue(issue.getSelf().toString().startsWith(jiraUri.toString()));

        assertEquals(3, Iterables.size(issue.getComments()));
        assertThat(issue.getExpandos(), IterableMatcher.hasOnlyElements("html"));

        assertEquals(4, Iterables.size(issue.getAttachments()));
        final Iterable<Attachment> items = issue.getAttachments();
        assertNotNull(items);
        final User user = new User(jiraRestUri("/user/admin"),
                "admin", "Administrator");
        Attachment attachment1 = new Attachment(IntegrationTestUtil.concat(jiraRestRootUri, "/attachment/10040"),
                "dla Paw\u0142a.txt", user, dateTime, 643, "text/plain",
                IntegrationTestUtil.concat(jiraUri, "/secure/attachment/10040/dla+Paw%C5%82a.txt"), null);

        assertEquals(attachment1, items.iterator().next());

		System.out.println(issue);

    }

    @Test
    public void testGetTransitions() throws Exception {
        configureJira();
        final Issue issue = client.getIssueClient().getIssue(new IssueArgsBuilder("TST-1").build(), new NullProgressMonitor());
        final Iterable<Transition> transitions = client.getIssueClient().getTransitions(issue, new NullProgressMonitor());
        assertEquals(3, Iterables.size(transitions));
        assertTrue(Iterables.contains(transitions, new Transition("Start Progress", "4", Collections.<Transition.Field>emptyList())));
    }

}
