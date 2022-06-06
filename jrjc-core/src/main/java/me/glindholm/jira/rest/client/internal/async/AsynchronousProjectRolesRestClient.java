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
import java.util.stream.Collectors;

import org.apache.hc.core5.net.URIBuilder;

import com.atlassian.httpclient.api.HttpClient;

import io.atlassian.util.concurrent.Promise;
import io.atlassian.util.concurrent.Promises;
import me.glindholm.jira.rest.client.api.ProjectRolesRestClient;
import me.glindholm.jira.rest.client.api.domain.BasicProjectRole;
import me.glindholm.jira.rest.client.api.domain.ProjectRole;
import me.glindholm.jira.rest.client.internal.json.BasicProjectRoleJsonParser;
import me.glindholm.jira.rest.client.internal.json.ProjectRoleJsonParser;

/**
 * Asynchronous implementation of ProjectRolesRestClient.
 *
 * @since v2.0
 */
public class AsynchronousProjectRolesRestClient extends AbstractAsynchronousRestClient implements ProjectRolesRestClient {

    private final ProjectRoleJsonParser projectRoleJsonParser;
    private final BasicProjectRoleJsonParser basicRoleJsonParser;

    public AsynchronousProjectRolesRestClient(final URI serverUri, final HttpClient client) {
        super(client);
        this.projectRoleJsonParser = new ProjectRoleJsonParser(serverUri);
        this.basicRoleJsonParser = new BasicProjectRoleJsonParser();
    }

    @Override
    public Promise<ProjectRole> getRole(URI uri) {
        return getAndParse(uri, projectRoleJsonParser);
    }

    @Override
    public Promise<ProjectRole> getRole(final URI projectUri, final Long roleId) throws URISyntaxException {
        final URI roleUri = new URIBuilder(projectUri).appendPath("role").appendPath(String.valueOf(roleId))
                .build();
        return getAndParse(roleUri, projectRoleJsonParser);
    }

    @Override
    public Promise<List<ProjectRole>> getRoles(final URI projectUri) throws URISyntaxException {
        final URI rolesUris = new URIBuilder(projectUri).appendPath("role")
                .build();
        final Promise<List<BasicProjectRole>> basicProjectRoles = getAndParse(rolesUris, basicRoleJsonParser);

        ;
        return Promises.promise(basicProjectRoles.claim().stream().map(basic -> getRole(basic.getSelf()).claim()).collect(Collectors.toList()));
    }
}
