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

package me.glindholm.jira.rest.client.internal.json;

import java.net.URI;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import me.glindholm.jira.rest.client.api.domain.IssuelinksType;

public class IssuelinksTypeJsonParserV5 implements JsonObjectParser<IssuelinksType> {
    @Override
    public IssuelinksType parse(final JSONObject json) throws JSONException {
        final URI selfUri = JsonParseUtil.getSelfUri(json);
        final String id = json.getString("id");
        final String name = json.getString("name");
        final String inward = json.getString("inward");
        final String outward = json.getString("outward");

        return new IssuelinksType(selfUri, id, name, inward, outward);
    }
}
