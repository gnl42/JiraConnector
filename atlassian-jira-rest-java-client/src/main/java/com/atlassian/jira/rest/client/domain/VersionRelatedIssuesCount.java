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

package com.atlassian.jira.rest.client.domain;

import com.atlassian.jira.rest.client.AddressableEntity;

import java.net.URI;

/**
 * TODO: Document this class / interface here
 *
 * @since v4.4
 */
public class VersionRelatedIssuesCount implements AddressableEntity{

	private final int numFixedIssues;

	private final int numAffectedIssues;

	public VersionRelatedIssuesCount(int numFixedIssues, int numAffectedIssues) {
		this.numAffectedIssues = numAffectedIssues;
		this.numFixedIssues = numFixedIssues;
	}

	@Override
	public URI getSelf() {
		return null;
	}

	public int getNumFixedIssues() {
		return numFixedIssues;
	}

	public int getNumAffectedIssues() {
		return numAffectedIssues;
	}
}
