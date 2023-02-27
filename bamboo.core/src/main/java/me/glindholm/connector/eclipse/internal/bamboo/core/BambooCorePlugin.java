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

package me.glindholm.connector.eclipse.internal.bamboo.core;

import java.io.File;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Plugin;
import org.eclipse.core.runtime.preferences.DefaultScope;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.IEclipsePreferences.IPreferenceChangeListener;
import org.eclipse.core.runtime.preferences.IEclipsePreferences.PreferenceChangeEvent;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.mylyn.tasks.core.RepositoryStatus;
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.osgi.framework.BundleContext;
import org.osgi.service.prefs.BackingStoreException;

import me.glindholm.connector.eclipse.internal.bamboo.core.BambooUtil.BuildChangeAction;
import me.glindholm.connector.eclipse.internal.core.CoreMessages;
import me.glindholm.connector.eclipse.internal.core.JiraConnectorLogger;
import me.glindholm.theplugin.commons.bamboo.BambooBuild;
import me.glindholm.theplugin.commons.bamboo.BuildStatus;
import me.glindholm.theplugin.commons.remoteapi.CaptchaRequiredException;
import me.glindholm.theplugin.commons.util.LoggerImpl;

/**
 * The activator class controls the plug-in life cycle.
 *
 * @author Shawn Minto
 */
public class BambooCorePlugin extends Plugin {

    private static final String REPOSITORY_CONFIGURATIONS_FOLDER_PATH = "repositoryConfigurations";

    public static final String ID_PLUGIN = "me.glindholm.connector.eclipse.bamboo.core";

    public static final String CONNECTOR_KIND = "bamboo";

    private static BambooRepositoryConnector repositoryConnector;

    private static BambooCorePlugin plugin;

    private static BuildPlanManager buildPlanManager;

    private static boolean initialized;

    private static BambooClientManager clientManager;

    public BambooCorePlugin() {
        // make sure that we have the logging going to the eclipse log
        LoggerImpl.setInstance(new JiraConnectorLogger());
    }

    @Override
    public void start(final BundleContext context) throws Exception {
        super.start(context);
        plugin = this;

        IEclipsePreferences preferences = new DefaultScope().getNode(BambooCorePlugin.ID_PLUGIN);
        preferences.putInt(BambooConstants.PREFERENCE_REFRESH_INTERVAL, BambooConstants.DEFAULT_REFRESH_INTERVAL);
        preferences.putBoolean(BambooConstants.PREFERENCE_AUTO_REFRESH, BambooConstants.DEFAULT_AUTO_REFRESH);

        buildPlanManager = new BuildPlanManager();

        final File serverCache = getStateLocation().append("serverCache").toFile(); //$NON-NLS-1$
        clientManager = new BambooClientManager(serverCache);
        clientManager.start();
        initialized = true;

        preferences = new InstanceScope().getNode(BambooCorePlugin.ID_PLUGIN);
        preferences.addPreferenceChangeListener(new IPreferenceChangeListener() {
            @Override
            public void preferenceChange(final PreferenceChangeEvent event) {
                if (event.getKey().equals(BambooConstants.PREFERENCE_AUTO_REFRESH) && BambooCorePlugin.isAutoRefresh()) {
                    buildPlanManager.refreshAllBuilds();
                }

                buildPlanManager.reInitializeScheduler();
            }
        });

        buildPlanManager.addBuildsChangedListener(new BuildsChangedListener() {

            @Override
            public void buildsUpdated(final BuildsChangedEvent event) {

                BambooUtil.runActionForChangedBuild(event, new BuildChangeAction() {

                    @Override
                    public void run(final BambooBuild build, final TaskRepository repository) {

                        if (build.getStatus() == BuildStatus.FAILURE) {

                            final IEclipsePreferences preferences = new InstanceScope().getNode(BambooCorePlugin.ID_PLUGIN);
                            final boolean isPlaySound = preferences.getBoolean(BambooConstants.PREFERENCE_PLAY_SOUND, BambooConstants.DEFAULT_PLAY_SOUND);

                            // @todo jjaroczynski - sound stuff should be in UI plugin, not in core!!!
                            if (isPlaySound) {
                                final String sound = preferences.get(BambooConstants.PREFERENCE_BUILD_SOUND, "");

                                if (sound != null && sound.length() > 0) {
                                    // play sound
//									InputStream in;
//									try {
//										in = new FileInputStream(sound);
//										sun.audio.AudioStream as = new sun.audio.AudioStream(in);
//										sun.audio.AudioPlayer.player.start(as);
//									} catch (FileNotFoundException e) {
//										StatusHandler.log(new Status(IStatus.ERROR, PLUGIN_ID,
//												"Cannot find audio file to play", e));
//									} catch (IOException e) {
//										StatusHandler.log(new Status(IStatus.ERROR, PLUGIN_ID,
//												"Cannot play audio file", e));
//									}
                                }
                            }
                        }
                    }
                });
            }
        });

        try {
            preferences.flush();
        } catch (final BackingStoreException e) {
            // TODO: decide if we want to swallow it here
        }
    }

