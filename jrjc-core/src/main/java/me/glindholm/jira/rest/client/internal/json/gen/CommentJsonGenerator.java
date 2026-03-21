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

package me.glindholm.jira.rest.client.internal.json.gen;

import com.fasterxml.jackson.core.JsonProcessingException;

import me.glindholm.jira.rest.client.api.domain.Comment;
import me.glindholm.jira.rest.client.api.domain.ServerInfo;
import me.glindholm.jira.rest.client.api.domain.Visibility;
import me.glindholm.jira.rest.client.internal.json.CommentJsonParser;
import me.glindholm.jira.rest.client.shim.jettison.json.JSONObject;

public class CommentJsonGenerator implements JsonGenerator<Comment> {

    private final ServerInfo serverInfo;

    public CommentJsonGenerator(final ServerInfo serverInfo) {
        this.serverInfo = serverInfo;
    }

    @Override
    public JSONObject generate(final Comment comment) throws JsonProcessingException {
        final JSONObject res = new JSONObject();
        if (comment.getBody() != null) {
            res.put("body", comment.getBody());
        }

        final Visibility commentVisibility = comment.getVisibility();
        if (commentVisibility != null) {

            final int buildNumber = serverInfo.getBuildNumber();
            final JSONObject visibilityJson = new JSONObject();
            final String commentVisibilityType;
            commentVisibilityType = commentVisibility.getType() == Visibility.Type.GROUP ? "group" : "role";
            visibilityJson.put("type", commentVisibilityType);
            visibilityJson.put("value", commentVisibility.getValue());
            res.put(CommentJsonParser.VISIBILITY_KEY, visibilityJson);
        }

        return res;
    }
}