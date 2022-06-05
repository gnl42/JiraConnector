/*
 * Copyright (C) 2012 Atlassian
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

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.List;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import me.glindholm.jira.rest.client.api.domain.CustomFieldOption;

/**
 * JSON parser for CustomFieldOption
 *
 * @since v1.0
 */
public class CustomFieldOptionJsonParser implements JsonObjectParser<CustomFieldOption> {

    private final JsonArrayParser<List<CustomFieldOption>> childrenParser = GenericJsonArrayParser.create(this);

    @Override
    public CustomFieldOption parse(JSONObject json) throws JSONException, URISyntaxException {
        final URI selfUri = JsonParseUtil.getSelfUri(json);
        final long id = json.getLong("id");
        final String value = json.getString("value");

        final JSONArray childrenArray = json.optJSONArray("children");
        final List<CustomFieldOption> children = childrenArray != null
                ? childrenParser.parse(childrenArray)
                        : Collections.emptyList();

        final JSONObject childObject = json.optJSONObject("child");
        final CustomFieldOption child = childObject != null ? parse(childObject) : null;

        return new CustomFieldOption(id, selfUri, value, children, child);
    }
}
