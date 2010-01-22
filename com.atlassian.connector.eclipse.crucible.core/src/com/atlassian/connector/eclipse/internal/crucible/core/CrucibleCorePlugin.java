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

package com.atlassian.connector.eclipse.internal.crucible.core;

import com.atlassian.connector.eclipse.internal.core.AtlassianLogger;
import com.atlassian.connector.eclipse.internal.crucible.core.client.model.ReviewCache;
import com.atlassian.theplugin.commons.util.LoggerImpl;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Plugin;
import org.eclipse.core.runtime.Status;
import org.eclipse.mylyn.commons.core.StatusHandler;
import org.osgi.framework.BundleContext;

import java.io.File;

/**
 * The activator class controls the plug-in life cycle
 * 
 * @author Shawn Minto
 */
public class CrucibleCorePlugin extends Plugin {

	private static final String REPOSITORY_CONFIGURATIONS_FOLDER_PATH = "repositoryConfigurations";

	// The plug-in ID
	public static final String PLUGIN_ID = "com.atlassian.connector.eclipse.crucible.core";

	public static final String CONNECTOR_KIND = "crucible";

	private static CrucibleRepositoryConnector repositoryConnector;

	// The shared instance
	private static CrucibleCorePlugin plugin;

	private ReviewCache reviewCache;

	/**
	 * The constructor
	 */
	public CrucibleCorePlugin() {

		// make sure that we have the logging going to the eclipse log
		LoggerImpl.setInstance(new AtlassianLogger());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.runtime.Plugin#start(org.osgi.framework.BundleContext)
	 */
	@Override
	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;

		reviewCache = new ReviewCache();
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.core.runtime.Plugin#stop(org.osgi.framework.BundleContext)
	 */
	@Override
	public void stop(BundleContext context) throws Exception {
		reviewCache = null;

		if (repositoryConnector != null) {
			repositoryConnector.flush();
		}

		plugin = null;
		super.stop(context);
	}

	/**
	 * Returns the shared instance
	 * 
	 * @return the shared instance
	 */
	public static CrucibleCorePlugin getDefault() {
		return plugin;
	}

	static void setRepositoryConnector(CrucibleRepositoryConnector repositoryConnector) {
		if (CrucibleCorePlugin.repositoryConnector == null) {
			CrucibleCorePlugin.repositoryConnector = repositoryConnector;
		} else {
			StatusHandler.log(new Status(IStatus.ERROR, CrucibleCorePlugin.PLUGIN_ID,
					"Cannot register a repository connector twice"));
		}
	}

	public static CrucibleRepositoryConnector getRepositoryConnector() {
		return repositoryConnector;
	}

	public File getRepositoryConfigurationCacheFile() {
		IPath stateLocation = Platform.getStateLocation(getBundle());
		IPath cacheFile = stateLocation.append(REPOSITORY_CONFIGURATIONS_FOLDER_PATH);
		return cacheFile.toFile();
	}

	public ReviewCache getReviewCache() {
		return reviewCache;
	}
}
