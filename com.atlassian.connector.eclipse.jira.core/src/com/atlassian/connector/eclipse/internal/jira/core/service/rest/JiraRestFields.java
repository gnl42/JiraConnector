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

package com.atlassian.connector.eclipse.internal.jira.core.service.rest;

public class JiraRestFields {
	private JiraRestFields() throws Exception {
		throw new Exception("Utility class"); //$NON-NLS-1$
	}

	static final String NAME = "name"; //$NON-NLS-1$

	static final String ASSIGNEE = "assignee"; //$NON-NLS-1$

	static final String DESCRIPTION = "description"; //$NON-NLS-1$

	static final String COMPONENTS = "components"; //$NON-NLS-1$

	static final String FIX_VERSIONS = "fixVersions"; //$NON-NLS-1$

	static final String VERSIONS = "versions"; //$NON-NLS-1$

	static final String DATE_FORMAT = "yyyy-MM-dd"; //$NON-NLS-1$

	static final String DUEDATE = "duedate"; //$NON-NLS-1$

	static final String PRIORITY = "priority"; //$NON-NLS-1$

	static final String ISSUETYPE = "issuetype"; //$NON-NLS-1$

	static final String KEY = "key"; //$NON-NLS-1$

	static final String PARENT = "parent"; //$NON-NLS-1$

	static final String ID = "id"; //$NON-NLS-1$

	static final String SECURITY = "security"; //$NON-NLS-1$

	static final String TIMETRACKING = "timetracking"; //$NON-NLS-1$

	static final String REMAINING_ESTIMATE = "remainingEstimate"; //$NON-NLS-1$

	static final String ORIGINAL_ESTIMATE = "originalEstimate"; //$NON-NLS-1$

	static final String ENVIRONMENT = "environment"; //$NON-NLS-1$

	public static final String SUMMARY = "summary"; //$NON-NLS-1$

}
