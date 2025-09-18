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

package me.glindholm.connector.eclipse.internal.jira.ui;

import java.util.HashSet;
import java.util.Set;

import me.glindholm.connector.eclipse.internal.jira.core.JiraCorePlugin;

/**
 * @author Steffen Pingel
 */
public class JiraTaskListMigrator {

    private static final String KEY_FILTER_CUSTOM_URL = "FilterCustomUrl"; //$NON-NLS-1$

    private static final String KEY_FILTER_ID = "FilterID"; //$NON-NLS-1$

    private static final String KEY_FILTER_NAME = "FilterName"; //$NON-NLS-1$

    private static final String KEY_JIRA_CUSTOM = "JiraJiraCustomQuery"; //$NON-NLS-1$

    private static final String KEY_JIRA_ISSUE = "JiraIssue"; //$NON-NLS-1$

    private static final String KEY_JIRA_QUERY = "JiraQuery"; //$NON-NLS-1$

    private static final String KEY_KEY = "Key"; //$NON-NLS-1$

    public String getConnectorKind() {
        return JiraCorePlugin.CONNECTOR_KIND;
    }

    public Set<String> getQueryElementNames() {
        final Set<String> names = new HashSet<>();
        names.add(KEY_JIRA_QUERY);
        names.add(KEY_JIRA_CUSTOM);
        return names;
    }

    public String getTaskElementName() {
        return KEY_JIRA_ISSUE;
    }

    // Migration using TaskData (modern Mylyn)
//    public void migrateQuery(final IRepositoryQuery query, final TaskData data) {
//        query.setAttribute(KEY_FILTER_CUSTOM_URL, data.getRoot().getAttribute(KEY_FILTER_CUSTOM_URL));
//        query.setAttribute(KEY_FILTER_ID, data.getRoot().getAttribute(KEY_FILTER_ID));
//        query.setAttribute(KEY_FILTER_NAME, data.getRoot().getAttribute(KEY_FILTER_NAME));
//    }

//    public void migrateTask(final ITask task, final TaskData data) {
//        String lastModDate = data.getRoot().getAttribute(KEY_LAST_MOD_DATE);
//        if (lastModDate != null) {
//            task.setModificationDate(Date.from(JiraUtil.stringToDate(lastModDate)));
//        }
//        task.setTaskKey(data.getRoot().getAttribute(KEY_KEY));
//    }

    // Optional: legacy XML migration for backward compatibility
//    public void migrateQueryFromElement(final IRepositoryQuery query, final Element element) {
//        query.setAttribute(KEY_FILTER_CUSTOM_URL, element.getAttribute(KEY_FILTER_CUSTOM_URL));
//        query.setAttribute(KEY_FILTER_ID, element.getAttribute(KEY_FILTER_ID));
//        query.setAttribute(KEY_FILTER_NAME, element.getAttribute(KEY_FILTER_NAME));
//    }

//    public void migrateTaskFromElement(final ITask task, final Element element) {
//        task.setModificationDate(Date.from(JiraUtil.stringToDate(element.getAttribute(KEY_LAST_MOD_DATE))));
//        task.setTaskKey(element.getAttribute(KEY_KEY));
//    }

}