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
package me.glindholm.jira.rest.client.internal.async;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

import org.apache.hc.core5.net.URIBuilder;

import com.atlassian.httpclient.api.HttpClient;

import io.atlassian.util.concurrent.Promise;
import me.glindholm.jira.rest.client.api.ProjectRestClient;
import me.glindholm.jira.rest.client.api.domain.BasicProject;
import me.glindholm.jira.rest.client.api.domain.Project;
import me.glindholm.jira.rest.client.api.domain.SecurityLevel;
import me.glindholm.jira.rest.client.internal.json.BasicProjectsJsonParser;
import me.glindholm.jira.rest.client.internal.json.ProjectJsonParser;
import me.glindholm.jira.rest.client.internal.json.SecurityLevelJsonParser;

/**
 * Asynchronous implementation of ProjectRestClient.
 *
 * @since v2.0
 */
public class AsynchronousProjectRestClient extends AbstractAsynchronousRestClient implements ProjectRestClient {

    private static final String PROJECT_URI_PREFIX = "project";
    private final ProjectJsonParser projectJsonParser = new ProjectJsonParser();
    private final BasicProjectsJsonParser basicProjectsJsonParser = new BasicProjectsJsonParser();
    private final SecurityLevelJsonParser securityJsonParser = new SecurityLevelJsonParser();

    private final URI baseUri;

    public AsynchronousProjectRestClient(final URI baseUri, final HttpClient client) {
        super(client);
        this.baseUri = baseUri;
    }

    @Override
    public Promise<Project> getProject(final String key) throws URISyntaxException {
        final URI uri = new URIBuilder(baseUri).appendPath(PROJECT_URI_PREFIX).appendPath(key).build();
        return getAndParse(uri, projectJsonParser);
    }

    @Override
    public Promise<Project> getProject(final URI projectUri) {
        return getAndParse(projectUri, projectJsonParser);
    }

    @Override
    public Promise<List<BasicProject>> getAllProjects() throws URISyntaxException {
        final URI uri = new URIBuilder(baseUri).appendPath(PROJECT_URI_PREFIX).build();
        return getAndParse(uri, basicProjectsJsonParser);
    }

    @Override
    public Promise<SecurityLevel> getSecurityLevel(final String projectKey) throws URISyntaxException {
        final URI uri = new URIBuilder(baseUri).appendPath(PROJECT_URI_PREFIX).appendPath(projectKey).appendPath("issuesecuritylevelscheme").build();
        return getAndParse(uri, securityJsonParser);

    }
}
