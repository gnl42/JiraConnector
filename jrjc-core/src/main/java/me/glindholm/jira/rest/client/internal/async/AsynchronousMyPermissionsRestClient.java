/*
 * Copyright (C) 2014 Atlassian
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

import org.apache.hc.core5.net.URIBuilder;

import com.atlassian.httpclient.api.HttpClient;

import io.atlassian.util.concurrent.Promise;
import me.glindholm.jira.rest.client.api.MyPermissionsRestClient;
import me.glindholm.jira.rest.client.api.domain.Permissions;
import me.glindholm.jira.rest.client.api.domain.input.MyPermissionsInput;
import me.glindholm.jira.rest.client.internal.json.PermissionsJsonParser;

public class AsynchronousMyPermissionsRestClient extends AbstractAsynchronousRestClient implements MyPermissionsRestClient {
    private static final String URI_PREFIX = "mypermissions";
    private final URI baseUri;
    private final PermissionsJsonParser permissionsJsonParser = new PermissionsJsonParser();

    protected AsynchronousMyPermissionsRestClient(final URI baseUri, final HttpClient client) {
        super(client);
        this.baseUri = baseUri;
    }

    @Override
    public Promise<Permissions> getMyPermissions(final MyPermissionsInput permissionInput) throws URISyntaxException {
        final URIBuilder uriBuilder = new URIBuilder(baseUri).appendPath(URI_PREFIX);
        addContextParams(uriBuilder, permissionInput);
        return getAndParse(uriBuilder.build(), permissionsJsonParser);
    }

    private URIBuilder addContextParams(final URIBuilder uriBuilder, final MyPermissionsInput permissionInput) {
        if (permissionInput != null) {
            if (permissionInput.getProjectKey() != null) {
                uriBuilder.addParameter("projectKey", permissionInput.getProjectKey());
            }
            if (permissionInput.getProjectId() != null) {
                uriBuilder.addParameter("projectId", String.valueOf(permissionInput.getProjectId()));
            }
            if (permissionInput.getIssueKey() != null) {
                uriBuilder.addParameter("issueKey", permissionInput.getIssueKey());
            }
            if (permissionInput.getIssueId() != null) {
                uriBuilder.addParameter("issueId", String.valueOf(permissionInput.getIssueId()));
            }
        }
        return uriBuilder;
    }
}
