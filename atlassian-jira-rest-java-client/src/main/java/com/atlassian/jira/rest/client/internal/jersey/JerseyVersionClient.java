/*
 * Copyright (C) 2011 Atlassian
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

import com.atlassian.jira.rest.client.ProgressMonitor;
import com.atlassian.jira.rest.client.VersionClient;
import com.atlassian.jira.rest.client.domain.Version;
import com.atlassian.jira.rest.client.domain.VersionRelatedIssuesCount;
import com.atlassian.jira.rest.client.domain.input.VersionInput;
import com.atlassian.jira.rest.client.domain.input.VersionPosition;
import com.atlassian.jira.rest.client.internal.json.JsonParseUtil;
import com.atlassian.jira.rest.client.internal.json.VersionJsonParser;
import com.sun.jersey.client.apache.ApacheHttpClient;
import org.codehaus.jettison.json.JSONObject;

import javax.annotation.Nullable;
import javax.ws.rs.core.UriBuilder;
import java.net.URI;
import java.util.concurrent.Callable;

/**
 * TODO: Document this class / interface here
 *
 * @since v4.4
 */
public class JerseyVersionClient extends AbstractJerseyRestClient implements VersionClient {

	private final URI versionRootUri;

	public JerseyVersionClient(URI baseUri, ApacheHttpClient client) {
		super(baseUri, client);
		versionRootUri = UriBuilder.fromUri(baseUri).path("version").build();
	}

	@Override
	public Version createVersion(final VersionInput version, ProgressMonitor progressMonitor) {
		return postAndParse(versionRootUri, new Callable<JSONObject>()	{
			@Override
			public JSONObject call() throws Exception {
				final JSONObject jsonObject = new JSONObject();
				jsonObject.put("name", version.getName());
				jsonObject.put("project", version.getProjectKey());
				if (version.getDescription() != null) {
					jsonObject.put("description", version.getDescription());
				}
				if (version.getReleaseDate() != null) {
					jsonObject.put("releaseDate", JsonParseUtil.format(version.getReleaseDate()));
				}
				jsonObject.put("released", version.isReleased());
				jsonObject.put("archived", version.isArchived());
				return jsonObject;
			}
		}, new VersionJsonParser(), progressMonitor);
	}

	@Override
	public void removeVersion(URI versionUri, @Nullable String moveFixIssuesTo, @Nullable String moveAffectedIssuesTo, ProgressMonitor progressMonitor) {
	}

	@Override
	public Version getVersion(URI versionUri, ProgressMonitor progressMonitor) {
		return null;
	}

	@Override
	public VersionRelatedIssuesCount getVersionRelatedIssuesCount(URI versionUri, ProgressMonitor progressMonitor) {
		return null;
	}

	@Override
	public int getNumUnresolvedIssues(URI versionUri, ProgressMonitor progressMonitor) {
		return 0;
	}

	@Override
	public Version moveVersionAfter(URI versionUri, URI afterVersionUri, ProgressMonitor progressMonitor) {
		return null;
	}

	@Override
	public Version moveVersion(URI versionUri, VersionPosition versionPosition, ProgressMonitor progressMonitor) {
		return null;
	}
}
