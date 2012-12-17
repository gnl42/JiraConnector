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

import com.atlassian.jira.rest.client.TestUtil;
import com.atlassian.jira.rest.client.api.domain.Votes;
import com.google.common.collect.Iterables;
import org.junit.Assert;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * @since v0.1
 */
public class VotesJsonParserTest {
	@Test
	public void testParse() throws Exception {
		final VotesJsonParser parser = new VotesJsonParser();
		final Votes votes = parser.parse(ResourceUtil.getJsonObjectFromResource("/json/votes/complete.json"));
		Assert.assertEquals(2, Iterables.size(votes.getUsers()));
		Assert.assertEquals(TestConstants.USER1_BASIC_DEPRECATED, Iterables.get(votes.getUsers(), 0));
		Assert.assertFalse(votes.hasVoted());
		Assert.assertEquals(2, votes.getVotes());
		Assert.assertEquals(TestUtil.toUri("http://localhost:8090/jira/rest/api/latest/issue/TST-1/votes"), votes.getSelf());
	}
}
