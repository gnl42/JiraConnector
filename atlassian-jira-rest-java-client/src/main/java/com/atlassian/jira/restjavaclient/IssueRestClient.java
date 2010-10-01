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

import com.atlassian.jira.restjavaclient.domain.*;

/**
 * TODO: Document this class / interface here
 *
 * @since v0.1
 */
public interface IssueRestClient {
	Issue getIssue(String issueKey, ProgressMonitor progressMonitor);

    Watchers getWatchers(Issue issue, ProgressMonitor progressMonitor);

	Votes getVotes(Issue issue, ProgressMonitor progressMonitor);
    
	Iterable<Transition> getTransitions(Issue issue, ProgressMonitor progressMonitor);

	void transition(Issue issue, TransitionInput transitionInput, ProgressMonitor progressMonitor);

	void vote(Issue issue, ProgressMonitor progressMonitor);
	void unvote(Issue issue, ProgressMonitor progressMonitor);

	void watch(Issue issue, ProgressMonitor progressMonitor);
	void unwatch(Issue issue, ProgressMonitor progressMonitor);
	void addWatcher(final Issue issue, final String username, ProgressMonitor progressMonitor);
	void removeWatcher(final Issue issue, final String username, ProgressMonitor progressMonitor);

}
