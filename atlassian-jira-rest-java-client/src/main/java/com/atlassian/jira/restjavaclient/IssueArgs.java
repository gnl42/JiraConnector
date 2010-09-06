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
	private final boolean withWatchers;
    private final boolean withHtml;

	public IssueArgs(String key, boolean withComments, boolean withAttachments, boolean withWorklogs, boolean withWatchers, boolean withHtml) {
		this.key = key;
		this.withComments = withComments;
		this.withAttachments = withAttachments;
		this.withWorklogs = withWorklogs;
		this.withWatchers = withWatchers;
        this.withHtml = withHtml;
    }

	public String getKey() {
		return key;
	}


	public boolean withComments() {
		return withComments;
	}

	public boolean withAttachments() {
		return withAttachments;
	}

    public boolean withHtml() {
        return withHtml;
    }

	public boolean withWorklogs() {
		return withWorklogs;
	}

	public boolean withWatchers() {
		return withWatchers;
	}
}
