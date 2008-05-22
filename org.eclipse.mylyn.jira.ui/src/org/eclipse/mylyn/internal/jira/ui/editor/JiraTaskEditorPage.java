/*******************************************************************************
 * Copyright (c) 2004, 2007 Mylyn project committers and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.mylyn.internal.jira.ui.editor;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.mylyn.internal.jira.core.JiraAttribute;
import org.eclipse.mylyn.internal.jira.core.JiraClientFactory;
import org.eclipse.mylyn.internal.jira.core.JiraCorePlugin;
import org.eclipse.mylyn.internal.jira.core.JiraTaskDataHandler;
import org.eclipse.mylyn.internal.jira.core.service.JiraClient;
import org.eclipse.mylyn.tasks.core.AbstractRepositoryConnector;
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.eclipse.mylyn.tasks.core.data.ITaskDataWorkingCopy;
import org.eclipse.mylyn.tasks.core.data.TaskAttributeMapper;
import org.eclipse.mylyn.tasks.core.data.TaskData;
import org.eclipse.mylyn.tasks.core.data.TaskDataModel;
import org.eclipse.mylyn.tasks.ui.TasksUi;
import org.eclipse.mylyn.tasks.ui.editors.AbstractTaskEditorPage;
import org.eclipse.mylyn.tasks.ui.editors.TaskEditor;
import org.eclipse.mylyn.tasks.ui.editors.TaskEditorInput;

/**
 * NOTE: This class is work in progress and currently not used.
 * 
 * @author Steffen Pingel
 */
public class JiraTaskEditorPage extends AbstractTaskEditorPage {

	public JiraTaskEditorPage(TaskEditor editor) {
		super(editor, JiraCorePlugin.CONNECTOR_KIND);
	}

	@Override
	protected TaskDataModel createModel(TaskEditorInput input) throws CoreException {
		if (input.getData() instanceof JiraTaskInitializationData) {
			// new task
			JiraTaskInitializationData data = (JiraTaskInitializationData) input.getData();
			TaskRepository taskRepository = TasksUi.getRepositoryManager().getRepository(JiraCorePlugin.CONNECTOR_KIND,
					data.taskRepository.getRepositoryUrl());

			AbstractRepositoryConnector connector = TasksUi.getRepositoryManager().getRepositoryConnector(
					JiraCorePlugin.CONNECTOR_KIND);

			JiraTaskDataHandler taskDataHandler = (JiraTaskDataHandler) connector.getTaskDataHandler();
			TaskAttributeMapper mapper = taskDataHandler.getAttributeMapper(taskRepository);
			TaskData taskData = new TaskData(mapper, JiraCorePlugin.CONNECTOR_KIND, taskRepository.getRepositoryUrl(),
					"");
			JiraClient client = JiraClientFactory.getDefault().getJiraClient(taskRepository);
			taskDataHandler.initializeTaskData(taskData, client, data.project);
			if (data.taskSelection != null) {
				taskDataHandler.cloneTaskData(data.taskSelection, taskData);
			}
			taskData.getMappedAttribute(JiraAttribute.PROJECT.getId()).setValue(data.project.getId());

			ITaskDataWorkingCopy workingCopy = TasksUi.getTaskDataManager()
					.createWorkingCopy(input.getTask(), taskData);
			return new TaskDataModel(taskRepository, input.getTask(), workingCopy);
		} else {
			return super.createModel(input);
		}
	}

}
