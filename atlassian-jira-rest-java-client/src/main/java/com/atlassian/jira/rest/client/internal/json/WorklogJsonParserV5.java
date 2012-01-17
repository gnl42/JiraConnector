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

import com.atlassian.jira.rest.client.domain.BasicUser;
import com.atlassian.jira.rest.client.domain.Visibility;
import com.atlassian.jira.rest.client.domain.Worklog;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.joda.time.DateTime;

import java.net.URI;

public class WorklogJsonParserV5 implements JsonParser<Worklog> {

	private final URI issue;

	public WorklogJsonParserV5(URI issue) {
		this.issue = issue;
	}


	@Override
	public Worklog parse(JSONObject json) throws JSONException {
		final URI self = JsonParseUtil.getSelfUri(json);
		final BasicUser author = JsonParseUtil.parseBasicUser(json.getJSONObject("author"));
		final BasicUser updateAuthor = JsonParseUtil.parseBasicUser(json.getJSONObject("updateAuthor"));
		final String comment = json.getString("comment");
		final DateTime creationDate = JsonParseUtil.parseDateTime(json, "created");
		final DateTime updateDate = JsonParseUtil.parseDateTime(json, "updated");
		final DateTime startDate = JsonParseUtil.parseDateTime(json, "started");
		final int secondsSpent = json.getInt("timeSpentSeconds");
		final Visibility visibility = new VisibilityJsonParser().parseVisibility(json);
        return new Worklog(self, issue, author, updateAuthor, comment, creationDate, updateDate, startDate, secondsSpent / 60, visibility);
	}
}
