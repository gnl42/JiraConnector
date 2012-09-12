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

package com.atlassian.jira.rest.client;

import com.atlassian.jira.rest.client.domain.User;

import java.net.URI;

/**
 * The client handling user resources.
 *
 * @since v0.1
 */
public interface UserRestClient {
	/**
	 * Retrieves detailed information about selected user.
	 * Try to use {@link #getUser(URI, ProgressMonitor)} instead as that method is more RESTful (well connected)
	 *
	 * @param username JIRA username/login
	 * @param progressMonitor progress monitor
	 * @return complete information about given user
	 * @throws RestClientException in case of problems (connectivity, malformed messages, etc.)
	 */
	User getUser(String username, ProgressMonitor progressMonitor);

	/**
	 * Retrieves detailed information about selected user.
	 * This method is preferred over {@link #getUser(String, ProgressMonitor)} as it's more RESTful (well connected) 
	 *
	 * @param userUri URI of user resource
	 * @param progressMonitor progress monitor
	 * @return complete information about given user
	 * @throws RestClientException in case of problems (connectivity, malformed messages, etc.)
	 */
	User getUser(URI userUri, ProgressMonitor progressMonitor);
}
