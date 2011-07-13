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

import com.atlassian.jira.rest.client.ComponentRestClient;
import com.atlassian.jira.rest.client.ProgressMonitor;
import com.atlassian.jira.rest.client.domain.Component;
import com.atlassian.jira.rest.client.domain.input.ComponentInput;
import com.atlassian.jira.rest.client.internal.domain.input.ComponentInputWithProjectKey;
import com.atlassian.jira.rest.client.internal.json.ComponentJsonParser;
import com.atlassian.jira.rest.client.internal.json.gen.ComponentInputWithProjectKeyJsonGenerator;
import com.sun.jersey.client.apache.ApacheHttpClient;

import javax.annotation.Nullable;
import javax.ws.rs.core.UriBuilder;
import java.net.URI;

/**
 * Jersey-based implementation of ComponentRestClient
 *
 * @since v0.1
 */
public class JerseyComponentRestClient extends AbstractJerseyRestClient implements ComponentRestClient {

	private final ComponentJsonParser componentJsonParser = new ComponentJsonParser();
	private final URI componentUri;

	public JerseyComponentRestClient(URI baseUri, ApacheHttpClient client) {
		super(baseUri, client);
		componentUri = UriBuilder.fromUri(baseUri).path("component").build();
	}

	@Override
	public Component getComponent(final URI componentUri, ProgressMonitor progressMonitor) {
		return getAndParse(componentUri, componentJsonParser, progressMonitor);
	}

	@Override
	public Component createComponent(String projectKey, ComponentInput componentInput, ProgressMonitor progressMonitor) {
		final ComponentInputWithProjectKey helper = new ComponentInputWithProjectKey(projectKey, componentInput);
		return postAndParse(componentUri, InputGeneratorCallable.create(new ComponentInputWithProjectKeyJsonGenerator(), helper),
				new ComponentJsonParser(), progressMonitor);
	}

	@Override
	public Component updateComponent(URI componentUri, ComponentInput componentInput, ProgressMonitor progressMonitor) {
		final ComponentInputWithProjectKey helper = new ComponentInputWithProjectKey(null, componentInput);
		return putAndParse(componentUri, InputGeneratorCallable.create(new ComponentInputWithProjectKeyJsonGenerator(), helper),
				new ComponentJsonParser(), progressMonitor);
	}

	@Override
	public void removeComponent(URI componentUri, @Nullable URI moveIssueToComponentUri, ProgressMonitor progressMonitor) {
		final UriBuilder uriBuilder = UriBuilder.fromUri(componentUri);
		if (moveIssueToComponentUri != null) {
			uriBuilder.queryParam("moveIssuesTo", moveIssueToComponentUri);
		}
		delete(uriBuilder.build(), progressMonitor);
	}

	@Override
	public int getComponentRelatedIssuesCount(URI componentUri, ProgressMonitor progressMonitor) {
		return 0;
	}

}
