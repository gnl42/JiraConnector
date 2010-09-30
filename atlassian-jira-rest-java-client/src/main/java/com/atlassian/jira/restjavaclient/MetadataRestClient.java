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
 * TODO: Document this class / interface here
 *
 * @since v0.1
 */
public interface MetadataRestClient {
	IssueType getIssueType(URI uri, ProgressMonitor progressMonitor);
	Status getStatus(URI uri, ProgressMonitor progressMonitor);
	Priority getPriority(URI uri, ProgressMonitor progressMonitor);
	Resolution getResolution(URI uri, ProgressMonitor progressMonitor);

	ServerInfo getServerInfo(ProgressMonitor progressMonitor);
}
