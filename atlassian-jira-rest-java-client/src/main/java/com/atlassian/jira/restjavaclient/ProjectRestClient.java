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

import com.atlassian.jira.restjavaclient.domain.Project;

/**
 * TODO: Document this class / interface here
 *
 * @since v0.1
 */
public interface ProjectRestClient {
	/**
	 * Retrieves complete information about given project 
	 * @param key unique key of the project (usually 2+ characters)
	 * @param progressMonitor progress monitor
	 * @return complete information about given project
	 */
	Project getProject(String key, ProgressMonitor progressMonitor);
}
