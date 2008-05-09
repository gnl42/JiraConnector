/*******************************************************************************
 * Copyright (c) 2004, 2007 Mylyn project committers and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.mylyn.internal.jira.ui;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.mylyn.internal.jira.core.model.filter.ContentFilter;
import org.eclipse.mylyn.internal.jira.core.model.filter.FilterDefinition;
import org.eclipse.mylyn.internal.jira.core.util.JiraUtil;
import org.eclipse.mylyn.tasks.core.AbstractDuplicateDetector;
import org.eclipse.mylyn.tasks.core.IRepositoryQuery;
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.eclipse.mylyn.tasks.core.data.TaskData;
import org.eclipse.mylyn.tasks.ui.TasksUi;

/**
 * Stack Trace duplicate detector
 * 
 * @author Eugene Kuleshov
 * @author Steffen Pingel
 */
public class JiraStackTraceDuplicateDetector extends AbstractDuplicateDetector {

	private static final String NO_STACK_MESSAGE = "Unable to locate a stack trace in the description text.";

	@Override
	public IRepositoryQuery getDuplicatesQuery(TaskRepository taskRepository, TaskData taskData, String text) {
		String searchString = AbstractDuplicateDetector.getStackTraceFromDescription(text);
		if (searchString == null) {
			MessageDialog.openWarning(null, "No Stack Trace Found", NO_STACK_MESSAGE);
			return null;
		}

		FilterDefinition filter = new FilterDefinition();
		filter.setContentFilter(new ContentFilter(searchString, false, true, false, true));
		IRepositoryQuery query = TasksUi.getTasksModel().createQuery(taskRepository);
		JiraUtil.setQuery(taskRepository, query, filter);
		return query;
	}

}
