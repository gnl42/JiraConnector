/*
 * Copyright (C) 2011 Atlassian
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.atlassian.jira.rest.client.domain;

import com.google.common.base.Objects;

/**
 * Represents search results - links to issues matching given filter (JQL query) with basic
 * information supporting the paging through the results.
 *
 * @since v0.2
 */
public class SearchResult {
	private final int startIndex;
	private final int maxResults;
	private final int size;
	private final Iterable<BasicIssue> issues;

	public SearchResult(int startIndex, int maxResults, int size, Iterable<BasicIssue> issues) {
		this.startIndex = startIndex;
		this.maxResults = maxResults;
		this.size = size;
		this.issues = issues;
	}

	public int getStartIndex() {
		return startIndex;
	}

	public int getMaxResults() {
		return maxResults;
	}

	public int getSize() {
		return size;
	}

	public Iterable<BasicIssue> getIssues() {
		return issues;
	}

	@Override
	public String toString() {
		return Objects.toStringHelper(this).
				add("startIndex", startIndex).
				add("maxResults", maxResults).
				add("size", size).
				add("issues", issues).
				toString();
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof SearchResult) {
			SearchResult that = (SearchResult) obj;
			return Objects.equal(this.startIndex, that.startIndex)
					&& Objects.equal(this.maxResults, that.maxResults)
					&& Objects.equal(this.size, that.size)
					&& Objects.equal(this.issues, that.issues);
		}
		return false;
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(startIndex, maxResults, size, issues);
	}

}
