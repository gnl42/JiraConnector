/*******************************************************************************
 * Copyright (c) 2004, 2008 Tasktop Technologies and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Tasktop Technologies - initial API and implementation
 *******************************************************************************/

package com.atlassian.connector.eclipse.monitor.core;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.UUID;

import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Plugin;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;
import org.eclipse.core.runtime.preferences.DefaultScope;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.IEclipsePreferences.IPreferenceChangeListener;
import org.eclipse.core.runtime.preferences.IEclipsePreferences.PreferenceChangeEvent;
import org.eclipse.mylyn.commons.core.StatusHandler;
import org.osgi.framework.BundleContext;
import org.osgi.service.prefs.BackingStoreException;

import com.atlassian.connector.eclipse.internal.monitor.core.Messages;
import com.atlassian.connector.eclipse.internal.monitor.core.MonitorPreferenceConstants;
import com.atlassian.connector.eclipse.internal.monitor.core.operations.UploadMonitoringStatusJob;
import com.atlassian.connector.eclipse.internal.monitor.core.operations.UsageDataUploadJob;

/**
 * @author Mik Kersten
 * @author Shawn Minto
 */
public class MonitorCorePlugin extends Plugin {

	private static final long HOUR = 3600 * 1000;

	public static final long DEFAULT_DELAY_BETWEEN_TRANSMITS = 7 * 24 * HOUR;

	public static final String MONITOR_LOG_NAME_OLD = "monitor-log.xml";

	public static final String MONITOR_LOG_NAME = "usage-data.xml";

	public static final String ID_PLUGIN = "com.atlassian.connector.eclipse.monitor.core"; //$NON-NLS-1$

	private static final long SIX_HOURS_IN_MS = 6 * 60 * 60 * 1000;

	private static final long TEN_MINUTES_IN_MS = 10 * 60 * 1000;

	private static final long FIVE_MINUTES_IN_MS = 5 * 60 * 1000;

	private InteractionEventLogger interactionLogger;

	private static MonitorCorePlugin plugin;

	private static boolean performingUpload = false;

	public static final String UPLOAD_URL = "http://update.atlassian.com/atlassian-eclipse-plugin/usage-collector/upload";

	public static final String HELP_URL = "http://confluence.atlassian.com/display/IDEPLUGIN/Collecting+Usage+Statistics+for+the+Eclipse+Connector";

	private UsageDataUploadJob scheduledStatisticsUploadJob;

	public MonitorCorePlugin() {
		plugin = this;
	}

	@Override
	public void start(BundleContext context) throws Exception {
		super.start(context);

		File oldMonitorFile = new File(getLogFilesRootDir(), MONITOR_LOG_NAME_OLD);
		if (oldMonitorFile.exists() && oldMonitorFile.canWrite()) {
			oldMonitorFile.delete();
		}

		try {
			interactionLogger = new InteractionEventLogger(getMonitorLogFile());

			if (isMonitoringEnabled()) {
				startMonitoring();
			}
		} catch (Throwable t) {
			StatusHandler.log(new Status(IStatus.ERROR, MonitorCorePlugin.ID_PLUGIN,
					Messages.UiUsageMonitorPlugin_failed_to_start, t));
		}

		getPreferenceStore().addPreferenceChangeListener(new IPreferenceChangeListener() {
			public void preferenceChange(PreferenceChangeEvent event) {
				if (event.getKey().equals(MonitorPreferenceConstants.PREF_MONITORING_ENABLED)) {
					if (isMonitoringEnabled()) {
						startMonitoring();
					} else {
						stopMonitoring();
					}
				}
			}
		});
	}

	public void startMonitoring() {
		interactionLogger.startMonitoring();

		// schedule statistics upload
		startUploadStatisticsJob();
	}

	public void stopMonitoring() {
		// stop statistics upload
		stopUploadStatisticsJob();

		// uninstallBrowserMonitor(workbench);
		interactionLogger.stopMonitoring();
	}

	@Override
	public void stop(BundleContext context) throws Exception {
		if (isMonitoringEnabled()) {
			stopMonitoring();
		}

		super.stop(context);
		plugin = null;
	}

	public File getLogFilesRootDir() {
		File rootDir = new File(ResourcesPlugin.getWorkspace().getRoot().getLocation().toString()
				+ "/.metadata/.atlassian-connector-for-eclipse"); //$NON-NLS-1$

		if (!rootDir.exists()) {
			rootDir.mkdirs();
		}

		return rootDir;
	}

