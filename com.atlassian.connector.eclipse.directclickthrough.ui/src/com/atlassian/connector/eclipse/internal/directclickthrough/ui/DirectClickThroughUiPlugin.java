package com.atlassian.connector.eclipse.internal.directclickthrough.ui;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.Map;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.equinox.http.jetty.JettyConfigurator;
import org.eclipse.mylyn.commons.core.StatusHandler;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

/**
 * The activator class controls the plug-in life cycle
 */
public class DirectClickThroughUiPlugin extends AbstractUIPlugin {

	// The plug-in ID
	public static final String PLUGIN_ID = "com.atlassian.connector.eclipse.directclickthrough.ui";

	// The shared instance
	private static DirectClickThroughUiPlugin plugin;
	
	/**
	 * The constructor
	 */
	public DirectClickThroughUiPlugin() {
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext)
	 */
	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;
		
		if (getPreferenceStore().getBoolean(IDirectClickThroughPreferenceConstants.ENABLED)) {
			Job serverJob = new Job("Start Embedded Web Server") {
				@Override
				protected IStatus run(IProgressMonitor monitor) {
					try {
						Hashtable<String, Object> params = new Hashtable<String, Object>();
						params.put("http.port", getPreferenceStore().getInt(IDirectClickThroughPreferenceConstants.PORT_NUMBER));
						params.put("http.host", InetAddress.getByName(null).toString());
						JettyConfigurator.startServer(PLUGIN_ID, params); 
					} catch (Exception e) {
						StatusHandler.log(new Status(IStatus.ERROR, DirectClickThroughUiPlugin.PLUGIN_ID, 
								"Unable to run embedded web server, Direct Click Through will not be available", e));
					}
					return null;
				}
			};
			serverJob.schedule();
		}
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext context) throws Exception {
		if (getPreferenceStore().getBoolean(IDirectClickThroughPreferenceConstants.ENABLED)) {
			JettyConfigurator.stopServer(PLUGIN_ID);
		}
		plugin = null;
		super.stop(context);
	}

	/**
	 * Returns the shared instance
	 *
	 * @return the shared instance
	 */
	public static DirectClickThroughUiPlugin getDefault() {
		return plugin;
	}

}
