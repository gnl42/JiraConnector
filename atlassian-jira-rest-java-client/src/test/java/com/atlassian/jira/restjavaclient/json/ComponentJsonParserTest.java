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

import com.atlassian.jira.restjavaclient.domain.BasicComponent;
import com.atlassian.jira.restjavaclient.domain.Component;
import org.junit.Test;

import java.net.URI;

import static org.junit.Assert.assertEquals;

/**
 * TODO: Document this class / interface here
 *
 * @since v0.1
 */
public class ComponentJsonParserTest {
	@Test
	public void testParseBasicComponent() throws Exception {
		BasicComponentJsonParser parser = new BasicComponentJsonParser();
		final BasicComponent component = parser.parse(ResourceUtil.getJsonObjectFromResource("/json/component/basic-valid.json"));
		assertEquals(new URI("http://localhost:8090/jira/rest/api/latest/component/10000"), component.getSelf());
		assertEquals("Component A", component.getName());
		assertEquals("this is some description of component A", component.getDescription());
	}

	@Test
	public void testParseComponent() throws Exception {
		ComponentJsonParser parser = new ComponentJsonParser();
		final Component component = parser.parse(ResourceUtil.getJsonObjectFromResource("/json/component/complete-valid.json"));
		assertEquals(new URI("http://localhost:8090/jira/rest/api/latest/component/10001"), component.getSelf());
		assertEquals("Component B", component.getName());
		assertEquals(TestConstants.USER1, component.getLead());
		assertEquals("another description", component.getDescription());
	}

}
