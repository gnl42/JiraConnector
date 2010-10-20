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

package com.atlassian.jira.rest.restjavaclient.internal.json;

import com.atlassian.jira.rest.restjavaclient.domain.BasicResolution;
import com.atlassian.jira.rest.restjavaclient.domain.Resolution;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

/**
 * @since v0.1
 */
public class ResolutionJsonParser implements JsonParser<Resolution> {
	private final BasicResolutionJsonParser basicResolutionJsonParser = new BasicResolutionJsonParser();
	@Override
	public Resolution parse(JSONObject json) throws JSONException {
		final BasicResolution basicResolution = basicResolutionJsonParser.parse(json);
		final String description = json.getString("description");
		return new Resolution(basicResolution.getSelf(), basicResolution.getName(), description);
	}
}
