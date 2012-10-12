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
package com.atlassian.jira.rest.client.internal.jersey;

import com.atlassian.jira.rest.client.ProgressMonitor;
import com.atlassian.jira.rest.client.ProjectRolesRestClient;
import com.atlassian.jira.rest.client.domain.BasicProject;
import com.atlassian.jira.rest.client.domain.BasicProjectRole;
import com.atlassian.jira.rest.client.domain.ProjectRole;
import com.atlassian.jira.rest.client.internal.json.BasicProjectRoleJsonParser;
import com.atlassian.jira.rest.client.internal.json.ProjectRoleJsonParser;
import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import com.sun.jersey.client.apache.ApacheHttpClient;

import javax.ws.rs.core.UriBuilder;
import java.net.URI;
import java.util.Collection;

/**
 * Jersey-based implementation of ProjectRolesRestClient.
 * @since 1.0
 */
public class JerseyProjectRolesRestClient extends AbstractJerseyRestClient implements ProjectRolesRestClient {

	private static final String PROJECT_URI_PREFIX = "project";
	private final ProjectRoleJsonParser projectRoleJsonParser;
	private final BasicProjectRoleJsonParser basicRoleJsonParser;

	public JerseyProjectRolesRestClient(
			final URI baseUri, final ApacheHttpClient client, final URI serverUri) {
		super(baseUri, client);
		this.projectRoleJsonParser = new ProjectRoleJsonParser(serverUri);
		this.basicRoleJsonParser = new BasicProjectRoleJsonParser();
	}

	@Override
	public ProjectRole getRole(final URI uri, final ProgressMonitor progressMonitor) {
		return getAndParse(uri, projectRoleJsonParser, progressMonitor);
	}

	@Override
	public ProjectRole getRole(final BasicProject project, final long roleId, final ProgressMonitor progressMonitor) {
		final URI roleUri = UriBuilder
				.fromUri(baseUri)
				.path(PROJECT_URI_PREFIX)
				.path(project.getKey())
				.path("role")
				.path(String.valueOf(roleId))
				.build();
		return getAndParse(roleUri, projectRoleJsonParser, progressMonitor);
	}

	@Override
	public Iterable<ProjectRole> getRoles(final BasicProject basicProject, final ProgressMonitor progressMonitor) {
		final URI rolesUris = UriBuilder
				.fromUri(baseUri)
				.path(PROJECT_URI_PREFIX)
				.path(basicProject.getKey())
				.path("role")
				.build();
		Collection<BasicProjectRole> basicProjectRoles = getAndParse(rolesUris, basicRoleJsonParser, progressMonitor);
		return Iterables.transform(
			basicProjectRoles,
			new Function<BasicProjectRole, ProjectRole>() {
				@Override
				public ProjectRole apply(final BasicProjectRole role) {
					return getRole(role.getSelf(), progressMonitor);
				}
			}
		);
	}
}
