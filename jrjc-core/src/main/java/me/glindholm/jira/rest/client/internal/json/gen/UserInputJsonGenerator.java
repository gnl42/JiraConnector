/*
 * Copyright (C) 2018 Atlassian
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

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import me.glindholm.jira.rest.client.api.domain.input.UserInput;

/**
 * Json Generator for UserInput
 *
 * @since v5.1.0
 */
public class UserInputJsonGenerator implements JsonGenerator<UserInput> {
    @Override
    public JSONObject generate(final UserInput user) throws JSONException {
        return new JSONObject()
                .putOpt("key", user.getKey())
                .putOpt("name", user.getName())
                .putOpt("password", user.getPassword())
                .putOpt("emailAddress", user.getEmailAddress())
                .putOpt("displayName", user.getDisplayName())
                .putOpt("notification", user.getNotification())
                .putOpt("applicationKeys", new JSONArray(user.getApplicationKeys()));
    }
}
