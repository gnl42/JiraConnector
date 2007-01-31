package org.eclipse.mylar.jira.core.internal.model.filter;

import org.eclipse.mylar.jira.core.internal.model.Issue;

public final class SingleIssueCollector implements IssueCollector {
	private Issue matchingIssue;

	public Issue getIssue() {
		return matchingIssue;
	}

	public void done() {
	}

	public boolean isCancelled() {
		return false;
	}

	public void collectIssue(Issue issue) {
		matchingIssue = issue;
	}

	public void start() {
	}
}