	/**
	 * Parallels TasksUiPlugin.getDefaultDataDirectory()
	 */
	public File getMonitorLogFile() {
		File rootDir = getLogFilesRootDir();
		File file = new File(rootDir, MONITOR_LOG_NAME);
		if (!file.exists() || !file.canWrite()) {
			try {
				file.createNewFile();
			} catch (IOException e) {
				StatusHandler.log(new Status(IStatus.ERROR, MonitorCorePlugin.ID_PLUGIN,
						Messages.UiUsageMonitorPlugin_cant_create_log_file, e));
			}
		}
		return file;
	}

	/**
	 * Returns the shared instance.
	 */
	public static MonitorCorePlugin getDefault() {
		return plugin;
	}

	public Job startUploadStatisticsJob() {
		scheduledStatisticsUploadJob = new UsageDataUploadJob(false);
		scheduledStatisticsUploadJob.addJobChangeListener(new JobChangeAdapter() {
			@Override
			public void done(IJobChangeEvent event) {
				scheduledStatisticsUploadJob.schedule(SIX_HOURS_IN_MS);
			}
		});
		scheduledStatisticsUploadJob.schedule(TEN_MINUTES_IN_MS); // schedule it in 10 minutes (all startup jobs were executed)
		return scheduledStatisticsUploadJob;
	}

	public Job stopUploadStatisticsJob() {
		scheduledStatisticsUploadJob.cancel();
		return scheduledStatisticsUploadJob;
	}

	public void incrementObservedEvents(int increment) {
		setObservedEvents(getPreferenceStore().getInt(MonitorPreferenceConstants.PREF_NUM_USER_EVENTS, 0) + increment);
	}

	public static boolean isPerformingUpload() {
		return performingUpload;
	}

	public static void setPerformingUpload(boolean performingUpload) {
		MonitorCorePlugin.performingUpload = performingUpload;
	}

	public InteractionEventLogger getInteractionLogger() {
		return interactionLogger;
	}

	public boolean isMonitoringEnabled() {
		return getPreferenceStore().getBoolean(MonitorPreferenceConstants.PREF_MONITORING_ENABLED,
				MonitorPreferenceConstants.PREF_MONITORING_INITIALLY_ENABLED);
	}

	public IEclipsePreferences getPreferenceStore() {
		return new DefaultScope().getNode(MonitorCorePlugin.ID_PLUGIN);
	}

	public String getUserId() {
		IEclipsePreferences prefs = getPreferenceStore();
		String uid = prefs.get(MonitorPreferenceConstants.PREF_MONITORING_USER_ID, null);
		if (uid == null) {
			prefs.put(MonitorPreferenceConstants.PREF_MONITORING_USER_ID, UUID.randomUUID().toString());
			uid = prefs.get(MonitorPreferenceConstants.PREF_MONITORING_USER_ID, null);
		}
		return uid;
	}

	public long getTransmitPromptPeriod() {
		return getPreferenceStore().getLong(MonitorPreferenceConstants.PREF_MONITORING_SUBMIT_FREQUENCY,
				DEFAULT_DELAY_BETWEEN_TRANSMITS);
	}

	/**
	 * @return null if it's not set, or Date
	 */
	public Date getPreviousTransmitDate() {
		IEclipsePreferences prefs = getPreferenceStore();
		if (prefs.get(MonitorPreferenceConstants.PREF_PREVIOUS_TRANSMIT_DATE, null) != null) {
			return new Date(prefs.getLong(MonitorPreferenceConstants.PREF_PREVIOUS_TRANSMIT_DATE, new Date().getTime()));
		}
		return null;
	}

	public void setPreviousTransmitDate(final Date lastTransmit) {
		IEclipsePreferences prefs = getPreferenceStore();
		prefs.putLong(MonitorPreferenceConstants.PREF_PREVIOUS_TRANSMIT_DATE, lastTransmit.getTime());
		try {
			prefs.flush();
		} catch (BackingStoreException e) {
			// ignore
		}
	}

	public void setObservedEvents(int number) {
		getPreferenceStore().putInt(MonitorPreferenceConstants.PREF_NUM_USER_EVENTS, number);
	}

	public void monitoringDisabled() {
		Job job = new UploadMonitoringStatusJob(false);
		job.schedule();
	}

	public void monitoringEnabled() {
		Job job = new UploadMonitoringStatusJob(true);
		job.schedule();
	}

	public void setMonitoringEnabled(boolean b) {
		getPreferenceStore().putBoolean(MonitorPreferenceConstants.PREF_MONITORING_ENABLED, b);
	}
}
