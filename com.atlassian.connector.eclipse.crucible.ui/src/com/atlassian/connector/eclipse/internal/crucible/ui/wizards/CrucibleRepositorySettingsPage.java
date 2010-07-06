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

package com.atlassian.connector.eclipse.internal.crucible.ui.wizards;

import com.atlassian.connector.eclipse.internal.commons.ui.MigrateToSecureStorageJob;
import com.atlassian.connector.eclipse.internal.commons.ui.dialogs.RemoteApiLockedDialog;
import com.atlassian.connector.eclipse.internal.crucible.core.CrucibleClientManager;
import com.atlassian.connector.eclipse.internal.crucible.core.CrucibleCorePlugin;
import com.atlassian.connector.eclipse.internal.crucible.core.CrucibleRepositoryConnector;
import com.atlassian.connector.eclipse.internal.crucible.core.client.CrucibleClient;
import com.atlassian.connector.eclipse.internal.crucible.core.client.CrucibleClientData;
import com.atlassian.connector.eclipse.internal.crucible.ui.CrucibleUiPlugin;
import com.atlassian.connector.eclipse.internal.fisheye.core.client.FishEyeClient;
import com.atlassian.connector.eclipse.internal.fisheye.core.client.FishEyeClientData;
import com.atlassian.theplugin.commons.remoteapi.RemoteApiException;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.mylyn.commons.net.Policy;
import org.eclipse.mylyn.internal.provisional.commons.ui.WorkbenchUtil;
import org.eclipse.mylyn.internal.tasks.core.IRepositoryConstants;
import org.eclipse.mylyn.tasks.core.RepositoryTemplate;
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.eclipse.mylyn.tasks.ui.wizards.AbstractRepositorySettingsPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.forms.widgets.ExpandableComposite;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Wizard for configuring the Crucible repository settings
 * 
 * @author Shawn Minto
 */
public class CrucibleRepositorySettingsPage extends AbstractRepositorySettingsPage {

	private Button fishEyeButton;

	private boolean isFishEyeDetected;

	private class CrucibleValidator extends Validator {

		private final TaskRepository taskRepository;

		public CrucibleValidator(TaskRepository taskRepository) {
			this.taskRepository = taskRepository;
		}

		@Override
		public void run(IProgressMonitor monitor) throws CoreException {
			isFishEyeDetected = false;
			CrucibleClientManager clientManager = CrucibleCorePlugin.getRepositoryConnector().getClientManager();
			CrucibleClient client = null;
			try {
				client = clientManager.createTempClient(taskRepository, new CrucibleClientData());

				monitor = Policy.backgroundMonitorFor(monitor);
				client.validate(monitor, taskRepository);
			} catch (CoreException e) {
				IStatus status = e.getStatus();
				if (e.getCause() != null && e.getCause() instanceof RemoteApiException
						&& e.getCause().getCause() != null && e.getCause().getCause() instanceof IOException) {
					if (e.getCause().getCause().getMessage().contains("HTTP 404")) {
						status = new Status(IStatus.ERROR, CrucibleUiPlugin.PLUGIN_ID,
								"HTTP 404 (Not Found) - Did you enable Remote API in Crucible?", e);
					}
					if (e.getCause().getCause().getMessage().contains("HTTP 403")
							&& e.getCause().getCause().getCause() != null
							&& e.getCause().getCause().getCause().getMessage().contains("maximum")) {
						Display.getDefault().asyncExec(new Runnable() {
							public void run() {
								new RemoteApiLockedDialog(WorkbenchUtil.getShell(), taskRepository.getRepositoryUrl()).open();
							}
						});
						status = new Status(IStatus.ERROR, CrucibleUiPlugin.PLUGIN_ID,
								"HTTP 403 (Permission denied) - You've been locked out from remote API.", e);
					}
				}
				setStatus(status);
				return;
			} finally {
				if (client != null) {
					clientManager.deleteTempClient(client.getServerData());
				}
			}

			// now try if it's FishEye
			FishEyeClient fishEyeClient = null;
			try {
				fishEyeClient = clientManager.createTempFishEyeClient(taskRepository, new FishEyeClientData());
				monitor = Policy.backgroundMonitorFor(monitor);
				fishEyeClient.validate(monitor, taskRepository);
				isFishEyeDetected = true;
			} catch (CoreException e) {
				if (CrucibleRepositoryConnector.isFishEye(taskRepository)) {
					setStatus(new Status(IStatus.ERROR, CrucibleUiPlugin.PLUGIN_ID,
							"This server does not seem to be integrated with FishEye.", e));
				}
				// if it's not marked as FishEye - that's OK. No exception re-thrown
			} finally {
				if (fishEyeClient != null) {
					clientManager.deleteTempFishEyeClient(fishEyeClient.getServerData());
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

	@Override
	protected void createContributionControls(Composite parentControl) {

		ExpandableComposite fishEyeSection = createSection(parentControl, "FishEye");
		fishEyeSection.setExpanded(true);
		fishEyeButton = new Button(fishEyeSection, SWT.CHECK);
		fishEyeButton.setText("Crucible Server Contains FishEye Instance");
		fishEyeSection.setClient(fishEyeButton);
		fishEyeButton.setSelection(repository != null && CrucibleRepositoryConnector.isFishEye(repository));

		// below line adds additional task repository settings section (supported wiki selection)
		// super.createContributionControls(parentControl);

	}

	@Override
	public void applyTo(TaskRepository repository) {
		MigrateToSecureStorageJob.migrateToSecureStorage(repository);
		super.applyTo(repository);
		repository.setProperty(IRepositoryConstants.PROPERTY_CATEGORY, IRepositoryConstants.CATEGORY_REVIEW);
		CrucibleCorePlugin.getRepositoryConnector();
		CrucibleRepositoryConnector.updateFishEyeStatus(repository, fishEyeButton.getSelection());

	}

	@Override
	protected void applyValidatorResult(Validator validator) {
		if (validator != null && validator.getStatus() == Status.OK_STATUS) {
			if (isFishEyeDetected && !fishEyeButton.getSelection()) {
				if (MessageDialog.openQuestion(getShell(), "Combined FishEye & Crucible detected",
						"This Crucible server is connected to Fisheye.\n"
								+ "Would you like to connect to the Fisheye server as well?")) {
					fishEyeButton.setSelection(true);
				}
			}
		}
		super.applyValidatorResult(validator);
	}

}
