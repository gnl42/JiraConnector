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

package com.atlassian.jira.restjavaclient.json;

import com.atlassian.jira.restjavaclient.domain.User;
import com.atlassian.jira.restjavaclient.domain.Worklog;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.joda.time.DateTime;

import java.net.URI;

/**
 * TODO: Document this class / interface here
 *
 * @since v0.1
 */
public class WorklogJsonParser {
	public Worklog parseWorklog(JSONObject json) throws JSONException {
		final URI self = JsonParseUtil.getSelfUri(json);
		final URI issueUri = JsonParseUtil.parseURI(json.getString("issue"));
		final User author = JsonParseUtil.parseUser(json.getJSONObject("author"));
		final User updateAuthor = JsonParseUtil.parseUser(json.getJSONObject("updateAuthor"));
		final String comment = json.getString("comment");
		final DateTime creationDate = JsonParseUtil.parseDateTime(json.getString("created"));
		final DateTime updateDate = JsonParseUtil.parseDateTime(json.getString("updated"));
		final DateTime startDate = JsonParseUtil.parseDateTime(json.getString("startDate"));
		final int minutesSpent = json.getInt("timeSpent");
		return new Worklog(self, issueUri, author, updateAuthor, comment, creationDate, updateDate, startDate, minutesSpent);
	}
}
