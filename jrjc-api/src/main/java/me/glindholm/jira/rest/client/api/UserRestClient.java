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

import org.eclipse.jdt.annotation.Nullable;

import io.atlassian.util.concurrent.Promise;
import me.glindholm.jira.rest.client.api.domain.User;
import me.glindholm.jira.rest.client.api.domain.input.UserInput;

/**
 * The me.glindholm.jira.rest.client.api handling user resources.
 *
 * @since v0.1
 */
public interface UserRestClient {

    /**
     * Retrieve current user
     *
     * @return user
     * @throws URISyntaxException
     */
    Promise<User> getCurrentUser() throws URISyntaxException;

    /**
     * Retrieves detailed information about selected user. Try to use {@link #getUser(URI)} instead as
     * that method is more RESTful (well connected)
     *
     * @param username JIRA username/login
     * @return complete information about given user
     * @throws URISyntaxException
     * @throws RestClientException in case of problems (connectivity, malformed messages, etc.)
     */
    Promise<User> getUser(String username) throws URISyntaxException;

    /**
     * Retrieves detailed information about selected user. This method is preferred over
     * {@link #getUser(String)} as com.atlassian.jira.rest.it's more RESTful (well connected)
     *
     * @param userUri URI of user resource
     * @return complete information about given user
     * @throws RestClientException in case of problems (connectivity, malformed messages, etc.)
     */
    Promise<User> getUser(URI userUri);

    /**
     * Create user. By default created user will not be notified with email. If password field is not
     * set then password will be randomly generated.
     *
     * @param userInput UserInput with data to update
     * @return complete information about selected user
     * @throws URISyntaxException
     * @throws RestClientException in case of problems (connectivity, malformed messages, etc.)
     *
     * @since v5.1.0
     */
    Promise<User> createUser(UserInput userInput) throws URISyntaxException;

    /**
     * Modify user. The "value" fields present will override the existing value. Fields skipped in
     * request will not be changed.
     *
     * @param userUri   URI to selected user resource
     * @param userInput UserInput with data to update
     * @return complete information about selected user
     * @throws RestClientException in case of problems (connectivity, malformed messages, etc.)
     *
     * @since v5.1.0
     */
    Promise<User> updateUser(URI userUri, UserInput userInput);

    /**
     * Removes user.
     *
     * @param userUri URI to selected user resource
     * @return Void
     * @throws RestClientException in case of problems (connectivity, malformed messages, etc.)
     *
     * @since v5.1.0
     */
    Promise<Void> removeUser(URI userUri);

    /**
     * Returns a list of users that match the search string. This resource cannot be accessed
     * anonymously.
     *
     * @param username A query string used to search username, name or e-mail address
     * @return list of users that match the search string
     * @throws URISyntaxException
     * @throws RestClientException in case of problems (connectivity, malformed messages, etc.)
     *
     * @since v5.1.0
     */
    Promise<List<User>> findUsers(String username) throws URISyntaxException;

    /**
     * Returns a list of users that match the search string. This resource cannot be accessed
     * anonymously.
     *
     * @param username        A query string used to search username, name or e-mail address
     * @param startAt         The index of the first user to return (0-based)
     * @param maxResults      The maximum number of users to return (defaults to 50). The maximum
     *                        allowed value is 1000. If you specify a value that is higher than this
     *                        number, your search results will be truncated.
     * @param includeActive   If true, then active users are included in the results (default true)
     * @param includeInactive If true, then inactive users are included in the results (default false)
     * @return list of users that match the search string
     * @throws URISyntaxException
     * @throws RestClientException in case of problems (connectivity, malformed messages, etc.)
     *
     * @since v5.1.0
     */
    Promise<List<User>> findUsers(String username, @Nullable Integer startAt, @Nullable Integer maxResults, @Nullable Boolean includeActive,
            @Nullable Boolean includeInactive) throws URISyntaxException;

    /**
     * Returns a list of users that can be assigned to a issue. This resource cannot be accessed
     * anonymously.
     *
     * @param projectKey      issueId or issueKey
     * @param startAt         The index of the first user to return (0-based)
     * @param maxResults      The maximum number of users to return (defaults to 50). The maximum
     *                        allowed value is 1000. If you specify a value that is higher than this
     *                        number, your search results will be truncated.
     * @param includeActive   If true, then active users are included in the results (default true)
     * @param includeInactive If true, then inactive users are included in the results (default false)
     * @return list of users that match the search string
     * @throws URISyntaxException
     * @throws RestClientException in case of problems (connectivity, malformed messages, etc.)
     *
     * @since v6.0.0
     */
    Promise<List<User>> findAssignableUsersForIssue(final String issueKey, @Nullable Integer startAt, @Nullable Integer maxResults,
            @Nullable Boolean includeActive, @Nullable Boolean includeInactive) throws URISyntaxException;

    Promise<List<User>> findAssignableUsersForProject(final String projectKey, @Nullable Integer startAt, @Nullable Integer maxResults,
            @Nullable Boolean includeActive, @Nullable Boolean includeInactive) throws URISyntaxException;

}
