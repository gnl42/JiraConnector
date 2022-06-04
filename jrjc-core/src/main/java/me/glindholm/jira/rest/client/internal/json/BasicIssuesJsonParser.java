/*
 * Copyright (C) 2012 Atlassian
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
import java.util.List;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import me.glindholm.jira.rest.client.api.domain.BasicIssue;
import me.glindholm.jira.rest.client.api.domain.BulkOperationErrorResult;
import me.glindholm.jira.rest.client.api.domain.BulkOperationResult;

/**
 * @since 1.1
 */
public class BasicIssuesJsonParser implements JsonObjectParser<BulkOperationResult<BasicIssue>> {

    @Override
    public BulkOperationResult<BasicIssue> parse(final JSONObject json) throws JSONException, URISyntaxException {
        final List<BasicIssue> issues =
                JsonParseUtil.parseJsonArray(json.getJSONArray("issues"), new BasicIssueJsonParser());

        final List<BulkOperationErrorResult> errors =
                JsonParseUtil.parseJsonArray(json.getJSONArray("errors"), new IssueErrorJsonParser());

        return new BulkOperationResult<>(issues, errors);
    }


}
