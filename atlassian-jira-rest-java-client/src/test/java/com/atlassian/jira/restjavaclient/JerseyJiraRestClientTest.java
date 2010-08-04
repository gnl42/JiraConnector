package com.atlassian.jira.restjavaclient;

import com.atlassian.jira.restjavaclient.domain.Comment;
import com.atlassian.jira.restjavaclient.domain.Issue;
import junit.framework.Assert;
import org.hamcrest.CoreMatchers;
import org.junit.Test;
import org.junit.internal.matchers.IsCollectionContaining;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.Collections;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNull;
import static junit.framework.Assert.assertTrue;

/**
 * TODO: Document this class / interface here
 *
 * @since v0.1
 */
public class JerseyJiraRestClientTest {
    final URI jiraUri;
    JerseyJiraRestClient client;

    public JerseyJiraRestClientTest() throws URISyntaxException {
        jiraUri = new URI("http://localhost:8090/jira/");
        client = new JerseyJiraRestClient(jiraUri);
    }

    @Test
    public void testGetIssue() throws Exception {
        final Issue issue = client.getIssue(new IssueArgs("TST-1").withAttachments(true).withComments(true),
                new NullProgressMonitor());
        assertEquals("TST-1", issue.getKey());
        assertTrue(issue.getSelf().toString().startsWith(jiraUri.toString()));

        assertEquals(3, issue.getComments().getSize());
        org.junit.Assert.assertThat(issue.getExpandos(), IsCollectionContaining.hasItems("fields","comments","worklogs","attachments","watchers"));
    }

    @Test
    public void testGetIssueNoAttachments() throws Exception {
        final Issue issue = client.getIssue(new IssueArgs("TST-1").withAttachments(false).withComments(true),
                new NullProgressMonitor());
        assertEquals("TST-1", issue.getKey());
        assertTrue(issue.getSelf().toString().startsWith(jiraUri.toString()));

        assertEquals(3, issue.getComments().getSize());
        final Iterable<Comment> comments = issue.getComments().getItems();
        assertEquals(4, issue.getAttachments().getSize());
        assertNull(issue.getAttachments().getItems());
    }


}
