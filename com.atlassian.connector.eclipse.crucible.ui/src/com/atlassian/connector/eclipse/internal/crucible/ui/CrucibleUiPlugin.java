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

import com.atlassian.connector.eclipse.crucible.ui.preferences.ActivateReview;
import com.atlassian.connector.eclipse.internal.crucible.core.CrucibleCorePlugin;
import com.atlassian.connector.eclipse.internal.crucible.ui.annotations.CrucibleEditorTracker;
import com.atlassian.connector.eclipse.internal.crucible.ui.annotations.EditorLinkWithReviewSelectionListener;
import com.atlassian.connector.eclipse.internal.crucible.ui.notifications.CrucibleNotificationProvider;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.MessageDialogWithToggle;
import org.eclipse.mylyn.monitor.ui.MonitorUi;
import org.eclipse.mylyn.tasks.ui.TasksUi;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.eclipse.ui.progress.UIJob;
import org.osgi.framework.BundleContext;

/**
 * The activator class controls the plug-in life cycle
 * 
 * @author Shawn Minto
 */
public class CrucibleUiPlugin extends AbstractUIPlugin {

	// The plug-in ID
	public static final String PLUGIN_ID = "com.atlassian.connector.eclipse.crucible.ui";

	public static final String PRODUCT_NAME = "Atlassian Crucible Connector";

	// The shared instance
	private static CrucibleUiPlugin plugin;

	private CrucibleEditorTracker crucibleEditorTracker;

	private ActiveReviewManager activeReviewManager;

	private CrucibleNotificationProvider crucibleNotificationManager;

	private EditorLinkWithReviewSelectionListener editorLinkWithReviewSelectionListener;

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

		crucibleEditorTracker = new CrucibleEditorTracker();

		activeReviewManager = new ActiveReviewManager(true);

		enableActiveReviewManager();

		crucibleNotificationManager = new CrucibleNotificationProvider();
		CrucibleCorePlugin.getDefault().getReviewCache().addCacheChangedListener(crucibleNotificationManager);

		// TODO determine if we should be doing this differently and not through mylyn
		MonitorUi.addWindowPartListener(crucibleEditorTracker);
		editorLinkWithReviewSelectionListener = new EditorLinkWithReviewSelectionListener();
		MonitorUi.addWindowPostSelectionListener(editorLinkWithReviewSelectionListener);

		plugin.getPreferenceStore().setDefault(CrucibleUiConstants.PREFERENCE_ACTIVATE_REVIEW,
				MessageDialogWithToggle.PROMPT);

		// PLE-516 we want to make sure that we init CrucibleEditorTracker only when this bundle is really started
		// as it otherwise may cause class loading problems (another thread attempts to load classes from not started bundle)
		// unfortunately this implementation does not guarantee 100%, but it's really unlikely that the problem will occur
		// i.e. other plugins seem to use such approach and it works for them
		UIJob job = new UIJob("Initializing Crucible editor annonation support") {
			@Override
			public IStatus runInUIThread(IProgressMonitor monitor) {
				crucibleEditorTracker.init();
				return Status.OK_STATUS;
			}
		};
		job.schedule();
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

		MonitorUi.removeWindowPostSelectionListener(editorLinkWithReviewSelectionListener);
		MonitorUi.removeWindowPartListener(crucibleEditorTracker);
		crucibleEditorTracker.dispose();
		crucibleEditorTracker = null;
		editorLinkWithReviewSelectionListener = null;

		activeReviewManager.dispose();
		activeReviewManager = null;
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

	public static ActivateReview getActivateReviewPreference() {

		ActivateReview ret = ActivateReview.getObjectFromKey(plugin.getPreferenceStore().getString(
				CrucibleUiConstants.PREFERENCE_ACTIVATE_REVIEW));

		return ret != null ? ret : ActivateReview.PROMPT;
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

}
