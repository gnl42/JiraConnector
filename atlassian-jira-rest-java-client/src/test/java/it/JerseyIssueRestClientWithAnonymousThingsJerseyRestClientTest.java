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

import com.atlassian.jira.rest.client.domain.Issue;
import org.junit.Test;

public class JerseyIssueRestClientWithAnonymousThingsJerseyRestClientTest extends AbstractRestoringJiraStateJerseyRestClientTest {
	@Override
	protected String getJiraDumpFile() {
		return "jira2-export-unassigned.xml";
	}

	@Test
	public void testGetUnassignedIssue() throws Exception {
		final Issue issue = client.getIssueClient().getIssue("TST-1", pm);
		assertEquals("TST-1", issue.getKey());
		assertNull(issue.getAssignee());
	}

}
