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

import com.atlassian.connector.eclipse.internal.bamboo.core.BambooUtil.BuildChangeAction;
import com.atlassian.connector.eclipse.internal.core.AtlassianLogger;
import com.atlassian.theplugin.commons.bamboo.BambooBuild;
import com.atlassian.theplugin.commons.bamboo.BuildStatus;
import com.atlassian.theplugin.commons.util.LoggerImpl;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Plugin;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.preferences.DefaultScope;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.core.runtime.preferences.IEclipsePreferences.IPreferenceChangeListener;
import org.eclipse.core.runtime.preferences.IEclipsePreferences.PreferenceChangeEvent;
import org.eclipse.mylyn.commons.core.StatusHandler;
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.osgi.framework.BundleContext;
import org.osgi.service.prefs.BackingStoreException;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

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

		IEclipsePreferences preferences = new DefaultScope().getNode(BambooCorePlugin.PLUGIN_ID);
		preferences.putInt(BambooConstants.PREFERENCE_REFRESH_INTERVAL, BambooConstants.DEFAULT_REFRESH_INTERVAL);
		preferences.putBoolean(BambooConstants.PREFERENCE_AUTO_REFRESH, BambooConstants.DEFAULT_AUTO_REFRESH);

		buildPlanManager = new BuildPlanManager();
		preferences.addPreferenceChangeListener(new IPreferenceChangeListener() {
			public void preferenceChange(PreferenceChangeEvent event) {
				buildPlanManager.reInitializeScheduler();
			}
		});

		buildPlanManager.addBuildsChangedListener(new BuildsChangedListener() {

			public void buildsUpdated(BuildsChangedEvent event) {

				BambooUtil.runActionForChangedBuild(event, new BuildChangeAction() {

					public void run(BambooBuild build, TaskRepository repository) {

						if (build.getStatus() == BuildStatus.FAILURE) {

							IEclipsePreferences preferences = new InstanceScope().getNode(BambooCorePlugin.PLUGIN_ID);
							boolean isPlaySound = preferences.getBoolean(BambooConstants.PREFERENCE_PLAY_SOUND,
									BambooConstants.DEFAULT_PLAY_SOUND);

							if (isPlaySound) {
								String sound = preferences.get(BambooConstants.PREFERENCE_BUILD_SOUND, "");

								if (sound != null && sound.length() > 0) {
									// play sound
									InputStream in;
									try {
										in = new FileInputStream(sound);
										sun.audio.AudioStream as = new sun.audio.AudioStream(in);
										sun.audio.AudioPlayer.player.start(as);
									} catch (FileNotFoundException e) {
										StatusHandler.log(new Status(IStatus.ERROR, PLUGIN_ID,
												"Cannot find audio file to play", e));
									} catch (IOException e) {
										StatusHandler.log(new Status(IStatus.ERROR, PLUGIN_ID,
												"Cannot play audio file", e));
									}
								}
							}
						}
					}
				});
			}
		});

		try {
			preferences.flush();
		} catch (BackingStoreException e) {
			// TODO: decide if we want to swallow it here 
		}
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
		int minutes = new InstanceScope().getNode(BambooCorePlugin.PLUGIN_ID).getInt(
				BambooConstants.PREFERENCE_REFRESH_INTERVAL, BambooConstants.DEFAULT_REFRESH_INTERVAL);
		return minutes;
	}

	public static void setRefreshIntervalMinutes(int minutes) {
		IEclipsePreferences preferences = new InstanceScope().getNode(BambooCorePlugin.PLUGIN_ID);
		preferences.putInt(BambooConstants.PREFERENCE_REFRESH_INTERVAL, minutes);
		try {
			preferences.flush();
		} catch (BackingStoreException e) {
			// TODO: 
		}
	}

	public static void toggleAutoRefresh() {
		IEclipsePreferences preferences = new InstanceScope().getNode(BambooCorePlugin.PLUGIN_ID);
		preferences.putBoolean(BambooConstants.PREFERENCE_AUTO_REFRESH, !isAutoRefresh());
		try {
			preferences.flush();
		} catch (BackingStoreException e) {
			// TODO: decide if we want to swallow it
		}
	}

	public static boolean isAutoRefresh() {
		IEclipsePreferences preferences = new InstanceScope().getNode(BambooCorePlugin.PLUGIN_ID);
		return preferences.getBoolean(BambooConstants.PREFERENCE_AUTO_REFRESH, BambooConstants.DEFAULT_AUTO_REFRESH);
	}
}
