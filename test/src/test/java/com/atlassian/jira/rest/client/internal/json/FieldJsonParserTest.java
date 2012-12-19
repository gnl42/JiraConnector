/*
 * Copyright (C) 2012 Atlassian
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

import com.atlassian.jira.rest.client.api.domain.Field;
import com.atlassian.jira.rest.client.api.domain.FieldSchema;
import com.atlassian.jira.rest.client.api.domain.FieldType;
import org.codehaus.jettison.json.JSONException;
import org.hamcrest.Matchers;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import static junit.framework.Assert.assertEquals;
import static org.junit.Assert.*;

public class FieldJsonParserTest {

	private static final FieldJsonParser fieldJsonParser = new FieldJsonParser();

	@Rule
	public final ExpectedException expectedException = ExpectedException.none();

	@Test
	public void testParseValidSingleField() throws JSONException {
		final Field field = fieldJsonParser.parse(ResourceUtil.getJsonObjectFromResource("/json/field/valid-system-field.json"));
		final FieldSchema schema = new FieldSchema("status", null, "status", null, null);
		final Field expectedField = new Field("status", "Status", FieldType.JIRA, false, true, true, schema);
		assertEquals(expectedField, field);
	}

	@Test
	public void testParseValidCustomField() throws JSONException {
		final Field field = fieldJsonParser.parse(ResourceUtil.getJsonObjectFromResource("/json/field/valid-custom-field.json"));
		final FieldSchema schema = new FieldSchema("array", "string", null, "com.atlassian.jira.plugin.system.customfieldtypes:multiselect", 10000l);
		final Field expectedField = new Field("customfield_10000", "MultiSelect Custom IssueField", FieldType.CUSTOM, true, true, true, schema);
		assertEquals(expectedField, field);
	}

	@Test
	public void testParseMultipleCustomFields() throws JSONException {
		JsonArrayParser<Iterable<Field>> fieldsParser = FieldJsonParser.createFieldsArrayParser();
		final Iterable<Field> fields = fieldsParser.parse(ResourceUtil.getJsonArrayFromResource("/json/field/valid-multiple-fields.json"));

		assertThat(fields, Matchers.hasItems(
				new Field("progress", "Progress", FieldType.JIRA, false, true, false,
						new FieldSchema("progress", null, "progress", null, null)),
				new Field("customfield_10000", "MultiSelect Custom IssueField", FieldType.CUSTOM, true, true, true,
						new FieldSchema("array", "string", null, "com.atlassian.jira.plugin.system.customfieldtypes:multiselect", 10000l)),
				new Field("thumbnail", "Images", FieldType.JIRA, false, true, false, null),
				new Field("issuekey", "Key", FieldType.JIRA, false, true, false, null),
				new Field("timetracking", "Time Tracking", FieldType.JIRA, true, false, true,
						new FieldSchema("timetracking", null, "timetracking", null, null)),
				new Field("components", "Component/s", FieldType.JIRA, true, true, true,
						new FieldSchema("array", "component", "components", null, null)),
				new Field("aggregatetimespent", "Σ Time Spent", FieldType.JIRA, false, true, false,
						new FieldSchema("number", null, "aggregatetimespent", null, null))
		));
	}

	@Test
	public void testParseFieldWithoutSomeFields() throws JSONException {
		expectedException.expect(JSONException.class);
		expectedException.expectMessage("JSONObject[\"orderable\"] not found.");
		fieldJsonParser.parse(ResourceUtil.getJsonObjectFromResource("/json/field/invalid-field.json"));
	}

}
