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

package com.atlassian.jira.restjavaclient.json;

import com.atlassian.jira.restjavaclient.IssueArgsBuilder;
import com.atlassian.jira.restjavaclient.domain.Issue;
import com.atlassian.jira.restjavaclient.domain.IssueType;
import com.atlassian.jira.restjavaclient.domain.Project;
import org.codehaus.jettison.json.JSONObject;
import org.junit.Assert;
import org.junit.Test;

import static com.atlassian.jira.restjavaclient.TestUtil.toUri;

/**
 * TODO: Document this class / interface here
 *
 * @since v0.1
 */
public class IssueJsonParserTest {
	@Test
	public void testParseIssue() throws Exception {
		final JSONObject issueJson = ResourceUtil.getJsonObjectFromResource("/json/issue/valid-all-expanded.json");
		final IssueJsonParser parser = new IssueJsonParser();
		final Issue issue = parser.parseIssue(new IssueArgsBuilder("TST-2").build(), issueJson);
		Assert.assertEquals("TST-2", issue.getKey());
		Assert.assertEquals(new IssueType(toUri("http://localhost:8090/jira/rest/api/latest/issueType/1"), "Bug", false),
				issue.getIssueType());
		Assert.assertEquals(new Project(toUri("http://localhost:8090/jira/rest/api/latest/project/TST"), "TST"), issue.getProject());
	}
}
