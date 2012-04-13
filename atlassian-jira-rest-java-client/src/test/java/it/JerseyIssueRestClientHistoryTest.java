/*
 * Copyright (C) 2012 Atlassian
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

import com.atlassian.jira.rest.client.IssueRestClient;
import com.atlassian.jira.rest.client.domain.Issue;
import com.atlassian.jira.rest.client.internal.json.IssueJsonParserTest;
import org.junit.Assert;

import java.util.Collections;

public class JerseyIssueRestClientHistoryTest extends AbstractRestoringJiraStateJerseyRestClientTest {
	@Override
	protected String getJiraDumpFile() {
		return "export-for-history-tests.xml";
	}

	public void testSimpleIssueHistory() throws Exception {
		final Issue issue = client.getIssueClient().getIssue("HST-1", pm);
		Assert.assertEquals("Simple history test - modified", issue.getSummary());
		Assert.assertNull(issue.getChangelog());

		final Issue issueWithChangelog = client.getIssueClient().getIssue("HST-1", Collections.singleton(IssueRestClient.Expandos.CHANGELOG), pm);
		Assert.assertEquals("Simple history test - modified", issueWithChangelog.getSummary());
		Assert.assertNotNull(issueWithChangelog.getChangelog());
		IssueJsonParserTest.verifyHST1Changelog(issueWithChangelog.getChangelog());
	}
}
