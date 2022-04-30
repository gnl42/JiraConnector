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

import java.net.URI;
import java.util.Objects;

import javax.annotation.Nullable;

import org.joda.time.DateTime;

import me.glindholm.jira.rest.client.api.AddressableEntity;

/**
 * Issue worklog - single worklog entry describing the work logged for selected issue
 *
 * @since v0.1
 */
public class Worklog implements AddressableEntity {

    private final URI self;
    private final URI issueUri;
    private final BasicUser author;
    private final BasicUser updateAuthor;
    private final String comment;
    private final DateTime creationDate;
    private final DateTime updateDate;
    private final DateTime startDate;
    private final int minutesSpent;
    @Nullable
    private final Visibility visibility;

    public Worklog(URI self, URI issueUri, BasicUser author, BasicUser updateAuthor, @Nullable String comment,
            DateTime creationDate, DateTime updateDate, DateTime startDate, int minutesSpent, @Nullable Visibility visibility) {
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

    public DateTime getCreationDate() {
        return creationDate;
    }

    public DateTime getUpdateDate() {
        return updateDate;
    }

    public DateTime getStartDate() {
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
    public boolean equals(Object obj) {
        if (obj instanceof Worklog) {
            Worklog that = (Worklog) obj;
            return Objects.equals(this.self, that.self)
                    && Objects.equals(this.issueUri, that.issueUri)
                    && Objects.equals(this.author, that.author)
                    && Objects.equals(this.updateAuthor, that.updateAuthor)
                    && Objects.equals(this.comment, that.comment)
                    && Objects.equals(this.visibility, that.visibility)
                    && this.creationDate.isEqual(that.creationDate)
                    && this.updateDate.isEqual(that.updateDate)
                    && this.startDate.isEqual(that.startDate)
                    && Objects.equals(this.minutesSpent, that.minutesSpent);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(self, issueUri, author, updateAuthor, comment, creationDate, updateDate, startDate, minutesSpent);
    }

}
