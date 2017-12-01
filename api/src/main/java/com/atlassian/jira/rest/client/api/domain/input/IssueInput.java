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

package com.atlassian.jira.rest.client.api.domain.input;

import com.atlassian.jira.rest.client.api.domain.EntityHelper;
import com.google.common.base.Objects;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Represents new JIRA issue
 *
 * @since v1.0
 */
public class IssueInput {

    private final Map<String, FieldInput> fields;
    private final List<PropertyInput> properties;

    public static IssueInput createWithFields(FieldInput... fields) {
        return new IssueInput(Maps.uniqueIndex(ImmutableList.copyOf(fields), EntityHelper.GET_ENTITY_STRING_ID_FUNCTION), new ArrayList<PropertyInput>());
    }

    public IssueInput(Map<String, FieldInput> fields, List<PropertyInput> properties) {
        this.fields = fields;
        this.properties = properties;
    }

    public Map<String, FieldInput> getFields() {
        return fields;
    }

    public List<PropertyInput> getProperties() {
        return properties;
    }

    @SuppressWarnings("unused")
    public FieldInput getField(String id) {
        return fields.get(id);
    }

    @Override
    public String toString() {
        return Objects.toStringHelper(this)
                .add("fields", fields)
                .add("properties", properties)
                .toString();
    }
}
