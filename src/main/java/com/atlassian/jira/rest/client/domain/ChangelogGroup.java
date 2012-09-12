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

package com.atlassian.jira.rest.client.domain;

import org.joda.time.DateTime;

/**
 * Represents Issue change history group
 * @since 0.6
 */
public class ChangelogGroup {
	private final BasicUser author;
	private final DateTime created;
	private final Iterable<ChangelogItem> items;

	public ChangelogGroup(BasicUser author, DateTime created, Iterable<ChangelogItem> items) {
		this.author = author;
		this.created = created;
		this.items = items;
	}

	public BasicUser getAuthor() {
		return author;
	}

	public DateTime getCreated() {
		return created;
	}

	public Iterable<ChangelogItem> getItems() {
		return items;
	}
}
