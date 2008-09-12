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

package org.eclipse.mylyn.internal.jira.core;

import java.util.Date;
import java.util.Map;

import org.eclipse.mylyn.tasks.core.ITask.PriorityLevel;
import org.eclipse.mylyn.tasks.core.data.TaskAttribute;
import org.eclipse.mylyn.tasks.core.data.TaskData;
import org.eclipse.mylyn.tasks.core.data.TaskMapper;

/**
 * @author Steffen Pingel
 */
public class JiraTaskMapper extends TaskMapper {

	public JiraTaskMapper(TaskData taskData) {
		super(taskData);
	}

	@Override
	public PriorityLevel getPriorityLevel() {
		TaskAttribute attribute = getTaskData().getRoot().getAttribute(JiraAttribute.PRIORITY.id());
		if (attribute != null) {
			return JiraRepositoryConnector.getPriorityLevel(attribute.getValue());
		}
		return PriorityLevel.getDefault();
	}

	@Override
	public Date getCompletionDate() {
		if (JiraRepositoryConnector.isCompleted(getTaskData())) {
			return getModificationDate();
		} else {
			return null;
		}
	}

	@Override
	public void setCompletionDate(Date dateCompleted) {
		// ignore
	}

	@Override
	public void setComponent(String component) {
		TaskAttribute attribute = getTaskData().getRoot().getAttribute(JiraAttribute.COMPONENTS.id());
		if (attribute != null && !attribute.getMetaData().isReadOnly()) {
			for (Map.Entry<String, String> entry : attribute.getOptions().entrySet()) {
				if (entry.getValue().equals(component)) {
					attribute.setValue(entry.getKey());
				}
			}
		}
	}

	@Override
	public void setProduct(String product) {
		// ignore, set during task data initialization
	}

}
