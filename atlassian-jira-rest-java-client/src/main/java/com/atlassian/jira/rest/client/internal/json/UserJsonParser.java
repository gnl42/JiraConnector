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

package com.atlassian.jira.rest.client.internal.json;

import com.atlassian.jira.rest.client.ExpandableProperty;
import com.atlassian.jira.rest.client.domain.BasicUser;
import com.atlassian.jira.rest.client.domain.User;
import com.google.common.collect.Maps;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import java.net.URI;
import java.util.Iterator;
import java.util.Map;

public class UserJsonParser implements JsonParser<User> {
	@Override
	public User parse(JSONObject json) throws JSONException {
		final BasicUser basicUser = JsonParseUtil.parseBasicUser(json);
		final String timezone = JsonParseUtil.getOptionalString(json, "timeZone");
		final String avatarUrl = JsonParseUtil.getOptionalString(json, "avatarUrl");
		Map<String, URI> avatarUris = Maps.newHashMap();
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
		final String emailAddress = json.getString("emailAddress");
		// we expect always expanded groups, serving them is anyway cheap - that was the case for JIRA prior 5.0, now groups are not expanded...
		final ExpandableProperty<String> groups = JsonParseUtil.parseExpandableProperty(json.getJSONObject("groups"), new JsonParser<String>() {
			@Override
			public String parse(JSONObject json) throws JSONException {
				return json.getString("name");
			}
		});
		return new User(basicUser.getSelf(), basicUser.getName(), basicUser.getDisplayName(), emailAddress, groups, avatarUris, timezone);
	}
}
