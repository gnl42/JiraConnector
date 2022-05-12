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

import static me.glindholm.jira.rest.client.api.IssueRestClient.Expandos.NAMES;
import static me.glindholm.jira.rest.client.api.IssueRestClient.Expandos.SCHEMA;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Set;

import javax.annotation.Nullable;

import org.apache.commons.lang3.StringUtils;
import org.apache.hc.core5.net.URIBuilder;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import com.atlassian.httpclient.api.HttpClient;
import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;

import io.atlassian.util.concurrent.Promise;
import me.glindholm.jira.rest.client.api.IssueRestClient;
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

    private static final Function<IssueRestClient.Expandos, String> EXPANDO_TO_PARAM = new Function<>() {
        @Override
        public String apply(IssueRestClient.Expandos from) {
            return from.name().toLowerCase();
        }
    };

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

    public AsynchronousSearchRestClient(final URI baseUri, final HttpClient asyncHttpClient) throws URISyntaxException {
        super(asyncHttpClient);
        this.baseUri = baseUri;
        this.searchUri = new URIBuilder(baseUri).appendPath(SEARCH_URI_PREFIX).build();
        this.favouriteUri = new URIBuilder(baseUri).appendPath(FILTER_FAVOURITE_PATH).build();
    }

    @Override
    public Promise<SearchResult> searchJql(@Nullable String jql) throws URISyntaxException {
        return searchJql(jql, null, null, null);
    }

    @Override
    public Promise<SearchResult> searchJql(@Nullable String jql, @Nullable Integer maxResults, @Nullable Integer startAt, @Nullable Set<String> fields)
            throws URISyntaxException {
        final Iterable<String> expandosValues = Iterables.transform(ImmutableList.of(SCHEMA, NAMES), EXPANDO_TO_PARAM);
        final String notNullJql = StringUtils.defaultString(jql);
        if (notNullJql.length() > MAX_JQL_LENGTH_FOR_HTTP_GET) {
            return searchJqlImplPost(maxResults, startAt, expandosValues, notNullJql, fields);
        } else {
            return searchJqlImplGet(maxResults, startAt, expandosValues, notNullJql, fields);
        }
    }

    private Promise<SearchResult> searchJqlImplGet(@Nullable Integer maxResults, @Nullable Integer startAt, Iterable<String> expandosValues, String jql,
            @Nullable Set<String> fields) throws URISyntaxException {
        final URIBuilder uriBuilder = new URIBuilder(searchUri)
                .addParameter(JQL_ATTRIBUTE, jql)
                .addParameter(EXPAND_ATTRIBUTE, Joiner.on(",").join(expandosValues));

        if (fields != null) {
            uriBuilder.addParameter(FIELDS_ATTRIBUTE, Joiner.on(",").join(fields));
        }
        addOptionalQueryParam(uriBuilder, MAX_RESULTS_ATTRIBUTE, maxResults);
        addOptionalQueryParam(uriBuilder, START_AT_ATTRIBUTE, startAt);

        return getAndParse(uriBuilder.build(), searchResultJsonParser);
    }

    private void addOptionalQueryParam(final URIBuilder uriBuilder, final String key, final Object value) {
        if (value != null) {
            uriBuilder.addParameter(key, String.valueOf(value));
        }
    }

    private Promise<SearchResult> searchJqlImplPost(@Nullable Integer maxResults, @Nullable Integer startAt, Iterable<String> expandosValues, String jql, @Nullable Set<String> fields) {
        final JSONObject postEntity = new JSONObject();

        try {
            postEntity.put(JQL_ATTRIBUTE, jql)
            .put(EXPAND_ATTRIBUTE, ImmutableList.copyOf(expandosValues))
            .putOpt(START_AT_ATTRIBUTE, startAt)
            .putOpt(MAX_RESULTS_ATTRIBUTE, maxResults);

            if (fields != null) {
                postEntity.put(FIELDS_ATTRIBUTE, fields); // putOpt doesn't work with collections
            }
        } catch (JSONException e) {
            throw new RestClientException(e);
        }
        return postAndParse(searchUri, postEntity, searchResultJsonParser);
    }

    @Override
    public Promise<Iterable<Filter>> getFavouriteFilters() {
        return getAndParse(favouriteUri, filtersParser);
    }

    @Override
    public Promise<Filter> getFilter(URI filterUri) {
        return getAndParse(filterUri, filterJsonParser);
    }

    @Override
    public Promise<Filter> getFilter(long id) throws URISyntaxException {
        return getFilter(new URIBuilder(baseUri).appendPath(String.format(FILTER_PATH_FORMAT, id)).build());
    }
}
