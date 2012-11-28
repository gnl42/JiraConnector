/*******************************************************************************
 * Copyright (c) 2009 Atlassian and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Atlassian - initial API and implementation
 ******************************************************************************/

package com.atlassian.connector.eclipse.internal.jira.core.model.filter;

/**
 * Representation of special values which can be used for some of standard JIRA fields. The {@link #getClassic()}
 * returns value for classic queries and the {@link #getJql()} for JQL ones.
 */
public enum JiraFieldSpecialValue {

	ISSUE_SPECIFIC_GROUP("specificgroup", "membersOf"), //$NON-NLS-1$ //$NON-NLS-2$ 
	ISSUE_SPECIFIC_USER("specificuser", null), //$NON-NLS-1$ 
	ISSUE_CURRENT_USER("issue_current_user", "currentUser()"), //$NON-NLS-1$ //$NON-NLS-2$ 
	ISSUE_NO_REPORTER("issue_no_reporter", "EMPTY"), //$NON-NLS-1$ //$NON-NLS-2$

	ISSUE_TYPE_STANDARD(null, "standardIssueTypes()"), //$NON-NLS-1$
	ISSUE_TYPE_SUBTASK(null, "subTaskIssueTypes()"), //$NON-NLS-1$

	UNASSIGNED("unassigned", "EMPTY"), //$NON-NLS-1$ //$NON-NLS-2$ 
	VERSION_NONE("-1", "EMPTY"), //$NON-NLS-1$ //$NON-NLS-2$ 
	VERSION_RELEASED("-3", "releasedVersions()"), //$NON-NLS-1$ //$NON-NLS-2$ 
	VERSION_UNRELEASED("-2", "unreleasedVersions()"), //$NON-NLS-1$ //$NON-NLS-2$ 
	UNRESOLVED("-1", "Unresolved"), //$NON-NLS-1$ //$NON-NLS-2$ 
	COMPONENT_NONE("-1", "EMPTY"); //$NON-NLS-1$ //$NON-NLS-2$

	private String classicValue;

	private String jqlValue;

	/**
	 * @param classic
	 *            representation of value in classic query
	 * @param jql
	 *            representation of value in JQL query
	 */
	private JiraFieldSpecialValue(String classic, String jql) {
		this.classicValue = classic;
		this.jqlValue = jql;
	}

	/**
	 * @param classicAndJql
	 *            value is the same for classic and JQL queries
	 */
	private JiraFieldSpecialValue(String classicAndJql) {
		this(classicAndJql, classicAndJql);
	}

	public String getClassic() {
		return classicValue;
	}

	public String getJql() {
		return jqlValue;
	}

	@Override
	public String toString() {
		return classicValue;
	}
}
