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
import me.glindholm.jira.rest.client.api.IdentifiableEntity;
import me.glindholm.jira.rest.client.api.NamedEntity;

/**
 * Complete information about a single issue type defined in JIRA
 *
 * @since v0.1
 */
public class IssueType implements Serializable, AddressableEntity, NamedEntity, IdentifiableEntity<Long> {
    private static final long serialVersionUID = 1L;

    private final URI self;
    private final Long id;
    private final String name;
    private final boolean isSubtask;
    private final String description;
    private final URI iconUri;

    public IssueType(final URI self, final Long id, final String name, final boolean isSubtask, final String description, final URI iconUri) {
        this.self = self;
        this.id = id;
        this.name = name;
        this.isSubtask = isSubtask;
        this.description = description;
        this.iconUri = iconUri;
    }

    @Override
    public Long getId() {
        return id;
    }

    @Override
    public String getName() {
        return name;
    }

    public boolean isSubtask() {
        return isSubtask;
    }

    @Override
    public URI getSelf() {
        return self;
    }

    public String getDescription() {
        return description;
    }

    public URI getIconUri() {
        return iconUri;
    }

    protected String getToStringHelper() {
        return toString();
    }

    @Override
    public String toString() {
        return "IssueType [self=" + self + ", id=" + id + ", name=" + name + ", isSubtask=" + isSubtask + ", description=" + description + ", iconUri="
                + iconUri + "]";
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj instanceof final IssueType that) {
            return Objects.equals(self, that.self) && Objects.equals(id, that.id) && Objects.equals(name, that.name)
                    && Objects.equals(isSubtask, that.isSubtask) && Objects.equals(description, that.description) && Objects.equals(iconUri, that.iconUri);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(self, id, name, isSubtask, description, iconUri);
    }

}
