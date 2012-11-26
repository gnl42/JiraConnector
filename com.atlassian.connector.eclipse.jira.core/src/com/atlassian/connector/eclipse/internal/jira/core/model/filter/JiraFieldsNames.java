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

public class JiraFieldsNames {

	/**
	 * Factory method which returns an instance of JiraFieldName which returns field names for classic query.
	 * 
	 * @return JiraFieldName for classic query
	 */
	public static JiraFieldsNames createClassic() {
		return new JiraFieldsNames(true);
	}

	/**
	 * Factory method which returns an instance of JiraFieldName which returns field names for JQL query.
	 * 
	 * @return JiraFieldName for JQL query
	 */
	public static JiraFieldsNames createJql() {
		return new JiraFieldsNames(true);
	}

	public String AFFECTED_VERSION() {
		return value(JiraFields.AFFECTED_VERSION);
	}

	public String ASSIGNEE() {
		return value(JiraFields.ASSIGNEE);
	}

	public String COMMENT() {
		return value(JiraFields.COMMENT);
	}

	public String COMPONENT() {
		return value(JiraFields.COMPONENT);
	}

	public String CREATED() {
		return value(JiraFields.CREATED);
	}

	public String DESCRIPTION() {
		return value(JiraFields.DESCRIPTION);
	}

	public String DUE_DATE() {
		return value(JiraFields.DUE_DATE);
	}

	public String ENVIRONMENT() {
		return value(JiraFields.ENVIRONMENT);
	}

	public String FIX_VERSION() {
		return value(JiraFields.FIX_VERSION);
	}

	public String ISSUE_TYPE() {
		return value(JiraFields.ISSUE_TYPE);
	}

	public String ISSUE_KEY() {
		return value(JiraFields.ISSUE_KEY);
	}

	public String PRIORITY() {
		return value(JiraFields.PRIORITY);
	}

	public String PROJECT() {
		return value(JiraFields.PROJECT);
	}

	public String REPORTER() {
		return value(JiraFields.REPORTER);
	}

	public String RESOLUTION() {
		return value(JiraFields.RESOLUTION);
	}

	public String STATUS() {
		return value(JiraFields.STATUS);
	}

	public String SUMMARY() {
		return value(JiraFields.SUMMARY);
	}

	public String UPDATED() {
		return value(JiraFields.UPDATED);
	}

	private final boolean isClassic;

	/**
	 * @param isClassic
	 *            true - will return classic field values, false - will return JQL field values
	 */
	private JiraFieldsNames(boolean isClassic) {
		this.isClassic = isClassic;
	}

	private String value(JiraFields jiraField) {
		return isClassic ? jiraField.getClassic() : jiraField.getJql();
	}

}