/*
 * Copyright (C) 2012-2013 Atlassian
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

import com.atlassian.jira.rest.client.api.domain.CimFieldInfo;
import com.atlassian.jira.rest.client.api.domain.Resolution;
import com.atlassian.jira.rest.client.api.domain.SecurityLevel;
import org.codehaus.jettison.json.JSONException;
import org.hamcrest.Matchers;
import org.hamcrest.collection.IsIterableContainingInAnyOrder;
import org.junit.Test;
import org.junit.matchers.JUnitMatchers;

import java.net.URI;
import java.util.Map;

import static com.atlassian.jira.rest.client.internal.json.ResourceUtil.getJsonObjectFromResource;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.*;

/**
 * @since v1.0
 */
public class CimFieldsInfoMapJsonParserTest {

	@Test
	public void testParseWithArrayOfArrayAllowedValuesBug() throws JSONException {
		final CimFieldsInfoMapJsonParser parser = new CimFieldsInfoMapJsonParser();
		final Map<String, CimFieldInfo> fieldsInfo = parser.parse(
				getJsonObjectFromResource("/json/createmeta/fieldsinfo/valid-with-array-of-array-bug.json")
		);

		assertElementsNotIterable(fieldsInfo.get("customfield_10010").getAllowedValues());
		assertElementsNotIterable(fieldsInfo.get("customfield_10020").getAllowedValues());
		assertElementsNotIterable(fieldsInfo.get("customfield_10021").getAllowedValues());
	}

	@Test
	public void testParseWithAllowedValuesForSecurityLevels() throws JSONException {
		final CimFieldsInfoMapJsonParser parser = new CimFieldsInfoMapJsonParser();
		final Map<String, CimFieldInfo> fieldsInfo = parser.parse(
				getJsonObjectFromResource("/json/createmeta/fieldsinfo/valid-with-all-issue-types.json")
		);

		assertThat(fieldsInfo.get("security").getAllowedValues(), IsIterableContainingInAnyOrder.<Object>containsInAnyOrder(
				new SecurityLevel(URI.create("http://localhost:2990/jira/rest/api/2/securitylevel/10000"), 10000L,
						"internal", "For internals only"),
				new SecurityLevel(URI.create("http://localhost:2990/jira/rest/api/2/securitylevel/10001"), 10001L,
						"public", "For everyone")
		));
	}

	@Test
	public void testParseWithAllowedValuesForResolution() throws JSONException {
		final CimFieldsInfoMapJsonParser parser = new CimFieldsInfoMapJsonParser();
		final Map<String, CimFieldInfo> fieldsInfo = parser.parse(
				getJsonObjectFromResource("/json/createmeta/fieldsinfo/valid-with-all-issue-types.json")
		);

		assertThat(fieldsInfo.get("resolution").getAllowedValues(), IsIterableContainingInAnyOrder.<Object>containsInAnyOrder(
				new Resolution(URI.create("http://localhost:2990/jira/rest/api/latest/resolution/1"), 1L, "Fixed", null),
				new Resolution(URI.create("http://localhost:2990/jira/rest/api/latest/resolution/2"), 2L, "Won't Fix", null),
				new Resolution(URI.create("http://localhost:2990/jira/rest/api/latest/resolution/3"), 3L, "Duplicate", null),
				new Resolution(URI.create("http://localhost:2990/jira/rest/api/latest/resolution/4"), 4L, "Incomplete", null),
				new Resolution(URI.create("http://localhost:2990/jira/rest/api/latest/resolution/5"), 5L, "Cannot Reproduce", null)
		));
	}

	@Test
	public void testParseWithArrayOfArrayAllowedValuesBugFixed() throws JSONException {
		final CimFieldsInfoMapJsonParser parser = new CimFieldsInfoMapJsonParser();
		final Map<String, CimFieldInfo> fieldsInfo = parser.parse(
				getJsonObjectFromResource("/json/createmeta/fieldsinfo/valid-with-array-of-array-bug-fixed.json")
		);

		assertElementsNotIterable(fieldsInfo.get("customfield_10010").getAllowedValues());
		assertElementsNotIterable(fieldsInfo.get("customfield_10020").getAllowedValues());
		assertElementsNotIterable(fieldsInfo.get("customfield_10021").getAllowedValues());
	}

