/*******************************************************************************
 * Copyright (c) 2006 - 2007 Mylar eclipse.org project and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Mylar project committers - initial API and implementation
 *******************************************************************************/

package org.eclipse.mylar.internal.jira.ui;

import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.mylar.internal.jira.core.model.Issue;
import org.eclipse.mylar.internal.jira.core.model.filter.IssueCollector;

/**
 * @author Mik Kersten
 */
class JiraIssueCollector implements IssueCollector {

	private final IProgressMonitor monitor;

	private final List<Issue> issues;

	private boolean done = false;

	private int maxHits;

	JiraIssueCollector(IProgressMonitor monitor, List<Issue> issues, int maxHits) {
		this.monitor = monitor;
		this.issues = issues;
		this.maxHits = maxHits;
	}

	public void start() {
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

	public boolean isDone() {
		return done;
	}

	public int getMaxHits() {
		return maxHits;
	}
}
