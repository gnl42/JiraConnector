/*
 * Copyright (C) 2014 Atlassian
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

package com.atlassian.jira.rest.client.internal.json;

import com.atlassian.jira.rest.client.api.domain.OperationGroup;
import com.atlassian.jira.rest.client.api.domain.OperationHeader;
import com.atlassian.jira.rest.client.api.domain.OperationLink;
import org.junit.Test;

import java.util.Collections;
import java.util.Set;

import static com.atlassian.jira.rest.client.TestUtil.EMPTY_GROUPS;
import static com.atlassian.jira.rest.client.TestUtil.EMPTY_LINKS;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.*;

public class OperationGroupJsonParserTest {

	@Test
	public void testParse() throws Exception {
		// given
		OperationGroupJsonParser parser = new OperationGroupJsonParser();

		// when
		OperationGroup actual = parser.parse(ResourceUtil.getJsonObjectFromResource("/json/operationGroup/valid.json"));

		// then
		String id = "opsbar-transitions";
		Set<OperationLink> links = Collections.singleton(new OperationLink("action_id_4", "issueaction-workflow-transition",
				"Start Progress", "Start work on the issue", "/secure/WorkflowUIDispatcher.jspa?id=93813&action=4&atl_token=",
				10, null));
		Set<OperationGroup> groups = Collections.singleton(new OperationGroup(null, EMPTY_LINKS, EMPTY_GROUPS,
				new OperationHeader("opsbar-transitions_more", "Workflow", null, null), null));
		int weight = 20;
		assertThat(actual, is(new OperationGroup(id, links, groups, null, weight)));
	}
}