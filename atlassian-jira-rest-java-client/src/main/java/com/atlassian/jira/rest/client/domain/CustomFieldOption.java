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

/**
 * Represents Custom Field Option
 *
 * @since v1.0
 */
public class CustomFieldOption {

	private final URI self;
	private final Long id;
	private final String value;

	public CustomFieldOption(Long id, URI self, String value) {
		this.value = value;
		this.id = id;
		this.self = self;
	}

	public URI getSelf() {
		return self;
	}

	public Long getId() {
		return id;
	}

	public String getValue() {
		return value;
	}

	/**
	 * Returns ToStringHelper with all fields inserted. Override this method to insert additional fields.
	 * @return ToStringHelper
	 */
	protected Objects.ToStringHelper getToStringHelper() {
		return Objects.toStringHelper(this)
				.add("self", self)
				.add("id", id)
				.add("value", value);
	}

	@Override
	public String toString() {
		return getToStringHelper().toString();
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(self, id, value);
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		final CustomFieldOption other = (CustomFieldOption) obj;
		return Objects.equal(this.self, other.self)
				&& Objects.equal(this.id, other.id)
				&& Objects.equal(this.value, other.value);
	}
}
