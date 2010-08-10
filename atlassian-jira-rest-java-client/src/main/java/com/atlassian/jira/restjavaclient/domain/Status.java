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

import com.google.common.base.Objects;

import java.net.URI;

/**
 * TODO: Document this class / interface here
 *
 * @since v0.1
 */
public class Status extends BasicStatus {
	private final String description;
	private final URI iconUrl;

	public Status(URI self, String name, String description, URI iconUrl) {
		super(self, name);
		this.description = description;
		this.iconUrl = iconUrl;
	}

	public String getDescription() {
		return description;
	}

	public URI getIconUrl() {
		return iconUrl;
	}

	@Override
	public String toString() {
		return Objects.toStringHelper(this).addValue(super.toString()).
				add("description", description).
				add("iconUrl", iconUrl).
				toString();
	}
}
