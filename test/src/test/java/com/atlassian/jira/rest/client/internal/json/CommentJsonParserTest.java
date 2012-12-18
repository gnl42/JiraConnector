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
import com.atlassian.jira.rest.client.api.domain.Comment;
import com.atlassian.jira.rest.client.api.domain.Visibility;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.junit.Assert;
import org.junit.Test;


public class CommentJsonParserTest {
	@Test
	public void testParse() throws Exception {
		final JSONObject commentsJson = ResourceUtil.getJsonObjectFromResource("/json/comment/valid.json");
		final CommentJsonParser parser = new CommentJsonParser();

		final JSONObject comment1Json = commentsJson.getJSONArray("value").getJSONObject(0);
		final Comment comment1 = parser.parse(comment1Json);
		Assert.assertEquals("some comment", comment1.getBody());
		Assert.assertEquals(TestConstants.USER_ADMIN_BASIC_DEPRECATED, comment1.getAuthor());
		Assert.assertEquals(TestConstants.USER_ADMIN_BASIC_DEPRECATED, comment1.getUpdateAuthor());
		Assert.assertEquals(TestUtil.toDateTime("2010-08-17T16:40:57.791+0200"), comment1.getCreationDate());
		Assert.assertEquals(TestUtil.toDateTime("2010-08-17T16:40:57.791+0200"), comment1.getUpdateDate());
		Assert.assertEquals(TestUtil.toUri("http://localhost:8090/jira/rest/api/latest/comment/10020"), comment1.getSelf());
		Assert.assertEquals(Long.valueOf(10020), comment1.getId());
		Assert.assertEquals(Visibility.role("Administrators"), comment1.getVisibility());

		final JSONObject comment3Json = commentsJson.getJSONArray("value").getJSONObject(2);
		final Comment comment3 = parser.parse(comment3Json);
		Assert.assertEquals(Long.valueOf(10022), comment3.getId());
		Assert.assertEquals(Visibility.group("jira-users"), comment3.getVisibility());

		final JSONObject comment2Json = commentsJson.getJSONArray("value").getJSONObject(1);
		final Comment comment2 = parser.parse(comment2Json);
		Assert.assertEquals(Long.valueOf(10021), comment2.getId());
		Assert.assertNull(comment2.getVisibility());
	}

	@Test
	public void testParseWithoutId() throws Exception {
		final JSONObject commentsJson = ResourceUtil.getJsonObjectFromResource("/json/comment/valid-without-id.json");
		final CommentJsonParser parser = new CommentJsonParser();

		final JSONObject comment1Json = commentsJson.getJSONArray("value").getJSONObject(0);
		final Comment comment1 = parser.parse(comment1Json);
		Assert.assertEquals("some comment", comment1.getBody());
		Assert.assertEquals(TestConstants.USER_ADMIN_BASIC_DEPRECATED, comment1.getAuthor());
		Assert.assertEquals(TestConstants.USER_ADMIN_BASIC_DEPRECATED, comment1.getUpdateAuthor());
		Assert.assertEquals(TestUtil.toDateTime("2010-08-17T16:40:57.791+0200"), comment1.getCreationDate());
		Assert.assertEquals(TestUtil.toDateTime("2010-08-17T16:40:57.791+0200"), comment1.getUpdateDate());
		Assert.assertEquals(TestUtil.toUri("http://localhost:8090/jira/rest/api/latest/comment/10020"), comment1.getSelf());
		Assert.assertEquals(null, comment1.getId());
		Assert.assertEquals(Visibility.role("Administrators"), comment1.getVisibility());

		final JSONObject comment3Json = commentsJson.getJSONArray("value").getJSONObject(2);
		final Comment comment3 = parser.parse(comment3Json);
		Assert.assertEquals(null, comment3.getId());
		Assert.assertEquals(Visibility.group("jira-users"), comment3.getVisibility());

		final JSONObject comment2Json = commentsJson.getJSONArray("value").getJSONObject(1);
		final Comment comment2 = parser.parse(comment2Json);
		Assert.assertEquals(null, comment2.getId());
		Assert.assertNull(comment2.getVisibility());

	}

	@Test
	public void testParseAnonymous() throws JSONException {
		final CommentJsonParser parser = new CommentJsonParser();
		final JSONObject json = ResourceUtil.getJsonObjectFromResource("/json/comment/valid-anonymous.json");
		final JSONObject commentJson = json.getJSONArray("value").getJSONObject(0);
		final Comment comment = parser.parse(commentJson);
		Assert.assertNull(comment.getAuthor());
		Assert.assertNull(comment.getUpdateAuthor());
		Assert.assertEquals("Comment from anonymous user", comment.getBody());

	}
}
