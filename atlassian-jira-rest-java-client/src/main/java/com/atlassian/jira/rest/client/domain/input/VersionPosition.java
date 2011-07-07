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

package com.atlassian.jira.rest.client.domain.input;

/**
* Defines a new position for a project version (while moving it) by {@link com.atlassian.jira.rest.client.VersionClient#moveVersion(java.net.URI, VersionPosition, com.atlassian.jira.rest.client.ProgressMonitor)}
*
*
* @since 0.3 client, 4.4 server
*/
public enum VersionPosition {
	FIRST, LAST, EARLIER, LATER
}
