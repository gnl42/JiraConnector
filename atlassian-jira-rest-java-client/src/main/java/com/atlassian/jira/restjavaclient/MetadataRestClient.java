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

import com.atlassian.jira.restjavaclient.domain.IssueType;
import com.atlassian.jira.restjavaclient.domain.Priority;
import com.atlassian.jira.restjavaclient.domain.Resolution;
import com.atlassian.jira.restjavaclient.domain.ServerInfo;
import com.atlassian.jira.restjavaclient.domain.Status;

import java.net.URI;

/**
 * Serves information about JIRA metadata like server information, issue types defined, stati, priorities and resolutions.
 * This data constitutes a data dictionary which then JIRA issues base on.
 *
 * @since v0.1
 */
public interface MetadataRestClient {
	/**
	 * Retrieves from the server complete information about selected issue type
	 * @param uri URI to issue type resource (one can get it e.g. from <code>self</code> attribute
	 * of issueType field of an issue).
	 * @param progressMonitor progress monitor
	 * @return complete information about issue type resource
	 */
	IssueType getIssueType(URI uri, ProgressMonitor progressMonitor);

	/**
	 * Retrieves complete information about selected status
	 * @param uri URI to this status resource (one can get it e.g. from <code>self</code> attribute
	 * of <code>status</code> field of an issue)
	 * @param progressMonitor progress monitor
	 * @return complete information about the selected status
	 */
	Status getStatus(URI uri, ProgressMonitor progressMonitor);

	/**
	 * Retrieves from the server complete information about selected priority
	 * @param uri URI for the priority resource
	 * @param progressMonitor progress monitor
	 * @return complete information about the selected priority
	 */
	Priority getPriority(URI uri, ProgressMonitor progressMonitor);


	/**
	 * Retrieves from the server complete information about selected resolution
	 * @param uri URI for the resolution resource
	 * @param progressMonitor progress monitor
	 * @return complete information about the selected resolution
	 */
	Resolution getResolution(URI uri, ProgressMonitor progressMonitor);

	/**
	 * Retrieves information about this JIRA instance
	 * @param progressMonitor progress monitor
	 * @return information about this JIRA instance
	 */
	ServerInfo getServerInfo(ProgressMonitor progressMonitor);
}
