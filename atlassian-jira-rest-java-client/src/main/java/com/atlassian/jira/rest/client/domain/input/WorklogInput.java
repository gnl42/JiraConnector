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

package com.atlassian.jira.rest.client.domain.input;

import com.atlassian.jira.rest.client.domain.BasicUser;
import com.atlassian.jira.rest.client.domain.Visibility;
import com.google.common.base.Objects;
import org.joda.time.DateTime;

import javax.annotation.Nullable;
import java.net.URI;

public class WorklogInput {
	@Nullable
	private final URI self;
	private final URI issueUri;
	@Nullable
	private final BasicUser author;
	@Nullable
	private final BasicUser updateAuthor;
	@Nullable
	private final String comment;
	private final DateTime startDate;
	private final int minutesSpent;
	@Nullable
	private final Visibility visibility;

	public WorklogInput(@Nullable URI self, URI issueUri, @Nullable BasicUser author, @Nullable BasicUser updateAuthor,
			@Nullable String comment, DateTime startDate, int minutesSpent, @Nullable Visibility visibility) {
		this.visibility = visibility;
		this.minutesSpent = minutesSpent;
		this.startDate = startDate;
		this.comment = comment;
		this.updateAuthor = updateAuthor;
		this.author = author;
		this.issueUri = issueUri;
		this.self = self;
	}

	public static WorklogInput create(URI issueUri, @Nullable String comment, DateTime startDate, int minutesSpent) {
		return new WorklogInputBuilder().setSelf(null).setIssueUri(issueUri).setAuthor(null).setUpdateAuthor(null)
				.setComment(comment).setStartDate(startDate).setMinutesSpent(minutesSpent).setVisibility(null)
				.build();
	}

	public static WorklogInput create(URI issueUri, @Nullable String comment, DateTime startDate, int minutesSpent, @Nullable Visibility visibility) {
		return new WorklogInputBuilder().setSelf(null).setIssueUri(issueUri).setAuthor(null).setUpdateAuthor(null)
				.setComment(comment).setStartDate(startDate).setMinutesSpent(minutesSpent).setVisibility(visibility)
				.build();
	}

	@Nullable
	public URI getSelf() {
		return self;
	}

	public URI getIssueUri() {
		return issueUri;
	}

	@Nullable
	public BasicUser getAuthor() {
		return author;
	}

	@Nullable
	public BasicUser getUpdateAuthor() {
		return updateAuthor;
	}

	@Nullable
	public String getComment() {
		return comment;
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
		return Objects.toStringHelper(this)
				.add("self", self)
				.add("issueUri", issueUri)
				.add("author", author)
				.add("updateAuthor", updateAuthor)
				.add("comment", comment)
				.add("startDate", startDate)
				.add("minutesSpent", minutesSpent)
				.add("visibility", visibility)
				.toString();
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof WorklogInput) {
			final WorklogInput that = (WorklogInput) obj;

			return Objects.equal(this.self, that.self)
					&& Objects.equal(this.issueUri, that.issueUri)
					&& Objects.equal(this.author, that.author)
					&& Objects.equal(this.updateAuthor, that.updateAuthor)
					&& Objects.equal(this.comment, that.comment)
					&& Objects.equal(this.startDate, that.startDate)
					&& Objects.equal(this.minutesSpent, that.minutesSpent)
					&& Objects.equal(this.visibility, that.visibility);
		}
		return false;
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(self, issueUri, author, updateAuthor, comment, startDate, minutesSpent, visibility);
	}
}

