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

package me.glindholm.jira.rest.client.api;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

import java.util.concurrent.CompletableFuture;
import me.glindholm.jira.rest.client.api.domain.BasicProject;
import me.glindholm.jira.rest.client.api.domain.Project;
import me.glindholm.jira.rest.client.api.domain.SecurityLevel;

/**
 * The me.glindholm.jira.rest.client.api handling project resources.
 *
 * @since v0.1
 */
public interface ProjectRestClient {
    /**
     * Retrieves complete information about given project.
     *
     * @param key unique key of the project (usually 2+ characters)
     * @return complete information about given project
     * @throws URISyntaxException
     * @throws RestClientException in case of problems (connectivity, malformed messages, etc.)
     */
    CompletableFuture<Project> getProject(String key) throws URISyntaxException;

    /**
     * Retrieves complete information about the project identified by the given URI.
     * <p>
     * This is a URI-based variant of {@link #getProject(String)}, intended for use when
     * the caller already has a project resource URI (for example, from another API call).
     *
     * @param projectUri the URI of the project resource
     * @return a future containing complete information about the given project
     * @throws RestClientException in case of problems (connectivity, malformed messages, etc.)
     */
    CompletableFuture<Project> getProject(URI projectUri);

    /**
     * Retrieves basic information about all projects visible to the currently authenticated user.
     *
     * @return a future containing a list of basic representations of all accessible projects
     * @throws URISyntaxException if the URI used to access the project collection is invalid
     * @throws RestClientException in case of problems (connectivity, malformed messages, etc.)
     */
    CompletableFuture<List<BasicProject>> getAllProjects() throws URISyntaxException;

    /**
     * Retrieves the issue security level configuration for the project identified by the given key.
     *
     * @param projectKey unique key of the project (usually 2+ characters)
     * @return a future containing the security level information for the specified project
     * @throws URISyntaxException if the URI used to access the project's security level is invalid
     * @throws RestClientException in case of problems (connectivity, malformed messages, etc.)
     */
    CompletableFuture<SecurityLevel> getSecurityLevel(String projectKey) throws URISyntaxException;

}
