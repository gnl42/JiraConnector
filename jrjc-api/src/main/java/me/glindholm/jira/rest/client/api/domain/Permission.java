/*
 * Copyright (C) 2014 Atlassian
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
import java.util.function.Function;

import javax.annotation.Nullable;

import me.glindholm.jira.rest.client.api.IdentifiableEntity;
import me.glindholm.jira.rest.client.api.NamedEntity;

public class Permission implements Serializable, NamedEntity, IdentifiableEntity<Integer> {
    private static final long serialVersionUID = 1L;

    private final Integer id;
    private final String key;
    private final String name;
    @Nullable
    private final String description;
    private final boolean havePermission;

    public Permission(final int id, final String key, final String name, @Nullable final String description,
            final boolean havePermission) {
        this.id = id;
        this.key = key;
        this.name = name;
        this.description = description;
        this.havePermission = havePermission;
    }

    @Override
    public Integer getId() {
        return id;
    }

    public String getKey() {
        return key;
    }

    @Override
    public String getName() {
        return name;
    }

    @Nullable
    public String getDescription() {
        return description;
    }

    public boolean havePermission() {
        return havePermission;
    }

    protected String getToStringHelper() {
        return toString();
    }

    @Override
    public String toString() {
        return "Permission [id=" + id + ", key=" + key + ", name=" + name + ", description=" + description + ", havePermission=" + havePermission + "]";
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof Permission) {
            Permission that = (Permission) o;
            return id == that.id
                    && Objects.equals(key, that.key)
                    && Objects.equals(name, that.name)
                    && Objects.equals(description, that.description)
                    && havePermission == that.havePermission;
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, key, name, description, havePermission);
    }

    public static final Function<Permission, String> TO_KEY = new Function<>() {
        @Override
        public String apply(final Permission input) {
            return input.getKey();
        }
    };
}
