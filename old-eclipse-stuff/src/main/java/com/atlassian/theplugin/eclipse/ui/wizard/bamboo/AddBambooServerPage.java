/*******************************************************************************
 * Copyright (c) 2005-2006 Polarion Software.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Alexander Gurov - Initial API and implementation
 *******************************************************************************/

package com.atlassian.theplugin.eclipse.ui.wizard.bamboo;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

import com.atlassian.theplugin.eclipse.core.bamboo.BambooUtility;
import com.atlassian.theplugin.eclipse.core.bamboo.IBambooServer;
import com.atlassian.theplugin.eclipse.core.operation.AbstractActionOperation;
import com.atlassian.theplugin.eclipse.core.operation.AbstractNonLockingOperation;
import com.atlassian.theplugin.eclipse.core.operation.CompositeOperation;
import com.atlassian.theplugin.eclipse.core.operation.IActionOperation;
import com.atlassian.theplugin.eclipse.core.operation.bamboo.AddBambooServerOperation;
import com.atlassian.theplugin.eclipse.core.operation.bamboo.RefreshBambooServersOperation;
import com.atlassian.theplugin.eclipse.core.operation.bamboo.SaveBambooServersOperation;
import com.atlassian.theplugin.eclipse.preferences.Activator;
import com.atlassian.theplugin.eclipse.ui.composite.bamboo.BambooServerPropertiesTabFolder;
import com.atlassian.theplugin.eclipse.ui.dialog.bamboo.NonValidBambooLocationErrorDialog;
import com.atlassian.theplugin.eclipse.ui.panel.AbstractDialogPanel;
import com.atlassian.theplugin.eclipse.ui.utility.UIMonitorUtil;
import com.atlassian.theplugin.eclipse.ui.verifier.AbstractFormattedVerifier;
import com.atlassian.theplugin.eclipse.ui.wizard.AbstractVerifiedWizardPage;
import com.atlassian.theplugin.eclipse.util.PluginIcons;
import com.atlassian.theplugin.eclipse.view.bamboo.BambooConfigurationStorage;

/**
 * Add repository location wizard page
 * 
 * @author Alexander Gurov
 */
public class AddBambooServerPage extends AbstractVerifiedWizardPage {
	protected BambooServerPropertiesTabFolder propertiesTabFolder;
	protected IActionOperation operationToPerform;
	protected IBambooServer editable;
	protected boolean alreadyConnected;
	protected boolean createNew;
	protected String initialUrl;
	protected String oldUrl;
	protected String oldLabel;
	protected String oldUuid;

	public AddBambooServerPage() {
		this(null);
	}

	public AddBambooServerPage(IBambooServer editable) {
		super(AddBambooServerPage.class.getName(), Activator.getDefault()
				.getResource("AddBambooServerPage.Title"), ImageDescriptor
				.createFromImage(PluginIcons.getImageRegistry().get(
						PluginIcons.ICON_BAMBOO_LARGE)));

		this.setDescription(Activator.getDefault().getResource(
				"AddBambooServerPage.Description"));
		this.editable = editable;
		if (editable != null) {
			this.oldUrl = editable.getUrl();
			this.oldLabel = editable.getLabel();
			this.oldUuid = null;
		}
		this.alreadyConnected = false;
		this.createNew = true;
	}

	protected Composite createControlImpl(Composite parent) {
		this.propertiesTabFolder = new BambooServerPropertiesTabFolder(parent,
				SWT.NONE, this, this.editable);
		this.propertiesTabFolder.initialize();
		this.propertiesTabFolder.setLayoutData(new GridData(
				GridData.FILL_HORIZONTAL));

		this.propertiesTabFolder.resetChanges();

		// Setting context help
		// PlatformUI.getWorkbench().getHelpSystem().setHelp(parent,
		// "org.atlassian.theplugin.help.newBambooServerWizContext");

		return this.propertiesTabFolder;
	}

	public void setInitialUrl(String initialUrl) {
		this.initialUrl = initialUrl;
		if (this.alreadyConnected = initialUrl != null) {
			this.createNew = initialUrl.trim().length() == 0;
			this.getBambooServer().setUrl(initialUrl);
			this.propertiesTabFolder.resetChanges();
		}
	}

