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

package com.atlassian.connector.eclipse.internal.fisheye.core;

import com.atlassian.connector.eclipse.core.monitoring.Monitoring;
import com.atlassian.connector.eclipse.internal.core.AtlassianLogger;
import com.atlassian.theplugin.commons.util.LoggerImpl;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Plugin;
import org.osgi.framework.BundleContext;

import java.io.File;

/**
 * The activator class controls the plug-in life cycle
 */
public class FishEyeCorePlugin extends Plugin {

	private static final String REPOSITORY_CONFIGURATIONS_FOLDER_PATH = "repositoryConfigurations";

	// The plug-in ID
	public static final String PLUGIN_ID = "com.atlassian.connector.eclipse.fisheye.core";

	public static final String CONNECTOR_KIND = "fisheye";

	private FishEyeRepositoryConnector repositoryConnector;

	// The shared instance
	private static FishEyeCorePlugin plugin;

	private static Monitoring monitoring = null;

	/**
	 * The constructor
	 */
	public FishEyeCorePlugin() {
		// make sure that we have the logging going to the eclipse log
		LoggerImpl.setInstance(new AtlassianLogger());
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.core.runtime.Plugins#start(org.osgi.framework.BundleContext)
	 */
	@Override
	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.core.runtime.Plugin#stop(org.osgi.framework.BundleContext)
	 */
	@Override
	public void stop(BundleContext context) throws Exception {
		plugin = null;
		if (repositoryConnector != null) {
			repositoryConnector.flush();
		}

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

	public File getRepositoryConfigurationCacheFile() {
		IPath stateLocation = Platform.getStateLocation(getBundle());
		IPath cacheFile = stateLocation.append(REPOSITORY_CONFIGURATIONS_FOLDER_PATH);
		return cacheFile.toFile();
	}

	public void setRepositoryConnector(FishEyeRepositoryConnector repositoryConnector) {
		this.repositoryConnector = repositoryConnector;
	}

	public FishEyeRepositoryConnector getRepositoryConnector() {
		return repositoryConnector;
	}

	public static Monitoring getMonitoring() {
		if (monitoring == null) {
			monitoring = new Monitoring(PLUGIN_ID);
		}
		return monitoring;
	}
}
