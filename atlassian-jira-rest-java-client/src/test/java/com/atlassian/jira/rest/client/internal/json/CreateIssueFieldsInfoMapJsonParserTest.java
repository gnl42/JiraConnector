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

import com.atlassian.jira.rest.client.domain.CreateIssueFieldInfo;
import org.codehaus.jettison.json.JSONException;
import org.junit.Test;

import java.util.Iterator;
import java.util.Map;

import static org.junit.Assert.*;

/**
 * @since v1.0
 */
public class CreateIssueFieldsInfoMapJsonParserTest {

	@Test
	public void testParseWithArrayOfArrayAllowedValuesBug() throws JSONException {
		final CreateIssueFieldsInfoMapJsonParser parser = new CreateIssueFieldsInfoMapJsonParser();
		final Map<String, CreateIssueFieldInfo> fieldsInfo = parser.parse(
				ResourceUtil.getJsonObjectFromResource("/json/createmeta/fieldsinfo/valid-with-array-of-array-bug.json")
		);

		assertAllowedValuesNotOfType(fieldsInfo.get("customfield_10010").getAllowedValues(), Iterable.class);
		assertAllowedValuesNotOfType(fieldsInfo.get("customfield_10020").getAllowedValues(), Iterable.class);
		assertAllowedValuesNotOfType(fieldsInfo.get("customfield_10021").getAllowedValues(), Iterable.class);
	}

	@Test
	public void testParseWithArrayOfArrayAllowedValuesBugFixed() throws JSONException {
		final CreateIssueFieldsInfoMapJsonParser parser = new CreateIssueFieldsInfoMapJsonParser();
		final Map<String, CreateIssueFieldInfo> fieldsInfo = parser.parse(
				ResourceUtil.getJsonObjectFromResource("/json/createmeta/fieldsinfo/valid-with-array-of-array-bug-fixed.json")
		);

		assertAllowedValuesNotOfType(fieldsInfo.get("customfield_10010").getAllowedValues(), Iterable.class);
		assertAllowedValuesNotOfType(fieldsInfo.get("customfield_10020").getAllowedValues(), Iterable.class);
		assertAllowedValuesNotOfType(fieldsInfo.get("customfield_10021").getAllowedValues(), Iterable.class);
	}

	private void assertAllowedValuesNotOfType(final Iterable<Object> allowedValues, Class type) {
		final Iterator<Object> iterator = allowedValues.iterator();
		assertTrue(iterator.hasNext());
		while (iterator.hasNext()) {
			final Object obj = iterator.next();
			assertNotNull(obj);
			assertFalse(type.isAssignableFrom(obj.getClass()));
		}
	}
}
