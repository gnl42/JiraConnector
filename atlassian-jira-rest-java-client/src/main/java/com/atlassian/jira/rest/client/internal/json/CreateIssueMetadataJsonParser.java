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

import com.atlassian.jira.rest.client.domain.CreateIssueMetadata;
import com.atlassian.jira.rest.client.domain.CreateIssueMetadataProject;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

/**
 * JSON parser for CreateIssueMetadata.
 *
 * @since v1.0
 */
public class CreateIssueMetadataJsonParser implements JsonParser<CreateIssueMetadata>{

	private final GenericJsonArrayParser<CreateIssueMetadataProject> projectsParser = new GenericJsonArrayParser<CreateIssueMetadataProject>(new CreateIssueMetadataProjectJsonParser());

	@Override
	public CreateIssueMetadata parse(final JSONObject json) throws JSONException {
		final Iterable<CreateIssueMetadataProject> projects = projectsParser.parse(json.getJSONArray("projects"));
		return new CreateIssueMetadata(projects);
	}
}
