/*
 * Copyright (C) 2011 Atlassian
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package me.glindholm.jira.rest.client.internal.json;

import java.net.URISyntaxException;
import java.util.Collections;
import java.util.List;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import me.glindholm.jira.rest.client.api.domain.Issue;
import me.glindholm.jira.rest.client.api.domain.SearchResult;

public class SearchResultJsonParser implements JsonObjectParser<SearchResult> {

    @Override
    public SearchResult parse(final JSONObject json) throws JSONException, URISyntaxException {
        final int startAt = json.optInt("startAt",0);
        final int maxResults = json.optInt("maxResults", 9999999);
        int total = json.optInt("total", -1);
        final JSONArray issuesJsonArray = json.getJSONArray("issues");

        final List<Issue> issues;
        if (issuesJsonArray.length() > 0) {
            final IssueJsonParser issueParser = new IssueJsonParser(json.getJSONObject("names"), json.getJSONObject("schema"));
            final GenericJsonArrayParser<Issue> issuesParser = GenericJsonArrayParser.create(issueParser);
            issues = issuesParser.parse(issuesJsonArray);
            total = issues.size();
        } else {
            issues = Collections.emptyList();
            total = 0;
        }
        return new SearchResult(startAt, maxResults, total, issues);
    }
}
