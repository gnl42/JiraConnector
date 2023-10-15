/*
 * Copyright (C) 2011 Atlassian
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

import java.time.OffsetDateTime;

import me.glindholm.jira.rest.client.api.domain.Version;

public class VersionInputBuilder {
    private final String projectKey;
    private String name;
    private String description;
    private OffsetDateTime releaseDate;
    private boolean archived;
    private boolean released;

    public VersionInputBuilder(final String projectKey) {
        this.projectKey = projectKey;
    }

    public VersionInputBuilder(final String projectKey, final Version version) {
        this(projectKey);
        name = version.getName();
        description = version.getDescription();
        archived = version.isArchived();
        released = version.isReleased();
        releaseDate = version.getReleaseDate();
    }

    public VersionInputBuilder setName(final String name) {
        this.name = name;
        return this;
    }

    public VersionInputBuilder setDescription(final String description) {
        this.description = description;
        return this;
    }

    public VersionInputBuilder setReleaseDate(final OffsetDateTime releaseDate) {
        this.releaseDate = releaseDate;
        return this;
    }

    public VersionInputBuilder setArchived(final boolean archived) {
        this.archived = archived;
        return this;
    }

    public VersionInputBuilder setReleased(final boolean released) {
        this.released = released;
        return this;
    }

    public VersionInput build() {
        return new VersionInput(projectKey, name, description, releaseDate, archived, released);
    }
}