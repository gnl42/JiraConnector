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

public class IssueArgsBuilder {
	private final String key;
	private boolean withComments;
	private boolean withAttachments;
	private boolean withWorklogs;
	private String renderer;
	private boolean withWatchers;

	public IssueArgsBuilder(String key) {
		this.key = key;
	}

	public IssueArgsBuilder withComments(boolean withComments) {
		this.withComments = withComments;
		return this;
	}

	public IssueArgsBuilder withAttachments(boolean withAttachments) {
		this.withAttachments = withAttachments;
		return this;
	}

	public IssueArgsBuilder withWorklogs(boolean withWorklogs) {
		this.withWorklogs = withWorklogs;
		return this;
	}

	public IssueArgsBuilder withRenderer(String renderer) {
		this.renderer = renderer;
		return this;
	}

	public IssueArgsBuilder withWatchers(boolean withWatchers) {
		this.withWatchers = withWatchers;
		return this;
	}

	public IssueArgs build() {
		return new IssueArgs(key, withComments, withAttachments, withWorklogs, renderer, withWatchers);
	}
}