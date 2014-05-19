/*
 * Copyright (C) 2014 Atlassian
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

import com.atlassian.jira.rest.client.api.IdentifiableEntity;
import com.atlassian.jira.rest.client.api.NamedEntity;
import com.google.common.base.Function;
import com.google.common.base.Objects;

import javax.annotation.Nullable;

public class Permission implements NamedEntity, IdentifiableEntity<Integer> {
	private final int id;
	private final String key;
	private final String name;
	@Nullable
	private final String description;
	private final boolean havePermission;

	public Permission(final int id, final String key, final String name, @Nullable final String description,
			final boolean havePermission) {
		this.id = id;
		this.key = key;
		this.name = name;
		this.description = description;
		this.havePermission = havePermission;
	}

	public Integer getId() {
		return id;
	}

	public String getKey() {
		return key;
	}

	public String getName() {
		return name;
	}

	@Nullable
	public String getDescription() {
		return description;
	}

	public boolean havePermission() {
		return havePermission;
	}

	protected Objects.ToStringHelper getToStringHelper() {
		return Objects.toStringHelper(this)
				.add("id", id)
				.add("key", key)
				.add("name", name)
				.add("description", description)
				.add("havePermission", havePermission);
	}

	@Override
	public String toString() {
		return getToStringHelper().toString();
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		Permission that = (Permission) o;

		if (havePermission != that.havePermission) return false;
		if (id != that.id) return false;
		if (!key.equals(that.key)) return false;
		if (!name.equals(that.name)) return false;

		return true;
	}

	@Override
	public int hashCode() {
		int result = id;
		result = 31 * result + key.hashCode();
		result = 31 * result + name.hashCode();
		result = 31 * result + (havePermission ? 1 : 0);
		return result;
	}

	public static final Function<Permission, String> TO_KEY = new Function<Permission, String>() {
		@Override
		public String apply(final Permission input) {
			return input.getKey();
		}
	};
}
