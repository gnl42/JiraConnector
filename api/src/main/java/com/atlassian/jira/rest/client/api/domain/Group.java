/*
 * Copyright (C) 2018 Atlassian
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

package com.atlassian.jira.rest.client.api.domain;

import com.google.common.base.Objects;

import java.net.URI;

/**
 * Complete information about a single JIRA group
 *
 * @since v5.1.0
 */
public class Group extends AddressableNamedEntity {

	public Group(URI self, String name) {
		super(self, name);
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof Group) {
			Group that = (Group) obj;
			return super.equals(obj) && Objects.equal(this.name, that.name);
		}
		return false;
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(super.hashCode(), name);
	}
}
