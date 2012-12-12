/*
 * Copyright (C) 2011 Atlassian
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.atlassian.jira.rest.client;

import org.codehaus.jettison.json.JSONObject;
import org.hamcrest.Description;
import org.junit.internal.matchers.TypeSafeMatcher;

public class JSONObjectMatcher extends TypeSafeMatcher<JSONObject> {

	private final JSONObject jsonObject;

	public JSONObjectMatcher(JSONObject jsonObject) {
		this.jsonObject = jsonObject;
	}

	public static JSONObjectMatcher isEqual(JSONObject jsonObject) {
		return new JSONObjectMatcher(jsonObject);
	}

	@Override
	public boolean matchesSafely(JSONObject item) {
		return item.toString().equals(jsonObject.toString());
	}

	@Override
	public void describeTo(Description description) {
		description.appendText("JSONObject " + jsonObject.toString());
	}
}
