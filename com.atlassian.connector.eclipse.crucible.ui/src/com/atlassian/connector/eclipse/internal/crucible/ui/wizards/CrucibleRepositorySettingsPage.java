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
import com.atlassian.connector.eclipse.internal.crucible.core.CrucibleRepositoryConnector;
import com.atlassian.connector.eclipse.internal.crucible.core.client.CrucibleClient;
import com.atlassian.connector.eclipse.internal.crucible.core.client.CrucibleClientData;
import com.atlassian.connector.eclipse.internal.crucible.ui.CrucibleUiPlugin;
import com.atlassian.connector.eclipse.internal.fisheye.core.client.FishEyeClient;
import com.atlassian.connector.eclipse.internal.fisheye.core.client.FishEyeClientData;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.mylyn.commons.net.Policy;
import org.eclipse.mylyn.tasks.core.RepositoryTemplate;
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.eclipse.mylyn.tasks.ui.wizards.AbstractRepositorySettingsPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.widgets.ExpandableComposite;

import java.net.MalformedURLException;
import java.net.URL;

/**
 * Wizard for configuring the Crucible repository settings
 * 
 * @author Shawn Minto
 */
public class CrucibleRepositorySettingsPage extends AbstractRepositorySettingsPage {

//	private Button buttonAlways;
//
//	private Button buttonNever;
//
//	private Button buttonPrompt;

	private Button fishEyeButton;

	private boolean isFishEyeDetected;

//	private ComboViewer defaultProjectCombo;
//
//	private ExpandableComposite defaultSection;
//
//	private Button updateRepositoryButton;

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
					throw new CoreException(new Status(IStatus.ERROR, CrucibleUiPlugin.PLUGIN_ID,
							"This server does not seem to be integrated with FishEye.", e));
				}
				// if it's not marked as FishEye - that's OK. No exception re-thrown
			} finally {
				if (fishEyeClient != null) {
					clientManager.deleteTempFishEyeClient(fishEyeClient.getServerData());
				}
			}
//			}
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
//		ExpandableComposite section = createSection(parentControl, "Review Activation");
//		section.setExpanded(true);
//		if (section.getLayoutData() instanceof GridData) {
//			GridData gd = ((GridData) section.getLayoutData());
//			gd.grabExcessVerticalSpace = true;
//			gd.verticalAlignment = SWT.FILL;
//		}
//
//		Composite composite = new Composite(section, SWT.NONE);
//		GridLayout layout = new GridLayout(2, false);
//		layout.marginWidth = 0;
//		composite.setLayout(layout);
//		section.setClient(composite);
//
//		Group group = new Group(composite, SWT.NONE);
//		group.setLayout(new GridLayout(3, true));
//		group.setText("Activate Review when opening a file from within the Review Editor");
//
//		buttonAlways = new Button(group, SWT.RADIO);
//		buttonAlways.setText("Always");
//		buttonNever = new Button(group, SWT.RADIO);
//		buttonNever.setText("Never");
//		buttonPrompt = new Button(group, SWT.RADIO);
//		buttonPrompt.setText("Prompt");

//		ActivateReview pref = CrucibleUiPlugin.getActivateReviewPreference();
//		if (pref.equals(ActivateReview.ALWAYS)) {
//			buttonAlways.setSelection(true);
//		} else if (pref.equals(ActivateReview.NEVER)) {
//			buttonNever.setSelection(true);
//		} else {
//			buttonPrompt.setSelection(true);
//		}

		ExpandableComposite fishEyeSection = createSection(parentControl, "FishEye");
		fishEyeSection.setExpanded(true);
		fishEyeButton = new Button(fishEyeSection, SWT.CHECK);
		fishEyeButton.setText("Crucible Server Contains FishEye Instance");
		fishEyeSection.setClient(fishEyeButton);
		fishEyeButton.setSelection(repository != null && CrucibleRepositoryConnector.isFishEye(repository));

