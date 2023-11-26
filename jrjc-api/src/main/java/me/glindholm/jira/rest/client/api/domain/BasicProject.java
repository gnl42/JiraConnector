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
import java.net.URI;
import java.util.Objects;

import org.eclipse.jdt.annotation.Nullable;

import me.glindholm.jira.rest.client.api.AddressableEntity;
import me.glindholm.jira.rest.client.api.NamedEntity;

/**
 * Basic information about a JIRA project
 *
 * @since v0.1
 */
public class BasicProject implements Serializable, AddressableEntity, NamedEntity {
    private static final long serialVersionUID = 1L;

    private final URI self;
    private final String key;
    @Nullable
    private final Long id;
    @Nullable
    private final String name;

    public BasicProject(final URI self, final String key, @Nullable final Long id, final @Nullable String name) {
        this.self = self;
        this.key = key;
        this.id = id;
        this.name = name;
    }

    @Override
    public URI getSelf() {
        return self;
    }

    public String getKey() {
        return key;
    }

    @Override
    @Nullable
    public String getName() {
        return name;
    }

    @Nullable
    public Long getId() {
        return id;
    }

    @Override
    public String toString() {
        return "BasicProject [self=" + self + ", key=" + key + ", id=" + id + ", name=" + name + "]";
    }

    protected String getToStringHelper() {
        return toString();
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj instanceof final BasicProject that) {
            return Objects.equals(self, that.self) && Objects.equals(name, that.name) && Objects.equals(id, that.id) && Objects.equals(key, that.key);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(self, name, id, key);
    }

}
