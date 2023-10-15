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

package me.glindholm.jira.rest.client.api.domain;

import java.io.Serializable;
import java.util.Objects;

import me.glindholm.jira.rest.client.api.IdentifiableEntity;
import me.glindholm.jira.rest.client.api.NamedEntity;

/**
 * JIRA issue field with its current value.
 *
 * @since v0.1
 */
public class IssueField implements Serializable, NamedEntity, IdentifiableEntity<String> {
    private static final long serialVersionUID = 1L;

    private final String id;
    private final String name;
    private final String type;
    private final Object value;

    public IssueField(final String id, final String name, final String type, final Object value) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.value = value;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }

    public Object getValue() {
        return value;
    }

    @Override
    public String toString() {
        return "IssueField [id=" + id + ", name=" + name + ", type=" + type + ", value=" + value + "]";
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, type); // for the sake of performance we don't include "value" field here
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj instanceof final IssueField that) {
            return Objects.equals(id, that.id) && Objects.equals(name, that.name) && Objects.equals(type, that.type) && Objects.equals(value, that.value);
        }
        return false;
    }

}
