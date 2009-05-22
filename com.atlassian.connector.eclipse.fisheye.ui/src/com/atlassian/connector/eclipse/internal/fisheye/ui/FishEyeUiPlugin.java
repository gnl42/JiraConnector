package com.atlassian.connector.eclipse.internal.fisheye.ui;

import com.atlassian.connector.eclipse.fisheye.ui.preferences.FishEyeSettingsManager;

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

	private FishEyeSettingsManager settingsManager;

	/**
	 * The constructor
	 */
	public FishEyeUiPlugin() {
	}

	public FishEyeSettingsManager getFishEyeSettingsManager() {
		return settingsManager;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext)
	 */
	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;
		settingsManager = new FishEyeSettingsManager(getPreferenceStore());
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext context) throws Exception {
		plugin = null;
		settingsManager = null;
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
