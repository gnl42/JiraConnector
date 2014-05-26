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

import com.atlassian.jira.rest.client.api.domain.OperationHeader;
import org.junit.Test;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.*;

public class OperationHeaderJsonParserTest {

	@Test
	public void testParseIdLabel() throws Exception {
		OperationHeaderJsonParser parser = new OperationHeaderJsonParser();
		OperationHeader actual = parser.parse(ResourceUtil.getJsonObjectFromResource("/json/operationHeader/valid-id-label.json"));
		assertThat(actual, is(new OperationHeader("opsbar-transitions_more", "Workflow", null, null)));
	}

	@Test
	public void testParseLabelTitleIconClass() throws Exception {
		OperationHeaderJsonParser parser = new OperationHeaderJsonParser();
		OperationHeader actual = parser.parse(ResourceUtil.getJsonObjectFromResource("/json/operationHeader/valid-label-title-iconClass.json"));
		assertThat(actual, is(new OperationHeader(null, "Views", "View this issue in another format", "icon-view")));
	}
}
