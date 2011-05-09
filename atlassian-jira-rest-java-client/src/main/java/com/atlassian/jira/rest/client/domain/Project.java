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

import javax.annotation.Nullable;
import java.net.URI;
import java.util.Collection;

/**
 * Complete information about single JIRA project.
 * Many REST resources instead include just @{}BasicProject 
 *
 * @since v0.1
 */
public class Project extends BasicProject {
	@Nullable
	private final String description;
	private final BasicUser lead;
	@Nullable
	private final URI uri;
	private final Collection<Version> versions;
	private final Collection<BasicComponent> components;

	public Project(URI self, String key, String description, BasicUser lead, URI uri, Collection<Version> versions,
				   Collection<BasicComponent> components) {
		super(self, key);
		this.description = description;
		this.lead = lead;
		this.uri = uri;
		this.versions = versions;
		this.components = components;
	}

	/**
	 * @return description provided for this project or null if there is no description specific for this project.
	 */
	@Nullable
	public String getDescription() {
		return description;
	}

	/**
	 *
	 * @return the person who leads this project
	 */
	public BasicUser getLead() {
		return lead;
	}

	/**
	 * @return user-defined URI to a web page for this project, or <code>null</code> if not defined.
	 */
	@Nullable
	public URI getUri() {
		return uri;
	}

	/**
	 * @return versions defined for this project
	 */
	public Iterable<Version> getVersions() {
		return versions;
	}

	/**
	 *
	 * @return components defined for this project
	 */
	public Iterable<BasicComponent> getComponents() {
		return components;
	}
}
