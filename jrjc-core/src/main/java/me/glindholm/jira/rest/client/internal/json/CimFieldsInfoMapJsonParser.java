/*
 * Copyright (C) 2012-2013 Atlassian
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
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import me.glindholm.jira.rest.client.api.domain.CimFieldInfo;

/**
 * JSON parser that produces Map of String =&gt; CimFieldInfo
 *
 * @since v1.0
 */
public class CimFieldsInfoMapJsonParser implements JsonObjectParser<Map<String, CimFieldInfo>> {

    private final CimFieldsInfoJsonParser cimFieldsInfoJsonParser = new CimFieldsInfoJsonParser();

    @Override
    public Map<String, CimFieldInfo> parse(final JSONObject json) throws JSONException, URISyntaxException {
        final Map<String, CimFieldInfo> res = new HashMap<>(json.length());
        final Iterator<String> keysIterator = json.keys();
        while (keysIterator.hasNext()) {
            final String id = keysIterator.next();
            res.put(id, cimFieldsInfoJsonParser.parse(json.getJSONObject(id), id));
        }
        return res;
    }
}
