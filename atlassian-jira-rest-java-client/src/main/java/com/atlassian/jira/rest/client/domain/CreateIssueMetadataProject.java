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

import java.net.URI;
import java.util.Map;

/**
 * Represents project returned by /issue/createmeta action.
 *
 * @since v1.0
 */
public class CreateIssueMetadataProject extends BasicProject {

	private final Map<String, URI> avatarUris;
	private final Iterable<CreateIssueIssueType> issueTypes;

	public CreateIssueMetadataProject(URI self, String key, String name, Map<String, URI> avatarUris, Iterable<CreateIssueIssueType> issueTypes) {
		super(self, key, name);
		this.avatarUris = avatarUris;
		this.issueTypes = issueTypes;
	}

	public Iterable<CreateIssueIssueType> getIssueTypes() {
		return issueTypes;
	}

	public Map<String, URI> getAvatarUris() {
		return avatarUris;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected Objects.ToStringHelper getToStringHelper() {
		return super.getToStringHelper().
				add("issueTypes", issueTypes).
				add("avatarUris", avatarUris);
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(super.hashCode(), avatarUris, issueTypes);
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof CreateIssueMetadataProject) {
			CreateIssueMetadataProject that = (CreateIssueMetadataProject) obj;
			return super.equals(obj)
					&& Objects.equal(this.avatarUris, that.avatarUris)
					&& Objects.equal(this.issueTypes, that.issueTypes);
		}
		return false;
	}
}
