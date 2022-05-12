/*
 * Copyright (C) 2018 Atlassian
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

import javax.annotation.Nullable;

import org.apache.hc.core5.net.URIBuilder;

import com.atlassian.httpclient.api.HttpClient;

import io.atlassian.util.concurrent.Promise;
import me.glindholm.jira.rest.client.api.GroupRestClient;
import me.glindholm.jira.rest.client.api.domain.Group;
import me.glindholm.jira.rest.client.internal.json.GroupsJsonParser;

/**
 * Asynchronous implementation of GroupRestClient.
 *
 * @since v5.1.0
 */
public class AsynchronousGroupRestClient extends AbstractAsynchronousRestClient implements GroupRestClient {

    private static final String GROUPS_URI_PREFIX = "groups";
    private static final String PICKER_URI_PREFIX = "picker";

    private static final String QUERY_ATTRIBUTE = "query";
    private static final String EXCLUDE_ATTRIBUTE = "exclude";
    private static final String MAX_RESULTS_ATTRIBUTE = "maxResults";
    private static final String USERNAME_ATTRIBUTE = "userName";

    private final GroupsJsonParser groupsJsonParser = new GroupsJsonParser();

    private final URI baseUri;

    public AsynchronousGroupRestClient(final URI baseUri, final HttpClient client) {
        super(client);
        this.baseUri = baseUri;
    }

    @Override
    public Promise<Iterable<Group>> findGroups() throws URISyntaxException {
        return findGroups(null, null, null, null);
    }

    @Override
    public Promise<Iterable<Group>> findGroups(@Nullable String query, @Nullable String exclude, @Nullable Integer maxResults, @Nullable String userName)
            throws URISyntaxException {
        URIBuilder uriBuilder = new URIBuilder(baseUri).appendPath(GROUPS_URI_PREFIX).appendPath(PICKER_URI_PREFIX);

        addOptionalQueryParam(uriBuilder, QUERY_ATTRIBUTE, query);
        addOptionalQueryParam(uriBuilder, EXCLUDE_ATTRIBUTE, exclude);
        addOptionalQueryParam(uriBuilder, MAX_RESULTS_ATTRIBUTE, maxResults);
        addOptionalQueryParam(uriBuilder, USERNAME_ATTRIBUTE, userName);

        final URI groupsUri = uriBuilder.build();
        return getAndParse(groupsUri, groupsJsonParser);
    }

    private static void addOptionalQueryParam(final URIBuilder uriBuilder, final String key, final Object value) {
        if (value != null) {
            uriBuilder.addParameter(key, String.valueOf(value));
        }
    }
}
