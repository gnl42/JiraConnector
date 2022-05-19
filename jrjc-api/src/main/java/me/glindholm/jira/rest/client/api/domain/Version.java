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
import java.time.OffsetDateTime;
import java.util.Objects;

import javax.annotation.Nullable;

import me.glindholm.jira.rest.client.api.AddressableEntity;
import me.glindholm.jira.rest.client.api.NamedEntity;

/**
 * Complete information about a version defined for a JIRA project
 *
 * @since v0.1
 */
public class Version implements Serializable, AddressableEntity, NamedEntity {
    private static final long serialVersionUID = 1L;

    private final URI self;
    @Nullable
    private final Long id;
    private final String description;
    private final String name;
    private final boolean isArchived;
    private final boolean isReleased;
    @Nullable
    private final OffsetDateTime releaseDate;

    public Version(URI self, @Nullable Long id, String name, String description, boolean archived, boolean released, @Nullable OffsetDateTime releaseDate) {
        this.self = self;
        this.id = id;
        this.description = description;
        this.name = name;
        isArchived = archived;
        isReleased = released;
        this.releaseDate = releaseDate;
    }

    @Override
    public URI getSelf() {
        return self;
    }

    @Nullable
    public Long getId() {
        return id;
    }

    public String getDescription() {
        return description;
    }

    @Override
    public String getName() {
        return name;
    }

    public boolean isArchived() {
        return isArchived;
    }

    public boolean isReleased() {
        return isReleased;
    }

    @Nullable
    public OffsetDateTime getReleaseDate() {
        return releaseDate;
    }

    @Override
    public String toString() {
        return "Version [self=" + self + ", id=" + id + ", description=" + description + ", name=" + name + ", isArchived=" + isArchived + ", isReleased="
                + isReleased + "]";
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Version) {
            Version that = (Version) obj;
            return Objects.equals(this.self, that.self)
                    && Objects.equals(this.id, that.id)
                    && Objects.equals(this.name, that.name)
                    && Objects.equals(this.description, that.description)
                    && Objects.equals(this.isArchived, that.isArchived)
                    && Objects.equals(this.isReleased, that.isReleased)
                    && Objects.equals(this.releaseDate, that.releaseDate);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(self, id, name, description, isArchived, isReleased, releaseDate);
    }

}
