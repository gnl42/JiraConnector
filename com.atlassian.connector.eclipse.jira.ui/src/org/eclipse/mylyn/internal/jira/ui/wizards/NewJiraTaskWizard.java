/*******************************************************************************
 * Copyright (c) 2004, 2008 Tasktop Technologies and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Tasktop Technologies - initial API and implementation
 *     Eugene Kuleshov - improvements
 *******************************************************************************/

package org.eclipse.mylyn.internal.jira.ui.wizards;

import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.mylyn.internal.jira.core.model.Project;
import org.eclipse.mylyn.tasks.core.ITaskMapping;
import org.eclipse.mylyn.tasks.core.TaskMapping;
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.eclipse.mylyn.tasks.ui.wizards.NewTaskWizard;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;

/**
 * Wizard for creating new JIRA tasks in a rich editor.
 * 
 * @author Steffen Pingel
 */
public class NewJiraTaskWizard extends NewTaskWizard implements INewWizard {

	private JiraProjectPage projectPage;

	public NewJiraTaskWizard(TaskRepository taskRepository, ITaskMapping taskSelection) {
		super(taskRepository, taskSelection);
		setWindowTitle(Messages.NewJiraTaskWizard_New_Jira_Task_Title);
	}

	@Override
	public void init(IWorkbench workbench, IStructuredSelection selection) {
	}

	@Override
	public void addPages() {
		projectPage = new JiraProjectPage(getTaskRepository());
		addPage(projectPage);
	}

	@Override
	protected ITaskMapping getInitializationData() {
		final Project project = projectPage.getSelectedProject();
		return new TaskMapping() {
			@Override
			public String getProduct() {
				return project.getKey();
			}
		};
	}

}
