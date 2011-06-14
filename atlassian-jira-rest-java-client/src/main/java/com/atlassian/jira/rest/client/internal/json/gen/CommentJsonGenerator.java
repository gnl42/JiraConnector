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
import com.atlassian.jira.rest.client.domain.Visibility;
import com.atlassian.jira.rest.client.internal.ServerVersionConstants;
import com.atlassian.jira.rest.client.internal.json.CommentJsonParser;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

public class CommentJsonGenerator implements JsonGenerator<Comment> {

	private final ServerInfo serverInfo;

	// this has to be "configurable" as JIRA 4.2 and JIRA 4.3 EAP differently name
	// this attribute for transitions and for issue linking
	private final String groupLevelAttribute;

	public CommentJsonGenerator(ServerInfo serverInfo) {
		this(serverInfo, "groupLevel");
	}

	public CommentJsonGenerator(ServerInfo serverInfo, String groupLevelAttribute) {
		this.serverInfo = serverInfo;
		this.groupLevelAttribute = groupLevelAttribute;
	}

	@Override
	public JSONObject generate(Comment comment) throws JSONException {
		JSONObject res = new JSONObject();
		if (comment.getBody() != null) {
			res.put("body", comment.getBody());
		}

		final Visibility commentVisibility = comment.getVisibility();
		if (commentVisibility != null) {

			if (serverInfo.getBuildNumber() >= ServerVersionConstants.BN_JIRA_4_3_OR_NEWER) {
				JSONObject visibilityJson = new JSONObject();
				visibilityJson.put("type", commentVisibility.getType() == Visibility.Type.GROUP ? "GROUP" : "ROLE");
				visibilityJson.put("value", commentVisibility.getValue());
				res.put(CommentJsonParser.VISIBILITY_KEY, visibilityJson);
			} else {
				if (commentVisibility.getType() == Visibility.Type.ROLE) {
					res.put("role", commentVisibility.getValue());
				} else {
					res.put(getGroupLevelAttribute(), commentVisibility.getValue());
				}
			}
		}

		return res;
	}

	protected String getGroupLevelAttribute() {
		return groupLevelAttribute;
	}
}
