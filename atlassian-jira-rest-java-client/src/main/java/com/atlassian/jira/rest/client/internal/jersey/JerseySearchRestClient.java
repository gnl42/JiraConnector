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

import com.atlassian.jira.rest.client.ProgressMonitor;
import com.atlassian.jira.rest.client.SearchRestClient;
import com.atlassian.jira.rest.client.domain.SearchResult;
import com.atlassian.jira.rest.client.internal.json.SearchResultJsonParser;
import com.sun.jersey.client.apache.ApacheHttpClient;

import javax.annotation.Nullable;
import javax.ws.rs.core.UriBuilder;
import java.net.URI;

/**
 * Jersey-based implementation of SearchRestClient
 *
 * @since v0.2
 */
public class JerseySearchRestClient extends AbstractJerseyRestClient implements SearchRestClient{
	private final SearchResultJsonParser searchResultJsonParser = new SearchResultJsonParser();

	private static final String SEARCH_URI_PREFIX = "search";

	public JerseySearchRestClient(URI baseUri, ApacheHttpClient client) {
		super(baseUri, client);
	}

	@Override
	public SearchResult searchJql(@Nullable String jql, ProgressMonitor progressMonitor) {
		if (jql == null) {
			jql = "";
		}
		final URI uri = UriBuilder.fromUri(baseUri).path(SEARCH_URI_PREFIX).queryParam("jql", jql).build();
		return getAndParse(uri, searchResultJsonParser, progressMonitor);
	}
}
