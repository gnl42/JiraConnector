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

package com.atlassian.jira.restjavaclient.internal.json;

import com.atlassian.jira.restjavaclient.domain.BasicPriority;
import com.atlassian.jira.restjavaclient.domain.Priority;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import java.net.URI;

/**
 * TODO: Document this class / interface here
 *
 * @since v0.1
 */
public class PriorityJsonParser implements JsonParser<Priority> {
	private final BasicPriorityJsonParser basicPriorityJsonParser = new BasicPriorityJsonParser();
	@Override
	public Priority parse(JSONObject json) throws JSONException {
		final BasicPriority basicPriority = basicPriorityJsonParser.parse(json);
		final String statusColor = json.getString("statusColor");
		final String description = json.getString("description");
		final URI iconUri = JsonParseUtil.parseURI(json.getString("iconUrl"));
		return new Priority(basicPriority.getSelf(), basicPriority.getName(), statusColor, description, iconUri);
	}
}
