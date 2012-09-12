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
public interface VersionRestClient {

	/**
	 * Retrieves full information about selected project version
	 *
	 * @param versionUri URI of the version to retrieve. You can get it for example from Project or it can be
	 *        referenced from an issue.
	 * @param progressMonitor progress monitor
	 * @return full information about selected project version
	 * @throws RestClientException in case of problems (connectivity, malformed messages, etc.)
	 */
	Version getVersion(URI versionUri, ProgressMonitor progressMonitor);

	/**
	 * Creates a new version (which logically belongs to a project)
	 *
	 * @param version details about version to create
	 * @param progressMonitor progress monitor
	 * @return newly created version
	 * @throws RestClientException in case of problems (connectivity, malformed messages, etc.)
	 */
	Version createVersion(VersionInput version, ProgressMonitor progressMonitor);

	/**
	 * Updates selected version with a new details.
	 *
	 * @param versionUri full URI to the version to update
	 * @param versionInput new details of the version. <code>null</code> fields will be ignored
	 * @param progressMonitor progress monitor
	 * @return newly updated version
	 * @throws RestClientException in case of problems (connectivity, malformed messages, etc.)
	 */
	Version updateVersion(URI versionUri, VersionInput versionInput, ProgressMonitor progressMonitor);

	/**
	 * Removes selected version optionally changing Fix Version(s) and/or Affects Version(s) fields of related issues.
	 *
	 * @param versionUri full URI to the version to remove
	 * @param moveFixIssuesToVersionUri URI of the version to which issues should have now set their Fix Version(s)
	 *        field instead of the just removed version. Use <code>null</code> to simply clear Fix Version(s) in all those issues
	 *        where the version removed was referenced.
	 * @param moveAffectedIssuesToVersionUri URI of the version to which issues should have now set their Affects Version(s)
	 *        field instead of the just removed version. Use <code>null</code> to simply clear Affects Version(s) in all those issues
	 *        where the version removed was referenced.
	 * @param progressMonitor progress monitor
	 * @throws RestClientException in case of problems (connectivity, malformed messages, etc.)
	 */
	void removeVersion(URI versionUri, @Nullable URI moveFixIssuesToVersionUri,
			@Nullable URI moveAffectedIssuesToVersionUri, ProgressMonitor progressMonitor);

	/**
	 * Retrieves basic statistics about issues which have their Fix Version(s) or Affects Version(s) field
	 * pointing to given version.
	 *
	 * @param versionUri full URI to the version you want to get related issues count for
	 * @param progressMonitor progress monitor
	 * @throws RestClientException in case of problems (connectivity, malformed messages, etc.)
	 * @return basic stats about issues related to given version
	 */
	VersionRelatedIssuesCount getVersionRelatedIssuesCount(URI versionUri, ProgressMonitor progressMonitor);

	/**
	 * Retrieves number of unresolved issues which have their Fix Version(s) field
	 * pointing to given version.
	 *
	 * @param versionUri full URI to the version you want to get the number of unresolved issues for
	 * @param progressMonitor progress monitor
	 * @throws RestClientException in case of problems (connectivity, malformed messages, etc.)
	 * @return number of unresolved issues having this version included in their Fix Version(s) field.
	 */
	int getNumUnresolvedIssues(URI versionUri, ProgressMonitor progressMonitor);

	/**
	 * Moves selected version after another version. Ordering of versions is important on various reports and whenever
	 * input version fields are rendered by JIRA.
	 * If version is already immediately after the other version (defined by <code>afterVersionUri</code>) then
	 * such call has no visual effect.
	 *
	 * @param versionUri full URI to the version to move
	 * @param afterVersionUri URI of the version to move selected version after
	 * @param progressMonitor progress monitor
	 * @throws RestClientException in case of problems (connectivity, malformed messages, etc.)
	 * @return just moved version
	 */
	Version moveVersionAfter(URI versionUri, URI afterVersionUri, ProgressMonitor progressMonitor);

	/**
	 * Moves selected version to another position.
	 * If version already occupies given position (e.g. is the last version and we want to move to a later position or to the last position)
	 * then such call does not change anything.
	 *
	 * @param versionUri full URI to the version to move
	 * @param versionPosition defines a new position of selected version
	 * @param progressMonitor progress monitor
	 * @throws RestClientException in case of problems (connectivity, malformed messages, etc.)
	 * @return just moved version
	 */
	Version moveVersion(URI versionUri, VersionPosition versionPosition, ProgressMonitor progressMonitor);

}
