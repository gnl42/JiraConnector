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
package me.glindholm.jira.rest.client.api.domain;

import java.io.Serializable;
import java.util.Objects;

import org.eclipse.jdt.annotation.Nullable;

import me.glindholm.jira.rest.client.api.IdentifiableEntity;
import me.glindholm.jira.rest.client.api.NamedEntity;

/**
 * Representation of JIRA field, either system or custom.
 */
public class Field implements Serializable, NamedEntity, IdentifiableEntity<String> {
    private static final long serialVersionUID = 1L;

    private final String id;
    private final String name;
    private final FieldType fieldType;
    private final boolean orderable;
    private final boolean navigable;
    private final boolean searchable;
    @Nullable
    private final FieldSchema schema;

    public Field(final String id, final String name, final FieldType fieldType, final boolean orderable, final boolean navigable, final boolean searchable,
            @Nullable final FieldSchema schema) {
        this.id = id;
        this.name = name;
        this.fieldType = fieldType;
        this.orderable = orderable;
        this.navigable = navigable;
        this.searchable = searchable;
        this.schema = schema;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public String getName() {
        return name;
    }

    @SuppressWarnings("unused")
    public FieldType getFieldType() {
        return fieldType;
    }

    @SuppressWarnings("unused")
    public boolean isOrderable() {
        return orderable;
    }

    @SuppressWarnings("unused")
    public boolean isNavigable() {
        return navigable;
    }

    @SuppressWarnings("unused")
    public boolean isSearchable() {
        return searchable;
    }

    @Nullable
    @SuppressWarnings("unused")
    public FieldSchema getSchema() {
        return schema;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, fieldType, orderable, navigable, searchable, schema);
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj instanceof final Field that) {
            return Objects.equals(id, that.id) && Objects.equals(name, that.name) && Objects.equals(fieldType, that.fieldType)
                    && Objects.equals(orderable, that.orderable) && Objects.equals(navigable, that.navigable) && Objects.equals(searchable, that.searchable)
                    && Objects.equals(schema, that.schema);
        }
        return false;
    }

    protected String getToStringHelper() {
        return toString();
    }

    @Override
    public String toString() {
        return "Field [id=" + id + ", name=" + name + ", fieldType=" + fieldType + ", orderable=" + orderable + ", navigable=" + navigable + ", searchable="
                + searchable + ", schema=" + schema + "]";
    }
}
