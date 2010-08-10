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

import javax.annotation.Nullable;

/**
 * TODO: Document this class / interface here
 *
 * @since v0.1
 */
public class IssueArgs {
	private final String key;
	private final boolean withComments;
	private final boolean withAttachments;
	private final boolean withWorklogs;
	private final String renderer;
	private final boolean withWatchers;

	public IssueArgs(String key, boolean withComments, boolean withAttachments, boolean withWorklogs, String renderer, boolean withWatchers) {
		this.key = key;
		this.withComments = withComments;
		this.withAttachments = withAttachments;
		this.withWorklogs = withWorklogs;
		this.renderer = renderer;
		this.withWatchers = withWatchers;
	}

	public String getKey() {
		return key;
	}


    @Nullable
    public String getRenderer() {
        return renderer;
    }

	public boolean withComments() {
		return withComments;
	}

	public boolean withAttachments() {
		return withAttachments;
	}

	public boolean withWorklogs() {
		return withWorklogs;
	}

	public boolean withWatchers() {
		return withWatchers;
	}
}
