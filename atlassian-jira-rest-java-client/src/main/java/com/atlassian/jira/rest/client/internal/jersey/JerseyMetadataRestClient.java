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

import com.atlassian.jira.rest.client.MetadataRestClient;
import com.atlassian.jira.rest.client.ProgressMonitor;
import com.atlassian.jira.rest.client.domain.IssueType;
import com.atlassian.jira.rest.client.domain.Priority;
import com.atlassian.jira.rest.client.domain.Resolution;
import com.atlassian.jira.rest.client.domain.ServerInfo;
import com.atlassian.jira.rest.client.domain.Status;
import com.atlassian.jira.rest.client.internal.json.IssueTypeJsonParser;
import com.atlassian.jira.rest.client.internal.json.PriorityJsonParser;
import com.atlassian.jira.rest.client.internal.json.ResolutionJsonParser;
import com.atlassian.jira.rest.client.internal.json.ServerInfoJsonParser;
import com.atlassian.jira.rest.client.internal.json.StatusJsonParser;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.client.apache.ApacheHttpClient;
import org.codehaus.jettison.json.JSONObject;

import javax.ws.rs.core.UriBuilder;
import java.net.URI;
import java.util.concurrent.Callable;

/**
 * TODO: Document this class / interface here
 *
 * @since v0.1
 */
public class JerseyMetadataRestClient extends AbstractJerseyRestClient implements MetadataRestClient {
	private final String SERVER_INFO_RESOURCE = "/serverInfo";
	private final ServerInfoJsonParser serverInfoJsonParser = new ServerInfoJsonParser();
	private final IssueTypeJsonParser issueTypeJsonParser = new IssueTypeJsonParser();
	private final StatusJsonParser statusJsonParser = new StatusJsonParser();
	private final PriorityJsonParser priorityJsonParser = new PriorityJsonParser();
	private final ResolutionJsonParser resolutionJsonParser = new ResolutionJsonParser();

	public JerseyMetadataRestClient(URI baseUri, ApacheHttpClient client) {
		super(baseUri, client);
	}

	@Override
	public IssueType getIssueType(final URI uri, ProgressMonitor progressMonitor) {
		return getAndParse(uri, issueTypeJsonParser, progressMonitor);
	}

	@Override
	public Status getStatus(final URI uri, ProgressMonitor progressMonitor) {
		return getAndParse(uri, statusJsonParser, progressMonitor);
	}

	@Override
	public Priority getPriority(final URI uri, ProgressMonitor progressMonitor) {
		return getAndParse(uri, priorityJsonParser, progressMonitor);
	}

	@Override
	public Resolution getResolution(URI uri, ProgressMonitor progressMonitor) {
		return getAndParse(uri, resolutionJsonParser, progressMonitor);
	}

	@Override
	public ServerInfo getServerInfo(ProgressMonitor progressMonitor) {
		return invoke(new Callable<ServerInfo>() {
			@Override
			public ServerInfo call() throws Exception {
				final WebResource serverInfoResource = client.resource(UriBuilder.fromUri(baseUri)
						.path(SERVER_INFO_RESOURCE).build());
				return serverInfoJsonParser.parse(serverInfoResource.get(JSONObject.class));
			}
		});
	}
}
