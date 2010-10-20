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

import com.atlassian.jira.rest.restjavaclient.domain.Priority;
import org.junit.Assert;
import org.junit.Test;

import static com.atlassian.jira.rest.restjavaclient.TestUtil.toUri;

public class PriorityJsonParserTest {
	@Test
	public void testParse() throws Exception {
		final PriorityJsonParser parser = new PriorityJsonParser();
		final Priority priority = parser.parse(ResourceUtil.getJsonObjectFromResource("/json/priority/complete.json"));
		Assert.assertEquals(new Priority(toUri("http://localhost:8090/jira/rest/api/latest/priority/4"), "Minor",
				"#006600", "Minor loss of function, or other problem where easy workaround is present.",
				toUri("http://localhost:8090/jira/images/icons/priority_minor.gif")), priority);
	}

}
