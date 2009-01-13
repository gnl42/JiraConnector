package com.atlassian.connector.eclipse.ui;

import com.atlassian.connector.eclipse.ui.team.TeamResourceManager;

import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

/**
 * The activator class controls the plug-in life cycle
 */
public class AtlassianUiPlugin extends AbstractUIPlugin {

	// The plug-in ID
	public static final String PLUGIN_ID = "com.atlassian.connector.eclipse.ui";

	// The shared instance
	private static AtlassianUiPlugin plugin;

	private TeamResourceManager teamResourceManager;

	/**
	 * The constructor
	 */
	public AtlassianUiPlugin() {
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext)
	 */
	@Override
	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext)
	 */
	@Override
	public void stop(BundleContext context) throws Exception {
		plugin = null;
		super.stop(context);
	}

	/**
	 * Returns the shared instance
	 * 
	 * @return the shared instance
	 */
	public static AtlassianUiPlugin getDefault() {
		return plugin;
	}

	public synchronized TeamResourceManager getTeamResourceManager() {
		if (teamResourceManager == null) {
			teamResourceManager = new TeamResourceManager();
		}
		return teamResourceManager;
	}

}
