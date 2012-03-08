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

import com.atlassian.jira.rest.client.domain.BasicComponent;
import com.atlassian.jira.rest.client.domain.BasicUser;
import com.atlassian.jira.rest.client.domain.Project;
import com.atlassian.jira.rest.client.domain.Version;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collection;

public class ProjectJsonParser implements JsonParser<Project> {
	private final VersionJsonParser versionJsonParser = new VersionJsonParser();
	private final BasicComponentJsonParser componentJsonParser = new BasicComponentJsonParser();
	@Override
	public Project parse(JSONObject json) throws JSONException {
		URI self = JsonParseUtil.getSelfUri(json);
		final BasicUser lead = JsonParseUtil.parseBasicUser(json.getJSONObject("lead"));
		final String key = json.getString("key");
        final String name = JsonParseUtil.getOptionalString(json, "name");
		final String urlStr = JsonParseUtil.getOptionalString(json, "url");
		URI uri;
		try {
			 uri = urlStr == null || "".equals(urlStr) ? null : new URI(urlStr);
		} catch (URISyntaxException e) {
			uri = null;
		}
		String description = JsonParseUtil.getOptionalString(json, "description");
		if ("".equals(description)) {
			description = null;
		}
		final Collection<Version> versions = JsonParseUtil.parseJsonArray(json.getJSONArray("versions"), versionJsonParser);
		final Collection<BasicComponent> components = JsonParseUtil.parseJsonArray(json.getJSONArray("components"), componentJsonParser);
		return new Project(self, key, name, description, lead, uri, versions, components);

	}
}
