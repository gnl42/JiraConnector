package com.atlassian.jira.restjavaclient;

import javax.annotation.Nullable;

/**
 * TODO: Document this class / interface here
 *
 * @since v0.1
 */
public class IssueArgs {
	public IssueArgs(String key) {
		this.key = key;
	}

	public String getKey() {
		return key;
	}

	private final String key;

	public IssueArgs withComments(boolean withComments) {
		this.withComments = withComments;
		return this;
	}

	public IssueArgs withAttachments(boolean withAttachments) {
		this.withAttachments = withAttachments;
		return this;
	}

	private boolean withComments;
	private boolean withAttachments;

    @Nullable
    public String getRenderer() {
        return renderer;
    }

    private String renderer;


	public boolean withComments() {
		return withComments;
	}

	public boolean withAttachments() {
		return withAttachments;
	}

	public IssueArgs withRenderer(String renderer) {
		this.renderer = renderer;
		return this;
	}
}
