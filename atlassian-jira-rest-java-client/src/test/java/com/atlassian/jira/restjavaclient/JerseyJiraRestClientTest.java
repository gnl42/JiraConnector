package com.atlassian.jira.restjavaclient;

import com.atlassian.jira.restjavaclient.domain.Issue;
import org.junit.Test;

import java.net.URI;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;

/**
 * TODO: Document this class / interface here
 *
 * @since v0.1
 */
public class JerseyJiraRestClientTest {
    @Test
    public void testGetIssue() throws Exception {
        final URI jiraUri = new URI("http://localhost:8090/jira/");
        JerseyJiraRestClient client = new JerseyJiraRestClient(jiraUri);
        final Issue issue = client.getIssue(new IssueArgs("TST-1"));
        assertEquals("TST-1", issue.getKey());
        assertTrue(issue.getSelf().toString().startsWith(jiraUri.toString()));
    }
}
