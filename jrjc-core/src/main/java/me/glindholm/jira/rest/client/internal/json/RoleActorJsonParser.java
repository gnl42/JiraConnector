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

import org.apache.hc.core5.net.URIBuilder;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import me.glindholm.jira.rest.client.api.domain.RoleActor;


public class RoleActorJsonParser implements JsonObjectParser<RoleActor> {

    private final URI baseJiraUri;

    public RoleActorJsonParser(URI baseJiraUri) {
        this.baseJiraUri = baseJiraUri;
    }

    @Override
    public RoleActor parse(final JSONObject json) throws JSONException, URISyntaxException {
        // Workaround for a bug in API. Id field should not be optional, unfortunately it is not returned for an admin role actor.
        final Long id = JsonParseUtil.getOptionalLong(json, "id");
        final String displayName = json.getString("displayName");
        final String type = json.getString("type");
        final String name = json.getString("name");
        return new RoleActor(id, displayName, type, name, parseAvatarUrl(json));
    }

    private URI parseAvatarUrl(final JSONObject json) throws URISyntaxException {
        final String pathToAvatar = JsonParseUtil.getOptionalString(json, "avatarUrl");
        if (pathToAvatar != null) {
            final URI avatarUri = new URIBuilder(pathToAvatar).build();
            return avatarUri.isAbsolute() ? avatarUri : baseJiraUri.resolve(pathToAvatar);
        } else {
            return null;
        }
    }

}
