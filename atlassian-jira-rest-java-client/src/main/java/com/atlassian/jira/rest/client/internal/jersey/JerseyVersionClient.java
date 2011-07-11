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
import com.atlassian.jira.rest.client.internal.json.JsonParser;
import com.atlassian.jira.rest.client.internal.json.VersionJsonParser;
import com.atlassian.jira.rest.client.internal.json.VersionRelatedIssueCountJsonParser;
import com.atlassian.jira.rest.client.internal.json.gen.VersionInputJsonGenerator;
import com.sun.jersey.client.apache.ApacheHttpClient;
import org.codehaus.jettison.json.JSONException;
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
		return postAndParse(versionRootUri, new VersionInputGeneratorCallable(version), new VersionJsonParser(), progressMonitor);
	}

	@Override
	public Version updateVersion(URI versionUri, final VersionInput version, ProgressMonitor progressMonitor) {
		return putAndParse(versionUri, new VersionInputGeneratorCallable(version), new VersionJsonParser(), progressMonitor);
	}

	@Override
	public void removeVersion(URI versionUri, @Nullable URI moveFixIssuesToVersionUri,
			@Nullable URI moveAffectedIssuesToVersionUri, ProgressMonitor progressMonitor) {
		final UriBuilder uriBuilder = UriBuilder.fromUri(versionUri);
		if (moveFixIssuesToVersionUri != null) {
			uriBuilder.queryParam("moveFixIssuesTo", moveFixIssuesToVersionUri);
		}
		if (moveAffectedIssuesToVersionUri != null) {
			uriBuilder.queryParam("moveAffectedIssuesTo", moveAffectedIssuesToVersionUri);
		}
		delete(uriBuilder.build(), progressMonitor);
	}

	@Override
	public Version getVersion(URI versionUri, ProgressMonitor progressMonitor) {
		return getAndParse(versionUri, new VersionJsonParser(), progressMonitor);
	}

	@Override
	public VersionRelatedIssuesCount getVersionRelatedIssuesCount(URI versionUri, ProgressMonitor progressMonitor) {
		final URI relatedIssueCountsUri = UriBuilder.fromUri(versionUri).path("relatedIssueCounts").build();
		return getAndParse(relatedIssueCountsUri, new VersionRelatedIssueCountJsonParser(), progressMonitor);
	}

	@Override
	public int getNumUnresolvedIssues(URI versionUri, ProgressMonitor progressMonitor) {
		final URI unresolvedIssueCountUri = UriBuilder.fromUri(versionUri).path("unresolvedIssueCount").build();
		return getAndParse(unresolvedIssueCountUri, new JsonParser<Integer>() {
			@Override
			public Integer parse(JSONObject json) throws JSONException {
				return json.getInt("issuesUnresolvedCount");
			}
		}, progressMonitor);
	}

	@Override
	public Version moveVersionAfter(URI versionUri, URI afterVersionUri, ProgressMonitor progressMonitor) {
		return null;
	}

	@Override
	public Version moveVersion(URI versionUri, VersionPosition versionPosition, ProgressMonitor progressMonitor) {
		return null;
	}

	private static class VersionInputGeneratorCallable implements Callable<JSONObject> {
		private final VersionInput version;

		public VersionInputGeneratorCallable(VersionInput version) {
			this.version = version;
		}

		@Override
		public JSONObject call() throws Exception {
			return new VersionInputJsonGenerator().generate(version);
		}
	}
}
