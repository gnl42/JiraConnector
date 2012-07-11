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
import com.atlassian.jira.rest.client.domain.CreateIssueIssueType;
import com.atlassian.jira.rest.client.domain.IssueType;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import java.util.Collections;
import java.util.Map;

/**
 * JSON parser for CreateIssueIssueType
 *
 * @since v1.0
 */
public class CreateIssueIssueTypeJsonParser implements JsonParser<CreateIssueIssueType> {

	final IssueTypeJsonParser issueTypeJsonParser = new IssueTypeJsonParser();
	final CreateIssueFieldsInfoMapJsonParser fieldsParser = new CreateIssueFieldsInfoMapJsonParser();

	@Override
	public CreateIssueIssueType parse(final JSONObject json) throws JSONException {
		final IssueType issueType = issueTypeJsonParser.parse(json);
		final JSONObject jsonFieldsMap = json.optJSONObject("fields");
		
		final Map<String, CreateIssueFieldInfo> fields = (jsonFieldsMap == null) ?
				Collections.<String, CreateIssueFieldInfo>emptyMap() : fieldsParser.parse(jsonFieldsMap);

		return new CreateIssueIssueType(issueType.getSelf(), issueType.getId(), issueType.getName(),
				issueType.isSubtask(), issueType.getDescription(), issueType.getIconUri(), fields);
	}
}
