/**
 * 
 */
package org.eclipse.mylar.internal.jira;

import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.tigris.jira.core.model.Issue;
import org.tigris.jira.core.model.filter.IssueCollector;

/**
 * @author Mik Kersten
 */
class JiraIssueCollector implements IssueCollector {

	private final IProgressMonitor monitor;

	private final List<Issue> issues;

	private boolean done = false;

	JiraIssueCollector(IProgressMonitor monitor, List<Issue> issues) {
		this.monitor = monitor;
		this.issues = issues;
	}

	public void done() {
		done = true;
	}

	public boolean isCancelled() {
		return monitor.isCanceled();
	}

	public void collectIssue(Issue issue) {
		issues.add(issue);
	}

	public void start() {

	}

	public boolean isDone() {
		return done;
	}
}