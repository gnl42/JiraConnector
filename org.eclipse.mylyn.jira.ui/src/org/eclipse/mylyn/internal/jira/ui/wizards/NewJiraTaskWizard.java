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

import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.mylar.internal.tasks.ui.ui.TaskListImages;
import org.eclipse.mylar.internal.tasks.ui.ui.TaskUiUtil;
import org.eclipse.mylar.tasks.core.TaskRepository;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;

/**
 * Wizard used to create new Jira task using web UI
 * 
 * @author Eugene Kuleshov
 * @author Mik Kersten
 */
public class NewJiraTaskWizard extends Wizard implements INewWizard {

	private final TaskRepository taskRepository;

	public NewJiraTaskWizard(TaskRepository taskRepository) {
		this.taskRepository = taskRepository;
		setWindowTitle("New JIRA Task");
		setDefaultPageImageDescriptor(TaskListImages.BANNER_REPOSITORY);
	}

	public void init(IWorkbench workbench, IStructuredSelection selection) {
	}

	public void addPages() {
		super.addPages();
		addPage(new NewJiraTaskPage());
	}

	public boolean canFinish() {
		return true;
	}

	public boolean performFinish() {
		String newTaskUrl = taskRepository.getUrl();
//		TaskUiUtil.openUrl(newTaskUrl);
		// below doesn't work because component parameter is missing, probably from CGI vars
		TaskUiUtil.openUrl(newTaskUrl + (newTaskUrl.endsWith("/") ? "" : "/") + "secure/CreateIssue!default.jspa");
		return true;
	}

}
