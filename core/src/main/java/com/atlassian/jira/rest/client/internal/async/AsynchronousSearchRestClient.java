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

import com.atlassian.jira.rest.client.internal.json.SearchResultJsonParser;
import com.atlassian.httpclient.api.HttpClient;
import com.atlassian.jira.rest.client.api.RestClientException;
import com.atlassian.jira.rest.client.api.SearchRestClient;
import com.atlassian.jira.rest.client.api.domain.SearchResult;
import com.atlassian.util.concurrent.Promise;
import org.apache.commons.lang.StringUtils;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import javax.annotation.Nullable;
import javax.ws.rs.core.UriBuilder;
import java.net.URI;

/**
 * Asynchronous implementation of SearchRestClient.
 *
 * @since v2.0
 */
public class AsynchronousSearchRestClient extends AbstractAsynchronousRestClient implements SearchRestClient {

	private static final String START_AT_ATTRIBUTE = "startAt";
	private static final String MAX_RESULTS_ATTRIBUTE = "maxResults";
	private static final int MAX_JQL_LENGTH_FOR_HTTP_GET = 500;
	private static final String JQL_ATTRIBUTE = "jql";
	private final SearchResultJsonParser searchResultJsonParser = new SearchResultJsonParser();

	private static final String SEARCH_URI_PREFIX = "search";
	private final URI searchUri;

	public AsynchronousSearchRestClient(final URI baseUri, final HttpClient asyncHttpClient) {
		super(asyncHttpClient);
		this.searchUri = UriBuilder.fromUri(baseUri).path(SEARCH_URI_PREFIX).build();
	}

	@Override
	public Promise<SearchResult> searchJql(@Nullable String jql) {
		if (jql == null) {
			jql = StringUtils.EMPTY;
		}
		if (jql.length() > MAX_JQL_LENGTH_FOR_HTTP_GET) {
			final JSONObject postEntity = new JSONObject();
			try {
				postEntity.put(JQL_ATTRIBUTE, jql);
			} catch (JSONException e) {
				throw new RestClientException(e);
			}
			return postAndParse(searchUri, postEntity, searchResultJsonParser);
		} else {
			final URI uri = UriBuilder.fromUri(searchUri).queryParam(JQL_ATTRIBUTE, jql).build();
			return getAndParse(uri, searchResultJsonParser);
		}
	}

	@Override
	public Promise<SearchResult> searchJql(@Nullable String jql, int maxResults, int startAt) {
		if (jql == null) {
			jql = StringUtils.EMPTY;
		}
		if (jql.length() > MAX_JQL_LENGTH_FOR_HTTP_GET) {
			final JSONObject postEntity = new JSONObject();
			try {
				postEntity.put(JQL_ATTRIBUTE, jql);
				postEntity.put(START_AT_ATTRIBUTE, startAt);
				postEntity.put(MAX_RESULTS_ATTRIBUTE, maxResults);
			} catch (JSONException e) {
				throw new RestClientException(e);
			}
			return postAndParse(searchUri, postEntity, searchResultJsonParser);
		} else {
			final URI uri = UriBuilder.fromUri(searchUri).queryParam(JQL_ATTRIBUTE, jql)
					.queryParam(MAX_RESULTS_ATTRIBUTE, maxResults)
					.queryParam(START_AT_ATTRIBUTE, startAt).build();
			return getAndParse(uri, searchResultJsonParser);
		}
	}
}
