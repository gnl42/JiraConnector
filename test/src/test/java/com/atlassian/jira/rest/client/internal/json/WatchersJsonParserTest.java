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

package com.atlassian.jira.rest.client.internal.json;

import com.atlassian.jira.rest.client.api.domain.BasicWatchers;
import com.atlassian.jira.rest.client.api.domain.Watchers;
import org.codehaus.jettison.json.JSONException;
import org.hamcrest.collection.IsIterableContainingInAnyOrder;
import org.junit.Assert;
import org.junit.Test;

import static org.hamcrest.collection.IsIterableContainingInAnyOrder.containsInAnyOrder;
import static org.junit.Assert.*;

public class WatchersJsonParserTest {
	@Test
	public void testParseBasicWatchers() throws JSONException {
		final JsonObjectParser<BasicWatchers> parser = WatchersJsonParserBuilder.createBasicWatchersParser();
		final BasicWatchers watcher = parser.parse(ResourceUtil.getJsonObjectFromResource("/json/watcher/basic-valid.json"));
		Assert.assertEquals(false, watcher.isWatching());
		Assert.assertEquals(1, watcher.getNumWatchers());

	}

	@Test
	public void testParseWatchers() throws JSONException {
		final JsonObjectParser<Watchers> parser = WatchersJsonParserBuilder.createWatchersParser();
		final Watchers watcher = parser.parse(ResourceUtil.getJsonObjectFromResource("/json/watcher/complete-valid.json"));
		assertEquals(false, watcher.isWatching());
		assertEquals(1, watcher.getNumWatchers());
		Assert.assertThat(watcher.getUsers(), IsIterableContainingInAnyOrder
				.containsInAnyOrder(TestConstants.USER1_BASIC_DEPRECATED, TestConstants.USER_ADMIN_BASIC_DEPRECATED));

	}

}
