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
import com.atlassian.connector.eclipse.internal.crucible.ui.notifications.CrucibleNotificationProvider;
import com.atlassian.connector.eclipse.ui.commons.ResourceSelectionTree.TreeViewMode;

import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.dialogs.MessageDialogWithToggle;
import org.eclipse.mylyn.tasks.ui.TasksUi;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

/**
 * The activator class controls the plug-in life cycle
 * 
 * @author Shawn Minto
 */
public class CrucibleUiPlugin extends AbstractUIPlugin {

	// The plug-in ID
	public static final String PLUGIN_ID = "com.atlassian.connector.eclipse.crucible.ui";

	public static final String REVIEW_PERSPECTIVE_ID = PLUGIN_ID + ".reviewPerspective";

	public static final String COMMENT_VIEW_ID = PLUGIN_ID + ".commentView";

	public static final String EXPLORER_VIEW_ID = PLUGIN_ID + ".explorerView";

	public static final String PRODUCT_NAME = "Atlassian Crucible Connector";

	// The shared instance
	private static CrucibleUiPlugin plugin;

	private ActiveReviewManager activeReviewManager;

	private CrucibleNotificationProvider crucibleNotificationManager;

	private SwitchingPerspectiveReviewActivationListener switchingPerspectivesListener;

	private AvatarImages avatarImages;

	/**
	 * The constructor
	 */
	public CrucibleUiPlugin() {
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext)
	 */
	@Override
	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;

		TasksUi.getRepositoryManager().addListener(CrucibleCorePlugin.getRepositoryConnector().getClientManager());

		switchingPerspectivesListener = new SwitchingPerspectiveReviewActivationListener();
		activeReviewManager = new ActiveReviewManager(true);
		activeReviewManager.addReviewActivationListener(switchingPerspectivesListener);

		avatarImages = new AvatarImages();

		enableActiveReviewManager();

		crucibleNotificationManager = new CrucibleNotificationProvider();
		CrucibleCorePlugin.getDefault().getReviewCache().addCacheChangedListener(crucibleNotificationManager);

		plugin.getPreferenceStore().setDefault(CrucibleUiConstants.PREFERENCE_ACTIVATE_REVIEW,
				MessageDialogWithToggle.PROMPT);
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext)
	 */
	@Override
	public void stop(BundleContext context) throws Exception {
		CrucibleCorePlugin.getDefault().getReviewCache().removeCacheChangedListener(crucibleNotificationManager);
		crucibleNotificationManager = null;

		disableActiveReviewManager();

		activeReviewManager.dispose();
		activeReviewManager = null;

		avatarImages.dispose();
		avatarImages = null;

		plugin = null;
		super.stop(context);

	}

	/**
	 * Returns the shared instance
	 * 
	 * @return the shared instance
	 */
	public static CrucibleUiPlugin getDefault() {
		return plugin;
	}

	public ActiveReviewManager getActiveReviewManager() {
		return activeReviewManager;
	}

	/**
	 * Method for testing purposes
	 */
	public void disableActiveReviewManager() {
		if (activeReviewManager != null) {
			TasksUi.getTaskActivityManager().removeActivationListener(activeReviewManager);
		}
	}

	/**
	 * Method for testing purposes
	 */
	public void enableActiveReviewManager() {
		if (activeReviewManager != null) {
			TasksUi.getTaskActivityManager().addActivationListener(activeReviewManager);
		}
	}

	public boolean getPreviousChangesetReviewSelection() {
		return plugin.getPreferenceStore().getBoolean(CrucibleUiConstants.PREVIOUS_CHANGESET_REVIEW_SELECTION);
	}

	public boolean getPreviousPatchReviewSelection() {
		return plugin.getPreferenceStore().getBoolean(CrucibleUiConstants.PREVIOUS_PATCH_REVIEW_SELECTION);
	}

	public boolean getPreviousWorkspacePatchReviewSelection() {
		return plugin.getPreferenceStore().getBoolean(CrucibleUiConstants.PREVIOUS_WORKSPACE_PATCH_REVIEW_SELECTION);
	}

	public void setPreviousChangesetReviewSelection(boolean value) {
		plugin.getPreferenceStore().setValue(CrucibleUiConstants.PREVIOUS_CHANGESET_REVIEW_SELECTION, value);
	}

	public void setPreviousPatchReviewSelection(boolean value) {
		plugin.getPreferenceStore().setValue(CrucibleUiConstants.PREVIOUS_PATCH_REVIEW_SELECTION, value);
	}

	public void setPreviousWorkspacePatchReviewSelection(boolean value) {
		plugin.getPreferenceStore().setValue(CrucibleUiConstants.PREVIOUS_WORKSPACE_PATCH_REVIEW_SELECTION, value);
	}

	public TreeViewMode getResourcesTreeViewMode() {
		int mode = plugin.getPreferenceStore().getInt(CrucibleUiConstants.PREFERENCE_RESOURCE_TREE_VIEW_MODE);
		for (TreeViewMode treeMode : TreeViewMode.values()) {
			if (treeMode.ordinal() == mode) {
				return treeMode;
			}
		}

		return TreeViewMode.MODE_COMPRESSED_FOLDERS;
	}

	public void setResourcesTreeViewMode(TreeViewMode mode) {
		plugin.getPreferenceStore().setValue(CrucibleUiConstants.PREFERENCE_RESOURCE_TREE_VIEW_MODE, mode.ordinal());
	}

	public IDialogSettings getDialogSettingsSection(String name) {
		IDialogSettings dialogSettings = getDialogSettings();
		IDialogSettings section = dialogSettings.getSection(name);
		if (section == null) {
			section = dialogSettings.addNewSection(name);
		}
		return section;
	}

	public AvatarImages getAvatarsCache() {
		return this.avatarImages;
	}
}
