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

package com.atlassian.jira.rest.client.api.domain;

import java.net.URI;
import java.util.Objects;

import javax.annotation.Nullable;

import org.joda.time.DateTime;

import com.atlassian.jira.rest.client.api.AddressableEntity;

/**
 * A JIRA comment
 *
 * @since v0.1
 */
public class Comment implements AddressableEntity {
    private final URI self;
    @Nullable
    private final Long id;
    @Nullable
    private final BasicUser author;
    @Nullable
    private final BasicUser updateAuthor;
    private final DateTime creationDate;
    private final DateTime updateDate;
    private final String body;
    @Nullable
    private final Visibility visibility;

    public Comment(URI self, String body, @Nullable BasicUser author, @Nullable BasicUser updateAuthor, DateTime creationDate, DateTime updateDate, Visibility visibility, @Nullable Long id) {
        this.author = author;
        this.updateAuthor = updateAuthor;
        this.creationDate = creationDate;
        this.updateDate = updateDate;
        this.body = body;
        this.self = self;
        this.visibility = visibility;
        this.id = id;
    }

    public static Comment valueOf(String body) {
        return new Comment(null, body, null, null, null, null, null, null);
    }

    public static Comment createWithRoleLevel(String body, String roleLevel) {
        return new Comment(null, body, null, null, null, null, Visibility.role(roleLevel), null);
    }

    public static Comment createWithGroupLevel(String body, String groupLevel) {
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

    public DateTime getCreationDate() {
        return creationDate;
    }

    public DateTime getUpdateDate() {
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
    public boolean equals(Object obj) {
        if (obj instanceof Comment) {
            Comment that = (Comment) obj;
            return Objects.equals(this.self, that.self)
                    && Objects.equals(this.id, that.id)
                    && Objects.equals(this.body, that.body)
                    && Objects.equals(this.author, that.author)
                    && Objects.equals(this.updateAuthor, that.updateAuthor)
                    && Objects.equals(this.creationDate, that.creationDate)
                    && Objects.equals(this.updateDate, that.updateDate)
                    && Objects.equals(this.visibility, that.visibility)
                    && Objects.equals(this.body, that.body);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(self, id, body, author, updateAuthor, creationDate, updateDate, visibility);
    }

}
