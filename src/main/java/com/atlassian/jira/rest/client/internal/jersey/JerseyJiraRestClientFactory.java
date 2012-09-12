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

package com.atlassian.jira.rest.client.internal.jersey;

import com.atlassian.jira.rest.client.AuthenticationHandler;
import com.atlassian.jira.rest.client.JiraRestClient;
import com.atlassian.jira.rest.client.JiraRestClientFactory;
import com.atlassian.jira.rest.client.auth.BasicHttpAuthenticationHandler;

import java.net.URI;

/**
 * Servers Jersey-based implementations of the JiraRestClient
 *
 * @since v0.1
 */
public class JerseyJiraRestClientFactory implements JiraRestClientFactory {
    @Override
    public JiraRestClient create(URI serverUri, AuthenticationHandler authenticationHandler) {
        return new JerseyJiraRestClient(serverUri, authenticationHandler);
    }

	@Override
	public JiraRestClient createWithBasicHttpAuthentication(URI serverUri, String username, String password) {
		return create(serverUri, new BasicHttpAuthenticationHandler(username, password));
	}


}
