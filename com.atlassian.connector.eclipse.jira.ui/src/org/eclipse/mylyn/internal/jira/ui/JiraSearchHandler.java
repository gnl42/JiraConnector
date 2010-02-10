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

package org.eclipse.mylyn.internal.jira.ui;

import java.util.StringTokenizer;

import org.eclipse.mylyn.internal.jira.core.JiraCorePlugin;
import org.eclipse.mylyn.internal.jira.core.model.filter.ContentFilter;
import org.eclipse.mylyn.internal.jira.core.model.filter.FilterDefinition;
import org.eclipse.mylyn.internal.jira.core.util.JiraUtil;
import org.eclipse.mylyn.internal.tasks.core.AbstractSearchHandler;
import org.eclipse.mylyn.tasks.core.IRepositoryQuery;
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.eclipse.mylyn.tasks.core.data.TaskData;

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
		StringBuilder sb = new StringBuilder(MAX_LENGTH);
		if (searchString.length() > MAX_LENGTH) {
			// searching for exact matches fails if strings are too long
			StringTokenizer t = new StringTokenizer(searchString, " \n\t()"); //$NON-NLS-1$
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
		} else {
			sb.append("\""); //$NON-NLS-1$
			sb.append(searchString);
			sb.append("\""); //$NON-NLS-1$
		}

		FilterDefinition filter = new FilterDefinition();
		filter.setContentFilter(new ContentFilter(sb.toString(), false, true, false, true));
		JiraUtil.setQuery(taskRepository, query, filter);
		return true;
	}

}
