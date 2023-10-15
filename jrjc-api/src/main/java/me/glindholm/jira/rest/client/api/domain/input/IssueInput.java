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

package me.glindholm.jira.rest.client.api.domain.input;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Represents new JIRA issue
 *
 * @since v1.0
 */
public class IssueInput {

    private final Map<String, FieldInput> fields;
    private final List<PropertyInput> properties;

    public static IssueInput createWithFields(final FieldInput... fields) {
        return new IssueInput(Map.copyOf(List.of(fields).stream().collect(Collectors.toMap(FieldInput::getId, Function.identity()))),
                new ArrayList<>());
    }

    public IssueInput(final Map<String, FieldInput> fields, final List<PropertyInput> properties) {
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
    public FieldInput getField(final String id) {
        return fields.get(id);
    }

    @Override
    public String toString() {
        return "IssueInput [fields=" + fields + ", properties=" + properties + "]";
    }
}
