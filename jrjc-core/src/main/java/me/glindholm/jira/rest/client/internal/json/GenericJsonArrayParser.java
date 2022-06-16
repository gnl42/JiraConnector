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

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;

public class GenericJsonArrayParser<T> implements JsonArrayParser<List<T>> {

    public static <K> GenericJsonArrayParser<K> create(JsonObjectParser<K> jsonParser) {
        return new GenericJsonArrayParser<>(jsonParser);
    }

    private final JsonObjectParser<T> jsonParser;

    public GenericJsonArrayParser(JsonObjectParser<T> jsonParser) {
        this.jsonParser = jsonParser;
    }

    @Override
    public List<T> parse(JSONArray json) throws JSONException, URISyntaxException {
        ArrayList<T> res = new ArrayList<>(json.length());
        for (int i = 0; i < json.length(); i++) {
            res.add(jsonParser.parse(json.getJSONObject(i)));
        }
        return res;
    }
}
