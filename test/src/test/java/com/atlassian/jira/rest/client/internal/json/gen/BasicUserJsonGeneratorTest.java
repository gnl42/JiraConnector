/*
 * Copyright (C) 2012 Atlassian
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

import com.atlassian.jira.rest.client.test.matchers.JSONObjectMatcher;
import com.atlassian.jira.rest.client.api.domain.BasicUser;
import com.atlassian.jira.rest.client.internal.json.ResourceUtil;
import org.junit.Assert;
import org.junit.Test;

import java.net.URI;

public class BasicUserJsonGeneratorTest {

	private final BasicUserJsonGenerator generator = new BasicUserJsonGenerator();

	@Test
	public void testGenerate() throws Exception {
		final BasicUser user = new BasicUser(new URI("http://localhost:2990/jira/rest/api/2/user?username=wseliga"), "wseliga", "Wojciech Seliga");
		Assert.assertThat(generator.generate(user), JSONObjectMatcher.isEqual(
				ResourceUtil.getJsonObjectFromResource("/json/user/valid-generated.json")
		));
	}
}