	public void setForceDisableRoots(boolean force) {
		this.propertiesTabFolder.setForceDisableRoots(force,
				this.initialUrl == null || this.initialUrl.length() == 0 ? null
						: new AbstractFormattedVerifier(Activator.getDefault()
								.getResource("AddBambooServerPage.RootURL")) {
							protected String getErrorMessageImpl(Control input) {
								/*
								 * String url = this.getText(input); if (!new
								 * Path(url).isPrefixOf(new
								 * Path(SVNUtility.decodeURL
								 * (AddBambooServerPage.this.initialUrl)))) {
								 * String message =
								 * Activator.getDefault().getResource
								 * ("AddBambooServerPage.FixedURL.Verifier.Error"
								 * ); return MessageFormat.format(message, new
								 * String[]
								 * {AbstractFormattedVerifier.FIELD_NAME,
								 * AddBambooServerPage.this.initialUrl}); }
								 */
								return null;
							}

							protected String getWarningMessageImpl(Control input) {
								return null;
							}
						});
	}

	public IBambooServer getBambooServer() {
		return this.propertiesTabFolder.getBambooServer();
	}

	public boolean canFlipToNextPage() {
		return (!this.alreadyConnected || this.createNew)
				&& this.isPageComplete();
	}

	public IWizardPage getNextPage() {
		return this.performFinish() ? super.getNextPage() : this;
	}

	public IWizardPage getPreviousPage() {
		this.performCancel();
		return super.getPreviousPage();
	}

	public void performCancel() {
		this.operationToPerform = null;
	}

