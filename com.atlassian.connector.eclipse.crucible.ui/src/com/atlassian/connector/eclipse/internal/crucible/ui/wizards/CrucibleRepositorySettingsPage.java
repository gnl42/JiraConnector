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

import com.atlassian.connector.eclipse.internal.crucible.core.CrucibleClientManager;
import com.atlassian.connector.eclipse.internal.crucible.core.CrucibleCorePlugin;
import com.atlassian.connector.eclipse.internal.crucible.core.client.CrucibleClient;
import com.atlassian.connector.eclipse.internal.crucible.core.client.CrucibleClientData;
import com.atlassian.connector.eclipse.internal.crucible.ui.CrucibleUiPlugin;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.MessageDialogWithToggle;
import org.eclipse.mylyn.tasks.core.RepositoryTemplate;
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.eclipse.mylyn.tasks.ui.wizards.AbstractRepositorySettingsPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.ui.forms.widgets.ExpandableComposite;

import java.net.MalformedURLException;
import java.net.URL;

/**
 * Wizard for configuring the Crucible repository settings
 * 
 * @author Shawn Minto
 */
public class CrucibleRepositorySettingsPage extends AbstractRepositorySettingsPage {

	private Button buttonAlways;

	private Button buttonNever;

	private Button buttonPrompt;

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

	@Override
	protected void createContributionControls(Composite parentControl) {
		ExpandableComposite section = createSection(parentControl, "Review Activation");
		section.setExpanded(true);
		if (section.getLayoutData() instanceof GridData) {
			GridData gd = ((GridData) section.getLayoutData());
			gd.grabExcessVerticalSpace = true;
			gd.verticalAlignment = SWT.FILL;
		}

		Composite composite = new Composite(section, SWT.NONE);
		GridLayout layout = new GridLayout(2, false);
		layout.marginWidth = 0;
		composite.setLayout(layout);
		section.setClient(composite);

		Group group = new Group(composite, SWT.NONE);
		group.setLayout(new GridLayout(3, true));
		group.setText("Activate Review when opening a file from within the Review Editor");

		buttonAlways = new Button(group, SWT.RADIO);
		buttonAlways.setText("Always");
		buttonNever = new Button(group, SWT.RADIO);
		buttonNever.setText("Never");
		buttonPrompt = new Button(group, SWT.RADIO);
		buttonPrompt.setText("Prompt");

		String pref = CrucibleUiPlugin.getActivateReviewPreference();
		if (pref.equals(MessageDialogWithToggle.ALWAYS)) {
			buttonAlways.setSelection(true);
		} else if (pref.equals(MessageDialogWithToggle.NEVER)) {
			buttonNever.setSelection(true);
		} else {
			buttonPrompt.setSelection(true);
		}
	}

	@Override
	public void applyTo(TaskRepository repository) {
		super.applyTo(repository);
		//store activation preference
		if (buttonAlways.getSelection()) {
			CrucibleUiPlugin.setActivateReviewPreference(MessageDialogWithToggle.ALWAYS);
		} else if (buttonNever.getSelection()) {
			CrucibleUiPlugin.setActivateReviewPreference(MessageDialogWithToggle.NEVER);
		} else {
			CrucibleUiPlugin.setActivateReviewPreference(MessageDialogWithToggle.PROMPT);
		}
	}
}
