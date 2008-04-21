/*******************************************************************************
 * Copyright (c) 2004, 2007 Mylyn project committers and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.mylyn.internal.jira.core;

import java.util.Map;

import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.eclipse.mylyn.tasks.core.data.AbstractAttributeMapper;
import org.eclipse.mylyn.tasks.core.data.TaskAttribute;

public class JiraAttributeMapper extends AbstractAttributeMapper {

	private final TaskRepository taskRepository;

	public JiraAttributeMapper(TaskRepository taskRepository) {
		this.taskRepository = taskRepository;
	}

	@Override
	public String getType(TaskAttribute taskAttribute) {
		if (TaskAttribute.DATE_CREATION.equals(taskAttribute.getId())
				|| TaskAttribute.DATE_MODIFIED.equals(taskAttribute.getId())) {
			return TaskAttribute.TYPE_DATE;
		}

		JiraFieldType type = JiraFieldType.valueByKey(taskAttribute.getMetaData(JiraAttributeFactory.TYPE_KEY));
		Map<String, String> options = taskAttribute.getOptions();
		if (type.equals(JiraFieldType.SELECT)
				&& (options == null || options.isEmpty() || taskAttribute.getMetaData(TaskAttribute.META_READ_ONLY) != null)) {
			type = JiraFieldType.TEXTFIELD;
		} else if (type.equals(JiraFieldType.MULTISELECT) && (options == null || options.isEmpty())) {
			type = JiraFieldType.TEXTFIELD;
		}

		switch (type) {
		case DATEPICKER:
			return TaskAttribute.TYPE_DATE;
		case ISSUELINK:
			return TaskAttribute.TYPE_TASK_DEPENDENCY;
		case ISSUELINKS:
			return TaskAttribute.TYPE_TASK_DEPENDENCY;
		case MULTISELECT:
			return TaskAttribute.TYPE_MULTI_SELECT;
		case SELECT:
			return TaskAttribute.TYPE_SINGLE_SELECT;
		case TEXTAREA:
			return TaskAttribute.TYPE_LONG_TEXT;
		default:
			return TaskAttribute.TYPE_SHORT_TEXT;
		}
	}

}