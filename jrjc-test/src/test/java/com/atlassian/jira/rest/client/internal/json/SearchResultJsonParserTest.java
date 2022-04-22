/*
 * Copyright (C) 2011 Atlassian
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

package com.atlassian.jira.rest.client.internal.json;

import com.atlassian.jira.rest.client.api.domain.BasicPriority;
import com.atlassian.jira.rest.client.api.domain.BasicProject;
import com.atlassian.jira.rest.client.api.domain.BasicVotes;
import com.atlassian.jira.rest.client.api.domain.BasicWatchers;
import com.atlassian.jira.rest.client.api.domain.Issue;
import com.atlassian.jira.rest.client.api.domain.IssueType;
import com.atlassian.jira.rest.client.api.domain.SearchResult;
import com.atlassian.jira.rest.client.api.domain.Status;
import com.google.common.collect.Iterables;
import org.codehaus.jettison.json.JSONException;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import static com.atlassian.jira.rest.client.TestUtil.assertEmptyIterable;
import static com.atlassian.jira.rest.client.TestUtil.toDateTime;
import static com.atlassian.jira.rest.client.TestUtil.toUri;
import static com.atlassian.jira.rest.client.api.domain.EntityHelper.findEntityById;
import static com.atlassian.jira.rest.client.internal.json.ResourceUtil.getJsonObjectFromResource;
import static com.atlassian.jira.rest.client.test.matchers.IssueMatchers.issuesWithKeys;
import static com.atlassian.jira.rest.client.test.matchers.SearchResultMatchers.searchResultWithParamsAndIssueCount;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;

public class SearchResultJsonParserTest {

    @Rule
    public final ExpectedException exception = ExpectedException.none();

    final SearchResultJsonParser parser = new SearchResultJsonParser();

    @Test
    public void testParse() throws Exception {
        final SearchResult searchResult = parser.parse(getJsonObjectFromResource("/json/search/issues1.json"));

        assertThat(searchResult, searchResultWithParamsAndIssueCount(0, 50, 1, 1));

        final Issue foundIssue = Iterables.getLast(searchResult.getIssues());
        assertIssueIsTST7(foundIssue);
    }

    @Test
    public void testParseMany() throws Exception {
        final SearchResult searchResult = parser.parse(getJsonObjectFromResource("/json/search/many-issues.json"));

        assertThat(searchResult, searchResultWithParamsAndIssueCount(0, 8, 15, 8));

        final Issue issue = findEntityById(searchResult.getIssues(), 10040L);
        assertIssueIsTST7(issue);

        final String[] expectedIssuesKeys = {"TST-13", "TST-12", "TST-11", "TST-10", "TST-9", "TST-8", "TST-7", "TST-6"};
        assertThat(searchResult.getIssues(), issuesWithKeys(expectedIssuesKeys));
    }

    @Test
    public void testParseInvalidTotal() throws Exception {
        exception.expect(JSONException.class);
        exception.expectMessage("JSONObject[\"total\"] is not a number.");

        parser.parse(getJsonObjectFromResource("/json/search/issues-invalid-total.json"));
    }

    private void assertIssueIsTST7(Issue issue) {
        assertEquals("TST-7", issue.getKey());
        assertEquals(Long.valueOf(10040), issue.getId());
        assertEquals(toUri("http://localhost:8090/jira/rest/api/latest/issue/10040"), issue.getSelf());
        assertEquals("A task where someone will vote", issue.getSummary());
        assertNull(issue.getDescription()); // by default search doesn't retrieve description

        final BasicPriority expectedPriority = new BasicPriority(toUri("http://localhost:8090/jira/rest/api/2/priority/3"), 3L, "Major");
        assertEquals(expectedPriority, issue.getPriority());

        final Status expectedStatus = new Status(toUri("http://localhost:8090/jira/rest/api/2/status/1"), 1L, "Open", "The issue is open and ready for the assignee to start work on it.", toUri("http://localhost:8090/jira/images/icons/status_open.gif"), null);
        assertEquals(expectedStatus, issue.getStatus());

        assertEmptyIterable(issue.getComments());
        assertEmptyIterable(issue.getComments());
        assertEmptyIterable(issue.getComponents());
        assertEmptyIterable(issue.getWorklogs());
        assertEmptyIterable(issue.getSubtasks());
        assertEmptyIterable(issue.getIssueLinks());
        assertEmptyIterable(issue.getFixVersions());
        assertEmptyIterable(issue.getAffectedVersions());
        assertEmptyIterable(issue.getLabels());
        assertNull(issue.getDueDate());
        assertNull(issue.getTimeTracking());
        assertNull(issue.getResolution());
        assertNull(issue.getChangelog());
        assertNull(issue.getAttachments());
        assertEquals(toDateTime("2010-09-22T18:06:32.000+02:00"), issue.getUpdateDate());
        assertEquals(toDateTime("2010-09-22T18:06:32.000+02:00"), issue.getCreationDate());
        assertEquals(TestConstants.USER1, issue.getReporter());
        assertEquals(TestConstants.USER_ADMIN, issue.getAssignee());

        final BasicProject expectedProject = new BasicProject(toUri("http://localhost:8090/jira/rest/api/2/project/TST"), "TST", 10000L, "Test Project");
        assertEquals(expectedProject, issue.getProject());

        final BasicVotes expectedVotes = new BasicVotes(toUri("http://localhost:8090/jira/rest/api/2/issue/TST-7/votes"), 0, false);
        assertEquals(expectedVotes, issue.getVotes());

        final BasicWatchers expectedWatchers = new BasicWatchers(toUri("http://localhost:8090/jira/rest/api/2/issue/TST-7/watchers"), false, 0);
        assertEquals(expectedWatchers, issue.getWatchers());

        final IssueType expectedIssueType = new IssueType(toUri("http://localhost:8090/jira/rest/api/2/issuetype/3"), 3L, "Task", false, "A task that needs to be done.", toUri("http://localhost:8090/jira/images/icons/task.gif"));
        assertEquals(expectedIssueType, issue.getIssueType());
    }

}
