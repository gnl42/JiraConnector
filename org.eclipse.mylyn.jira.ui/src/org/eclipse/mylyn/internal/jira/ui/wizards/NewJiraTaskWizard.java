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
import org.eclipse.mylar.internal.tasklist.ui.TaskUiUtil;
import org.eclipse.mylar.provisional.tasklist.TaskRepository;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;

/**
 * Wizard used to create new Jira task using web UI
 * 
 * @author Eugene Kuleshov
 */
public class NewJiraTaskWizard extends Wizard implements INewWizard {

	private final TaskRepository taskRepository;

	public NewJiraTaskWizard(TaskRepository taskRepository) {
		this.taskRepository = taskRepository;
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
		TaskUiUtil.openUrl(newTaskUrl + (newTaskUrl.endsWith("/") ? "" : "/") + "secure/CreateIssue!default.jspa");
		return true;
	}

}
