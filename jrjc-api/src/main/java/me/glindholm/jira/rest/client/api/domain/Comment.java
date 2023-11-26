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

import org.eclipse.jdt.annotation.Nullable;

import me.glindholm.jira.rest.client.api.AddressableEntity;

/**
 * A JIRA comment
 *
 * @since v0.1
 */
public class Comment implements Serializable, AddressableEntity {
    private static final long serialVersionUID = 1L;

    private final URI self;
    @Nullable
    private final Long id;
    @Nullable
    private final BasicUser author;
    @Nullable
    private final BasicUser updateAuthor;
    private final OffsetDateTime creationDate;
    private final OffsetDateTime updateDate;
    private final String body;
    @Nullable
    private final Visibility visibility;

    public Comment(final URI self, final String body, @Nullable final BasicUser author, @Nullable final BasicUser updateAuthor,
            final OffsetDateTime creationDate, final OffsetDateTime updateDate, final Visibility visibility, @Nullable final Long id) {
        this.author = author;
        this.updateAuthor = updateAuthor;
        this.creationDate = creationDate;
        this.updateDate = updateDate;
        this.body = body;
        this.self = self;
        this.visibility = visibility;
        this.id = id;
    }

    public static Comment valueOf(final String body) {
        return new Comment(null, body, null, null, null, null, null, null);
    }

    public static Comment createWithRoleLevel(final String body, final String roleLevel) {
        return new Comment(null, body, null, null, null, null, Visibility.role(roleLevel), null);
    }

    public static Comment createWithGroupLevel(final String body, final String groupLevel) {
        return new Comment(null, body, null, null, null, null, Visibility.group(groupLevel), null);
    }

    public boolean wasUpdated() {
        return updateDate.isAfter(creationDate);
    }

    public String getBody() {
        return body;
    }

    @Nullable
    public Long getId() {
        return id;
    }

    @Override
    public URI getSelf() {
        return self;
    }

    @Nullable
    public BasicUser getAuthor() {
        return author;
    }

    @Nullable
    public BasicUser getUpdateAuthor() {
        return updateAuthor;
    }

    public OffsetDateTime getCreationDate() {
        return creationDate;
    }

    public OffsetDateTime getUpdateDate() {
        return updateDate;
    }

    @Nullable
    public Visibility getVisibility() {
        return visibility;
    }

    @Override
    public String toString() {
        return "Comment [self=" + self + ", id=" + id + ", author=" + author + ", updateAuthor=" + updateAuthor + ", creationDate=" + creationDate
                + ", updateDate=" + updateDate + ", body=" + body + ", visibility=" + visibility + "]";
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj instanceof final Comment that) {
            return Objects.equals(self, that.self) && Objects.equals(id, that.id) && Objects.equals(body, that.body) && Objects.equals(author, that.author)
                    && Objects.equals(updateAuthor, that.updateAuthor) && Objects.equals(creationDate, that.creationDate)
                    && Objects.equals(updateDate, that.updateDate) && Objects.equals(visibility, that.visibility) && Objects.equals(body, that.body);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(self, id, body, author, updateAuthor, creationDate, updateDate, visibility);
    }

}
