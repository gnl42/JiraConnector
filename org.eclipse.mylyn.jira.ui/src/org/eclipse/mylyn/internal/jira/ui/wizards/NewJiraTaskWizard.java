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
import org.eclipse.mylyn.internal.jira.ui.editor.JiraTaskInitializationData;
import org.eclipse.mylyn.internal.tasks.core.LocalTask;
import org.eclipse.mylyn.internal.tasks.ui.TasksUiImages;
import org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin;
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.eclipse.mylyn.tasks.core.TaskSelection;
import org.eclipse.mylyn.tasks.core.AbstractTask.SynchronizationState;
import org.eclipse.mylyn.tasks.ui.TasksUiUtil;
import org.eclipse.mylyn.tasks.ui.editors.TaskEditor;
import org.eclipse.mylyn.tasks.ui.editors.TaskEditorInput;
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

	private final TaskRepository taskRepository;

	private final JiraProjectPage projectPage;

	private final TaskSelection taskSelection;

	public NewJiraTaskWizard(TaskRepository taskRepository, TaskSelection taskSelection) {
		this.taskRepository = taskRepository;
		this.taskSelection = taskSelection;

		this.projectPage = new JiraProjectPage(taskRepository);

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
		JiraTaskInitializationData data = new JiraTaskInitializationData();
		data.project = projectPage.getSelectedProject();
		data.taskRepository = taskRepository;
		data.taskSelection = taskSelection;

		LocalTask task = TasksUiPlugin.getTaskListManager().createNewLocalTask(null);
		task.setSynchronizationState(SynchronizationState.OUTGOING);

		TaskEditorInput editorInput = new TaskEditorInput(taskRepository, task);
		editorInput.setData(data);
		IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
		TasksUiUtil.openEditor(editorInput, TaskEditor.ID_EDITOR, page);
		return true;
	}

}
