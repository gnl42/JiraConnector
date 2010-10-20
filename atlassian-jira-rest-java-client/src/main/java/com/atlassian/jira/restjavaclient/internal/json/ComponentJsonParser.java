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

import com.atlassian.jira.restjavaclient.domain.BasicComponent;
import com.atlassian.jira.restjavaclient.domain.Component;
import com.atlassian.jira.restjavaclient.domain.BasicUser;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

/**
 * TODO: Document this class / interface here
 *
 * @since v0.1
 */
public class ComponentJsonParser implements JsonParser<Component> {
	@Override
	public Component parse(JSONObject json) throws JSONException {
		final BasicComponent basicComponent = BasicComponentJsonParser.parseBasicComponent(json);
		final JSONObject leadJson = json.optJSONObject("lead");
		final BasicUser lead = leadJson != null ? JsonParseUtil.parseBasicUser(leadJson) : null;
		return new Component(basicComponent.getSelf(), basicComponent.getName(), basicComponent.getDescription(), lead);
	}
}
