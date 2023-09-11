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

package me.glindholm.jira.rest.client.internal.json;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import me.glindholm.jira.rest.client.api.domain.BasicProject;

import java.net.URI;

public class BasicProjectJsonParser implements JsonObjectParser<BasicProject> {
    @Override
    public BasicProject parse(JSONObject json) throws JSONException {
        final URI selfUri = JsonParseUtil.getSelfUri(json);
        final String key = json.getString("key");
        final Long id = JsonParseUtil.getOptionalLong(json, "id");
        final String name = JsonParseUtil.getOptionalString(json, "name");
        return new BasicProject(selfUri, key, id, name);
    }
}
