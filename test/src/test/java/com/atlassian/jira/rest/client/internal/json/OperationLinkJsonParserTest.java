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

import com.atlassian.jira.rest.client.api.domain.OperationLink;
import org.codehaus.jettison.json.JSONException;
import org.hamcrest.Matcher;
import org.junit.Test;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.*;

public class OperationLinkJsonParserTest {
	@Test
	public void testParseFull() throws Exception {
		testParseResource("/json/operationLink/valid.json", is(new OperationLink("comment-issue",
				"issueaction-comment-issue add-issue-comment", "Comment", "Comment on this issue",
				"/secure/AddComment!default.jspa?id=10100", 10, "aui-icon aui-icon-small aui-iconfont-comment icon-comment")));
	}

	@Test
	public void testParsePartial() throws Exception {
		testParseResource("/json/operationLink/with-label-href-only.json", is(new OperationLink(null,
				null, "Comment", null, "/secure/AddComment!default.jspa?id=10100", null, null)));
	}

	private void testParseResource(String resourcePath, Matcher<OperationLink> expected) throws JSONException {
		OperationLinkJsonParser parser = new OperationLinkJsonParser();
		OperationLink actual = parser.parse(ResourceUtil.getJsonObjectFromResource(resourcePath));
		assertThat(actual, expected);
	}
}