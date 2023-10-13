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

package me.glindholm.jira.rest.client.internal.json;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import me.glindholm.jira.rest.client.api.domain.BasicProject;
import me.glindholm.jira.rest.client.api.domain.CimIssueType;
import me.glindholm.jira.rest.client.api.domain.CimProject;

/**
 * JSON parser for CimProject
 *
 * @since v1.0
 */
public class CimProjectJsonParser implements JsonObjectParser<CimProject> {

    private final JsonArrayParser<List<CimIssueType>> issueTypesParser = GenericJsonArrayParser
            .create(new CimIssueTypeJsonParser());

    private final BasicProjectJsonParser basicProjectJsonParser = new BasicProjectJsonParser();

    @Override
    public CimProject parse(final JSONObject json) throws JSONException, URISyntaxException {
        final BasicProject basicProject = basicProjectJsonParser.parse(json);
        final JSONArray issueTypesArray = json.optJSONArray("issuetypes");
        final List<CimIssueType> issueTypes = issueTypesArray != null ?
                issueTypesParser.parse(issueTypesArray) : Collections.emptyList();

        final Map<String, URI> avatarUris = JsonParseUtil.getAvatarUris(json.getJSONObject("avatarUrls"));
        return new CimProject(basicProject.getSelf(), basicProject.getKey(), basicProject.getId(),
                basicProject.getName(), avatarUris, issueTypes);
    }
}
