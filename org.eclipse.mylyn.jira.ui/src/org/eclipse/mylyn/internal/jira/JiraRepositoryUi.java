/*******************************************************************************
 * Copyright (c) 2004 - 2006 Mylar committers and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.mylar.internal.jira;

import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.window.Window;
import org.eclipse.jface.wizard.IWizard;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.mylar.context.core.MylarStatusHandler;
import org.eclipse.mylar.internal.jira.ui.wizards.EditJiraQueryWizard;
import org.eclipse.mylar.internal.jira.ui.wizards.JiraRepositorySettingsPage;
import org.eclipse.mylar.internal.jira.ui.wizards.NewJiraQueryWizard;
import org.eclipse.mylar.internal.tasks.ui.wizards.AbstractRepositorySettingsPage;
import org.eclipse.mylar.internal.tasks.ui.wizards.NewWebTaskWizard;
import org.eclipse.mylar.tasks.core.AbstractRepositoryQuery;
import org.eclipse.mylar.tasks.core.TaskRepository;
import org.eclipse.mylar.tasks.ui.AbstractRepositoryUi;
import org.eclipse.mylar.tasks.ui.TasksUiPlugin;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;

/**
 * @author Mik Kersten
 */
public class JiraRepositoryUi extends AbstractRepositoryUi {

	private static final String TITLE_EDIT_QUERY = "Edit Jira Query";

	public AbstractRepositorySettingsPage getSettingsPage() {
		return new JiraRepositorySettingsPage(this);
	}

	@Override
	public void openEditQueryDialog(AbstractRepositoryQuery query) {
		try {
			TaskRepository repository = TasksUiPlugin.getRepositoryManager().getRepository(
					query.getRepositoryKind(), query.getRepositoryUrl());
			if (repository == null)
				return;
	
			IWizard wizard = null;
			if (query instanceof JiraRepositoryQuery || query instanceof JiraCustomQuery) {
				wizard = new EditJiraQueryWizard(repository, query);
			}
	
			Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
			if (wizard != null && shell != null && !shell.isDisposed()) {
				WizardDialog dialog = new WizardDialog(shell, wizard);
				dialog.create();
				dialog.setTitle(TITLE_EDIT_QUERY);
				dialog.setBlockOnOpen(true);
				if (dialog.open() == Window.CANCEL) {
					dialog.close();
					return;
				}
			}
		} catch (Exception e) {
			MylarStatusHandler.fail(e, e.getMessage(), true);
		}
	
	}

	@Override
	public IWizard getNewTaskWizard(TaskRepository taskRepository, IStructuredSelection selection) {
		String newTaskUrl = taskRepository.getUrl();
		return new NewWebTaskWizard(taskRepository, newTaskUrl + (newTaskUrl.endsWith("/") ? "" : "/") + "secure/CreateIssue!default.jspa");
	}

	@Override
	public boolean hasRichEditor() {
		return true;
	}

	public IWizard getNewQueryWizard(TaskRepository repository, IStructuredSelection selection) {
		return new NewJiraQueryWizard(repository);
	}

	@Override
	public String getRepositoryType() {
		return MylarJiraPlugin.REPOSITORY_KIND;
	}

	@Override
	public boolean hasSearchPage() {
		return false;
	}

}
