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

package com.atlassian.connector.eclipse.internal.bamboo.ui;

import com.atlassian.connector.eclipse.internal.bamboo.core.BambooCorePlugin;
import com.atlassian.connector.eclipse.ui.CaptchaAwareTaskRepositoryLocationUiFactory;

import org.eclipse.jface.wizard.IWizard;
import org.eclipse.mylyn.tasks.core.IRepositoryQuery;
import org.eclipse.mylyn.tasks.core.ITask;
import org.eclipse.mylyn.tasks.core.ITaskMapping;
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.eclipse.mylyn.tasks.ui.AbstractRepositoryConnectorUi;
import org.eclipse.mylyn.tasks.ui.wizards.ITaskRepositoryPage;

/**
 * UI integration for Mylyn tasks framework.
 * 
 * @author Shawn Minto
 */
public class BambooRepositoryConnectorUi extends AbstractRepositoryConnectorUi {

	public BambooRepositoryConnectorUi() {
		BambooCorePlugin.getRepositoryConnector()
				.getClientManager()
				.setTaskRepositoryLocationFactory(new CaptchaAwareTaskRepositoryLocationUiFactory());
	}

	@Override
	public String getConnectorKind() {
		return BambooCorePlugin.CONNECTOR_KIND;
	}

	@Override
	public IWizard getNewTaskWizard(TaskRepository taskRepository, ITaskMapping selection) {
		return null;
	}

	@Override
	public IWizard getQueryWizard(TaskRepository repository, IRepositoryQuery query) {
		return null;
	}

	@Override
	public ITaskRepositoryPage getSettingsPage(TaskRepository taskRepository) {
		return new BambooRepositorySettingsPage(taskRepository);
	}

	@Override
	public boolean hasSearchPage() {
		return true;
	}

	@Override
	public String getTaskKindLabel(ITask task) {
		return "Build Plan";
	}

	@Override
	public String getAccountCreationUrl(TaskRepository taskRepository) {
		return taskRepository.getRepositoryUrl() + "/signupUser!default.action"; //$NON-NLS-1$
	}

	@Override
	public String getAccountManagementUrl(TaskRepository taskRepository) {
		return taskRepository.getRepositoryUrl() + "/profile/userProfile.action"; //$NON-NLS-1$
	}

}
