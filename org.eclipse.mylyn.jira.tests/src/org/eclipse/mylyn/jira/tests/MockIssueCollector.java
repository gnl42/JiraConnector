/**
 * 
 */
package org.eclipse.mylyn.jira.tests;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.mylyn.internal.jira.core.model.Issue;
import org.eclipse.mylyn.internal.jira.core.model.filter.IssueCollector;

public class MockIssueCollector implements IssueCollector {

	List<Issue> issues = new ArrayList<Issue>();

	boolean done;

	boolean started;

	public void collectIssue(Issue issue) {
		issues.add(issue);
	}

	public void done() {
		done = true;
	}

	public int getMaxHits() {
		return 5000;
	}

	public boolean isCancelled() {
		return false;
	}

	public void start() {
		this.started = true;
	}

}