//		defaultSection = createSection(parentControl, "Default Crucible Project");
//		defaultSection.setExpanded(true);
//
//		composite = new Composite(defaultSection, SWT.NONE);
//		layout = new GridLayout(3, false);
//		layout.marginWidth = 0;
//		composite.setLayout(layout);
//		defaultSection.setClient(composite);
//
//		Label project = new Label(composite, SWT.NONE);
//		project.setText("Project:");
//
//		defaultProjectCombo = new ComboViewer(composite);
//
//		defaultProjectCombo.setLabelProvider(new CrucibleProjectsLabelProvider());
//		defaultProjectCombo.setContentProvider(new CrucibleProjectsContentProvider());
//		defaultProjectCombo.setSorter(new ViewerSorter());
//
//		fillCrucibleProjectsCombo();
//
//		updateRepositoryButton = new Button(composite, SWT.PUSH);
//		updateRepositoryButton.setText("Update Repository Data");
//		updateRepositoryButton.addSelectionListener(new SelectionAdapter() {
//			@Override
//			public void widgetSelected(SelectionEvent e) {
//
//				IRunnableWithProgress runnable = new IRunnableWithProgress() {
//					public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
//						CrucibleRepositoryConnector connector = CrucibleCorePlugin.getRepositoryConnector();
//						CrucibleClient client = connector.getClientManager().getClient(getRepository());
//						if (client != null) {
//							try {
//								client.updateRepositoryData(monitor, getRepository());
//							} catch (CoreException e) {
//								StatusHandler.log(new Status(IStatus.ERROR, CrucibleUiPlugin.PLUGIN_ID,
//										"Failed to update repository data", e));
//							}
//						}
//					}
//				};
//				try {
//					if (getRepository() != null) {
//						getWizard().getContainer().run(true, true, runnable);
//					}
//				} catch (Exception ex) {
//					StatusHandler.log(new Status(IStatus.ERROR, CrucibleUiPlugin.PLUGIN_ID,
//							"Failed to update repository data", ex));
//				}
//				if (!CrucibleUiUtil.hasCachedData(getRepository())) {
//					setErrorMessage("Could not retrieve available projects and users from server.");
//				} else {
//					fillCrucibleProjectsCombo();
//				}
//			}
//		});

	}

//	private void fillCrucibleProjectsCombo() {
//		if (repository != null) {
//			Set<CrucibleProject> cachedProjects = CrucibleUiUtil.getCachedProjects(repository);
//			defaultProjectCombo.setInput(cachedProjects);
//
//			CrucibleProject defaultProject = CrucibleRepositoryConnector.getDefaultProject(repository, cachedProjects);
//			if (defaultProject != null) {
//				defaultProjectCombo.setSelection(new StructuredSelection(defaultProject));
//			}
//
//			// repaint the combo (adjust width to the data)
//			defaultSection.setExpanded(false);
//			defaultSection.setExpanded(true);
//		}
//	}

	@Override
	public void applyTo(TaskRepository repository) {
		super.applyTo(repository);
		CrucibleCorePlugin.getRepositoryConnector();
		CrucibleRepositoryConnector.updateFishEyeStatus(repository, fishEyeButton.getSelection());

//		// save default project selection
//		Object firstElement = ((IStructuredSelection) defaultProjectCombo.getSelection()).getFirstElement();
//		if (firstElement != null) {
//			CrucibleRepositoryConnector.updateDefaultProject(repository, (CrucibleProject) firstElement);
//		}

		// @todo wseliga I think it does not belong here. Should be in Crucible preference page
		//store activation preference
//		if (buttonAlways.getSelection()) {
//			CrucibleUiPlugin.setActivateReviewPreference(MessageDialogWithToggle.ALWAYS);
//		} else if (buttonNever.getSelection()) {
//			CrucibleUiPlugin.setActivateReviewPreference(MessageDialogWithToggle.NEVER);
//		} else {
//			CrucibleUiPlugin.setActivateReviewPreference(MessageDialogWithToggle.PROMPT);
//		}
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

//	@Override
//	public boolean isPageComplete() {
//		boolean ret = super.isPageComplete();
//		updateRepositoryButton.setEnabled(ret);
//		return ret;
//	}

}
