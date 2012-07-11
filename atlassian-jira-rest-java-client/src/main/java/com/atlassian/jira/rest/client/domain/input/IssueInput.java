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

package com.atlassian.jira.rest.client.domain.input;

import com.google.common.base.Objects;
import com.google.common.collect.Maps;

import java.util.Map;

/**
 * Represents JIRA issue
 *
 * @since v1.0
 */
public class IssueInput {

	public static final String SUMMARY_FIELD = "summary";
	public static final String PROJECT_FIELD = "project";
	public static final String ISSUE_TYPE_FIELD = "issuetype";
	public static final String DESCRIPTION_FIELD = "description";
	public static final String ASSIGNEE_FIELD = "assignee";
	public static final String VERSIONS_FIELD = "versions";
	public static final String COMPONENTS_FIELD = "components";
	public static final String DUE_DATE_FIELD = "duedate";
	public static final String FIX_VERSIONS_FIELD = "fixVersions";
	public static final String PRIORITY_FIELD = "priority";
	public static final String REPORTER_FIELD = "reporter";

	private final Map<String, FieldInput> fields;

	public static IssueInput createWithFields(FieldInput ... fields) {
		final Map<String, FieldInput> fieldsMap = Maps.newHashMapWithExpectedSize(fields.length);
		for (FieldInput fi : fields) {
			fieldsMap.put(fi.getId(), fi);
		}
		return new IssueInput(fieldsMap);
	}

	public IssueInput(Map<String, FieldInput> fields) {
		this.fields = fields;
	}

	public Map<String, FieldInput> getFields() {
		return fields;
	}

	public FieldInput getField(String id) {
		return fields.get(id);
	}

	@Override
	public String toString() {
		return Objects.toStringHelper(this)
				.add("fields", fields)
				.toString();
	}
}
