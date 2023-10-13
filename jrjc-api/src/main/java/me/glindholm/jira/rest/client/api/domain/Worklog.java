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
 * Issue worklog - single worklog entry describing the work logged for selected
 * issue
 *
 * @since v0.1
 */
public class Worklog implements Serializable, AddressableEntity {
    private static final long serialVersionUID = 1L;

    private final URI self;
    private final URI issueUri;
    private final BasicUser author;
    private final BasicUser updateAuthor;
    private final String comment;
    private final OffsetDateTime creationDate;
    private final OffsetDateTime updateDate;
    private final OffsetDateTime startDate;
    private final int minutesSpent;
    @Nullable
    private final Visibility visibility;

    public Worklog(final URI self, final URI issueUri, final BasicUser author, final BasicUser updateAuthor, @Nullable final String comment,
            final OffsetDateTime creationDate, final OffsetDateTime updateDate, final OffsetDateTime startDate, final int minutesSpent,
            @Nullable final Visibility visibility) {
        this.self = self;
        this.issueUri = issueUri;
        this.author = author;
        this.updateAuthor = updateAuthor;
        this.comment = comment;
        this.creationDate = creationDate;
        this.updateDate = updateDate;
        this.startDate = startDate;
        this.minutesSpent = minutesSpent;
        this.visibility = visibility;
    }

    @Override
    public URI getSelf() {
        return self;
    }

    public URI getIssueUri() {
        return issueUri;
    }

    public BasicUser getAuthor() {
        return author;
    }

    public BasicUser getUpdateAuthor() {
        return updateAuthor;
    }

    public String getComment() {
        return comment;
    }

    public OffsetDateTime getCreationDate() {
        return creationDate;
    }

    public OffsetDateTime getUpdateDate() {
        return updateDate;
    }

    public OffsetDateTime getStartDate() {
        return startDate;
    }

    public int getMinutesSpent() {
        return minutesSpent;
    }

    @Nullable
    public Visibility getVisibility() {
        return visibility;
    }

    @Override
    public String toString() {
        return "Worklog [self=" + self + ", issueUri=" + issueUri + ", author=" + author + ", updateAuthor=" + updateAuthor + ", comment=" + comment
                + ", minutesSpent=" + minutesSpent + ", visibility=" + visibility + "]";
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj instanceof Worklog) {
            final Worklog that = (Worklog) obj;
            return Objects.equals(self, that.self) && Objects.equals(issueUri, that.issueUri) && Objects.equals(author, that.author)
                    && Objects.equals(updateAuthor, that.updateAuthor) && Objects.equals(comment, that.comment) && Objects.equals(visibility, that.visibility)
                    && creationDate.isEqual(that.creationDate) && updateDate.isEqual(that.updateDate) && startDate.isEqual(that.startDate)
                    && Objects.equals(minutesSpent, that.minutesSpent);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(self, issueUri, author, updateAuthor, comment, creationDate, updateDate, startDate, minutesSpent);
    }

}
