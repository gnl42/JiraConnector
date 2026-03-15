/*
 * Copyright (C) 2011 Atlassian
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
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
import java.util.Set;

import org.eclipse.jdt.annotation.Nullable;

import java.util.concurrent.CompletableFuture;
import me.glindholm.jira.rest.client.api.domain.Filter;
import me.glindholm.jira.rest.client.api.domain.SearchResult;

/**
 * The client handling search REST resource
 *
 * @since 2.0 client, 4.3 server
 */
public interface SearchRestClient {
    /**
     * Performs a JQL search and returns issues matching the query
     *
     * @param jql a valid JQL query (will be properly encoded by JIRA client). Restricted JQL characters
     *            (like '/') must be properly escaped.
     * @return issues matching given JQL query
     * @throws URISyntaxException
     * @throws RestClientException in case of problems (connectivity, malformed messages, invalid JQL
     *                             query, etc.)
     */
    CompletableFuture<SearchResult> searchJql(@Nullable String jql, final boolean newJqlPath) throws URISyntaxException;

    // ...existing javadoc...
    CompletableFuture<SearchResult> searchJql(@Nullable String jql, @Nullable Integer maxResults,
    		@Nullable Integer startAt, @Nullable Set<String> fields, final boolean newJqlPath)
            throws URISyntaxException;

    /**
     * Retrieves list of your favourite filters.
     */
    CompletableFuture<List<Filter>> getFavouriteFilters();

    /**
     * Retrieves filter for given URI.
     */
    CompletableFuture<Filter> getFilter(URI filterUri);

    /**
     * Retrieves filter for given id.
     */
    CompletableFuture<Filter> getFilter(long id) throws URISyntaxException;
}
