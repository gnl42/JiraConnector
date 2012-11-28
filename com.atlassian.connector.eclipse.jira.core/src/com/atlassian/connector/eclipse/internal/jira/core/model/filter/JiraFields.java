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
 * Enumeration keeping mapping (jiraField -> classicName, jqlName). See also the {@link JiraFieldsNames} helper class
 * which wraps the getClassic()/getJql() getters.
 */
public enum JiraFields {
	AFFECTED_VERSION("version", "affectedVersion"), //$NON-NLS-1$ //$NON-NLS-2$
	ASSIGNEE("assignee"), //$NON-NLS-1$
	COMMENT("body", "comment"), //$NON-NLS-1$ //$NON-NLS-2$
	COMPONENT("component"), //$NON-NLS-1$
	CREATED("created"), //$NON-NLS-1$
	DESCRIPTION("description"), //$NON-NLS-1$
	DUE_DATE("duedate"), //$NON-NLS-1$
	ENVIRONMENT("environment"), //$NON-NLS-1$
	FIX_VERSION("fixfor", "fixVersion"), //$NON-NLS-1$ //$NON-NLS-2$
	ISSUE_TYPE("issuetype"), //$NON-NLS-1$
	ISSUE_KEY("issuekey"), //$NON-NLS-1$
	PRIORITY("priority"), //$NON-NLS-1$
	PROJECT("pid", "project"), //$NON-NLS-1$ //$NON-NLS-2$
	REPORTER("reporter"), //$NON-NLS-1$
	RESOLUTION("resolution"), //$NON-NLS-1$	
	STATUS("status"), //$NON-NLS-1$
	SUMMARY("summary"), //$NON-NLS-1$	
	UPDATED("updated"); //$NON-NLS-1$

	private final String classicFieldName;

	private final String jqlFieldName;

	/**
	 * @param classicFieldName
	 *            name of the field for classic query
	 * @param jqlFieldName
	 *            name of the field for JQL query
	 */
	private JiraFields(String classicFieldName, String jqlFieldName) {
		this.classicFieldName = classicFieldName;
		this.jqlFieldName = jqlFieldName;
	}

	private JiraFields(String classicFieldName) {
		this(classicFieldName, classicFieldName);
	}

	public String getClassic() {
		return this.classicFieldName;
	}

	public String getJql() {
		return this.jqlFieldName;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return this.classicFieldName;
	}
}
