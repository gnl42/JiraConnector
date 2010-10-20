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

package com.atlassian.jira.rest.restjavaclient.internal.json;

import com.atlassian.jira.rest.restjavaclient.TestUtil;
import com.atlassian.jira.rest.restjavaclient.domain.BasicPriority;
import org.junit.Assert;
import org.junit.Test;

public class BasicPriorityJsonParserTest {
	@Test
	public void testParse() throws Exception {
		final BasicPriorityJsonParser parser = new BasicPriorityJsonParser();
		final BasicPriority basicPriority = parser.parse(ResourceUtil.getJsonObjectFromResource("/json/priority/valid.json"));
		Assert.assertEquals(new BasicPriority(TestUtil.toUri("http://localhost:8090/jira/rest/api/latest/priority/3"), "XMajor"), basicPriority);
	}
}
