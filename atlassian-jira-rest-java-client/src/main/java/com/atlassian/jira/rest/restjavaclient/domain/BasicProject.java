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

package com.atlassian.jira.rest.restjavaclient.domain;

import com.atlassian.jira.rest.restjavaclient.AddressableEntity;
import com.google.common.base.Objects;

import java.net.URI;

/**
 * TODO: Document this class / interface here
 *
 * @since v0.1
 */
public class BasicProject implements AddressableEntity {
	private final URI self;
	private final String key;

	public BasicProject(URI self, String key) {
		this.self = self;
		this.key = key;
	}

	@Override
	public URI getSelf() {
		return self;
	}

	public String getKey() {
		return key;
	}

	@Override
	public String toString() {
		return Objects.toStringHelper(this).
				add("self", self).
				add("key", key).
				toString();
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof BasicProject) {
			BasicProject that = (BasicProject) obj;
			return Objects.equal(this.self, that.self)
					&& Objects.equal(this.key, that.key);
		}
		return false;
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(self, key);
	}

}
