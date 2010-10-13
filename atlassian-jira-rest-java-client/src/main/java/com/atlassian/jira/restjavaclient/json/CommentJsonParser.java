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

import com.atlassian.jira.restjavaclient.domain.BasicUser;
import com.atlassian.jira.restjavaclient.domain.Comment;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import java.net.URI;

/**
 * TODO: Document this class / interface here
 *
 * @since v0.1
 */
public class CommentJsonParser implements JsonParser<Comment> {

	@Override
	public Comment parse(JSONObject json) throws JSONException {
		final URI selfUri = JsonParseUtil.getSelfUri(json);
		final String body = json.getString("body");
		final BasicUser author = JsonParseUtil.parseBasicUser(json.getJSONObject("author"));
		final BasicUser updateAuthor = JsonParseUtil.parseBasicUser(json.getJSONObject("updateAuthor"));
		final String roleLevel = json.optString("roleLevel", null);
		final String groupLevel = json.optString("groupLevel", null);
		return new Comment(selfUri, body, author, updateAuthor, JsonParseUtil.parseDateTime(json.getString("created")),
				JsonParseUtil.parseDateTime(json.getString("updated")), roleLevel, groupLevel);
	}
}
