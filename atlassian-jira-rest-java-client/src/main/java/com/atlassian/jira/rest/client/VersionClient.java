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

package com.atlassian.jira.rest.client;

import com.atlassian.jira.rest.client.domain.Version;
import com.atlassian.jira.rest.client.domain.VersionRelatedIssuesCount;
import com.atlassian.jira.rest.client.domain.input.VersionInput;
import com.atlassian.jira.rest.client.domain.input.VersionPosition;

import javax.annotation.Nullable;
import java.net.URI;

/**
 * The client responsible for Project version(s) related operations
 *
 * @since 0.3 client, 4.4 server
 */
public interface VersionClient {

	Version createVersion(VersionInput version, ProgressMonitor progressMonitor);
	Version updateVersion(URI versionUri, VersionInput versionInput, ProgressMonitor progressMonitor);
	void removeVersion(URI versionUri, @Nullable URI moveFixIssuesToVersionUri,
			@Nullable URI moveAffectedIssuesToVersionUri, ProgressMonitor progressMonitor);
	Version getVersion(URI versionUri, ProgressMonitor progressMonitor);
	VersionRelatedIssuesCount getVersionRelatedIssuesCount(URI versionUri, ProgressMonitor progressMonitor);
	int getNumUnresolvedIssues(URI versionUri, ProgressMonitor progressMonitor);

	Version moveVersionAfter(URI versionUri, URI afterVersionUri, ProgressMonitor progressMonitor);
	Version moveVersion(URI versionUri, VersionPosition versionPosition, ProgressMonitor progressMonitor);

}
