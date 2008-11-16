/**
 * Copyright (C) 2008 Atlassian
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *    http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.atlassian.theplugin.eclipse.preferences;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.TimerTask;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.Preferences.IPropertyChangeListener;
import org.eclipse.core.runtime.Preferences.PropertyChangeEvent;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.Device;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

import com.atlassian.theplugin.commons.ConfigurationListener;
import com.atlassian.theplugin.commons.SchedulableChecker;
import com.atlassian.theplugin.commons.bamboo.BambooStatusChecker;
import com.atlassian.theplugin.commons.configuration.ConfigurationFactory;
import com.atlassian.theplugin.eclipse.EclipseActionScheduler;
import com.atlassian.theplugin.eclipse.EclipseLogger;
import com.atlassian.theplugin.eclipse.MissingPasswordHandler;
import com.atlassian.theplugin.eclipse.core.operation.IConsoleStream;
import com.atlassian.theplugin.eclipse.util.FileUtil;
import com.atlassian.theplugin.eclipse.util.PluginUtil;
import com.atlassian.theplugin.eclipse.view.bamboo.BambooConfigurationStorage;


/**
 * The activator class controls the plug-in life cycle
 */
public class Activator extends AbstractUIPlugin {

	private static final int THREAD_SLEEP = 100;

	// The plug-in ID
	public static final String PLUGIN_ID = "com.atlassian.theplugin.eclipse";

	// The shared instance
	private static Activator plugin;

	private EclipsePluginConfiguration pluginConfiguration;

	private BambooStatusChecker bambooChecker;

	// private Collection<TimerTask> scheduledComponents = new
	// ArrayList<TimerTask>();
	private Collection<Job> scheduledComponents = new ArrayList<Job>();

	private Collection<SchedulableChecker> schedulableCheckers = new ArrayList<SchedulableChecker>();

	private Set<ConfigurationListener> configurationListeners = new HashSet<ConfigurationListener>();

	private ConfigListener configListener;

	/**
	 * The constructor
	 */
	public Activator() {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext
	 * )
	 */
	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;

		// create logger
		PluginUtil.setLogger(new EclipseLogger(getLog())); 
		// now you can use PluginUtil.getLogger
		PluginUtil.getLogger().info(PluginUtil.getPluginName() + " started.");

		// create configuration
		reloadConfiguration();

		// create bamboo checker
		MissingPasswordHandler missingPasswordHandler = new MissingPasswordHandler();
		bambooChecker = BambooStatusChecker.getInstance(EclipseActionScheduler
				.getInstance(), pluginConfiguration, missingPasswordHandler,
				PluginUtil.getLogger());
		schedulableCheckers.add(bambooChecker);
		registerConfigurationListener(bambooChecker);

		// create configuration changes listener
		configListener = new ConfigListener();
		getPluginPreferences().addPropertyChangeListener(configListener);

