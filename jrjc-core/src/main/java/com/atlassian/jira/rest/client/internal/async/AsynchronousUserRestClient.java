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
package com.atlassian.jira.rest.client.internal.async;

import com.atlassian.httpclient.api.HttpClient;
import com.atlassian.jira.rest.client.api.UserRestClient;
import com.atlassian.jira.rest.client.api.domain.User;
import com.atlassian.jira.rest.client.api.domain.input.UserInput;
import com.atlassian.jira.rest.client.internal.json.UserJsonParser;
import com.atlassian.jira.rest.client.internal.json.UsersJsonParser;
import com.atlassian.jira.rest.client.internal.json.gen.UserInputJsonGenerator;
import io.atlassian.util.concurrent.Promise;

import javax.annotation.Nullable;
import javax.ws.rs.core.UriBuilder;
import java.net.URI;

/**
 * Asynchronous implementation of UserRestClient.
 *
 * @since v2.0
 */
public class AsynchronousUserRestClient extends AbstractAsynchronousRestClient implements UserRestClient {

    private static final String USER_URI_PREFIX = "user";
    private static final String SEARCH_URI_PREFIX = "search";

    private static final String USERNAME_ATTRIBUTE = "username";
    private static final String START_AT_ATTRIBUTE = "startAt";
    private static final String MAX_RESULTS_ATTRIBUTE = "maxResults";
    private static final String INCLUDE_ACTIVE_ATTRIBUTE = "includeActive";
    private static final String INCLUDE_INACTIVE_ATTRIBUTE = "includeInactive";

    private final UserJsonParser userJsonParser = new UserJsonParser();
    private final UsersJsonParser usersJsonParser = new UsersJsonParser();

    private final URI baseUri;

    public AsynchronousUserRestClient(final URI baseUri, final HttpClient client) {
        super(client);
        this.baseUri = baseUri;
    }

    @Override
    public Promise<User> getUser(final String username) {
        final URI userUri = UriBuilder.fromUri(baseUri).path(USER_URI_PREFIX)
                .queryParam("username", username).queryParam("expand", "groups").build();
        return getUser(userUri);
    }

    @Override
    public Promise<User> getUser(final URI userUri) {
        return getAndParse(userUri, userJsonParser);
    }

    @Override
    public Promise<User> createUser(UserInput user) {
        final UriBuilder uriBuilder = UriBuilder.fromUri(baseUri).path(USER_URI_PREFIX);
        return postAndParse(uriBuilder.build(), user, new UserInputJsonGenerator(), userJsonParser);
    }

    @Override
    public Promise<User> updateUser(URI userUri, UserInput user) {
        return putAndParse(userUri, user, new UserInputJsonGenerator(), userJsonParser);
    }

    @Override
    public Promise<Void> removeUser(URI userUri) {
        return delete(userUri);
    }

    @Override
    public Promise<Iterable<User>> findUsers(String username) {
        return findUsers(username, null, null, null, null);
    }

    @Override
    public Promise<Iterable<User>> findUsers(String username, @Nullable Integer startAt, @Nullable Integer maxResults,
            @Nullable Boolean includeActive, @Nullable Boolean includeInactive) {

         UriBuilder uriBuilder = UriBuilder.fromUri(baseUri).path(USER_URI_PREFIX).path(SEARCH_URI_PREFIX)
                .queryParam(USERNAME_ATTRIBUTE, username);

        addOptionalQueryParam(uriBuilder, START_AT_ATTRIBUTE, startAt);
        addOptionalQueryParam(uriBuilder, MAX_RESULTS_ATTRIBUTE, maxResults);
        addOptionalQueryParam(uriBuilder, INCLUDE_ACTIVE_ATTRIBUTE, includeActive);
        addOptionalQueryParam(uriBuilder, INCLUDE_INACTIVE_ATTRIBUTE, includeInactive);

        final URI usersUri = uriBuilder.build();
        return getAndParse(usersUri, usersJsonParser);
    }

    private void addOptionalQueryParam(final UriBuilder uriBuilder, final String key, final Object value) {
        if (value != null) {
            uriBuilder.queryParam(key, value);
        }
    }

}
