/*******************************************************************************
 * Copyright (c) 2004, 2007 Mylyn project committers and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.mylyn.internal.jira.core;

import org.eclipse.mylyn.tasks.core.RepositoryTaskAttribute;
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.eclipse.mylyn.tasks.core.data.TaskAttribute;
import org.eclipse.mylyn.tasks.core.data.TaskAttributeMapper;
import org.eclipse.mylyn.tasks.core.data.TaskAttributeProperties;

public class JiraAttributeMapper extends TaskAttributeMapper {

	private final TaskRepository taskRepository;

	public JiraAttributeMapper(TaskRepository taskRepository) {
		this.taskRepository = taskRepository;
	}

	@Override
	public String mapToRepositoryKey(TaskAttribute parent, String key) {
		if (RepositoryTaskAttribute.COMPONENT.equals(key)) {
			return JiraAttributeFactory.ATTRIBUTE_COMPONENTS;
		}
		return super.mapToRepositoryKey(parent, key);
	}

	@Override
	public TaskAttribute getAssoctiatedAttribute(TaskAttribute taskAttribute) {
		String id = taskAttribute.getMetaData(TaskAttribute.META_ASSOCIATED_ATTRIBUTE_ID);
		if (id != null) {
			if (TaskAttribute.TYPE_OPERATION.equals(TaskAttributeProperties.from(taskAttribute).getType())) {
				return taskAttribute.getTaskData().getRoot().getAttribute(id);
			}
			return taskAttribute.getAttribute(id);
		}
		return null;
	}

	@Override
	public String getType(TaskAttribute taskAttribute) {
		TaskAttributeProperties properties = TaskAttributeProperties.from(taskAttribute);
		if (properties.getType() != null) {
			return properties.getType();
		}

		JiraFieldType type = JiraFieldType.fromKey(taskAttribute.getMetaData(JiraAttributeFactory.META_TYPE));
		if (type.getTaskType() != null) {
			return type.getTaskType();
		}

		return TaskAttribute.TYPE_SHORT_TEXT;
	}
}