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

package com.atlassian.connector.eclipse.internal.monitor.usage;

import java.io.File;
import java.io.IOException;
import java.util.Date;

import org.apache.commons.httpclient.HostConfiguration;
import org.apache.commons.httpclient.HttpClient;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.preference.IPersistentPreferenceStore;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.mylyn.commons.core.StatusHandler;
import org.eclipse.mylyn.commons.net.WebLocation;
import org.eclipse.mylyn.commons.net.WebUtil;
import org.eclipse.mylyn.internal.provisional.commons.ui.WorkbenchUtil;
import org.eclipse.ui.IStartup;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.eclipse.ui.progress.UIJob;
import org.eclipse.update.internal.ui.security.Authentication;
import org.osgi.framework.BundleContext;

import com.atlassian.connector.eclipse.internal.core.CoreConstants;
import com.atlassian.connector.eclipse.internal.core.RuntimeUtil;
import com.atlassian.connector.eclipse.internal.monitor.usage.dialogs.PermissionToMonitorDialog;
import com.atlassian.connector.eclipse.internal.monitor.usage.operations.UploadMonitoringStatusJob;
import com.atlassian.connector.eclipse.internal.monitor.usage.operations.UsageDataUploadJob;

/**
 * @author Mik Kersten
 * @author Shawn Minto
 */
public class UiUsageMonitorPlugin extends AbstractUIPlugin {

	private static final long HOUR = 3600 * 1000;

	public static final long DEFAULT_DELAY_BETWEEN_TRANSMITS = 7 * 24 * HOUR;

	public static final String MONITOR_LOG_NAME_OLD = "monitor-log.xml";

	public static final String MONITOR_LOG_NAME = "usage-data.xml";

	public static final String ID_PLUGIN = "com.atlassian.connector.eclipse.monitor.usage"; //$NON-NLS-1$

	private static final long SIX_HOURS_IN_MS = 6 * 60 * 60 * 1000;

	private static final long TEN_MINUTES_IN_MS = 10 * 60 * 1000;

	private static final long FIVE_MINUTES_IN_MS = 5 * 60 * 1000;

	private InteractionEventLogger interactionLogger;

	private static UiUsageMonitorPlugin plugin;

	private final Authentication uploadAuthentication = null;

	private static boolean performingUpload = false;

	private final StudyParameters studyParameters = new StudyParameters(CoreConstants.PRODUCT_NAME,
			"http://update.atlassian.com/atlassian-eclipse-plugin/usage-collector/upload",
			"http://confluence.atlassian.com/display/IDEPLUGIN/Collecting+Usage+Statistics+for+the+Eclipse+Connector");

	private ImageRegistry customLogosRegistry;

	public static class UiUsageMonitorStartup implements IStartup {
		public void earlyStartup() {
			// everything happens on normal start
		}
	}

	private UsageDataUploadJob scheduledStatisticsUploadJob;

	public UiUsageMonitorPlugin() {
		plugin = this;
	}