		// start timer/checkers
		startTimer();

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext
	 * )
	 */
	public void stop(BundleContext context) throws Exception {

		getPluginPreferences().removePropertyChangeListener(configListener);

		getPluginPreferences().setValue(
				PreferenceConstants.BAMBOO_TAB_COLUMNS_ORDER,
				getPluginConfiguration().getBambooTabConfiguration()
						.getColumnsOrderString());
		getPluginPreferences().setValue(
				PreferenceConstants.BAMBOO_TAB_COLUMNS_WIDTH,
				getPluginConfiguration().getBambooTabConfiguration()
						.getColumnsWidthString());

		disableTimer();

		plugin = null;
		super.stop(context);
	}

	public void registerConfigurationListener(
			ConfigurationListener configListener) {
		this.configurationListeners.add(configListener);
	}

	public void unregisterConfigurationListener(
			ConfigurationListener configListener) {
		this.configurationListeners.remove(configListener);
	}

	private void notifyConfigurationListeners() {
		for (ConfigurationListener listener : configurationListeners) {
			listener.updateConfiguration(pluginConfiguration);
		}
	}

	public void reloadConfiguration() {
		ProjectConfigurationWrapper configurationWrapper = new ProjectConfigurationWrapper(
				getPluginPreferences());
		pluginConfiguration = configurationWrapper.getPluginConfiguration();
		ConfigurationFactory.setConfiguration(pluginConfiguration);
		
		try {
			IPath stateLocation = this.getStateLocation();
			
			BambooConfigurationStorage storage = BambooConfigurationStorage.instance();
			storage.initialize(stateLocation);
			
			// check for previous plugin versions repository info storage
			if (storage.getBambooServers().length == 0) {
				String coreFolderName = stateLocation.lastSegment();
				IPath uiLocation = stateLocation.removeLastSegments(1).append(
						coreFolderName.substring(0, coreFolderName.length() - 4) + "ui");
				File previousState = new File(
						uiLocation + File.pathSeparator + BambooConfigurationStorage.STATE_INFO_FILE_NAME);
				
				if (previousState.exists()) {
					FileUtil.copyFile(
							new File(stateLocation.toString() 
									+ File.pathSeparator 
									+ BambooConfigurationStorage.STATE_INFO_FILE_NAME), 
							previousState, new NullProgressMonitor());
					previousState.delete();
					storage.initialize(stateLocation);
				}
			}
		} catch (Exception e) {
			// FIXME: Probably we should do something about this
		}
	}

	/**
	 * Returns the shared instance
	 * 
	 * @return the shared instance
	 */
	public static Activator getDefault() {
		while (Activator.plugin == null) {
    		try {
    			Thread.sleep(THREAD_SLEEP);
    		} catch (InterruptedException ex) {
    			break;
    		}
    	}
		return plugin;
	}

	/**
	 * Returns an image descriptor for the image file at the given plug-in
	 * relative path
	 * 
	 * @param path
	 *            the path
	 * @return the image descriptor
	 */
	public static ImageDescriptor getImageDescriptor(String path) {
		return imageDescriptorFromPlugin(PLUGIN_ID, path);
	}

	// public Timer getTimer() {
	// return timer;
	// }

	public EclipsePluginConfiguration getPluginConfiguration() {
		return pluginConfiguration;
	}

	public BambooStatusChecker getBambooChecker() {
		return bambooChecker;
	}


    public String getResource(String key) {
        return FileUtil.getResource(Platform.getResourceBundle(this.getBundle()), key);
    }
    
	private void disableTimer() {
		Job.getJobManager().cancel(BackgroundTaskType.CHECKERS);

		// Iterator<Job> i = scheduledComponents.iterator();
		// while (i.hasNext()) {
		// Job job = i.next();
		// i.remove();
		// job.cancel();
		// }
	}

	private void startTimer() {
		for (final SchedulableChecker checker : schedulableCheckers) {
			if (checker.canSchedule()) {
				final TimerTask newTask = checker.newTimerTask();
				final Job newJob = new Job(checker.getName()) {

					@Override
					protected IStatus run(IProgressMonitor monitor) {
						newTask.run();
						schedule(checker.getInterval());
						return Status.OK_STATUS;
					}

					@Override
					public boolean belongsTo(Object family) {
						if (!(family instanceof BackgroundTaskType)) {
							return false;
						}

						switch ((BackgroundTaskType) family) {
						case ALL:
							return true;
						case CHECKERS:
							return true;
						default:
							return false;
						}
					}
				};
				newJob.schedule(0); // start checker immediately

				scheduledComponents.add(newJob);
				// scheduledComponents.add(newTask);
				// timer.schedule(newTask, 0,
				// pluginConfiguration.getBambooConfigurationData
				// ().getPollTime() * MILLISECONDS_IN_MINUTE);
				// timer.schedule(newTask, 0, checker.getInterval());
			} else {
				checker.resetListenersState();
			}
		}
	}

	public void rescheduleStatusCheckers() {
		disableTimer();
		startTimer();
	}

	public Device getDisplay() {
		return this.getWorkbench().getDisplay();
	}

	public Shell getShell() {
		return this.getWorkbench().getDisplay().getActiveShell();
	}

	public class ConfigListener implements IPropertyChangeListener {
		public void propertyChange(PropertyChangeEvent event) {
			reloadConfiguration();
			notifyConfigurationListeners();
			// bambooChecker.setConfiguration(pluginConfiguration);
			rescheduleStatusCheckers();
		}
	}

	public IConsoleStream getConsoleStream() {
		// TODO Auto-generated method stub
		return null;
	}
	
}
