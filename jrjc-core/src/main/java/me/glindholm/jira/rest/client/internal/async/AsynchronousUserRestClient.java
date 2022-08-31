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

import javax.annotation.Nullable;

import org.apache.hc.core5.net.URIBuilder;

import com.atlassian.httpclient.api.HttpClient;

import io.atlassian.util.concurrent.Promise;
import me.glindholm.jira.rest.client.api.UserRestClient;
import me.glindholm.jira.rest.client.api.domain.User;
import me.glindholm.jira.rest.client.api.domain.input.UserInput;
import me.glindholm.jira.rest.client.internal.json.UserJsonParser;
import me.glindholm.jira.rest.client.internal.json.UsersJsonParser;
import me.glindholm.jira.rest.client.internal.json.gen.UserInputJsonGenerator;

/**
 * Asynchronous implementation of UserRestClient.
 *
 * @since v2.0
 */
public class AsynchronousUserRestClient extends AbstractAsynchronousRestClient implements UserRestClient {

    private static final String USER_URI_PREFIX = "user";
    private static final String SEARCH_URI_PREFIX = "search";
    private static final String ASSIGNABLE_SEARCH_URI_PREFIX = "assignable/search";

    private static final String USERNAME_ATTRIBUTE = "username";
    private static final String PROJECT_ATTRIBUTE = "project";
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
    public Promise<User> getUser(final String username) throws URISyntaxException {
        final URI userUri = new URIBuilder(baseUri).appendPath(USER_URI_PREFIX).addParameter("username", username).addParameter("expand", "groups").build();
        return getUser(userUri);
    }

    @Override
    public Promise<User> getUser(final URI userUri) {
        return getAndParse(userUri, userJsonParser);
    }

    @Override
    public Promise<User> createUser(final UserInput user) throws URISyntaxException {
        final URIBuilder uriBuilder = new URIBuilder(baseUri).appendPath(USER_URI_PREFIX);
        return postAndParse(uriBuilder.build(), user, new UserInputJsonGenerator(), userJsonParser);
    }

    @Override
    public Promise<User> updateUser(final URI userUri, final UserInput user) {
        return putAndParse(userUri, user, new UserInputJsonGenerator(), userJsonParser);
    }

    @Override
    public Promise<Void> removeUser(final URI userUri) {
        return delete(userUri);
    }

    @Override
    public Promise<List<User>> findUsers(final String username) throws URISyntaxException {
        return findUsers(username, null, null, null, null);
    }

    @Override
    public Promise<List<User>> findUsers(final String username, @Nullable final Integer startAt, @Nullable final Integer maxResults,
            @Nullable final Boolean includeActive, @Nullable final Boolean includeInactive) throws URISyntaxException {

        final URIBuilder uriBuilder = new URIBuilder(baseUri).appendPath(USER_URI_PREFIX).appendPath(SEARCH_URI_PREFIX).addParameter(USERNAME_ATTRIBUTE,
                username);

        addOptionalQueryParam(uriBuilder, START_AT_ATTRIBUTE, startAt);
        addOptionalQueryParam(uriBuilder, MAX_RESULTS_ATTRIBUTE, maxResults);
        addOptionalQueryParam(uriBuilder, INCLUDE_ACTIVE_ATTRIBUTE, includeActive);
        addOptionalQueryParam(uriBuilder, INCLUDE_INACTIVE_ATTRIBUTE, includeInactive);

        final URI usersUri = uriBuilder.build();
        return getAndParse(usersUri, usersJsonParser);
    }

    @Override
    public Promise<List<User>> findAssignableUsers(final String projectKey, final Integer startAt, final Integer maxResults, final Boolean includeActive,
            final Boolean includeInactive) throws URISyntaxException {
        final URIBuilder uriBuilder = new URIBuilder(baseUri).appendPath(USER_URI_PREFIX).appendPath(ASSIGNABLE_SEARCH_URI_PREFIX)
                .addParameter(PROJECT_ATTRIBUTE, projectKey);

        addOptionalQueryParam(uriBuilder, START_AT_ATTRIBUTE, startAt);
        addOptionalQueryParam(uriBuilder, MAX_RESULTS_ATTRIBUTE, maxResults);
        addOptionalQueryParam(uriBuilder, INCLUDE_ACTIVE_ATTRIBUTE, includeActive);
        addOptionalQueryParam(uriBuilder, INCLUDE_INACTIVE_ATTRIBUTE, includeInactive);

        final URI usersUri = uriBuilder.build();
        return getAndParse(usersUri, usersJsonParser);
    }

    private static void addOptionalQueryParam(final URIBuilder uriBuilder, final String key, final Object value) {
        if (value != null) {
            uriBuilder.addParameter(key, String.valueOf(value));
        }
    }

}
