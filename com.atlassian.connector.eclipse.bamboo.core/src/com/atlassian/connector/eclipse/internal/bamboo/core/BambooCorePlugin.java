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

package com.atlassian.connector.eclipse.internal.bamboo.core;

import com.atlassian.connector.eclipse.internal.core.AtlassianLogger;
import com.atlassian.theplugin.commons.util.LoggerImpl;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Plugin;
import org.eclipse.core.runtime.Preferences.IPropertyChangeListener;
import org.eclipse.core.runtime.Preferences.PropertyChangeEvent;
import org.osgi.framework.BundleContext;

import java.io.File;

/**
 * The activator class controls the plug-in life cycle.
 * 
 * @author Shawn Minto
 */
public class BambooCorePlugin extends Plugin {

	private static final String REPOSITORY_CONFIGURATIONS_FOLDER_PATH = "repositoryConfigurations";

	public static final String PLUGIN_ID = "com.atlassian.connector.eclipse.bamboo.core";

	public static final String CONNECTOR_KIND = "bamboo";

	private static BambooRepositoryConnector repositoryConnector;

	private static BambooCorePlugin plugin;

	private static BuildPlanManager buildPlanManager;

	public BambooCorePlugin() {
		// make sure that we have the logging going to the eclipse log
		LoggerImpl.setInstance(new AtlassianLogger());
	}

	@Override
	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;
		plugin.getPluginPreferences().setDefault(BambooConstants.PREFERENCE_REFRESH_INTERVAL,
				BambooConstants.DEFAULT_REFRESH_INTERVAL);
		plugin.getPluginPreferences().setDefault(BambooConstants.PREFERENCE_AUTO_REFRESH,
				BambooConstants.DEFAULT_AUTO_REFRESH);
		buildPlanManager = new BuildPlanManager();
		plugin.getPluginPreferences().addPropertyChangeListener(new IPropertyChangeListener() {
			public void propertyChange(PropertyChangeEvent event) {
				buildPlanManager.reInitializeScheduler();
			}
		});

	}

	@Override
	public void stop(BundleContext context) throws Exception {
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
	public static BambooCorePlugin getDefault() {
		return plugin;
	}

	public static void setRepositoryConnector(BambooRepositoryConnector repositoryConnector) {
		BambooCorePlugin.repositoryConnector = repositoryConnector;
	}

	public static BambooRepositoryConnector getRepositoryConnector() {
		return repositoryConnector;
	}

	public File getRepositoryConfigurationCacheFile() {
		IPath stateLocation = Platform.getStateLocation(getBundle());
		IPath cacheFile = stateLocation.append(REPOSITORY_CONFIGURATIONS_FOLDER_PATH);
		return cacheFile.toFile();
	}

	public static BuildPlanManager getBuildPlanManager() {
		return buildPlanManager;
	}

	public static int getRefreshIntervalMinutes() {
		int minutes = plugin.getPluginPreferences().getInt(BambooConstants.PREFERENCE_REFRESH_INTERVAL);
		return minutes;
	}

	public static void setRefreshIntervalMinutes(int minutes) {
		plugin.getPluginPreferences().setValue(BambooConstants.PREFERENCE_REFRESH_INTERVAL, minutes);
		plugin.savePluginPreferences();
	}

	public static void toggleAutoRefresh() {
		plugin.getPluginPreferences().setValue(BambooConstants.PREFERENCE_AUTO_REFRESH, !isAutoRefresh());
		plugin.savePluginPreferences();
	}

	public static boolean isAutoRefresh() {
		return plugin.getPluginPreferences().getBoolean(BambooConstants.PREFERENCE_AUTO_REFRESH);
	}
}
