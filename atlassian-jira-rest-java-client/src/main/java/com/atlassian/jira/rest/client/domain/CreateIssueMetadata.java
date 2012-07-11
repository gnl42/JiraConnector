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

import com.google.common.base.Objects;

/**
 * Contains information about projects that may be used to create new issue.
 *
 * @since v1.0
 */
public class CreateIssueMetadata {

	private final Iterable<CreateIssueMetadataProject> projects;

	public CreateIssueMetadata(Iterable<CreateIssueMetadataProject> projects) {
		this.projects = projects;
	}

	public Iterable<CreateIssueMetadataProject> getProjects() {
		return projects;
	}

	@Override
	public String toString() {
		return getToStringHelper().toString();
	}

	/**
	 * Returns ToStringHelper with all fields inserted. Override this method to insert additional fields.
	 * @return ToStringHelper
	 */
	protected Objects.ToStringHelper getToStringHelper() {
		return Objects.toStringHelper(this).
				add("projects", projects);
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof CreateIssueMetadata) {
			CreateIssueMetadata that = (CreateIssueMetadata) obj;
			return Objects.equal(this.projects, that.projects);
		}
		return false;
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(projects);
	}
}
