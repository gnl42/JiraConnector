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