	public boolean performFinish() {
		/*
		 * String newUrl = this.propertiesTabFolder.getServerUrl();
		 * ProjectListPanel panel = null; ArrayList connectedProjects = new
		 * ArrayList(); IProject []projectsArray = null; if (this.editable !=
		 * null && !newUrl.equals(this.oldUrl) && this.oldUuid == null) {
		 * IProject []projects =
		 * ResourcesPlugin.getWorkspace().getRoot().getProjects(); for (int i =
		 * 0; i < projects.length; i++) { RepositoryProvider tmp =
		 * RepositoryProvider.getProvider(projects[i]); if (tmp != null &&
		 * Activator.PLUGIN_ID.equals(tmp.getID())) { SVNTeamProvider provider =
		 * (SVNTeamProvider)tmp; if
		 * (AddBambooServerPage.this.editable.equals(provider
		 * .getRepositoryLocation())) { connectedProjects.add(projects[i]); } }
		 * } if (connectedProjects.size() > 0) { projectsArray = (IProject
		 * [])connectedProjects.toArray(new IProject[connectedProjects.size()]);
		 * Info2 info = this.getLocationInfo(this.editable); this.oldUuid = info
		 * == null ? null : info.reposUUID; // if (info == null) { // panel =
		 * new ProjectListPanel(projectsArray, false); // } // else { //
		 * this.oldUuid = info.getUuid(); // } } }
		 */

		this.propertiesTabFolder.saveChanges();

		/*
		 * if (this.propertiesTabFolder.isStructureEnabled()) { IPreferenceStore
		 * store = Activator.getDefault().getPreferenceStore();
		 * 
		 * if (newUrl.endsWith(SVNTeamPreferences.getRepositoryString(store,
		 * SVNTeamPreferences.REPOSITORY_HEAD_NAME)) ||
		 * newUrl.endsWith(SVNTeamPreferences.getRepositoryString(store,
		 * SVNTeamPreferences.REPOSITORY_BRANCHES_NAME)) ||
		 * newUrl.endsWith(SVNTeamPreferences.getRepositoryString(store,
		 * SVNTeamPreferences.REPOSITORY_TAGS_NAME))) { final int []result = new
		 * int[1]; final MessageDialog dialog = new
		 * MessageDialog(this.getShell(),
		 * Activator.getDefault().getResource("AddBambooServerPage.Normalize.Title"
		 * ), null,Activator.getDefault().getResource(
		 * "AddBambooServerPage.Normalize.Message"), MessageDialog.WARNING, new
		 * String[] {IDialogConstants.YES_LABEL, IDialogConstants.NO_LABEL}, 0);
		 * UIMonitorUtil.getDisplay().syncExec(new Runnable() { public void
		 * run() { result[0] = dialog.open(); } }); if (result[0] ==
		 * IDialogConstants.OK_ID) { BambooServer location = this.editable ==
		 * null ? this.getBambooServer() : this.editable; boolean useCustomLabel
		 * = false; useCustomLabel =
		 * !location.getUrl().equals(location.getLabel()); newUrl = (new
		 * Path(newUrl)).removeLastSegments(1).toString();
		 * location.setUrl(newUrl); if (!useCustomLabel) {
		 * location.setLabel(newUrl); } location.reconfigure(); } } }
		 */

		/*
		 * if (connectedProjects.size() > 0) { if (panel == null) {
		 * this.editable.reconfigure(); Info2 newInfo =
		 * this.getLocationInfo(this.editable); if (newInfo == null) { panel =
		 * new ProjectListPanel(projectsArray, false); } else if (this.oldUuid
		 * != null && !this.oldUuid.equals(newInfo.reposUUID)) { panel = new
		 * ProjectListPanel(projectsArray, true); } } if (panel != null) {
		 * this.editable.setUrl(this.oldUrl);
		 * this.editable.setLabel(this.oldLabel); this.editable.reconfigure();
		 * new DefaultDialog(this.getShell(), panel).open(); } }
		 */

		if (this.propertiesTabFolder.isValidateOnFinishRequested() /*
																	 * && panel
																	 * == null
																	 */) {
			final Exception[] problem = new Exception[1];
			UIMonitorUtil.doTaskNowDefault(this.getShell(),
					new AbstractNonLockingOperation(
							"Operation.ValidateBambooServer") {
						protected void runImpl(IProgressMonitor monitor)
								throws Exception {
							problem[0] = BambooUtility
									.validateBambooLocation(AddBambooServerPage.this.propertiesTabFolder
											.getBambooServer());
						}
					}, false);
			if (problem[0] != null) {
				NonValidBambooLocationErrorDialog dialog = new NonValidBambooLocationErrorDialog(
						this.getShell(), problem[0].getMessage());
				if (dialog.open() != 0) {
					return false;
				}
			}
		}

		boolean shouldntBeAdded = this.editable == null ? false
				: (BambooConfigurationStorage.instance().getBambooServer(
						this.editable.getId()) != null);

		AbstractActionOperation mainOp = shouldntBeAdded ? new AbstractNonLockingOperation(
				"Operation.CommitLocationChanges") {
			protected void runImpl(IProgressMonitor monitor) throws Exception {
				// FIXME: AddBambooServerPage.this.editable.reconfigure();
			}
		}
				: (AbstractActionOperation) new AddBambooServerOperation(this
						.getBambooServer());

		CompositeOperation op = new CompositeOperation(mainOp.getId());

		op.add(mainOp);
		op.add(new SaveBambooServersOperation());
		op.add(shouldntBeAdded ? new RefreshBambooServersOperation(
				new IBambooServer[] { this.editable }, true)
				: new RefreshBambooServersOperation(false));

		this.operationToPerform = op;

		return true;
	}

	public IActionOperation getOperationToPeform() {
		return this.operationToPerform;
	}

	protected static class ProjectListPanel extends AbstractDialogPanel {
		protected IProject[] resources;
		protected TableViewer tableViewer;

		public ProjectListPanel(IProject[] input, boolean differentUuid) {
			super(new String[] { IDialogConstants.OK_LABEL });

			this.dialogTitle = Activator.getDefault().getResource(
					"AddBambooServerPage.ProjectList.Title");
			this.dialogDescription = Activator.getDefault().getResource(
					"AddBambooServerPage.ProjectList.Description");
			this.defaultMessage = Activator.getDefault().getResource(
					differentUuid ? "AddBambooServerPage.ProjectList.Message1"
							: "AddBambooServerPage.ProjectList.Message2");
			this.resources = input;
		}

		public void createControls(Composite parent) {
			// ProjectListComposite composite = new ProjectListComposite(parent,
			// SWT.FILL, this.resources, false);
			// composite.initialize();
		}

		protected void saveChanges() {
		}

		protected void cancelChanges() {
		}
	}

}
