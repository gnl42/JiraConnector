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

package com.atlassian.jira.restjavaclient.internal.json;

import com.atlassian.jira.restjavaclient.TestUtil;
import com.atlassian.jira.restjavaclient.domain.BasicResolution;
import org.junit.Assert;
import org.junit.Test;

public class BasicResolutionJsonParserTest {
	@Test
	public void testParse() throws Exception {
		final BasicResolutionJsonParser parser = new BasicResolutionJsonParser();
		final BasicResolution basicresolution = parser.parse(ResourceUtil.getJsonObjectFromResource("/json/resolution/valid.json"));
		Assert.assertEquals(new BasicResolution(TestUtil.toUri("http://localhost:8090/jira/rest/api/latest/resolution/4"), "Incomplete"), basicresolution);
	}
}
