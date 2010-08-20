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

import com.google.common.base.Objects;
import org.joda.time.DateTime;

import javax.annotation.Nullable;
import java.net.URI;

/**
 * TODO: Document this class / interface here
 *
 * @since v0.1
 */
public class Worklog {
	private final URI self;
	private final URI issue;
	private final User author;
	private final User updateAuthor;
	private final String comment;
	private final DateTime creationDate;
	private final DateTime updateDate;
	private final DateTime startDate;
	private final int minutesSpent;
	@Nullable
	private final String groupLevel = null;
	@Nullable
	private final String roleLevel= null;			

	public Worklog(URI self, URI issue, User author, User updateAuthor, String comment, DateTime creationDate, 
			DateTime updateDate, DateTime startDate, int minutesSpent) {
		this.self = self;
		this.issue = issue;
		this.author = author;
		this.updateAuthor = updateAuthor;
		this.comment = comment;
		this.creationDate = creationDate;
		this.updateDate = updateDate;
		this.startDate = startDate;
		this.minutesSpent = minutesSpent;
	}

	public URI getSelf() {
		return self;
	}

	public URI getIssueUri() {
		return issue;
	}

	public User getAuthor() {
		return author;
	}

	public User getUpdateAuthor() {
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

	@Override
	public String toString() {
		return Objects.toStringHelper(this).
				add("self", self).
				add("issue", issue).
				add("author", author).
				add("updateAuthor", updateAuthor).
				add("comment", comment).
				add("creationDate", creationDate).
				add("updateDate", updateDate).
				add("startDate", startDate).
				add("minutesSpent", minutesSpent).
				toString();
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof Worklog) {
			Worklog that = (Worklog) obj;
			return Objects.equal(this.self, that.self)
					&& Objects.equal(this.issue, that.issue)
					&& Objects.equal(this.author, that.author)
					&& Objects.equal(this.updateAuthor, that.updateAuthor)
					&& Objects.equal(this.comment, that.comment)
					&& this.creationDate.isEqual(that.creationDate)
					&& this.updateDate.isEqual(that.updateDate)
					&& this.startDate.isEqual(that.startDate)
					&& Objects.equal(this.minutesSpent, that.minutesSpent);
		}
		return false;
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(self, issue, author, updateAuthor, comment, creationDate, updateDate, startDate, minutesSpent);
	}


}
