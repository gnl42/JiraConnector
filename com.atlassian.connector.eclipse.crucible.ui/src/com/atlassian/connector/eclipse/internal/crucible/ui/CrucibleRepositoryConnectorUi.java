/*******************************************************************************
 * Copyright (c) 2009 Atlassian and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Atlassian - initial API and implementation
 ******************************************************************************/

package com.atlassian.connector.eclipse.internal.crucible.ui;

import com.atlassian.connector.eclipse.internal.crucible.core.CrucibleCorePlugin;
import com.atlassian.connector.eclipse.internal.crucible.core.CrucibleUtil;
import com.atlassian.connector.eclipse.internal.crucible.ui.wizards.CrucibleCustomFilterPage;
import com.atlassian.connector.eclipse.internal.crucible.ui.wizards.CrucibleNamedFilterPage;
import com.atlassian.connector.eclipse.internal.crucible.ui.wizards.CrucibleRepositorySettingsPage;
import com.atlassian.connector.eclipse.internal.crucible.ui.wizards.CrucibleReviewWizard;

import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.IWizard;
import org.eclipse.mylyn.tasks.core.IRepositoryQuery;
import org.eclipse.mylyn.tasks.core.ITask;
import org.eclipse.mylyn.tasks.core.ITaskMapping;
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.eclipse.mylyn.tasks.ui.AbstractRepositoryConnectorUi;
import org.eclipse.mylyn.tasks.ui.TaskRepositoryLocationUiFactory;
import org.eclipse.mylyn.tasks.ui.wizards.ITaskRepositoryPage;
import org.eclipse.mylyn.tasks.ui.wizards.ITaskSearchPage;
import org.eclipse.mylyn.tasks.ui.wizards.RepositoryQueryWizard;

/**
 * Core class for integration with Mylyn tasks framework UI
 * 
 * @author Shawn Minto
 */
public class CrucibleRepositoryConnectorUi extends AbstractRepositoryConnectorUi {

	public CrucibleRepositoryConnectorUi() {
		CrucibleCorePlugin.getRepositoryConnector()
				.getClientManager()
				.setTaskRepositoryLocationFactory(new TaskRepositoryLocationUiFactory());
	}

	@Override
	public String getConnectorKind() {
		return CrucibleCorePlugin.CONNECTOR_KIND;
	}

	@Override
	public IWizard getNewTaskWizard(TaskRepository taskRepository, ITaskMapping selection) {
		return new CrucibleReviewWizard(taskRepository);
	}

	@Override
	public ITaskSearchPage getSearchPage(TaskRepository repository, IStructuredSelection selection) {
		return new CrucibleCustomFilterPage(repository);
	}

	@Override
	public IWizard getQueryWizard(TaskRepository repository, IRepositoryQuery query) {

		RepositoryQueryWizard wizard = new RepositoryQueryWizard(repository);
		if (query != null) {
			if (CrucibleUtil.isFilterDefinition(query)) {
				wizard.addPage(new CrucibleCustomFilterPage(repository, query));
			} else {
				wizard.addPage(new CrucibleNamedFilterPage(repository, query));
			}
		} else {
			wizard.addPage(new CrucibleNamedFilterPage(repository));
		}
		return wizard;
	}

	@Override
	public ITaskRepositoryPage getSettingsPage(TaskRepository taskRepository) {
		return new CrucibleRepositorySettingsPage(taskRepository);
	}

	@Override
	public boolean hasSearchPage() {
		return true;
	}

	@Override
	public boolean hasCustomNotifications() {
		return true;
	}

	@Override
	public String getTaskKindLabel(ITask task) {
		return "Review";
	}

	@Override
	public String getAccountCreationUrl(TaskRepository taskRepository) {
		return taskRepository.getRepositoryUrl() + "/login/signup.do"; //$NON-NLS-1$
	}

	@Override
	public String getAccountManagementUrl(TaskRepository taskRepository) {
		return taskRepository.getRepositoryUrl() + "/profile/editDisplaySettings-default.do"; //$NON-NLS-1$
	}
}
