/*******************************************************************************
 * Copyright (c) 2004 - 2006 Mylar committers and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.mylar.internal.jira.ui;

import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.IWizard;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.mylar.internal.jira.core.model.filter.FilterDefinition;
import org.eclipse.mylar.internal.jira.ui.wizards.EditJiraQueryWizard;
import org.eclipse.mylar.internal.jira.ui.wizards.JiraQueryPage;
import org.eclipse.mylar.internal.jira.ui.wizards.JiraRepositorySettingsPage;
import org.eclipse.mylar.internal.jira.ui.wizards.NewJiraQueryWizard;
import org.eclipse.mylar.internal.jira.ui.wizards.NewJiraTaskWizard;
import org.eclipse.mylar.tasks.core.AbstractRepositoryQuery;
import org.eclipse.mylar.tasks.core.AbstractRepositoryTask;
import org.eclipse.mylar.tasks.core.TaskRepository;
import org.eclipse.mylar.tasks.ui.AbstractRepositoryConnectorUi;
import org.eclipse.mylar.tasks.ui.wizards.AbstractRepositorySettingsPage;

/**
 * @author Mik Kersten
 * @author Eugene Kuleshov
 * @author Steffen Pingel
 */
public class JiraRepositoryUi extends AbstractRepositoryConnectorUi {

	public String getTaskKindLabel(AbstractRepositoryTask repositoryTask) {
		return "Issue";
	}
	
	@Override
	public WizardPage getSearchPage(TaskRepository repository, IStructuredSelection selection) {
		return new JiraQueryPage(repository, new FilterDefinition());
	} 

	@Override
	public AbstractRepositorySettingsPage getSettingsPage() {
		return new JiraRepositorySettingsPage(this);
	}

	@Override
	public IWizard getQueryWizard(TaskRepository repository, AbstractRepositoryQuery query) {
		if (query instanceof JiraRepositoryQuery || query instanceof JiraCustomQuery) {
			return new EditJiraQueryWizard(repository, query);
		}
		return new NewJiraQueryWizard(repository);
	}
	
	@Override
	public IWizard getNewTaskWizard(TaskRepository taskRepository) {
		return new NewJiraTaskWizard(taskRepository);
	}

	@Override
	public boolean hasRichEditor() {
		return true;
	}

	@Override
	public String getRepositoryType() {
		return JiraUiPlugin.REPOSITORY_KIND;
	}

	@Override
	public boolean hasSearchPage() {
		return true;
	}

}
