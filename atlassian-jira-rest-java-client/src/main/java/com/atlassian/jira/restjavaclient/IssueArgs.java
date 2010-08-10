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
