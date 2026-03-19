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
import java.util.concurrent.CompletableFuture;

import org.eclipse.jdt.annotation.Nullable;

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
    private static final String ISSUE_ATTRIBUTE = "issueKey";

    private final UserJsonParser userJsonParser = new UserJsonParser();
    private final UsersJsonParser usersJsonParser = new UsersJsonParser();

    private final URI baseUri;

    public AsynchronousUserRestClient(final URI baseUri, final DisposableHttpClient client) {
        super(client);
        this.baseUri = baseUri;
    }

    @Override
    public CompletableFuture<User> getCurrentUser() throws URISyntaxException {
        final URI userUri = new UriBuilder(baseUri).appendPath("myself").build();
        return getUser(userUri);
    }

    @Override
    public CompletableFuture<User> getUser(final String username) throws URISyntaxException {
        final URI userUri = new UriBuilder(baseUri).appendPath(USER_URI_PREFIX).addParameter("username", username).addParameter("expand", "groups").build();
        return getUser(userUri);
    }

    @Override
    public CompletableFuture<User> getUser(final URI userUri) {
        return getAndParse(userUri, userJsonParser);
    }

    @Override
    public CompletableFuture<User> createUser(final UserInput user) throws URISyntaxException {
        final UriBuilder uriBuilder = new UriBuilder(baseUri).appendPath(USER_URI_PREFIX);
        return postAndParse(uriBuilder.build(), user, new UserInputJsonGenerator(), userJsonParser);
    }

    @Override
    public CompletableFuture<User> updateUser(final URI userUri, final UserInput user) {
        return putAndParse(userUri, user, new UserInputJsonGenerator(), userJsonParser);
    }

    @Override
    public CompletableFuture<Void> removeUser(final URI userUri) {
        return delete(userUri);
    }

    @Override
    public CompletableFuture<List<User>> findUsers(final String username) throws URISyntaxException {
        return findUsers(username, null, null, null, null);
    }

    @Override
    public CompletableFuture<List<User>> findUsers(final String username, @Nullable final Integer startAt, @Nullable final Integer maxResults,
            @Nullable final Boolean includeActive, @Nullable final Boolean includeInactive) throws URISyntaxException {

        final UriBuilder uriBuilder = new UriBuilder(baseUri).appendPath(USER_URI_PREFIX).appendPath(SEARCH_URI_PREFIX).addParameter(USERNAME_ATTRIBUTE, username);

        addOptionalQueryParam(uriBuilder, START_AT_ATTRIBUTE, startAt);
        addOptionalQueryParam(uriBuilder, MAX_RESULTS_ATTRIBUTE, maxResults);
        addOptionalQueryParam(uriBuilder, INCLUDE_ACTIVE_ATTRIBUTE, includeActive);
        addOptionalQueryParam(uriBuilder, INCLUDE_INACTIVE_ATTRIBUTE, includeInactive);

        final URI usersUri = uriBuilder.build();
        return getAndParse(usersUri, usersJsonParser);
    }

    @Override
    public CompletableFuture<List<User>> findAssignableUsersForIssue(final String issueKey, final Integer startAt, final Integer maxResults,
            final Boolean includeActive, final Boolean includeInactive) throws URISyntaxException {
        return findAssignableUsers(ISSUE_ATTRIBUTE, issueKey, startAt, maxResults, includeActive, includeInactive);
    }

    @Override
    public CompletableFuture<List<User>> findAssignableUsersForProject(final String projectKey, final Integer startAt, final Integer maxResults,
            final Boolean includeActive, final Boolean includeInactive) throws URISyntaxException {
        return findAssignableUsers(PROJECT_ATTRIBUTE, projectKey, startAt, maxResults, includeActive, includeInactive);
    }

    private CompletableFuture<List<User>> findAssignableUsers(final String searchAttribute, final String key, final Integer startAt, final Integer maxResults,
            final Boolean includeActive, final Boolean includeInactive) throws URISyntaxException {
        final UriBuilder uriBuilder = new UriBuilder(baseUri).appendPath(USER_URI_PREFIX).appendPath(ASSIGNABLE_SEARCH_URI_PREFIX)
                .addParameter(searchAttribute, key);

        addOptionalQueryParam(uriBuilder, START_AT_ATTRIBUTE, startAt);
        addOptionalQueryParam(uriBuilder, MAX_RESULTS_ATTRIBUTE, maxResults);
        addOptionalQueryParam(uriBuilder, INCLUDE_ACTIVE_ATTRIBUTE, includeActive);
        addOptionalQueryParam(uriBuilder, INCLUDE_INACTIVE_ATTRIBUTE, includeInactive);

        final URI usersUri = uriBuilder.build();
        return getAndParse(usersUri, usersJsonParser);
    }

    private static void addOptionalQueryParam(final UriBuilder uriBuilder, final String key, final Object value) {
        if (value != null) {
            uriBuilder.addParameter(key, String.valueOf(value));
        }
    }
}