/*******************************************************************************
 * Copyright (c) 2004, 2008 Brock Janiczak and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Brock Janiczak - initial API and implementation
 *     Tasktop Technologies - improvements
 *******************************************************************************/

package com.atlassian.connector.eclipse.internal.jira.core.model.filter;

import com.atlassian.connector.eclipse.internal.jira.core.model.JiraIssue;

/**
 * @author Brock Janiczak
 */
public final class SingleIssueCollector implements IssueCollector {

	private JiraIssue matchingIssue;

	public JiraIssue getIssue() {
		return matchingIssue;
	}

	public void done() {
	}

	public boolean isCancelled() {
		return false;
	}

	public void collectIssue(JiraIssue issue) {
		matchingIssue = issue;
	}

	public void start() {
	}

	public int getMaxHits() {
		return 1;
	}
}