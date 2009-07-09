package com.atlassian.eclipse.connector.internal.vqembweb.core;

import org.eclipse.core.runtime.Plugin;
import org.osgi.framework.BundleContext;

import com.atlassian.connector.eclipse.internal.core.AtlassianLogger;
import com.atlassian.theplugin.commons.util.LoggerImpl;

public class AtlassianVqembwebCorePlugin extends Plugin {

	// The plug-in ID
	public static final String PLUGIN_ID = "com.atlassian.connector.eclipse.vqembweb.core";
	
	private static AtlassianVqembwebCorePlugin plugin;
	
	public AtlassianVqembwebCorePlugin() {
		LoggerImpl.setInstance(new AtlassianLogger(PLUGIN_ID));
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
	
	public static AtlassianVqembwebCorePlugin getDefault() {
		return plugin;
	}
}
