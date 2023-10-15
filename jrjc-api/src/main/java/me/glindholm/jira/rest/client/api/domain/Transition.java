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
import java.util.List;
import java.util.Objects;

import me.glindholm.jira.rest.client.api.NamedEntity;

/**
 * Information about selected transition including fields which can or must be set while performing
 * the transition.
 *
 * @since v0.1
 */
public class Transition implements Serializable, NamedEntity {
    private static final long serialVersionUID = 1L;

    private final String name;
    private final int id;
    private final List<Field> fields;

    public Transition(final String name, final int id, final List<Field> fields) {
        this.name = name;
        this.id = id;
        this.fields = fields;
    }

    @Override
    public String getName() {
        return name;
    }

    public int getId() {
        return id;
    }

    public List<Field> getFields() {
        return fields;
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj instanceof final Transition that) {
            return Objects.equals(name, that.name) && Objects.equals(id, that.id) && Objects.equals(fields, that.fields);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, fields);
    }

    @Override
    public String toString() {
        return "Transition [name=" + name + ", id=" + id + ", fields=" + fields + "]";
    }

    public static class Field {
        private final String id;
        private final boolean isRequired;
        private final String type;

        public Field(final String id, final boolean isRequired, final String type) {
            this.id = id;
            this.isRequired = isRequired;
            this.type = type;
        }

        public String getId() {
            return id;
        }

        public boolean isRequired() {
            return isRequired;
        }

        public String getType() {
            return type;
        }

        @Override
        public int hashCode() {
            return Objects.hash(id, isRequired, type);
        }

        @Override
        public boolean equals(final Object obj) {
            if (obj instanceof final Field that) {
                return Objects.equals(id, that.id) && Objects.equals(isRequired, that.isRequired) && Objects.equals(type, that.type);
            }
            return false;
        }

    }
}
