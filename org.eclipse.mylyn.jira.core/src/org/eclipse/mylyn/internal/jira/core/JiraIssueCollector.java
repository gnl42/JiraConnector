/*******************************************************************************
 * Copyright (c) 2004, 2007 Mylyn project committers and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.mylyn.internal.jira.core;

import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.mylyn.internal.jira.core.model.JiraIssue;
import org.eclipse.mylyn.internal.jira.core.model.filter.IssueCollector;

/**
 * @author Mik Kersten
 */
class JiraIssueCollector implements IssueCollector {

	private final IProgressMonitor monitor;

	private final List<JiraIssue> issues;

	private boolean done = false;

	private final int maxHits;

	JiraIssueCollector(IProgressMonitor monitor, List<JiraIssue> issues, int maxHits) {
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

	public void collectIssue(JiraIssue issue) {
		issues.add(issue);
	}

	public boolean isDone() {
		return done;
	}

	public int getMaxHits() {
		return maxHits;
	}
}
