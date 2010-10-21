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
import com.google.common.collect.Iterables;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import java.net.URI;
import java.util.ArrayList;

public class UserJsonParser implements JsonParser<User> {
	@Override
	public User parse(JSONObject json) throws JSONException {
		final BasicUser basicUser = JsonParseUtil.parseBasicUser(json);
		final URI avatarUri = JsonParseUtil.parseURI(json.getString("avatarUrl"));
		final String emailAddress = json.getString("emailAddress");
		// we expect always expanded groups, serving them is anyway cheap
		final ExpandableProperty<String> groups = JsonParseUtil.parseExpandableProperty(json.getJSONObject("groups"), new JsonParser<String>() {
			@Override
			public String parse(JSONObject json) throws JSONException {
				return json.getString("name");
			}
		});
		ArrayList<String> groupList = new ArrayList<String>();
		if (groups.getSize() > 0) {
			Iterables.addAll(groupList, groups.getItems());
		}
		return new User(basicUser.getSelf(), basicUser.getName(), basicUser.getDisplayName(), emailAddress, avatarUri, groupList);
	}
}
