/*******************************************************************************
 * Copyright (c) 2004, 2008 Tasktop Technologies and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Tasktop Technologies - initial API and implementation
 *******************************************************************************/

package me.glindholm.connector.eclipse.jira.tests.util;

import java.util.ArrayList;
import java.util.List;

import me.glindholm.connector.eclipse.internal.jira.core.model.JiraIssue;
import me.glindholm.connector.eclipse.internal.jira.core.model.filter.IssueCollector;

/**
 * @author Steffen Pingel
 */
public class MockIssueCollector implements IssueCollector {

	public List<JiraIssue> issues = new ArrayList<>();

	public boolean done;

	public boolean started;

	@Override
	public void collectIssue(final JiraIssue issue) {
		issues.add(issue);
	}

	@Override
	public void done() {
		done = true;
	}

	@Override
	public int getMaxHits() {
		return 1000;
	}

	@Override
	public boolean isCancelled() {
		return false;
	}

	@Override
	public void start() {
		started = true;
	}

}