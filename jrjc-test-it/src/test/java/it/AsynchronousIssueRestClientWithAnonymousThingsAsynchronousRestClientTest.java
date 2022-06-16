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

import com.atlassian.jira.rest.client.IntegrationTestUtil;
import com.atlassian.jira.rest.client.api.domain.Issue;
import com.atlassian.jira.rest.client.internal.json.TestConstants;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class AsynchronousIssueRestClientWithAnonymousThingsAsynchronousRestClientTest extends AbstractAsynchronousRestClientTest {

    private static boolean alreadyRestored;

    @Before
    public void setup() {
        if (!alreadyRestored) {
            IntegrationTestUtil.restoreAppropriateJiraData(TestConstants.JIRA_DUMP_UNASSIGNED_FILE, administration);
            alreadyRestored = true;
        }
    }

    @Test
    public void testGetUnassignedIssue() throws Exception {
        final Issue issue = client.getIssueClient().getIssue("TST-1").claim();
        assertEquals("TST-1", issue.getKey());
        assertNull(issue.getAssignee());
    }

}
