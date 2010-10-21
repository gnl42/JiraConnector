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

package com.atlassian.jira.rest.client.domain;

import com.google.common.base.Objects;

import java.net.URI;

/**
 * Complete information about a single issue type defined in JIRA  
 *
 * @since v0.1
 */
public class IssueType extends BasicIssueType {
	private final String description;
	private final URI iconUri;

	public IssueType(URI self, String name, boolean isSubtask, String description, URI iconUri) {
		super(self, name, isSubtask);
		this.description = description;
		this.iconUri = iconUri;
	}

	public String getDescription() {
		return description;
	}

	public URI getIconUri() {
		return iconUri;
	}

	@Override
	public String toString() {
		return Objects.toStringHelper(this).addValue(super.toString()).
				add("description", description).
				add("iconUri", iconUri).
				toString();
	}


	@Override
	public boolean equals(Object obj) {
		if (obj instanceof IssueType) {
			IssueType that = (IssueType) obj;
			return super.equals(obj) && Objects.equal(this.description, that.description)
					&& Objects.equal(this.iconUri, that.iconUri);
		}
		return false;
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(super.hashCode(), description, iconUri);
	}

}
