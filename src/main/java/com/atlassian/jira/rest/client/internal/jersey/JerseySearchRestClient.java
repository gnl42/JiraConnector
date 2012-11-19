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

package com.atlassian.jira.rest.client.internal.jersey;

import com.atlassian.jira.rest.client.*;
import com.atlassian.jira.rest.client.domain.FavouriteFilter;
import com.atlassian.jira.rest.client.domain.SearchResult;
import com.atlassian.jira.rest.client.internal.json.FavouriteFilterJsonParser;
import com.atlassian.jira.rest.client.internal.json.GenericJsonArrayParser;
import com.atlassian.jira.rest.client.internal.json.SearchResultJsonParser;
import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.collect.Iterables;
import com.sun.jersey.client.apache.ApacheHttpClient;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import javax.annotation.Nullable;
import javax.ws.rs.core.UriBuilder;
import java.net.URI;
import java.util.EnumSet;

/**
 * Jersey-based implementation of SearchRestClient
 *
 * @since v0.2
 */
public class JerseySearchRestClient extends AbstractJerseyRestClient implements SearchRestClient{
    private static final EnumSet<IssueRestClient.Expandos> DEFAULT_EXPANDS = EnumSet.of(IssueRestClient.Expandos.NAMES, IssueRestClient.Expandos.SCHEMA);
    private static final Function<IssueRestClient.Expandos, String> EXPANDO_TO_PARAM = new Function<IssueRestClient.Expandos, String>() {
        @Override
        public String apply(IssueRestClient.Expandos from) {
            return from.name().toLowerCase();
        }
    };
    private static final String EXPAND_ATTRIBUTE = "expand";
    private static final String START_AT_ATTRIBUTE = "startAt";
	private static final String MAX_RESULTS_ATTRIBUTE = "maxResults";
	private static final int MAX_JQL_LENGTH_FOR_HTTP_GET = 500;
	private static final String JQL_ATTRIBUTE = "jql";
	private final SearchResultJsonParser keyOnlySearchResultJsonParser = new SearchResultJsonParser(false);
    private final SearchResultJsonParser fullSearchResultJsonParser = new SearchResultJsonParser(true);
    private final GenericJsonArrayParser<FavouriteFilter> favouriteFiltersJsonParser = GenericJsonArrayParser.create(new FavouriteFilterJsonParser());

	private static final String SEARCH_URI_PREFIX = "search";
	private final URI searchUri;

	public JerseySearchRestClient(URI baseUri, ApacheHttpClient client) {
		super(baseUri, client);
		searchUri = UriBuilder.fromUri(baseUri).path(SEARCH_URI_PREFIX).build();
	}

	@Override
	public SearchResult searchJql(@Nullable String jql, ProgressMonitor progressMonitor) {
        return searchJqlImpl(jql, null, null, progressMonitor, keyOnlySearchResultJsonParser);
	}

	@Override
	public SearchResult searchJql(@Nullable String jql, int maxResults, int startAt, ProgressMonitor progressMonitor) {
		return searchJqlImpl(jql, maxResults, startAt, progressMonitor, keyOnlySearchResultJsonParser);
	}

    @Override
    public SearchResult searchJqlWithFullIssues(@Nullable String jql, int maxResults, int startAt, ProgressMonitor progressMonitor) {
        return searchJqlImpl(jql, maxResults, startAt, progressMonitor, fullSearchResultJsonParser);
    }

    private SearchResult searchJqlImpl(@Nullable String jql, Integer maxResults, Integer startAt, ProgressMonitor progressMonitor, SearchResultJsonParser parser) {
        if (jql == null) {
            jql = "";
        }

        if (jql.length() > MAX_JQL_LENGTH_FOR_HTTP_GET) {
            UriBuilder uriBuilder = UriBuilder.fromUri(searchUri);
            final JSONObject postEntity = new JSONObject();
            try {
                postEntity.put(JQL_ATTRIBUTE, jql);
                if (maxResults != null && startAt != null) {
                    postEntity.put(START_AT_ATTRIBUTE, startAt);
                    postEntity.put(MAX_RESULTS_ATTRIBUTE, maxResults);
                }
                uriBuilder = uriBuilder.queryParam(EXPAND_ATTRIBUTE, Joiner.on(',').join(Iterables.transform(DEFAULT_EXPANDS, EXPANDO_TO_PARAM)));
            } catch (JSONException e) {
                throw new RestClientException(e);
            }
            return postAndParse(uriBuilder.build(), postEntity, parser, progressMonitor);
        } else {
            UriBuilder uriBuilder = UriBuilder.fromUri(searchUri).queryParam(JQL_ATTRIBUTE, jql);
            if (maxResults != null && startAt != null) {
                uriBuilder = uriBuilder.queryParam(MAX_RESULTS_ATTRIBUTE, maxResults).queryParam(START_AT_ATTRIBUTE, startAt);
            }
            URI uri = uriBuilder.queryParam(EXPAND_ATTRIBUTE, Joiner.on(',').join(Iterables.transform(DEFAULT_EXPANDS, EXPANDO_TO_PARAM))).build();
            return getAndParse(uri, parser, progressMonitor);
        }
    }

        @Override
    public Iterable<FavouriteFilter> getFavouriteFilters(NullProgressMonitor progressMonitor) {
        final URI uri = UriBuilder.fromUri(baseUri).path("filter/favourite").build();
        return getAndParse(uri, favouriteFiltersJsonParser, progressMonitor);
    }
}