    @Override
    public void stop(final BundleContext context) throws Exception {
        super.stop(context);
//        if (repositoryConnector != null) {
//            repositoryConnector.flush();
//        }
        if (clientManager != null) {
            clientManager.stop();
        }
        plugin = null;
    }

    /**
     * Returns the shared instance
     *
     * @return the shared instance
     */
    public static BambooCorePlugin getDefault() {
        return plugin;
    }

    public static void setRepositoryConnector(final BambooRepositoryConnector repositoryConnector) {
        BambooCorePlugin.repositoryConnector = repositoryConnector;
    }

    public static BambooRepositoryConnector getRepositoryConnector() {
        return repositoryConnector;
    }

    public File getRepositoryConfigurationCacheFile() {
        final IPath stateLocation = Platform.getStateLocation(getBundle());
        final IPath cacheFile = stateLocation.append(REPOSITORY_CONFIGURATIONS_FOLDER_PATH);
        return cacheFile.toFile();
    }

    public static BuildPlanManager getBuildPlanManager() {
        return buildPlanManager;
    }

    public static int getRefreshIntervalMinutes() {
        final int minutes = new InstanceScope().getNode(BambooCorePlugin.ID_PLUGIN).getInt(BambooConstants.PREFERENCE_REFRESH_INTERVAL,
                BambooConstants.DEFAULT_REFRESH_INTERVAL);
        return minutes;
    }

    public static void setRefreshIntervalMinutes(final int minutes) {
        final IEclipsePreferences preferences = new InstanceScope().getNode(BambooCorePlugin.ID_PLUGIN);
        preferences.putInt(BambooConstants.PREFERENCE_REFRESH_INTERVAL, minutes);
        try {
            preferences.flush();
        } catch (final BackingStoreException e) {
            // TODO:
        }
    }

    public static void toggleAutoRefresh() {
        final IEclipsePreferences preferences = new InstanceScope().getNode(BambooCorePlugin.ID_PLUGIN);
        preferences.putBoolean(BambooConstants.PREFERENCE_AUTO_REFRESH, !isAutoRefresh());
        try {
            preferences.flush();
        } catch (final BackingStoreException e) {
            // TODO: decide if we want to swallow it
        }
    }

    public static boolean isAutoRefresh() {
        final IEclipsePreferences preferences = new InstanceScope().getNode(BambooCorePlugin.ID_PLUGIN);
        return preferences.getBoolean(BambooConstants.PREFERENCE_AUTO_REFRESH, BambooConstants.DEFAULT_AUTO_REFRESH);
    }

    public static BambooClientManager getClientManager() {
        if (!initialized) {
            throw new IllegalStateException("Not yet initialized"); //$NON-NLS-1$
        }
        return clientManager;
    }

    public static IStatus toStatus(final TaskRepository repository, final Exception e) {
        final String url = repository.getRepositoryUrl();
        if (e instanceof CaptchaRequiredException) {
            return new RepositoryStatus(repository.getRepositoryUrl(), IStatus.ERROR, ID_PLUGIN, RepositoryStatus.ERROR_REPOSITORY_LOGIN,
                    CoreMessages.Captcha_authentication_required);
        } else {
            return RepositoryStatus.createInternalError(ID_PLUGIN, "Unexpected error", e); //$NON-NLS-1$

        }
    }

}
