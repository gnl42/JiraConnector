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

import me.glindholm.jira.rest.client.api.AddressableEntity;
import me.glindholm.jira.rest.client.api.NamedEntity;

/**
 * Any resource which is addressable (has "self" URI) and has a name.
 *
 * @since v0.1
 */
public class AddressableNamedEntity implements Serializable, AddressableEntity, NamedEntity {
    private static final long serialVersionUID = 1L;

    protected final URI self;
    protected final String name;

    public AddressableNamedEntity(final URI self, final String name) {
        this.name = name;
        this.self = self;
    }

    @Override
    public URI getSelf() {
        return self;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return "AddressableNamedEntity [self=" + self + ", name=" + name + "]";
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj instanceof final AddressableNamedEntity that) {
            return Objects.equals(self, that.self) && Objects.equals(name, that.name);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(self, name);
    }
}
