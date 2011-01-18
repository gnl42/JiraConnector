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

	/**
	 * Performs a JQL search and returns issues matching the query using default maxResults (as configured in JIRA - usually 50) and startAt=0
	 *
	 * @param jql a valid JQL query (will be properly encoded by JIRA client). Restricted JQL characters (like '/') must be properly escaped.
	 * @param maxResults maximum results (page/window size) for this search. The page will contain issues from
	 * <code>startAt div maxResults</code> (no remnant) and will include at most <code>maxResults</code> matching issues.
	 * @param startAt starting index (0-based) defining the page/window for the results. It will be aligned by the server to the beginning
	 * on the page (startAt = startAt div maxResults). For example for startAt=5 and maxResults=3 the results will include matching issues
	 * with index 3, 4 and 5. For startAt = 6 and maxResults=3 the issues returned are from position 6, 7 and 8.
	 * @param progressMonitor progress monitor
	 * @return issues matching given JQL query
	 * @throws RestClientException in case of problems (connectivity, malformed messages, invalid JQL query, etc.)
	 */
	SearchResult searchJql(@Nullable String jql, int maxResults, int startAt, ProgressMonitor progressMonitor);
}
