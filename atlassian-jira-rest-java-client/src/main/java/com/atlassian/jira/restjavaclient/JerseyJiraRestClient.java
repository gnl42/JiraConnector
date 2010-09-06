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

package com.atlassian.jira.restjavaclient;

import com.atlassian.jira.restjavaclient.domain.User;
import com.sun.jersey.client.apache.ApacheHttpClient;
import com.sun.jersey.client.apache.config.ApacheHttpClientConfig;
import com.sun.jersey.client.apache.config.DefaultApacheHttpClientConfig;

import javax.ws.rs.core.UriBuilder;
import java.net.URI;

/**
 * TODO: Document this class / interface here
 *
 * @since v0.1
 */
public class JerseyJiraRestClient implements JiraRestClient {

	private ApacheHttpClient client;
	private final URI baseUri;
    private final IssueRestClient issueRestClient;

	public JerseyJiraRestClient(URI serverUri) {
		this.baseUri = UriBuilder.fromUri(serverUri).path("/rest/api/latest").build();
		DefaultApacheHttpClientConfig config = new DefaultApacheHttpClientConfig();
		config.getState().setCredentials(null, null, -1, "admin", "admin");
		// @todo check with Justus why 404 is returned instead of 401 when no credentials are provided automagically
		config.getProperties().put(ApacheHttpClientConfig.PROPERTY_PREEMPTIVE_AUTHENTICATION, true);
		client = ApacheHttpClient.create(config);
        issueRestClient = new JerseyIssueRestClient(baseUri, client);
	}

    @Override
    public IssueRestClient getIssueClient() {
        return issueRestClient;
    }

	@Override
    public void login() {
	}

	@Override
    public User getUser() {
		return null;
	}

}
