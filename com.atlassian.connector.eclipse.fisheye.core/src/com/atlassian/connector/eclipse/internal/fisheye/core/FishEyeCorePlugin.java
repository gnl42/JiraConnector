package com.atlassian.connector.eclipse.internal.fisheye.core;

import org.eclipse.core.runtime.Plugin;
import org.osgi.framework.BundleContext;

/**
 * The activator class controls the plug-in life cycle
 */
public class FishEyeCorePlugin extends Plugin {

	// The plug-in ID
	public static final String PLUGIN_ID = "com.atlassian.connector.eclipse.fisheye.core";
	
	public static final String CONNECTOR_KIND = "fisheye";
	

	private FishEyeRepositoryConnector repositoryConnector;
	
	// The shared instance
	private static FishEyeCorePlugin plugin;
	
	/**
	 * The constructor
	 */
	public FishEyeCorePlugin() {
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.core.runtime.Plugins#start(org.osgi.framework.BundleContext)
	 */
	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.core.runtime.Plugin#stop(org.osgi.framework.BundleContext)
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
	public static FishEyeCorePlugin getDefault() {
		return plugin;
	}

	public void setRepositoryConnector(FishEyeRepositoryConnector repositoryConnector) {
		this.repositoryConnector = repositoryConnector;
	}

	public FishEyeRepositoryConnector getRepositoryConnector() {
		return repositoryConnector;
	}

}
