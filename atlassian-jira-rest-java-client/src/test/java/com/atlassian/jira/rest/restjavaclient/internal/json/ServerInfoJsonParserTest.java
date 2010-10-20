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
import com.atlassian.jira.rest.restjavaclient.domain.ServerInfo;
import org.junit.Test;
import org.junit.Assert;

/**
 * TODO: Document this class / interface here
 *
 * @since v0.1
 */
public class ServerInfoJsonParserTest {
	@Test
	public void testParse() throws Exception {
		final ServerInfoJsonParser parser = new ServerInfoJsonParser();
		final ServerInfo serverInfo = parser.parse(ResourceUtil.getJsonObjectFromResource("/json/serverInfo/valid.json"));
		Assert.assertEquals(new ServerInfo(TestUtil.toUri("http://localhost:8090/jira"), "4.2-SNAPSHOT",
				580, TestUtil.toDateTime("2010-09-23T00:00:00.000+0200"), TestUtil.toDateTime("2010-09-30T16:11:09.767+0200"),
				"abc128082", "Your Company JIRA"), serverInfo);
	}

	@Test
	public void testParseAnonymous() throws Exception {
		final ServerInfoJsonParser parser = new ServerInfoJsonParser();
		final ServerInfo serverInfo = parser.parse(ResourceUtil.getJsonObjectFromResource("/json/serverInfo/valid-for-anonymous.json"));
		Assert.assertEquals(new ServerInfo(TestUtil.toUri("http://localhost:8090/jira"), "4.2-SNAPSHOT",
				580, TestUtil.toDateTime("2010-09-23T00:00:00.000+0200"), null, "128082", "Your Company JIRA"), serverInfo);

	}
}
