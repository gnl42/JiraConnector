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

    // ...existing javadoc...
    CompletableFuture<Project> getProject(URI projectUri);

    // ...existing javadoc...
    CompletableFuture<List<BasicProject>> getAllProjects() throws URISyntaxException;

    // ...existing javadoc...
    CompletableFuture<SecurityLevel> getSecurityLevel(String projectKey) throws URISyntaxException;

}
