/*******************************************************************************
 * Copyright (c) 2004, 2007 Mylyn project committers and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.mylyn.internal.jira.ui.editor;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.mylyn.internal.jira.core.JiraClientFactory;
import org.eclipse.mylyn.internal.jira.core.JiraCorePlugin;
import org.eclipse.mylyn.internal.jira.core.JiraTaskDataHandler2;
import org.eclipse.mylyn.internal.jira.core.service.JiraClient;
import org.eclipse.mylyn.internal.tasks.core.data.TaskDataUtil;
import org.eclipse.mylyn.internal.tasks.ui.editors.AbstractTaskEditorPage;
import org.eclipse.mylyn.internal.tasks.ui.editors.AttributeEditorFactory;
import org.eclipse.mylyn.internal.tasks.ui.editors.AttributeEditorToolkit;
import org.eclipse.mylyn.tasks.core.AbstractRepositoryConnector;
import org.eclipse.mylyn.tasks.core.RepositoryTaskAttribute;
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.eclipse.mylyn.tasks.core.data.ITaskDataWorkingCopy;
import org.eclipse.mylyn.tasks.core.data.TaskAttributeMapper;
import org.eclipse.mylyn.tasks.core.data.TaskData;
import org.eclipse.mylyn.tasks.core.data.TaskDataModel;
import org.eclipse.mylyn.tasks.ui.TasksUi;
import org.eclipse.mylyn.tasks.ui.editors.TaskEditor;
import org.eclipse.mylyn.tasks.ui.editors.TaskEditorInput;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.handlers.IHandlerService;

/**
 * NOTE: This class is work in progress and currently not used.
 * 
 * @author Steffen Pingel
 */
@SuppressWarnings("restriction")
public class JiraTaskEditor2 extends AbstractTaskEditorPage {

	private AttributeEditorFactory attributeEditorFactory;

	private AttributeEditorToolkit attributeEditorToolkit;

	public JiraTaskEditor2(TaskEditor editor) {
		super(editor, JiraCorePlugin.CONNECTOR_KIND);
	}

	@Override
	protected void createFormContent(IManagedForm managedForm) {
		attributeEditorFactory = new AttributeEditorFactory(getModel(), getTaskRepository());
		IHandlerService handlerService = (IHandlerService) getSite().getService(IHandlerService.class);
		attributeEditorToolkit = new AttributeEditorToolkit(handlerService, getEditorSite().getActionBarContributor());

		super.createFormContent(managedForm);
	}

	@Override
	protected AttributeEditorFactory getAttributeEditorFactory() {
		return attributeEditorFactory;
	}

	@Override
	public AttributeEditorToolkit getAttributeEditorToolkit() {
		return attributeEditorToolkit;
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

			JiraTaskDataHandler2 taskDataHandler = (JiraTaskDataHandler2) connector.getTaskDataHandler2();
			TaskAttributeMapper mapper = taskDataHandler.getAttributeMapper(taskRepository);
			TaskData taskData = new TaskData(mapper, JiraCorePlugin.CONNECTOR_KIND, taskRepository.getRepositoryUrl(),
					"");
			JiraClient client = JiraClientFactory.getDefault().getJiraClient(taskRepository);
			taskDataHandler.initializeTaskData(taskData, client, data.project);
			if (data.taskSelection != null) {
				// FIXME EDITOR
				TaskData source = TaskDataUtil.toTaskData(data.taskSelection.getTaskData(), mapper);
				taskDataHandler.cloneTaskData(source, taskData);
			}
			taskData.getMappedAttribute(RepositoryTaskAttribute.PRODUCT).setValue(data.project.getId());

			ITaskDataWorkingCopy workingCopy = TasksUi.getTaskDataManager().createWorkingCopy(input.getTask(),
					taskData.getConnectorKind(), taskData);
			return new TaskDataModel(workingCopy);
		} else {
			return super.createModel(input);
		}
	}

}
