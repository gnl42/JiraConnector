/*******************************************************************************
 * Copyright (c) 2004, 2007 Mylyn project committers and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.mylyn.internal.jira.ui.wizards;

import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.mylyn.internal.jira.core.model.Project;
import org.eclipse.mylyn.internal.jira.core.service.JiraClient;
import org.eclipse.mylyn.internal.jira.ui.JiraClientFactory;
import org.eclipse.mylyn.internal.jira.ui.JiraTaskDataHandler;
import org.eclipse.mylyn.internal.jira.ui.JiraUiPlugin;
import org.eclipse.mylyn.internal.tasks.ui.TasksUiImages;
import org.eclipse.mylyn.tasks.core.AbstractAttributeFactory;
import org.eclipse.mylyn.tasks.core.AbstractRepositoryConnector;
import org.eclipse.mylyn.tasks.core.AbstractTask;
import org.eclipse.mylyn.tasks.core.RepositoryTaskAttribute;
import org.eclipse.mylyn.tasks.core.RepositoryTaskData;
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.eclipse.mylyn.tasks.ui.TasksUiPlugin;
import org.eclipse.mylyn.tasks.ui.TasksUiUtil;
import org.eclipse.mylyn.tasks.ui.editors.NewTaskEditorInput;
import org.eclipse.mylyn.tasks.ui.editors.TaskEditor;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;

/**
 * Wizard for creating new Jira tasks through a rich editor..
 * 
 * @author Steffen Pingel
 */
public class NewJiraTaskWizard extends Wizard implements INewWizard {

	private TaskRepository taskRepository;

	private JiraProjectPage projectPage;

	public NewJiraTaskWizard(TaskRepository taskRepository) {
		this.taskRepository = taskRepository;

		projectPage = new JiraProjectPage(taskRepository);

		setWindowTitle("New Task");
		setDefaultPageImageDescriptor(TasksUiImages.BANNER_REPOSITORY);

		setNeedsProgressMonitor(true);
	}

	public void init(IWorkbench workbench, IStructuredSelection selection) {
	}

	@Override
	public void addPages() {
		addPage(projectPage);
	}

	@Override
	public boolean performFinish() {
		AbstractRepositoryConnector connector = TasksUiPlugin.getRepositoryManager().getRepositoryConnector(
				JiraUiPlugin.REPOSITORY_KIND);

		JiraTaskDataHandler taskDataHandler = (JiraTaskDataHandler) connector.getTaskDataHandler();
		AbstractAttributeFactory attributeFactory = taskDataHandler.getAttributeFactory(taskRepository.getUrl(),
				taskRepository.getConnectorKind(), AbstractTask.DEFAULT_TASK_KIND);
		RepositoryTaskData taskData = new RepositoryTaskData(attributeFactory, JiraUiPlugin.REPOSITORY_KIND,
				taskRepository.getUrl(), TasksUiPlugin.getDefault().getNextNewRepositoryTaskId());
		taskData.setNew(true);
		JiraClient server = JiraClientFactory.getDefault().getJiraClient(taskRepository);
		Project project = projectPage.getSelectedProject();
		taskDataHandler.initializeTaskData(taskData, server, project);
		taskData.setAttributeValue(RepositoryTaskAttribute.PRODUCT, project.getName());

		NewTaskEditorInput editorInput = new NewTaskEditorInput(taskRepository, taskData);
		IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
		TasksUiUtil.openEditor(editorInput, TaskEditor.ID_EDITOR, page);
		return true;
	}

}
