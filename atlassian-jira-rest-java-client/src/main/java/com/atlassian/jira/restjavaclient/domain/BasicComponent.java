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

import java.net.URI;

/**
 * TODO: Document this class / interface here
 *
 * @since v0.1
 */
public class BasicComponent implements AddressableEntity {
	private final URI self;
	private final String name;
	private final String description;

	public BasicComponent(URI self, String name, String description) {
		this.self = self;
		this.name = name;
		this.description = description;
	}

	public URI getSelf() {
		return self;
	}

	public String getName() {
		return name;
	}

	public String getDescription() {
		return description;
	}
}