	@Test
	public void testParseWithAllIssueTypesShouldReturnFieldsWithNames() throws JSONException {
		final CimFieldsInfoMapJsonParser parser = new CimFieldsInfoMapJsonParser();
		final Map<String, CimFieldInfo> fieldsInfo = parser.parse(
				getJsonObjectFromResource("/json/createmeta/fieldsinfo/valid-with-all-issue-types.json")
		);

		assertThat(fieldsInfo.get("summary").getName(), equalTo("Summary"));
		assertThat(fieldsInfo.get("security").getName(), equalTo("Security Level"));
		assertThat(fieldsInfo.get("timetracking").getName(), equalTo("Time Tracking"));
		assertThat(fieldsInfo.get("issuetype").getName(), equalTo("Issue Type"));
		assertThat(fieldsInfo.get("customfield_10138").getName(), equalTo("Single Version Picker CF"));
		assertThat(fieldsInfo.get("customfield_10137").getName(), equalTo("Read-only Text Field CF"));
		assertThat(fieldsInfo.get("fixVersions").getName(), equalTo("Fix Version/s"));
		assertThat(fieldsInfo.get("customfield_10139").getName(), equalTo("URL Field CF"));
		assertThat(fieldsInfo.get("resolution").getName(), equalTo("Resolution"));
		assertThat(fieldsInfo.get("customfield_10122").getName(), equalTo("Free Text Field (unlimited text) CF"));
		assertThat(fieldsInfo.get("reporter").getName(), equalTo("Reporter"));
		assertThat(fieldsInfo.get("customfield_10140").getName(), equalTo("Version Picker CF"));
		assertThat(fieldsInfo.get("customfield_10123").getName(), equalTo("Hidden Job Switch CF"));
		assertThat(fieldsInfo.get("customfield_10124").getName(), equalTo("Job Checkbox CF"));
		assertThat(fieldsInfo.get("customfield_10125").getName(), equalTo("Multi Checkboxes CF"));
		assertThat(fieldsInfo.get("customfield_10120").getName(), equalTo("Bug Import Id CF"));
		assertThat(fieldsInfo.get("priority").getName(), equalTo("Priority"));
		assertThat(fieldsInfo.get("description").getName(), equalTo("Description"));
		assertThat(fieldsInfo.get("customfield_10121").getName(), equalTo("Date Picker CF"));
		assertThat(fieldsInfo.get("customfield_10001").getName(), equalTo("My Radio buttons"));
		assertThat(fieldsInfo.get("duedate").getName(), equalTo("Due Date"));
		assertThat(fieldsInfo.get("customfield_10020").getName(), equalTo("Extra User"));
		assertThat(fieldsInfo.get("issuelinks").getName(), equalTo("Linked Issues"));
		assertThat(fieldsInfo.get("worklog").getName(), equalTo("Log Work"));
		assertThat(fieldsInfo.get("customfield_10000").getName(), equalTo("My Number Field New"));
		assertThat(fieldsInfo.get("labels").getName(), equalTo("Labels"));
		assertThat(fieldsInfo.get("assignee").getName(), equalTo("Assignee"));
		assertThat(fieldsInfo.get("attachment").getName(), equalTo("Attachment"));
		assertThat(fieldsInfo.get("customfield_10129").getName(), equalTo("Cascading Select CF"));
		assertThat(fieldsInfo.get("customfield_10128").getName(), equalTo("Text Field (< 255 characters) CF"));
		assertThat(fieldsInfo.get("customfield_10127").getName(), equalTo("Select List CF"));
		assertThat(fieldsInfo.get("customfield_10126").getName(), equalTo("Multi Select CF"));
		assertThat(fieldsInfo.get("customfield_10135").getName(), equalTo("Multi User Picker CF"));
		assertThat(fieldsInfo.get("versions").getName(), equalTo("Affects Version/s"));
		assertThat(fieldsInfo.get("customfield_10136").getName(), equalTo("Project Picker CF"));
		assertThat(fieldsInfo.get("project").getName(), Matchers.nullValue());
		assertThat(fieldsInfo.get("customfield_10133").getName(), equalTo("Labels CF"));
		assertThat(fieldsInfo.get("environment").getName(), equalTo("Environment"));
		assertThat(fieldsInfo.get("customfield_10134").getName(), equalTo("Multi Group Picker CF"));
		assertThat(fieldsInfo.get("customfield_10131").getName(), equalTo("Group Picker CF"));
		assertThat(fieldsInfo.get("customfield_10132").getName(), equalTo("Import Id CF"));
		assertThat(fieldsInfo.get("customfield_10130").getName(), equalTo("Date Time CF"));
		assertThat(fieldsInfo.get("components").getName(), equalTo("Component/s"));
		assertThat(fieldsInfo.get("customfield_10010").getName(), equalTo("project3"));
		assertThat(fieldsInfo.get("customfield_10011").getName(), equalTo("project2"));
	}

	private void assertElementsNotIterable(final Iterable<Object> allowedValues) {
		assertThat(allowedValues, JUnitMatchers.everyItem(Matchers.not(Matchers.instanceOf(Iterable.class))));
	}
}
