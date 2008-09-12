/*******************************************************************************
* Copyright (c) 2004, 2008 Tasktop Technologies and others.
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

	@Override
	public String getConnectorKind() {
		return JiraCorePlugin.CONNECTOR_KIND;
	}

	@Override
	public boolean queryForText(TaskRepository taskRepository, IRepositoryQuery query, TaskData taskData,
			String searchString) {
		FilterDefinition filter = new FilterDefinition();
		filter.setContentFilter(new ContentFilter(searchString, false, true, false, true));
		JiraUtil.setQuery(taskRepository, query, filter);
		return true;
	}

}
