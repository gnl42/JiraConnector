/*******************************************************************************
 * Copyright (c) 2005-2006 Polarion Software.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Sergiy Logvin - Initial API and implementation
 *    Sergey Vasilchenko - [patch] bug fix: Trunk, branches and tags default values are not stored if they are disabled while creating new location
 *******************************************************************************/
package com.atlassian.theplugin.eclipse.ui.composite.bamboo;

import org.eclipse.compare.internal.TabFolderLayout;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Text;

import com.atlassian.theplugin.eclipse.core.bamboo.IBambooServer;
import com.atlassian.theplugin.eclipse.preferences.Activator;
import com.atlassian.theplugin.eclipse.ui.dialog.DefaultDialog;
import com.atlassian.theplugin.eclipse.ui.panel.IPropertiesPanel;
import com.atlassian.theplugin.eclipse.ui.verifier.AbstractVerifier;
import com.atlassian.theplugin.eclipse.ui.verifier.IValidationManager;
import com.atlassian.theplugin.eclipse.view.bamboo.BambooConfigurationStorage;

/**
 * Repository properties tab folder
 * 
 * @author Sergiy Logvin
 */
public class BambooServerPropertiesTabFolder extends Composite implements
		IPropertiesPanel {

	protected Composite parent;
	protected IBambooServer bambooServer;
	protected int style;
	protected IValidationManager validationManager;
	protected Button validateButton;
	protected Button resetChangesButton;
	protected boolean validateOnFinish;
	protected boolean forceDisableRoots;
	protected boolean createNew;
	protected Combo cachedRealms;
	protected BambooServerPropertiesComposite serverPropertiesPanel;

	protected IBambooServer backup;

	public BambooServerPropertiesTabFolder(Composite parent, int style,
			IValidationManager validationManager, IBambooServer bambooServer) {
		super(parent, style);
		this.parent = parent;
		this.style = style;
		this.validationManager = validationManager;
		this.bambooServer = bambooServer;
		this.createNew = bambooServer == null;
		if (this.createNew) {
			this.bambooServer = BambooConfigurationStorage.instance()
					.newBambooServer();
		} else {
			this.backup = BambooConfigurationStorage.instance()
					.newBambooServer();
			BambooConfigurationStorage.instance().copyBambooServer(this.backup,
					this.bambooServer);
		}
	}

	@SuppressWarnings("restriction")
	public void initialize() {
		GridLayout layout = new GridLayout();
		layout.verticalSpacing = 7;
		this.setLayout(layout);
		TabFolder tabFolder = new TabFolder(this, SWT.NONE);
		tabFolder.setLayout(new TabFolderLayout());
		tabFolder.setLayoutData(new GridData(GridData.FILL_BOTH));

		TabItem tabItem = new TabItem(tabFolder, SWT.NONE);
		tabItem.setText(Activator.getDefault().getResource(
				"BambooServerPropertiesTabFolder.General"));
		tabItem.setControl(this.createBambooServerPropertiesPanel(tabFolder));

		// tabItem = new TabItem(tabFolder, SWT.NONE);
		// tabItem.setText(Activator.getDefault().getResource(
		// "BambooServerPropertiesTabFolder.Advanced"));
		// tabItem.setControl(this.createRepositoryRootsComposite(tabFolder));

		GridData data = null;

		Composite bottomPart = new Composite(this, SWT.NONE);
		layout = new GridLayout();
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		layout.numColumns = 3;
		bottomPart.setLayout(layout);
		data = new GridData(GridData.FILL_HORIZONTAL);
		bottomPart.setLayoutData(data);

		Composite realmsComposite = new Composite(bottomPart, SWT.NONE);
		layout = new GridLayout();
		layout.marginWidth = layout.marginHeight = 0;
		layout.numColumns = 3;
		realmsComposite.setLayout(layout);
		data = new GridData(GridData.FILL_HORIZONTAL);
		data.horizontalSpan = 3;
		realmsComposite.setLayoutData(data);

		Label label = new Label(realmsComposite, SWT.NONE);
		data = new GridData();
		label.setLayoutData(data);
		label.setText(Activator.getDefault().getResource(
				"BambooServerPropertiesTabFolder.ShowFor"));

		this.cachedRealms = new Combo(realmsComposite, SWT.BORDER
				| SWT.READ_ONLY);
		// final Button deleteRealm = new Button(realmsComposite, SWT.PUSH);

		/*
		 * data = new GridData(GridData.FILL_HORIZONTAL);
		 * this.cachedRealms.setLayoutData(data); final ArrayList itemSet = new
		 * ArrayList(); itemSet.add("<Repository Location>");
		 * itemSet.addAll(this.bambooServer.getRealms());
		 * this.cachedRealms.setItems((String [])itemSet.toArray(new
		 * String[itemSet.size()])); this.cachedRealms.select(0);
		 * this.cachedRealms.addSelectionListener(new SelectionAdapter() {
		 * public void widgetSelected(SelectionEvent e) {
		 * deleteRealm.setEnabled(
		 * BambooServerPropertiesTabFolder.this.cachedRealms.getSelectionIndex()
		 * != 0); BambooServerPropertiesTabFolder.this.realmSelectionChanged();
		 * } });
		 */

		/*
		 * ImageDescriptor imgDescr =
		 * Activator.getDefault().getImageDescriptor("icons/common/delete_realm.gif"
		 * ); deleteRealm.setImage(imgDescr.createImage()); data = new
		 * GridData(); data.heightHint = this.cachedRealms.getTextHeight() + 2;
		 * deleteRealm.setLayoutData(data); deleteRealm.addSelectionListener(new
		 * SelectionAdapter() { public void widgetSelected(SelectionEvent e) {
		 * int idx =
		 * BambooServerPropertiesTabFolder.this.cachedRealms.getSelectionIndex
		 * (); if (idx != 0) { String item =
		 * (String)BambooServerPropertiesTabFolder
		 * .this.cachedRealms.getItem(idx); itemSet.remove(item);
		 * BambooServerPropertiesTabFolder.this.cachedRealms.setItems((String
		 * [])itemSet.toArray(new String[itemSet.size()]));
		 * BambooServerPropertiesTabFolder.this.cachedRealms.select(idx - 1);
		 * BambooServerPropertiesTabFolder.this.realmSelectionChanged(); }
		 * boolean enabled =
		 * BambooServerPropertiesTabFolder.this.cachedRealms.getItems().length >
		 * 1; ((Button)e.widget).setEnabled(enabled);
		 * BambooServerPropertiesTabFolder
		 * .this.cachedRealms.setEnabled(enabled); idx =
		 * BambooServerPropertiesTabFolder
		 * .this.cachedRealms.getSelectionIndex(); if (idx == 0) {
		 * ((Button)e.widget).setEnabled(false); } } });
		 * deleteRealm.setEnabled(false);
		 * BambooServerPropertiesTabFolder.this.cachedRealms
		 * .setEnabled(itemSet.size() > 1);
		 */

		this.validateButton = new Button(bottomPart, SWT.CHECK);
		data = new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING);
		this.validateButton.setLayoutData(data);
		this.validateButton.setText(Activator.getDefault().getResource(
				"BambooServerPropertiesTabFolder.ValidateOnFinish"));
		this.validateButton.setSelection(true);

		Text empty = new Text(bottomPart, SWT.READ_ONLY);
		data = new GridData(GridData.FILL_HORIZONTAL);
		empty.setLayoutData(data);
		empty.setVisible(false);

		this.resetChangesButton = new Button(bottomPart, SWT.PUSH);
		data = new GridData(GridData.HORIZONTAL_ALIGN_END);
		this.resetChangesButton.setText(Activator.getDefault().getResource(
				"BambooServerPropertiesTabFolder.ResetChanges"));
		int widthHint = DefaultDialog
				.computeButtonWidth(this.resetChangesButton);
		data.widthHint = widthHint;
		this.resetChangesButton.setLayoutData(data);
		this.resetChangesButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				BambooServerPropertiesTabFolder.this.resetChanges();
				BambooServerPropertiesTabFolder.this.validationManager
						.validateContent();
			}
		});

		/*
		 * if ((this.bambooServer.getUsername() == null ||
		 * this.bambooServer.getUsername().length() == 0) &&
		 * this.bambooServer.getRealms().size() > 0) {
		 * this.cachedRealms.select(1); deleteRealm.setEnabled(true);
		 * this.realmSelectionChanged(); }
		 */
	}

	protected Control createBambooServerPropertiesPanel(TabFolder tabFolder) {
		this.serverPropertiesPanel = new BambooServerPropertiesComposite(
				tabFolder, this.style, this.validationManager);
		this.serverPropertiesPanel.setBambooServer(this.bambooServer,
				this.createNew ? null : this.bambooServer.getUrl());
		this.serverPropertiesPanel.initialize();

		return this.serverPropertiesPanel;
	}

	protected void realmSelectionChanged() {
		/*
		 * BambooServer location = this.bambooServer; int idx =
		 * this.cachedRealms.getSelectionIndex(); if (idx != 0) { location =
		 * location.getLocationForRealm(this.cachedRealms.getItem(idx)); }
		 * 
		 * this.repositoryPropertiesPanel.saveChanges();
		 * this.repositoryPropertiesPanel.setCredentialsInput(location);
		 * this.repositoryPropertiesPanel.resetChanges(); if
		 * (CoreExtensionsManager
		 * .instance().getSVNClientWrapperFactory().isSSHOptionsAllowed()) {
		 * this.sshComposite.saveChanges();
		 * this.sshComposite.setCredentialsInput(location.getSSHSettings());
		 * this.sshComposite.resetChanges(); } this.sslComposite.saveChanges();
		 * this.sslComposite.setCredentialsInput(location.getSSLSettings());
		 * this.sslComposite.resetChanges();
		 */
	}

	/*
	 * public void updateTabContent(boolean wasAvailable, boolean isAvailable,
	 * TabItem tab, AbstractDynamicComposite availableComposite, Composite
	 * unavailableComposite) { if (isAvailable) { if (!wasAvailable) {
	 * availableComposite.restoreAppearance();
	 * tab.setControl(availableComposite); } } else { if (wasAvailable) {
	 * availableComposite.saveAppearance();
	 * tab.setControl(unavailableComposite);
	 * availableComposite.revalidateContent(); } } }
	 */

	public IBambooServer getBambooServer() {
		return this.bambooServer;
	}

	public boolean isValidateOnFinishRequested() {
		return this.validateOnFinish;
	}

	public void saveChanges() {
		this.serverPropertiesPanel.saveChanges();
		/*
		 * if(CoreExtensionsManager.instance().getSVNClientWrapperFactory().
		 * isSSHOptionsAllowed()) { this.sshComposite.saveChanges(); }
		 * this.sslComposite.saveChanges(); if
		 * (CoreExtensionsManager.instance().
		 * getSVNClientWrapperFactory().isProxyOptionsAllowed()) {
		 * this.proxyComposite.saveChanges(); }
		 * this.rootsComposite.saveChanges();
		 * 
		 * if(CoreExtensionsManager.instance().getSVNClientWrapperFactory().
		 * isProxyOptionsAllowed()) { ProxySettings proxySettings =
		 * this.BambooServer.getProxySettings();
		 * proxySettings.setEnabled(this.proxyComposite.isProxyEnabled());
		 * proxySettings
		 * .setAuthenticationEnabled(this.proxyComposite.isAuthenticationEnabled
		 * ()); proxySettings.setHost(this.proxyComposite.getHost());
		 * proxySettings.setPort(this.proxyComposite.getPort());
		 * proxySettings.setUsername(this.proxyComposite.getUsername());
		 * proxySettings.setPassword(this.proxyComposite.getPassword());
		 * proxySettings.setPasswordSaved(this.proxyComposite.isSavePassword());
		 * }
		 * 
		 * IPreferenceStore store = Activator.getDefault().getPreferenceStore();
		 * boolean enabled = this.rootsComposite.isStructureEnabled(); if
		 * (enabled) {
		 * this.BambooServer.setTrunkLocation(this.rootsComposite.getTrunkLocation
		 * ());this.BambooServer.setBranchesLocation(this.rootsComposite.
		 * getBranchesLocation());
		 * this.BambooServer.setTagsLocation(this.rootsComposite
		 * .getTagsLocation()); } else if (this.createNew) {
		 * this.BambooServer.setTrunkLocation
		 * (SVNTeamPreferences.getRepositoryString(store,
		 * SVNTeamPreferences.REPOSITORY_HEAD_NAME));
		 * this.BambooServer.setBranchesLocation
		 * (SVNTeamPreferences.getRepositoryString(store,
		 * SVNTeamPreferences.REPOSITORY_BRANCHES_NAME));
		 * this.BambooServer.setTagsLocation
		 * (SVNTeamPreferences.getRepositoryString(store,
		 * SVNTeamPreferences.REPOSITORY_TAGS_NAME)); }
		 * this.BambooServer.setStructureEnabled(enabled);
		 * 
		 * HashSet realms = new
		 * HashSet(Arrays.asList(this.cachedRealms.getItems())); for (Iterator
		 * it = this.BambooServer.getRealms().iterator(); it.hasNext(); ) { if
		 * (!realms.contains(it.next())) { it.remove(); } }
		 */
		this.validateOnFinish = this.validateButton.getSelection();
	}

	public void resetChanges() {
		this.serverPropertiesPanel.resetChanges();
		/*
		 * if(CoreExtensionsManager.instance().getSVNClientWrapperFactory().
		 * isSSHOptionsAllowed()) { this.sshComposite.resetChanges(); }
		 * this.sslComposite.resetChanges(); if
		 * (CoreExtensionsManager.instance()
		 * .getSVNClientWrapperFactory().isProxyOptionsAllowed()) {
		 * this.proxyComposite.resetChanges(); }
		 * this.rootsComposite.resetChanges();
		 */
	}

	public void cancelChanges() {
		if (!this.createNew) {
			BambooConfigurationStorage.instance().copyBambooServer(
					this.bambooServer, this.backup);
		}
	}

	public void setForceDisableRoots(boolean forceDisableRoots,
			AbstractVerifier verifier) {
		/*
		 * if (this.rootsComposite != null) {
		 * this.rootsComposite.setForceDisableRoots(forceDisableRoots); } if
		 * (this.repositoryPropertiesPanel != null) {
		 * this.repositoryPropertiesPanel.defineUrlVerifier(verifier); }
		 */
	}

	public String getServerUrl() {
		return this.serverPropertiesPanel.getLocationUrl();
	}

}
