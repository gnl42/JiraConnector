package com.atlassian.jira.restjavaclient.domain;

import com.google.common.base.Objects;
import org.joda.time.DateTime;

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

	public URI getIssue() {
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
}
