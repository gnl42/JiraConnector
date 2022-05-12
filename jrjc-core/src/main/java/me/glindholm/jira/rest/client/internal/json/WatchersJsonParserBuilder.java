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

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collection;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import me.glindholm.jira.rest.client.api.domain.BasicUser;
import me.glindholm.jira.rest.client.api.domain.BasicWatchers;
import me.glindholm.jira.rest.client.api.domain.Watchers;

public class WatchersJsonParserBuilder {

    public static JsonObjectParser<Watchers> createWatchersParser() {
        return new JsonObjectParser<>() {
            private final BasicUserJsonParser userJsonParser = new BasicUserJsonParser();

            @Override
            public Watchers parse(JSONObject json) throws JSONException, URISyntaxException {
                final Collection<BasicUser> watchers = JsonParseUtil.parseJsonArray(json
                        .getJSONArray("watchers"), userJsonParser);
                return new Watchers(parseValueImpl(json), watchers);
            }
        };
    }

    public static JsonObjectParser<BasicWatchers> createBasicWatchersParser() {
        return new JsonObjectParser<>() {
            @Override
            public BasicWatchers parse(JSONObject json) throws JSONException {
                return parseValueImpl(json);
            }
        };
    }


    private static BasicWatchers parseValueImpl(JSONObject json) throws JSONException {
        final URI self = JsonParseUtil.getSelfUri(json);
        final boolean isWatching = json.getBoolean("isWatching");
        final int numWatchers = json.getInt("watchCount");
        return new BasicWatchers(self, isWatching, numWatchers);
    }

}
