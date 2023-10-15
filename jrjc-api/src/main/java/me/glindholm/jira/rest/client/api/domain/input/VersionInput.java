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
import java.util.Objects;

import org.eclipse.jdt.annotation.Nullable;

/**
 * Input data describing details of a project version to create.
 *
 * @since v4.4
 */
public class VersionInput {
    private final String projectKey;
    private final String name;
    @Nullable
    private final String description;
    private final OffsetDateTime releaseDate;
    private final boolean isArchived;
    private final boolean isReleased;

    public VersionInput(final String projectKey, final String name, @Nullable final String description, @Nullable final OffsetDateTime releaseDate,
            final boolean isArchived, final boolean isReleased) {
        this.projectKey = projectKey;
        this.name = name;
        this.description = description;
        this.releaseDate = releaseDate;
        this.isArchived = isArchived;
        this.isReleased = isReleased;
    }

    public static VersionInput create(final String projectKey, final String name, @Nullable final String description,
            @Nullable final OffsetDateTime releaseDate, final boolean archived, final boolean release) {
        return new VersionInput(projectKey, name, description, releaseDate, archived, release);
    }

    public String getProjectKey() {
        return projectKey;
    }

    public String getName() {
        return name;
    }

    @Nullable
    public String getDescription() {
        return description;
    }

    public OffsetDateTime getReleaseDate() {
        return releaseDate;
    }

    public boolean isArchived() {
        return isArchived;
    }

    public boolean isReleased() {
        return isReleased;
    }

    @Override
    public String toString() {
        return "VersionInput [projectKey=" + projectKey + ", name=" + name + ", description=" + description + ", isArchived=" + isArchived + ", isReleased="
                + isReleased + "]";
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj instanceof final VersionInput that) {
            return Objects.equals(projectKey, that.projectKey) && Objects.equals(name, that.name) && Objects.equals(releaseDate, that.releaseDate)
                    && Objects.equals(isArchived, that.isArchived) && Objects.equals(isReleased, that.isReleased);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, projectKey, description, releaseDate, isArchived, isReleased);
    }

}
