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

package com.atlassian.jira.restjavaclient.domain;

import com.atlassian.jira.restjavaclient.AddressableEntity;
import com.google.common.base.Objects;
import org.joda.time.DateTime;

import java.net.URI;

/**
 * TODO: Document this class / interface here
 *
 * @since v0.1
 */
public class Comment implements AddressableEntity {
	private final URI self;
	private final User author;
	private final User updateAuthor;
	private final DateTime creationDate;
	private final DateTime updateDate;
	private final String body;
	private final String renderer;

    public Comment(URI self, String body, User author, User updateAuthor, DateTime creationDate, DateTime updateDate, String renderer) {
        this.author = author;
        this.updateAuthor = updateAuthor;
        this.creationDate = creationDate;
        this.updateDate = updateDate;
        this.body = body;
        this.self = self;
        this.renderer = renderer;
    }

	public String getRenderer() {
		return renderer;
	}

	public boolean wasUpdated() {
		return updateDate.isAfter(creationDate);
	}
	public String getBody() {
		return body;
	}

	public URI getSelf() {
		return self;
	}

    @Override
    public String toString() {
        return Objects.toStringHelper(this)
                .add("self", self)
                .add("body", body)
                .add("author", author)
                .add("updateAuthor", updateAuthor)
                .add("creationDate", creationDate)
                .add("updateDate", updateDate).toString();
    }
}
