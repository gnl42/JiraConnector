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

package me.glindholm.jira.rest.client.api.domain.input;

import java.io.Serializable;
import java.util.Objects;

import me.glindholm.jira.rest.client.api.IdentifiableEntity;
import me.glindholm.jira.rest.client.api.domain.IssueFieldId;

/**
 * New value for selected field - used while changing issue fields - e.g. while transitioning issue.
 *
 * @since v0.1
 */
public class FieldInput implements Serializable, IdentifiableEntity<String> {
    private static final long serialVersionUID = 1L;

    private final String id;
    private final Object value;

    /**
     * @param id    field id
     * @param value new value for this issue field
     */
    public FieldInput(final String id, final Object value) {
        this.id = id;
        this.value = value;
    }

    /**
     * @param field issue field
     * @param value new value for this issue field
     */
    public FieldInput(final IssueFieldId field, final Object value) {
        id = field.id;
        this.value = value;
    }

    /**
     * @return field id
     */
    @Override
    public String getId() {
        return id;
    }

    /**
     * @return new value for this issue field
     */
    public Object getValue() {
        return value;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, value);
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj instanceof final FieldInput other) {
            return Objects.equals(id, other.id) && Objects.equals(value, other.value);
        }
        return false;
    }

    @Override
    public String toString() {
        return "FieldInput [id=" + id + ", value=" + value + "]";
    }
}
