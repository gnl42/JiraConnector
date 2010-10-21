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

import com.atlassian.jira.rest.client.DateTimeMatcher;
import com.atlassian.jira.rest.client.domain.Version;
import org.codehaus.jettison.json.JSONException;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.junit.Assert;
import org.junit.Test;

import java.net.URI;
import java.net.URISyntaxException;

import static org.junit.Assert.assertEquals;

public class VersionJsonParserTest {

	@Test
	public void testParse() throws JSONException, URISyntaxException {
		VersionJsonParser parser = new VersionJsonParser();
		final Version version = parser.parse(ResourceUtil.getJsonObjectFromResource("/json/version/valid.json"));
		
		assertEquals(new URI("http://localhost:8090/jira/rest/api/latest/version/10000"), version.getSelf());
		assertEquals("1.1", version.getName());
		assertEquals("Some version", version.getDescription());
		Assert.assertFalse(version.isReleased());
		Assert.assertTrue(version.isArchived());
		Assert.assertThat(version.getReleaseDate(), DateTimeMatcher.isEqual(
				new DateTime(2010, 8, 25, 0, 0, 0, 0, DateTimeZone.forOffsetHours(2))));
	}

	@Test
	public void testParse2() throws JSONException, URISyntaxException {
		VersionJsonParser parser = new VersionJsonParser();
		final Version version = parser.parse(ResourceUtil.getJsonObjectFromResource("/json/version/valid2-no-releaseDate.json"));

		assertEquals(new URI("http://localhost:8090/jira/rest/api/latest/version/10000"), version.getSelf());
		assertEquals("1.1abc", version.getName());
		Assert.assertNull(version.getDescription());
		Assert.assertTrue(version.isReleased());
		Assert.assertFalse(version.isArchived());
		Assert.assertNull(version.getReleaseDate());
	}

}
