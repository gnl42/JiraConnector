/*******************************************************************************
 * Copyright (c) 2004 - 2006 University Of British Columbia and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     University Of British Columbia - initial API and implementation
 *******************************************************************************/

package org.eclipse.mylar.internal.jira.ui.wizards;

import java.util.List;

import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.mylar.internal.jira.core.model.Project;
import org.eclipse.mylar.internal.jira.core.service.JiraClient;
import org.eclipse.mylar.internal.jira.ui.JiraAttributeFactory;
import org.eclipse.mylar.internal.jira.ui.JiraClientFacade;
import org.eclipse.mylar.internal.jira.ui.JiraTaskDataHandler;
import org.eclipse.mylar.internal.jira.ui.JiraUiPlugin;
import org.eclipse.mylar.internal.tasks.ui.TasksUiImages;
import org.eclipse.mylar.internal.tasks.ui.TaskListPreferenceConstants;
import org.eclipse.mylar.tasks.core.AbstractAttributeFactory;
import org.eclipse.mylar.tasks.core.AbstractRepositoryConnector;
import org.eclipse.mylar.tasks.core.RepositoryTaskAttribute;
import org.eclipse.mylar.tasks.core.RepositoryTaskData;
import org.eclipse.mylar.tasks.core.Task;
import org.eclipse.mylar.tasks.core.TaskRepository;
import org.eclipse.mylar.tasks.ui.TasksUiPlugin;
import org.eclipse.mylar.tasks.ui.TasksUiUtil;
import org.eclipse.mylar.tasks.ui.editors.NewTaskEditorInput;
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

		setWindowTitle("New Repository Task");
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
		AbstractRepositoryConnector connector = TasksUiPlugin.getRepositoryManager()
			.getRepositoryConnector(JiraUiPlugin.REPOSITORY_KIND);
		
		JiraTaskDataHandler taskDataHandler = (JiraTaskDataHandler) connector.getTaskDataHandler();
		AbstractAttributeFactory attributeFactory = taskDataHandler.getAttributeFactory(taskRepository.getUrl(), taskRepository.getKind(), Task.DEFAULT_TASK_KIND);
		RepositoryTaskData taskData = new RepositoryTaskData(attributeFactory , JiraUiPlugin.REPOSITORY_KIND,
				taskRepository.getUrl(), TasksUiPlugin.getDefault().getNextNewRepositoryTaskId(), Task.DEFAULT_TASK_KIND);
		taskData.setNew(true);
		JiraClient server = JiraClientFacade.getDefault().getJiraClient(taskRepository);
		Project project = projectPage.getSelectedProject();
		taskDataHandler.initializeTaskData(taskData, server, project);
		taskData.setAttributeValue(RepositoryTaskAttribute.PRODUCT, project.getName());
		intializeToFirst(taskData.getAttribute(JiraAttributeFactory.ATTRIBUTE_TYPE));
		intializeToFirst(taskData.getAttribute(RepositoryTaskAttribute.PRIORITY));
		
		NewTaskEditorInput editorInput = new NewTaskEditorInput(taskRepository, taskData);
		IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
		TasksUiUtil.openEditor(editorInput, TaskListPreferenceConstants.TASK_EDITOR_ID, page);
		return true;
	}

	private void intializeToFirst(RepositoryTaskAttribute attribute) {
		List<String> options = attribute.getOptions();
		if (options != null && !options.isEmpty()) {
			attribute.setValue(options.get(0));
		}
	}

}
