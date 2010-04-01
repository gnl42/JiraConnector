/*******************************************************************************
 * Copyright (c) 2004, 2008 Eugene Kuleshov and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Eugene Kuleshov - initial API and implementation
 *     Tasktop Technologies - improvements
 *******************************************************************************/

package com.atlassian.connector.eclipse.internal.jira.ui;

import com.atlassian.connector.eclipse.internal.jira.core.JiraCorePlugin;
import com.atlassian.connector.eclipse.internal.jira.core.model.filter.ContentFilter;
import com.atlassian.connector.eclipse.internal.jira.core.model.filter.FilterDefinition;
import com.atlassian.connector.eclipse.internal.jira.core.util.JiraUtil;

import org.eclipse.mylyn.internal.tasks.core.AbstractSearchHandler;
import org.eclipse.mylyn.tasks.core.IRepositoryQuery;
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.eclipse.mylyn.tasks.core.data.TaskData;

import java.util.StringTokenizer;

/**
 * Stack Trace duplicate detector
 * 
 * @author Eugene Kuleshov
 * @author Steffen Pingel
 */
@SuppressWarnings("restriction")
public class JiraSearchHandler extends AbstractSearchHandler {

	private static final int MAX_LENGTH = 950;

	@Override
	public String getConnectorKind() {
		return JiraCorePlugin.CONNECTOR_KIND;
	}

	@Override
	public boolean queryForText(TaskRepository taskRepository, IRepositoryQuery query, TaskData taskData,
			String searchString) {
		final String preparedSearchString = prepareSearchString(searchString);
		FilterDefinition filter = new FilterDefinition();
		filter.setContentFilter(new ContentFilter(preparedSearchString, false, true, false, true));
		JiraUtil.setQuery(taskRepository, query, filter);
		return true;
	}

	public static String prepareSearchString(String searchString) {
		StringBuilder sb = new StringBuilder(MAX_LENGTH);
		// on JIRA 4+ you cannot search for :
		// on older JIRAs your search fails if the query is too long
		// because I don't have a progress monitor here, I can not easily fetch JIRA version (it may be not yet cached)
		// so I assume the worst and apply the solution (not perfect but good enough) which should
		// work with all supported JIRA versions
		StringTokenizer t = new StringTokenizer(searchString, " :\n\t()$"); //$NON-NLS-1$
		while (t.hasMoreTokens() && sb.length() < MAX_LENGTH - 20) {
			if (sb.length() > 0) {
				sb.append(" AND "); //$NON-NLS-1$
			}
			int remaining = MAX_LENGTH - sb.length();
			String token = t.nextToken();
			if (token.length() > remaining) {
				sb.append(token.substring(0, remaining));
				sb.append("*"); //$NON-NLS-1$
			} else {
				sb.append(token);
			}
		}
		return sb.toString();
	}

}
