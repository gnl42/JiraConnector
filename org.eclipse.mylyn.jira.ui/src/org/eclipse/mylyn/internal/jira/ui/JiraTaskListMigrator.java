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

package org.eclipse.mylyn.internal.jira.ui;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.mylyn.internal.jira.core.JiraCorePlugin;
import org.eclipse.mylyn.internal.jira.core.util.JiraUtil;
import org.eclipse.mylyn.tasks.core.AbstractTaskListMigrator;
import org.eclipse.mylyn.tasks.core.IRepositoryQuery;
import org.eclipse.mylyn.tasks.core.ITask;
import org.w3c.dom.Element;

/**
 * @author Steffen Pingel
 */
public class JiraTaskListMigrator extends AbstractTaskListMigrator {

	private static final String KEY_FILTER_CUSTOM_URL = "FilterCustomUrl"; //$NON-NLS-1$

	private static final String KEY_FILTER_ID = "FilterID"; //$NON-NLS-1$

	private static final String KEY_FILTER_NAME = "FilterName"; //$NON-NLS-1$

	private static final String KEY_JIRA_CUSTOM = "JiraJiraCustomQuery"; //$NON-NLS-1$

	private static final String KEY_JIRA_ISSUE = "JiraIssue"; //$NON-NLS-1$

	private static final String KEY_JIRA_QUERY = "JiraQuery"; //$NON-NLS-1$

	private static final String KEY_KEY = "Key"; //$NON-NLS-1$

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
		task.setModificationDate(JiraUtil.stringToDate(element.getAttribute(KEY_LAST_MOD_DATE)));
		task.setTaskKey(element.getAttribute(KEY_KEY));
	}

}
