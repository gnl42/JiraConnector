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

import java.time.OffsetDateTime;

import me.glindholm.jira.rest.client.api.AddressableEntity;

/**
 * A file attachment attached to an issue
 *
 * @since v0.1
 */
public class Attachment implements AddressableEntity {
    private final URI self;
    private final String filename;
    private final BasicUser author;
    private final OffsetDateTime creationDate;
    private final int size;
    private final String mimeType;
    private final URI contentUri;

    @Nullable
    private final URI thumbnailUri;

    public Attachment(URI self, String filename, BasicUser author, OffsetDateTime creationDate, int size, String mimeType, URI contentUri, URI thumbnailUri) {
        this.self = self;
        this.filename = filename;
        this.author = author;
        this.creationDate = creationDate;
        this.size = size;
        this.mimeType = mimeType;
        this.contentUri = contentUri;
        this.thumbnailUri = thumbnailUri;
    }

    public boolean hasThumbnail() {
        return thumbnailUri != null;
    }

    @Override
    public URI getSelf() {
        return self;
    }

    public String getFilename() {
        return filename;
    }

    public BasicUser getAuthor() {
        return author;
    }

    public OffsetDateTime getCreationDate() {
        return creationDate;
    }

    public int getSize() {
        return size;
    }

    public String getMimeType() {
        return mimeType;
    }

    public URI getContentUri() {
        return contentUri;
    }

    @Nullable
    public URI getThumbnailUri() {
        return thumbnailUri;
    }

    @Override
    public String toString() {
        return "Attachment [self=" + self + ", filename=" + filename + ", author=" + author + ", size=" + size + ", mimeType=" + mimeType + ", contentUri="
                + contentUri + ", thumbnailUri=" + thumbnailUri + "]";
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Attachment) {
            Attachment that = (Attachment) obj;
            return Objects.equals(this.self, that.self)
                    && Objects.equals(this.filename, that.filename)
                    && Objects.equals(this.author, that.author)
                    && this.creationDate.isEqual(that.creationDate)
                    && Objects.equals(this.size, that.size)
                    && Objects.equals(this.mimeType, that.mimeType)
                    && Objects.equals(this.contentUri, that.contentUri)
                    && Objects.equals(this.thumbnailUri, that.thumbnailUri);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(self, filename, author, creationDate, size, mimeType, contentUri, thumbnailUri);
    }
}
