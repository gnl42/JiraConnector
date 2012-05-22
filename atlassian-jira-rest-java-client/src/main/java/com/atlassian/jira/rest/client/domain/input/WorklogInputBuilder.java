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
import com.atlassian.jira.rest.client.domain.Worklog;
import org.joda.time.DateTime;

import java.net.URI;

public class WorklogInputBuilder {
	private URI self;
	private URI issueUri;
	private BasicUser author;
	private BasicUser updateAuthor;
	private String comment;
	private DateTime startDate;
	private int minutesSpent;
	private Visibility visibility;

	public WorklogInputBuilder() {
	}

	public WorklogInputBuilder copyFromWorklog(Worklog worklog) {
		return this
				.setSelf(worklog.getSelf())
				.setIssueUri(worklog.getIssueUri())
				.setAuthor(worklog.getAuthor())
				.setUpdateAuthor(worklog.getUpdateAuthor())
				.setComment(worklog.getComment())
				.setStartDate(worklog.getStartDate())
				.setMinutesSpent(worklog.getMinutesSpent())
				.setVisibility(worklog.getVisibility());
	}

	public WorklogInputBuilder setSelf(URI self) {
		this.self = self;
		return this;
	}

	public WorklogInputBuilder setIssueUri(URI issueUri) {
		this.issueUri = issueUri;
		return this;
	}

	public WorklogInputBuilder setAuthor(BasicUser author) {
		this.author = author;
		return this;
	}

	public WorklogInputBuilder setUpdateAuthor(BasicUser updateAuthor) {
		this.updateAuthor = updateAuthor;
		return this;
	}

	public WorklogInputBuilder setComment(String comment) {
		this.comment = comment;
		return this;
	}

	public WorklogInputBuilder setStartDate(DateTime startDate) {
		this.startDate = startDate;
		return this;
	}

	public WorklogInputBuilder setMinutesSpent(int minutesSpent) {
		this.minutesSpent = minutesSpent;
		return this;
	}

	public WorklogInputBuilder setVisibility(Visibility visibility) {
		this.visibility = visibility;
		return this;
	}

	public WorklogInput build() {
		return new WorklogInput(self, issueUri, author, updateAuthor, comment, startDate, minutesSpent, visibility);
	}
}