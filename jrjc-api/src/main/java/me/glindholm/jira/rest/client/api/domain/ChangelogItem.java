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


import java.util.Objects;

import org.eclipse.jdt.annotation.Nullable;

/**
 * Represents single item in Issue change history.
 *
 * @since 0.6
 */
public class ChangelogItem {
    private final FieldType fieldType;
    private final String field;
    private final String from;
    private final String fromString;
    private final String to;
    private final String toString;

    public ChangelogItem(FieldType fieldType, String field, String from, String fromString, String to, String toString) {
        this.fieldType = fieldType;
        this.field = field;
        this.from = from;
        this.fromString = fromString;
        this.to = to;
        this.toString = toString;
    }

    public FieldType getFieldType() {
        return fieldType;
    }

    public String getField() {
        return field;
    }

    @Nullable
    public String getFrom() {
        return from;
    }

    @Nullable
    public String getFromString() {
        return fromString;
    }

    @Nullable
    public String getTo() {
        return to;
    }

    @Nullable
    public String getToString() {
        return toString;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof ChangelogItem) {
            ChangelogItem that = (ChangelogItem) obj;
            return Objects.equals(this.fieldType, that.fieldType)
                    && Objects.equals(this.field, that.field)
                    && Objects.equals(this.from, that.from)
                    && Objects.equals(this.fromString, that.fromString)
                    && Objects.equals(this.to, that.to)
                    && Objects.equals(this.toString, that.toString);
        }
        return false;

    }

    @Override
    public int hashCode() {
        return Objects.hash(fieldType, field, from, fromString, to, toString);
    }

    @Override
    public String toString() {
        return "ChangelogItem [fieldType=" + fieldType + ", field=" + field + ", from=" + from + ", fromString=" + fromString + ", to=" + to + ", toString="
                + toString + "]";
    }

}
