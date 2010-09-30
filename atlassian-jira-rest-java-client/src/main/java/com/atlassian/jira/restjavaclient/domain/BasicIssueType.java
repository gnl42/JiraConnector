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

import java.net.URI;

/**
 * Basic information about issue type.
 *
 * @since v0.1
 */
public class BasicIssueType implements AddressableEntity {
	private final URI self;

	private final String name;

	private final boolean isSubtask;

	public BasicIssueType(URI self, String name, boolean isSubtask) {
		this.self = self;
		this.name = name;
		this.isSubtask = isSubtask;
	}

	public String getName() {
		return name;
	}

	public boolean isSubtask() {
		return isSubtask;
	}

	@Override
	public URI getSelf() {
		return self;
	}

	@Override
	public String toString() {
		return Objects.toStringHelper(this).
				add("self", self).
				add("name", name).
				add("isSubtask", isSubtask).
				toString();
	}


	@Override
	public boolean equals(Object obj) {
		if (obj instanceof BasicIssueType) {
			BasicIssueType that = (BasicIssueType) obj;
			return Objects.equal(this.self, that.self)
					&& Objects.equal(this.name, that.name)
					&& Objects.equal(this.isSubtask, that.isSubtask);
		}
		return false;
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(self, name, isSubtask);
	}
}