	@Override
	public void start(BundleContext context) throws Exception {
		super.start(context);

		File oldMonitorFile = new File(getLogFilesRootDir(), MONITOR_LOG_NAME_OLD);
		if (oldMonitorFile.exists() && oldMonitorFile.canWrite()) {
			oldMonitorFile.delete();
		}

		PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {
			public void run() {
				try {
					interactionLogger = new InteractionEventLogger(getMonitorLogFile());

					if (isMonitoringEnabled()) {
						startMonitoring();
					}

					if (isFirstTime() && !RuntimeUtil.suppressConfigurationWizards()) {
						askUserToEnableMonitoring();
					}
				} catch (Throwable t) {
					StatusHandler.log(new Status(IStatus.ERROR, UiUsageMonitorPlugin.ID_PLUGIN,
							Messages.UiUsageMonitorPlugin_failed_to_start, t));
				}
			}
		});
	}

	public void startMonitoring() {
		if (getPreferenceStore().contains(MonitorPreferenceConstants.PREF_MONITORING_STARTED)) {
			return;
		}
		interactionLogger.startMonitoring();

		// schedule statistics upload
		startUploadStatisticsJob();

		getPreferenceStore().setValue(MonitorPreferenceConstants.PREF_MONITORING_STARTED, true);
	}

	public void stopMonitoring() {
		if (!getPreferenceStore().contains(MonitorPreferenceConstants.PREF_MONITORING_STARTED)) {
			return;
		}

		// stop statistics upload
		stopUploadStatisticsJob();

		// uninstallBrowserMonitor(workbench);
		interactionLogger.stopMonitoring();

		getPreferenceStore().setValue(MonitorPreferenceConstants.PREF_MONITORING_STARTED, false);
	}

	@Override
	public void stop(BundleContext context) throws Exception {
		if (isMonitoringEnabled()) {
			stopMonitoring();
		}

		if (customLogosRegistry != null) {
			customLogosRegistry.dispose();
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
				StatusHandler.log(new Status(IStatus.ERROR, UiUsageMonitorPlugin.ID_PLUGIN,
						Messages.UiUsageMonitorPlugin_cant_create_log_file, e));
			}
		}
		return file;
	}

	/**
	 * Returns the shared instance.
	 */
	public static UiUsageMonitorPlugin getDefault() {
		return plugin;
	}

	/**
	 * One time action (after this plugin was installed) - after the plugin was installed inform user that monitoring is
	 * enabled.
	 */
	private void askUserToEnableMonitoring() {
		final IPreferenceStore store = getPreferenceStore();

		UIJob informUserJob = new UIJob("Ask User about Usage Data Monitoring") {
			@Override
			public IStatus runInUIThread(IProgressMonitor monitor) {
				// must not use boolean here, it will not be stored
				store.setValue(MonitorPreferenceConstants.PREF_MONITORING_FIRST_TIME, "false");
				if (!isMonitoringEnabled()) {
					if (new PermissionToMonitorDialog(WorkbenchUtil.getShell()).open() == IDialogConstants.YES_ID) {
						getPrefs().setValue(MonitorPreferenceConstants.PREF_MONITORING_ENABLED, true);
						monitoringEnabled();
						startMonitoring();
					}
				}
				return Status.OK_STATUS;
			}
		};

		informUserJob.setPriority(Job.INTERACTIVE);
		informUserJob.schedule(FIVE_MINUTES_IN_MS);
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
		int numEvents = getPreferenceStore().getInt(MonitorPreferenceConstants.PREF_NUM_USER_EVENTS);
		numEvents += increment;
		getPreferenceStore().setValue(MonitorPreferenceConstants.PREF_NUM_USER_EVENTS, numEvents);
	}

	public void configureProxy(HttpClient httpClient, String uploadScript) {
		WebUtil.configureHttpClient(httpClient, null);
		HostConfiguration hostConfiguration = WebUtil.createHostConfiguration(httpClient, new WebLocation(uploadScript,
				uploadAuthentication.getUser(), uploadAuthentication.getPassword()), null);
		httpClient.setHostConfiguration(hostConfiguration);
	}

	public static IPreferenceStore getPrefs() {
		return getDefault().getPreferenceStore();
	}

	public static boolean isPerformingUpload() {
		return performingUpload;
	}

	public static void setPerformingUpload(boolean performingUpload) {
		UiUsageMonitorPlugin.performingUpload = performingUpload;
	}

	public InteractionEventLogger getInteractionLogger() {
		return interactionLogger;
	}

	public StudyParameters getStudyParameters() {
		return studyParameters;
	}

	public boolean isMonitoringEnabled() {
		return getPreferenceStore().getBoolean(MonitorPreferenceConstants.PREF_MONITORING_ENABLED);
	}

	public String getUserId() {
		return getPreferenceStore().getString(MonitorPreferenceConstants.PREF_MONITORING_USER_ID);
	}

	public long getTransmitPromptPeriod() {
		return getPreferenceStore().getLong(MonitorPreferenceConstants.PREF_MONITORING_SUBMIT_FREQUENCY);
	}

	public boolean isFirstTime() {
		return !getPreferenceStore().contains(MonitorPreferenceConstants.PREF_MONITORING_FIRST_TIME)
				|| getPreferenceStore().getBoolean(MonitorPreferenceConstants.PREF_MONITORING_FIRST_TIME);
	}

	/**
	 * @return null if it's not set, or Date
	 */
	public Date getPreviousTransmitDate() {
		if (plugin.getPreferenceStore().contains(MonitorPreferenceConstants.PREF_PREVIOUS_TRANSMIT_DATE)) {
			return new Date(plugin.getPreferenceStore().getLong(MonitorPreferenceConstants.PREF_PREVIOUS_TRANSMIT_DATE));
		}
		return null;
	}

	public void setPreviousTransmitDate(final Date lastTransmit) {
		IPreferenceStore store = getPreferenceStore();
		store.setValue(MonitorPreferenceConstants.PREF_PREVIOUS_TRANSMIT_DATE, lastTransmit.getTime());
		if (store instanceof IPersistentPreferenceStore) {
			try {
				((IPersistentPreferenceStore) store).save();
			} catch (IOException e) {
				// ignore
			}
		}
	}

	public void setObservedEvents(int number) {
		getPreferenceStore().setValue(MonitorPreferenceConstants.PREF_NUM_USER_EVENTS, number);
	}

	public void monitoringDisabled() {
		Job job = new UploadMonitoringStatusJob(false);
		job.schedule();
	}

	public void monitoringEnabled() {
		Job job = new UploadMonitoringStatusJob(true);
		job.schedule();
	}
}
