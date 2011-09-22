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

import com.atlassian.jira.rest.client.domain.Field;
import org.codehaus.jettison.json.JSONObject;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class JsonFieldParserTest {
	@Test
	public void testParse() throws Exception {
		JsonFieldParser parser = new JsonFieldParser();
		final JSONObject fieldsJs = ResourceUtil.getJsonObjectFromResource("/json/field/valid-fields.json");
		final Field field = parser.parse(fieldsJs.getJSONObject("customfield_10000"), "customfield_10000");
		assertEquals(1.45, (Double) field.getValue(), 0.001);

		final Field userField = parser.parse(fieldsJs.getJSONObject("customfield_10020"), "customfield_10020");
		assertEquals(TestConstants.USER1, userField.getValue());

	}


}
