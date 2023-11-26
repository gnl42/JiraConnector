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

import java.io.Serializable;
import java.net.URI;
import java.util.Objects;

import org.eclipse.jdt.annotation.Nullable;

import me.glindholm.jira.rest.client.api.NamedEntity;

/**
 * Association between users and project roles.
 *
 * @since 1.0
 */
public class RoleActor implements Serializable, NamedEntity {
    private static final long serialVersionUID = 1L;

    /**
     * Type of a role actor which associates a project with some particular user.
     */
    @SuppressWarnings("UnusedDeclaration")
    private static final String TYPE_ATLASSIAN_USER_ROLE = "atlassian-user-role-actor";

    /**
     * Type of a role actor which associates a project with a group of users, for instance:
     * administrators, developers.
     */
    @SuppressWarnings("UnusedDeclaration")
    private static final String TYPE_ATLASSIAN_GROUP_ROLE = "atlassian-group-role-actor";

    private final Long id;
    private final String displayName;
    private final String type;
    private final String name;
    private final URI avatarUrl;

    public RoleActor(final Long id, final String displayName, final String type, final String name, @Nullable final URI avatarUrl) {
        this.id = id;
        this.displayName = displayName;
        this.type = type;
        this.name = name;
        this.avatarUrl = avatarUrl;
    }

    @Override
    public String getName() {
        return name;
    }

    /**
     * @return the viewable name of this role actor.
     */
    public String getDisplayName() {
        return displayName;
    }

    /**
     * @return string identifying the implementation type.
     */
    public String getType() {
        return type;
    }

    /**
     * @return an URI of the avatar of this role actor.
     */
    public URI getAvatarUri() {
        return avatarUrl;
    }

    /**
     * @return the unique identifier for this role actor.
     */
    public Long getId() {
        return id;
    }

    @Override
    public boolean equals(final Object o) {
        if (o instanceof final RoleActor that) {
            return Objects.equals(getName(), that.getName()) && Objects.equals(id, that.getId()) && Objects.equals(getAvatarUri(), that.getAvatarUri())
                    && Objects.equals(getType(), that.getType()) && Objects.equals(getDisplayName(), that.getDisplayName());
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), name, avatarUrl, type, displayName);
    }

    @Override
    public String toString() {
        return "RoleActor [id=" + id + ", displayName=" + displayName + ", type=" + type + ", name=" + name + ", avatarUrl=" + avatarUrl + "]";
    }

    protected String getToStringHelper() {
        return toString();
    }
}
