/*
 * Copyright (C) 2014 Atlassian
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

import com.atlassian.jira.rest.client.api.domain.Permission;
import com.atlassian.jira.rest.client.api.domain.Permissions;
import com.google.common.collect.Lists;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import java.util.Iterator;
import java.util.List;

public class PermissionsJsonParser implements JsonObjectParser<Permissions> {
	private final PermissionJsonParser permissionJsonParser = new PermissionJsonParser();

	@Override
	public Permissions parse(JSONObject json) throws JSONException {
		JSONObject permissionsObject = json.getJSONObject("permissions");

		List<Permission> permissions = Lists.newArrayList();
		Iterator it = permissionsObject.keys();
		while (it.hasNext()) {
			String key = it.next().toString();
			JSONObject permissionObject = permissionsObject.getJSONObject(key);
			Permission permission = permissionJsonParser.parse(permissionObject);
			permissions.add(permission);
		}
		return new Permissions(permissions);
	}
}
