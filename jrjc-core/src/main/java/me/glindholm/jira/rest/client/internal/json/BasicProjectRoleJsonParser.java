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

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.eclipse.jdt.annotation.Nullable;

import me.glindholm.jira.rest.client.api.domain.BasicProjectRole;

public class BasicProjectRoleJsonParser implements JsonObjectParser<List<BasicProjectRole>> {

    @Override
    public List<BasicProjectRole> parse(@Nullable final JSONObject json) throws JSONException {
        if (json == null) {
            return List.of();
        } else {
            final Iterable<String> it = () -> JsonParseUtil.getStringKeys(json);
            try {
                return StreamSupport.stream(it.spliterator(), false).map(key -> extracted(json, key)).collect(Collectors.toUnmodifiableList());
            } catch (final Exception e) {
                throw new JSONException(e);
            }
        }
    }

    private static BasicProjectRole extracted(final JSONObject json, final String key) throws RuntimeException {
        try {
            return new BasicProjectRole(JsonParseUtil.parseURI(json.getString(key)), key);
        } catch (final JSONException e) {
            throw new RuntimeException(e);
        }
    }

}
