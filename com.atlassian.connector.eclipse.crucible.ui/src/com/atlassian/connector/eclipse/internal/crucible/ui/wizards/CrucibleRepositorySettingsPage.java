/*******************************************************************************
 * Copyright (c) 2008 Atlassian and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Atlassian - initial API and implementation
 ******************************************************************************/

package com.atlassian.connector.eclipse.internal.crucible.ui.wizards;

import com.atlassian.connector.eclipse.internal.crucible.core.CrucibleClientManager;
import com.atlassian.connector.eclipse.internal.crucible.core.CrucibleCorePlugin;
import com.atlassian.connector.eclipse.internal.crucible.core.client.CrucibleClient;
import com.atlassian.connector.eclipse.internal.crucible.core.client.CrucibleClientData;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.mylyn.tasks.core.RepositoryTemplate;
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.eclipse.mylyn.tasks.ui.wizards.AbstractRepositorySettingsPage;
import org.eclipse.swt.widgets.Composite;

import java.net.MalformedURLException;
import java.net.URL;

/**
 * Wizard for configuring the Crucible repository settings
 * 
 * @author Shawn Minto
 */
public class CrucibleRepositorySettingsPage extends AbstractRepositorySettingsPage {

	private class CrucibleValidator extends Validator {

		private final TaskRepository taskRepository;

		public CrucibleValidator(TaskRepository taskRepository) {
			this.taskRepository = taskRepository;
		}

		@Override
		public void run(IProgressMonitor monitor) throws CoreException {
			CrucibleClientManager clientManager = CrucibleCorePlugin.getRepositoryConnector().getClientManager();
			CrucibleClient client = null;
			try {
				client = clientManager.createTempClient(taskRepository, new CrucibleClientData());

				client.validate(monitor);
			} finally {
				if (client != null) {
					clientManager.deleteTempClient(client);
				}
			}
		}
	}

	public CrucibleRepositorySettingsPage(TaskRepository taskRepository) {
		super("Crucible Repository Settings", "Enter Crucible server information", taskRepository);
		setNeedsHttpAuth(true);
		setNeedsEncoding(false);
		setNeedsAnonymousLogin(false);
		setNeedsAdvanced(false);
	}

	@Override
	protected void repositoryTemplateSelected(RepositoryTemplate template) {
		repositoryLabelEditor.setStringValue(template.label);
		setUrl(template.repositoryUrl);
		getContainer().updateButtons();

	}

	@Override
	protected void createAdditionalControls(Composite parent) {
		addRepositoryTemplatesToServerUrlCombo();
	}

	@Override
	public String getConnectorKind() {
		return CrucibleCorePlugin.CONNECTOR_KIND;
	}

	@Override
	protected Validator getValidator(TaskRepository repository) {
		return new CrucibleValidator(repository);
	}

	@Override
	protected boolean isValidUrl(String name) {
		if (name.startsWith(URL_PREFIX_HTTPS) || name.startsWith(URL_PREFIX_HTTP)) {
			try {
				new URL(name);
				return true;
			} catch (MalformedURLException e) {
				// ignore
			}
		}
		return false;
	}

}
