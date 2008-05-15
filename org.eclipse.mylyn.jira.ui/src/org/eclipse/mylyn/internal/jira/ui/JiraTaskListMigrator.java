/*******************************************************************************
 * Copyright (c) 2004, 2007 Mylyn project committers and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.mylyn.internal.jira.ui;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.mylyn.internal.jira.core.JiraCorePlugin;
import org.eclipse.mylyn.tasks.core.AbstractTaskListMigrator;
import org.eclipse.mylyn.tasks.core.IRepositoryQuery;
import org.eclipse.mylyn.tasks.core.ITask;
import org.w3c.dom.Element;

/**
 * @author Steffen Pingel
 */
public class JiraTaskListMigrator extends AbstractTaskListMigrator {

	private static final String KEY_FILTER_CUSTOM_URL = "FilterCustomUrl";

	private static final String KEY_FILTER_ID = "FilterID";

	private static final String KEY_FILTER_NAME = "FilterName";

	private static final String KEY_JIRA_CUSTOM = "JiraJiraCustomQuery";

	private static final String KEY_JIRA_ISSUE = "JiraIssue";

	private static final String KEY_JIRA_QUERY = "JiraQuery";

	@Override
	public String getConnectorKind() {
		return JiraCorePlugin.CONNECTOR_KIND;
	}

	@Override
	public Set<String> getQueryElementNames() {
		Set<String> names = new HashSet<String>();
		names.add(KEY_JIRA_QUERY);
		names.add(KEY_JIRA_CUSTOM);
		return names;
	}

	@Override
	public String getTaskElementName() {
		return KEY_JIRA_ISSUE;
	}

	@Override
	public void migrateQuery(IRepositoryQuery query, Element element) {
		query.setAttribute(KEY_FILTER_CUSTOM_URL, element.getAttribute(KEY_FILTER_CUSTOM_URL));
		query.setAttribute(KEY_FILTER_ID, element.getAttribute(KEY_FILTER_ID));
		query.setAttribute(KEY_FILTER_NAME, element.getAttribute(KEY_FILTER_NAME));
	}

	@Override
	public void migrateTask(ITask task, Element element) {
		// nothing to do
	}

}
