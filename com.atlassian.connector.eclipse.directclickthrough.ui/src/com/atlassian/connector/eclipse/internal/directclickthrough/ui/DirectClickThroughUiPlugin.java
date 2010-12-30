package com.atlassian.connector.eclipse.internal.directclickthrough.ui;

import com.atlassian.connector.eclipse.internal.core.jobs.JobWithStatus;
import com.atlassian.connector.eclipse.internal.directclickthrough.servlet.DirectClickThroughServlet;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.mylyn.commons.core.StatusHandler;
import org.eclipse.ui.IStartup;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.mortbay.jetty.Connector;
import org.mortbay.jetty.Server;
import org.mortbay.jetty.bio.SocketConnector;
import org.mortbay.jetty.servlet.Context;
import org.mortbay.jetty.servlet.ServletHolder;
import org.mortbay.log.Log;
import org.osgi.framework.BundleContext;

/**
 * The activator class controls the plug-in life cycle
 */
public class DirectClickThroughUiPlugin extends AbstractUIPlugin {

	private Server embeddedServer;

	// The plug-in ID
	public static final String PLUGIN_ID = "com.atlassian.connector.eclipse.directclickthrough.ui";

	// The shared instance
	private static DirectClickThroughUiPlugin plugin;
	
	public static class EarlyStartup implements IStartup {
		public void earlyStartup() {
			Log.setLog(new JettyLogger());
		}
	}
	
	/**
	 * The constructor
	 */
	public DirectClickThroughUiPlugin() {
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext)
	 */
	@Override
	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;
		
		getPreferenceStore().addPropertyChangeListener(new IPropertyChangeListener() {
			public void propertyChange(PropertyChangeEvent event) {
				stopEmbeddedServer();
				
				if (getPreferenceStore().getBoolean(IDirectClickThroughPreferenceConstants.ENABLED)) {
					startEmbeddedServer();
				}
			}
		});
		
		if (getPreferenceStore().getBoolean(IDirectClickThroughPreferenceConstants.ENABLED)) {
			startEmbeddedServer();
		}
	}
	
	private void startEmbeddedServer() {
		if (embeddedServer != null) {
			stopEmbeddedServer();
		}
		
		final JobWithStatus serverJob = new JobWithStatus("Start Embedded Web Server") {
			@Override
			protected void runImpl(IProgressMonitor monitor) {
				try {
					embeddedServer = new Server();
					Connector connector = new SocketConnector();
					connector.setHost("127.0.0.1");
					connector.setPort(getPortNumber());
					embeddedServer.addConnector(connector);
					
					Context context = new Context(embeddedServer, "/", Context.NO_SESSIONS | Context.NO_SECURITY);
					context.addServlet(new ServletHolder(new DirectClickThroughServlet()), "/*");
					
					embeddedServer.start();
				} catch (Exception e) {
					setStatus(new Status(IStatus.INFO, DirectClickThroughUiPlugin.PLUGIN_ID,
							"Unable to run embedded web server, Direct Click Through will not be available", e));
				}
			}
		};
		serverJob.addJobChangeListener(new JobChangeAdapter() {
			@Override
			public void done(IJobChangeEvent event) {
				if (!serverJob.getStatus().isOK()) {
					StatusHandler.log(serverJob.getStatus());
				}
			}
		});
		serverJob.schedule();
	}
	
	public int getPortNumber() {
		return getPreferenceStore().getInt(IDirectClickThroughPreferenceConstants.PORT_NUMBER);
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext)
	 */
	@Override
	public void stop(BundleContext context) throws Exception {
		if (getPreferenceStore().getBoolean(IDirectClickThroughPreferenceConstants.ENABLED)) {
			stopEmbeddedServer();
		}
		plugin = null;
		super.stop(context);
	}

	private void stopEmbeddedServer() {
		try {
			if (embeddedServer != null) {
				embeddedServer.stop();
				embeddedServer = null;
			}
		} catch(Exception e) {
			StatusHandler.log(new Status(IStatus.WARNING, DirectClickThroughUiPlugin.PLUGIN_ID, 
					"Unabled to stop embedded Direct Click Through server"));
		}
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
