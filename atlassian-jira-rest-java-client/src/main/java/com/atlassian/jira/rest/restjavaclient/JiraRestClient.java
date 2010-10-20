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

package com.atlassian.jira.rest.restjavaclient;

/**
 * Main access point to REST client.
 * As there are many types resources exposed by JIRA REST API, various resources are grouped into clusters
 * and then handled by different specialized *RestClient classes.
 *
 * @since v0.1
 */
public interface JiraRestClient {
	/**
	 *
	 * @return client for performing operations on selected issue
	 */
    IssueRestClient getIssueClient();

	/**
	 * @return the client handling session information
	 */
    SessionRestClient getSessionClient();

	/**
	 * @return the client handling full user information
	 */
	UserRestClient getUserClient();

	/**
	 * @return the client handling project metadata
	 */
	ProjectRestClient getProjectClient();

	/**
	 * @return the client handling components
	 */
	ComponentRestClient getComponentClient();

	/**
	 * @return the client handling basic meta-data (data dictionaries defined in JIRA - like resolutions, statuses,
	 * priorities)
	 */
	MetadataRestClient getMetadataClient();
}
