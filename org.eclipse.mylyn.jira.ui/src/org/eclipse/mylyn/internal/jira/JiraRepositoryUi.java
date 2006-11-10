/*******************************************************************************
 * Copyright (c) 2004 - 2006 Mylar committers and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.mylar.internal.jira;

import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.IWizard;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.mylar.internal.jira.ui.wizards.EditJiraQueryWizard;
import org.eclipse.mylar.internal.jira.ui.wizards.JiraQueryPage;
import org.eclipse.mylar.internal.jira.ui.wizards.JiraRepositorySettingsPage;
import org.eclipse.mylar.internal.jira.ui.wizards.NewJiraQueryWizard;
import org.eclipse.mylar.internal.tasks.ui.wizards.AbstractRepositorySettingsPage;
import org.eclipse.mylar.internal.tasks.ui.wizards.NewWebTaskWizard;
import org.eclipse.mylar.tasks.core.AbstractRepositoryQuery;
import org.eclipse.mylar.tasks.core.TaskRepository;
import org.eclipse.mylar.tasks.ui.AbstractRepositoryConnectorUi;
import org.tigris.jira.core.model.filter.FilterDefinition;

/**
 * @author Mik Kersten
 */
public class JiraRepositoryUi extends AbstractRepositoryConnectorUi {

	@Override
	public WizardPage getSearchPage(TaskRepository repository, IStructuredSelection selection) {
		return new JiraQueryPage(repository, new FilterDefinition(), true, false);
	} 

	public AbstractRepositorySettingsPage getSettingsPage() {
		return new JiraRepositorySettingsPage(this);
	}

	public IWizard getQueryWizard(TaskRepository repository, AbstractRepositoryQuery query) {
		if (query instanceof JiraRepositoryQuery || query instanceof JiraCustomQuery) {
			return new EditJiraQueryWizard(repository, query);
		}
		return new NewJiraQueryWizard(repository);
	}
	
	@Override
	public IWizard getNewTaskWizard(TaskRepository taskRepository) {
		String newTaskUrl = taskRepository.getUrl();
		return new NewWebTaskWizard(taskRepository, newTaskUrl + (newTaskUrl.endsWith("/") ? "" : "/") + "secure/CreateIssue!default.jspa");
	}

	@Override
	public boolean hasRichEditor() {
		return true;
	}

	@Override
	public String getRepositoryType() {
		return MylarJiraPlugin.REPOSITORY_KIND;
	}

	@Override
	public boolean hasSearchPage() {
		return true;
	}

}
