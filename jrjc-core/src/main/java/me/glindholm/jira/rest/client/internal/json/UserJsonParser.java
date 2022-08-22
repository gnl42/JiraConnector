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
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import me.glindholm.jira.rest.client.api.ExpandableProperty;
import me.glindholm.jira.rest.client.api.domain.BasicUser;
import me.glindholm.jira.rest.client.api.domain.User;

public class UserJsonParser implements JsonObjectParser<User> {
    @Override
    public User parse(final JSONObject json) throws JSONException, URISyntaxException {
        final BasicUser basicUser = Objects.requireNonNull(JsonParseUtil.parseBasicUser(json));
        final String timezone = JsonParseUtil.getOptionalString(json, "timeZone");
        final String avatarUrl = JsonParseUtil.getOptionalString(json, "avatarUrl");
        final Map<String, URI> avatarUris = new HashMap<>();
        if (avatarUrl != null) {
            // JIRA prior 5.0
            final URI avatarUri = JsonParseUtil.parseURI(avatarUrl);
            avatarUris.put(User.S48_48, avatarUri);
        } else {
            // JIRA 5.0+
            final JSONObject avatarUrlsJson = json.getJSONObject("avatarUrls");
            @SuppressWarnings("unchecked")
            final Iterator<String> iterator = avatarUrlsJson.keys();
            while (iterator.hasNext()) {
                final String key = iterator.next();
                avatarUris.put(key, JsonParseUtil.parseURI(avatarUrlsJson.getString(key)));
            }
        }
        // optional because groups are not returned for issue->{reporter,assignee}
        final ExpandableProperty<String> groups = JsonParseUtil.parseOptionalExpandableProperty(json.optJSONObject("groups"), new JsonObjectParser<String>() {
            @Override
            public String parse(final JSONObject json) throws JSONException {
                return json.getString("name");
            }
        });
        return new User(basicUser, groups, avatarUris, timezone);
    }
}
