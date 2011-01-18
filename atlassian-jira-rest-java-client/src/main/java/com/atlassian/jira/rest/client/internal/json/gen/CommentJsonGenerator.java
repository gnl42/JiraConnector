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

package com.atlassian.jira.rest.client.internal.json.gen;

import com.atlassian.jira.rest.client.domain.Comment;
import com.atlassian.jira.rest.client.domain.ServerInfo;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

public class CommentJsonGenerator implements JsonGenerator<Comment> {

	private final ServerInfo serverInfo;

	public CommentJsonGenerator(ServerInfo serverInfo) {
		this.serverInfo = serverInfo;
	}

	@Override
	public JSONObject generate(Comment comment) throws JSONException {
		JSONObject res = new JSONObject();
		if (comment.getBody() != null) {
			res.put("body", comment.getBody());
		}
		if (comment.getRoleLevel() != null) {
			// JIRA 4.3+ changes the attribute name from "role" to "roleLevel"
			final String roleAttribute = (serverInfo.getBuildNumber() > 600) ? "roleLevel" : "role";
			res.put(roleAttribute, comment.getRoleLevel());
		}
		if (comment.getGroupLevel() != null) {
			res.put("group", comment.getGroupLevel());
		}
		return res;
	}
}
