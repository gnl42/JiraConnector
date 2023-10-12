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
 * Basic information about a project component
 *
 * @since v0.1
 */
public class BasicComponent implements Serializable, AddressableEntity, NamedEntity {
    private static final long serialVersionUID = 1L;

    @Nullable
    private final Long id;
    private final URI self;
    private final String name;
    @Nullable
    private final String description;

    public BasicComponent(final URI self, @Nullable final Long id, final String name, @Nullable final String description) {
        this.self = self;
        this.id = id;
        this.name = name;
        this.description = description;
    }

    @Override
    public URI getSelf() {
        return self;
    }

    @Override
    public String getName() {
        return name;
    }

    @Nullable
    public Long getId() {
        return id;
    }

    /**
     * @return optional description for this project (as defined by the project
     *         admin)
     */
    @Nullable
    public String getDescription() {
        return description;
    }

    @Override
    public String toString() {
        return "BasicComponent [id=" + id + ", self=" + self + ", name=" + name + ", description=" + description + "]";
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj instanceof BasicComponent) {
            final BasicComponent that = (BasicComponent) obj;
            return Objects.equals(self, that.self) && Objects.equals(id, that.id) && Objects.equals(name, that.name)
                    && Objects.equals(description, that.description);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(self, name, description);
    }

}
