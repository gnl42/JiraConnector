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

package com.atlassian.jira.rest.client.internal.json.gen;

import com.atlassian.jira.rest.client.JSONObjectMatcher;
import com.atlassian.jira.rest.client.domain.Comment;
import com.atlassian.jira.rest.client.domain.ServerInfo;
import com.atlassian.jira.rest.client.domain.input.LinkIssuesInput;
import com.atlassian.jira.rest.client.internal.ServerVersionConstants;
import com.atlassian.jira.rest.client.internal.json.ResourceUtil;
import org.junit.Test;

import static org.junit.Assert.assertThat;

public class LinkIssuesInputGeneratorTest {

	private final ServerInfo serverInfo = new ServerInfo(null, "1.2.3", ServerVersionConstants.BN_JIRA_4_3_OR_NEWER, null, null, null, null);
	private final LinkIssuesInputGenerator inputGenerator = new LinkIssuesInputGenerator(serverInfo);

	@Test
	public void testGenerateWithoutComment() throws Exception {
		LinkIssuesInput input1 = new LinkIssuesInput("TST-1", "TST-2", "MyLinkType");
		assertThat(inputGenerator.generate(input1), JSONObjectMatcher.isEqual(ResourceUtil.getJsonObjectFromResource("/json/issueLinkInput/no-comment.json")));
	}

	@Test
	public void testGenerate() throws Exception {
		LinkIssuesInput input1 = new LinkIssuesInput("TST-1", "TST-2", "MyLinkType", Comment.valueOf("simple comment"));
		assertThat(inputGenerator.generate(input1), JSONObjectMatcher.isEqual(ResourceUtil.getJsonObjectFromResource("/json/issueLinkInput/simple.json")));
	}

	@Test
	public void testGenerateWithRoleLevel() throws Exception {
		LinkIssuesInput input1 = new LinkIssuesInput("TST-1", "TST-2", "MyLinkType", Comment.createWithRoleLevel("simple comment", "Users"));
		assertThat(inputGenerator.generate(input1), JSONObjectMatcher.isEqual(ResourceUtil.getJsonObjectFromResource("/json/issueLinkInput/with-project-role.json")));
	}

	@Test
	public void testGenerateWithGroupLevel() throws Exception {
		LinkIssuesInput input1 = new LinkIssuesInput("TST-1", "TST-2", "MyLinkType", Comment.createWithGroupLevel("simple comment", "jira-users"));
		assertThat(inputGenerator.generate(input1), JSONObjectMatcher.isEqual(ResourceUtil.getJsonObjectFromResource("/json/issueLinkInput/with-user-group.json")));
	}

}
