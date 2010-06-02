package com.atlassian.connector.eclipse.internal.fisheye.ui;

import com.atlassian.connector.eclipse.internal.commons.ui.MigrateToSecureStorageJob;
import com.atlassian.connector.eclipse.internal.fisheye.core.FishEyeCorePlugin;

import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

/**
 * The activator class controls the plug-in life cycle
 */
public class FishEyeUiPlugin extends AbstractUIPlugin {

	// The plug-in ID
	public static final String PLUGIN_ID = "com.atlassian.connector.eclipse.fisheye.ui";

	public static final String PRODUCT_NAME = "FishEye Connector";

	// The shared instance
	private static FishEyeUiPlugin plugin;

	/**
	 * The constructor
	 */
	public FishEyeUiPlugin() {
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext)
	 */
	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;

		if (!getPreferenceStore().getBoolean(FishEyeConstants.PREFERENCE_SECURE_STORAGE_MIGRATED)) {
			Job migrateJob = new MigrateToSecureStorageJob(FishEyeCorePlugin.CONNECTOR_KIND);
			migrateJob.addJobChangeListener(new JobChangeAdapter() {
				@Override
				public void done(IJobChangeEvent event) {
					getPreferenceStore().setValue(FishEyeConstants.PREFERENCE_SECURE_STORAGE_MIGRATED, Boolean.TRUE);
				}
			});
			migrateJob.schedule();
		}

	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext context) throws Exception {
		plugin = null;
		super.stop(context);
	}

	/**
	 * Returns the shared instance
	 *
	 * @return the shared instance
	 */
	public static FishEyeUiPlugin getDefault() {
		return plugin;
	}

}
