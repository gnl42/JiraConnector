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

package com.atlassian.jira.rest.client;

import com.atlassian.jira.rest.client.domain.SearchResult;

import javax.annotation.Nullable;

/**
 * The client handling search REST resource
 *
 * @since v0.2
 */
public interface SearchRestClient {
	/**
	 * Performs a JQL search and returns issues matching the query
	 *
	 * @param jql a valid JQL query (will be properly encoded by JIRA client). Restricted JQL characters (like '/') must be properly escaped.
	 * @param progressMonitor progress monitor
	 * @return issues matching given JQL query
	 * @throws RestClientException in case of problems (connectivity, malformed messages, invalid JQL query, etc.)
	 */
	SearchResult searchJql(@Nullable String jql, ProgressMonitor progressMonitor);
}
