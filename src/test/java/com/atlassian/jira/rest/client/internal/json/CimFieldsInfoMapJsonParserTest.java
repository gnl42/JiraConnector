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

import com.atlassian.jira.rest.client.domain.CimFieldInfo;
import org.codehaus.jettison.json.JSONException;
import org.hamcrest.Matchers;
import org.junit.Test;
import org.junit.matchers.JUnitMatchers;

import java.util.Map;

import static org.junit.Assert.*;

/**
 * @since v1.0
 */
public class CimFieldsInfoMapJsonParserTest {

	@Test
	public void testParseWithArrayOfArrayAllowedValuesBug() throws JSONException {
		final CimFieldsInfoMapJsonParser parser = new CimFieldsInfoMapJsonParser();
		final Map<String, CimFieldInfo> fieldsInfo = parser.parse(
				ResourceUtil.getJsonObjectFromResource("/json/createmeta/fieldsinfo/valid-with-array-of-array-bug.json")
		);

		assertElementsNotIterable(fieldsInfo.get("customfield_10010").getAllowedValues());
		assertElementsNotIterable(fieldsInfo.get("customfield_10020").getAllowedValues());
		assertElementsNotIterable(fieldsInfo.get("customfield_10021").getAllowedValues());
	}

	@Test
	public void testParseWithArrayOfArrayAllowedValuesBugFixed() throws JSONException {
		final CimFieldsInfoMapJsonParser parser = new CimFieldsInfoMapJsonParser();
		final Map<String, CimFieldInfo> fieldsInfo = parser.parse(
				ResourceUtil.getJsonObjectFromResource("/json/createmeta/fieldsinfo/valid-with-array-of-array-bug-fixed.json")
		);

		assertElementsNotIterable(fieldsInfo.get("customfield_10010").getAllowedValues());
		assertElementsNotIterable(fieldsInfo.get("customfield_10020").getAllowedValues());
		assertElementsNotIterable(fieldsInfo.get("customfield_10021").getAllowedValues());
	}

	private void assertElementsNotIterable(final Iterable<Object> allowedValues) {
		assertThat(allowedValues, JUnitMatchers.everyItem(Matchers.not(Matchers.instanceOf(Iterable.class))));
	}
}
