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

package com.atlassian.jira.rest.restjavaclient.internal.jersey;

import com.atlassian.jira.rest.restjavaclient.ProgressMonitor;
import com.atlassian.jira.rest.restjavaclient.UserRestClient;
import com.atlassian.jira.rest.restjavaclient.domain.User;
import com.atlassian.jira.rest.restjavaclient.internal.json.UserJsonParser;
import com.sun.jersey.client.apache.ApacheHttpClient;

import javax.ws.rs.core.UriBuilder;
import java.net.URI;

/**
 * Jersey-based implementation of UserRestClient
 *
 * @since v0.1
 */
public class JerseyUserRestClient extends AbstractJerseyRestClient implements UserRestClient {
	private static final String USER_URI_PREFIX = "user";
	private final UserJsonParser userJsonParser = new UserJsonParser();

	public JerseyUserRestClient(URI baseUri, ApacheHttpClient client) {
		super(baseUri, client);
	}

	@Override
	public User getUser(String username, ProgressMonitor progressMonitor) {
		final URI userUri = UriBuilder.fromUri(baseUri).path(USER_URI_PREFIX)
				.queryParam("username", username).queryParam("expand", "groups").build();
		return getUser(userUri, progressMonitor);
	}

	@Override
	public User getUser(URI userUri, ProgressMonitor progressMonitor) {
		return getAndParse(userUri, userJsonParser, progressMonitor);
	}
}
