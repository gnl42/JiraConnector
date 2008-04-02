/*******************************************************************************
 * Copyright (c) 2005, 2007 Mylyn project committers and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
/**
 * 
 */
package org.eclipse.mylyn.jira.tests;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.mylyn.internal.jira.core.model.JiraIssue;
import org.eclipse.mylyn.internal.jira.core.model.filter.IssueCollector;

public class MockIssueCollector implements IssueCollector {

	List<JiraIssue> issues = new ArrayList<JiraIssue>();

	boolean done;

	boolean started;

	public void collectIssue(JiraIssue issue) {
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