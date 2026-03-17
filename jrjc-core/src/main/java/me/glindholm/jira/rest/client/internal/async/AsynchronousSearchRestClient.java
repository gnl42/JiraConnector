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

import static me.glindholm.jira.rest.client.api.IssueRestClient.Expandos.EDITMETA;
import static me.glindholm.jira.rest.client.api.IssueRestClient.Expandos.NAMES;
import static me.glindholm.jira.rest.client.api.IssueRestClient.Expandos.SCHEMA;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.codehaus.jettison.json.JSONObject;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.eclipse.jdt.annotation.Nullable;

import me.glindholm.jira.rest.client.api.RestClientException;
import me.glindholm.jira.rest.client.api.SearchRestClient;
import me.glindholm.jira.rest.client.api.domain.Filter;
import me.glindholm.jira.rest.client.api.domain.SearchResult;
import me.glindholm.jira.rest.client.internal.json.FilterJsonParser;
import me.glindholm.jira.rest.client.internal.json.GenericJsonArrayParser;
import me.glindholm.jira.rest.client.internal.json.SearchResultJsonParser;

/**
 * Asynchronous implementation of SearchRestClient.
 *
 * @since v2.0
 */
public class AsynchronousSearchRestClient extends AbstractAsynchronousRestClient implements SearchRestClient {

    private static final List<String> SEARCH_EXPANDOS = List.of(SCHEMA, NAMES, EDITMETA).stream().map(expando -> expando.name().toLowerCase())
            .collect(Collectors.toUnmodifiableList());

    private static final String START_AT_ATTRIBUTE = "startAt";
    private static final String MAX_RESULTS_ATTRIBUTE = "maxResults";
    private static final int MAX_JQL_LENGTH_FOR_HTTP_GET = 500;
    private static final String JQL_ATTRIBUTE = "jql";
    private static final String FILTER_FAVOURITE_PATH = "filter/favourite";
    private static final String FILTER_PATH_FORMAT = "filter/%s";
    private static final String SEARCH_URI_PREFIX = "search";
    private static final String EXPAND_ATTRIBUTE = "expand";
    private static final String FIELDS_ATTRIBUTE = "fields";

    private final SearchResultJsonParser searchResultJsonParser = new SearchResultJsonParser();
    private final FilterJsonParser filterJsonParser = new FilterJsonParser();
    private final GenericJsonArrayParser<Filter> filtersParser = GenericJsonArrayParser.create(new FilterJsonParser());

    private final URI searchUri;
    private final URI favouriteUri;
    private final URI baseUri;

    public AsynchronousSearchRestClient(final URI baseUri, final DisposableHttpClient asyncHttpClient) throws URISyntaxException {
        super(asyncHttpClient);
        this.baseUri = baseUri;
        searchUri = new UriBuilder(baseUri).appendPath(SEARCH_URI_PREFIX).build();
        favouriteUri = new UriBuilder(baseUri).appendPath(FILTER_FAVOURITE_PATH).build();
    }

    @Override
    public CompletableFuture<SearchResult> searchJql(@Nullable final String jql, final boolean newpqlPath) throws URISyntaxException {
        return searchJql(jql, null, null, null, newpqlPath);
    }

    @Override
    public CompletableFuture<SearchResult> searchJql(@Nullable final String jql, @Nullable final Integer maxResults, @Nullable final Integer startAt,
            @Nullable final Set<String> fields, final boolean newpqlPath) throws URISyntaxException {
        final String notNullJql = StringUtils.defaultString(jql);
        if (notNullJql.length() > MAX_JQL_LENGTH_FOR_HTTP_GET) {
            return searchJqlImplPost(maxResults, startAt, SEARCH_EXPANDOS, notNullJql, fields, newpqlPath);
        } else {
            return searchJqlImplGet(maxResults, startAt, SEARCH_EXPANDOS, notNullJql, fields, newpqlPath);
        }
    }

    private CompletableFuture<SearchResult> searchJqlImplGet(@Nullable final Integer maxResults, @Nullable final Integer startAt, final List<String> expandosValues,
            final String jql, @Nullable final Set<String> fields, final boolean newJqlPath) throws URISyntaxException {
        final UriBuilder uriBuilder = new UriBuilder(searchUri).appendPath(newJqlPath ? "/jql" : "")
                .addParameter(JQL_ATTRIBUTE, jql).addParameter(EXPAND_ATTRIBUTE, String.join(",", expandosValues));

        if (fields != null) {
            uriBuilder.addParameter(FIELDS_ATTRIBUTE, String.join(",", fields));
        }
        addOptionalQueryParam(uriBuilder, MAX_RESULTS_ATTRIBUTE, maxResults);
        addOptionalQueryParam(uriBuilder, START_AT_ATTRIBUTE, startAt);

        return getAndParse(uriBuilder.build(), searchResultJsonParser);
    }

    private static void addOptionalQueryParam(final UriBuilder uriBuilder, final String key, final Object value) {
        if (value != null) {
            uriBuilder.addParameter(key, String.valueOf(value));
        }
    }

    private CompletableFuture<SearchResult> searchJqlImplPost(@Nullable final Integer maxResults, @Nullable final Integer startAt, final List<String> expandosValues,
            final String jql, @Nullable final Set<String> fields, final boolean newJqlPath) throws URISyntaxException {
        final JSONObject postEntity = new JSONObject();

        try {
            postEntity.put(JQL_ATTRIBUTE, jql).put(EXPAND_ATTRIBUTE, List.copyOf(expandosValues)).putOpt(START_AT_ATTRIBUTE, startAt)
                    .putOpt(MAX_RESULTS_ATTRIBUTE, maxResults);

            if (fields != null) {
                postEntity.put(FIELDS_ATTRIBUTE, fields);
            }
        } catch (final JsonProcessingException e) {
            throw new RestClientException(e);
        }
        return postAndParse(new UriBuilder(searchUri).appendPath(newJqlPath ? "/jql" : "").build(), postEntity, searchResultJsonParser);
    }

    @Override
    public CompletableFuture<List<Filter>> getFavouriteFilters() {
        return getAndParse(favouriteUri, filtersParser);
    }

    @Override
    public CompletableFuture<Filter> getFilter(final URI filterUri) {
        return getAndParse(filterUri, filterJsonParser);
    }

    @Override
    public CompletableFuture<Filter> getFilter(final long id) throws URISyntaxException {
        return getFilter(new UriBuilder(baseUri).appendPath(String.format(FILTER_PATH_FORMAT, id)).build());
    }
}