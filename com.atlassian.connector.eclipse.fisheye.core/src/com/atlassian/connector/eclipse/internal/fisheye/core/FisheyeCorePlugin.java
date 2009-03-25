/**
 * 
 */
package com.atlassian.connector.eclipse.internal.fisheye.core;

import org.eclipse.core.runtime.Plugin;
import org.osgi.framework.BundleContext;

import com.atlassian.connector.eclipse.internal.core.AtlassianLogger;
import com.atlassian.theplugin.commons.util.LoggerImpl;

/**
 * The activator class controls the plug-in life cycle.
 * 
 * @author Thomas Ehrnhoefer
 *
 */
public class FisheyeCorePlugin extends Plugin {

	//plug-in ID
	public static final String PLUGIN_ID = "The activator class controls the plug-in life cycle";
	
	public static final String CONNECTOR_KIND = "fisheye";
	
	//shared instance
	private static FisheyeCorePlugin plugin;

	public FisheyeCorePlugin() {
		// make sure that we have the logging going to the eclipse log
		LoggerImpl.setInstance(new AtlassianLogger());
	}
	
	public static FisheyeCorePlugin getDefault() {
		return plugin; 
	}
	
	@Override
	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;
	}
	
	@Override
	public void stop(BundleContext context) throws Exception {
		plugin = null;
		super.stop(context);
	}

}
