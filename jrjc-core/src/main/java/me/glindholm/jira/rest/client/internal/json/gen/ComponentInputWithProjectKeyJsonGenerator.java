/*
 * Copyright (C) 2011 Atlassian
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

package me.glindholm.jira.rest.client.internal.json.gen;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import me.glindholm.jira.rest.client.api.RestClientException;
import me.glindholm.jira.rest.client.api.domain.AssigneeType;
import me.glindholm.jira.rest.client.internal.domain.AssigneeTypeConstants;
import me.glindholm.jira.rest.client.internal.domain.input.ComponentInputWithProjectKey;

public class ComponentInputWithProjectKeyJsonGenerator implements JsonGenerator<ComponentInputWithProjectKey> {

    @Override
    public JSONObject generate(final ComponentInputWithProjectKey componentInput) throws JSONException {
        final JSONObject res = new JSONObject();
        if (componentInput.getProjectKey() != null) {
            res.put("project", componentInput.getProjectKey());
        }
        if (componentInput.getName() != null) {
            res.put("name", componentInput.getName());
        }
        if (componentInput.getDescription() != null) {
            res.put("description", componentInput.getDescription());
        }
        if (componentInput.getLeadUsername() != null) {
            res.put("leadUserName", componentInput.getLeadUsername());
        }
        final AssigneeType assigneeType = componentInput.getAssigneeType();
        if (assigneeType != null) {
            final String assigneeTypeStr = switch (assigneeType) {
            case PROJECT_DEFAULT -> AssigneeTypeConstants.PROJECT_DEFAULT;
            case COMPONENT_LEAD -> AssigneeTypeConstants.COMPONENT_LEAD;
            case PROJECT_LEAD -> AssigneeTypeConstants.PROJECT_LEAD;
            case UNASSIGNED -> AssigneeTypeConstants.UNASSIGNED;
            default -> throw new RestClientException("Unexpected assignee type [" + assigneeType + "]", null);
            };
            res.put("assigneeType", assigneeTypeStr);
        }
        return res;
    }
}
