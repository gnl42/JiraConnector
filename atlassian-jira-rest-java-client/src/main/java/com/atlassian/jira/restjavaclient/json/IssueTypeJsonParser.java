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

import com.atlassian.jira.restjavaclient.domain.IssueType;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import java.net.URI;

/**
 * TODO: Document this class / interface here
 *
 * @since v0.1
 */
public class IssueTypeJsonParser implements JsonParser<IssueType> {
    @Override
    public IssueType parse(JSONObject json) throws JSONException {
        final JSONObject valueJson = json.getJSONObject(JsonParseUtil.VALUE_KEY);
        final URI selfUri = JsonParseUtil.getSelfUri(valueJson);
        final String name = valueJson.getString("name");
        final boolean isSubtask = valueJson.getBoolean("subtask");
        return new IssueType(selfUri, name, isSubtask);
    }
}
