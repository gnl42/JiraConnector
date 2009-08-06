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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import org.apache.commons.httpclient.HostConfiguration;
import org.apache.commons.httpclient.HttpClient;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.ListenerList;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.preference.IPersistentPreferenceStore;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.mylyn.commons.core.CoreUtil;
import org.eclipse.mylyn.commons.core.StatusHandler;
import org.eclipse.mylyn.commons.net.WebLocation;
import org.eclipse.mylyn.commons.net.WebUtil;
import org.eclipse.mylyn.internal.context.core.ContextCorePlugin;
import org.eclipse.mylyn.internal.context.core.InteractionContextManager;
import org.eclipse.mylyn.internal.monitor.ui.ActionExecutionMonitor;
import org.eclipse.mylyn.internal.monitor.ui.ActivityChangeMonitor;
import org.eclipse.mylyn.internal.monitor.ui.KeybindingCommandMonitor;
import org.eclipse.mylyn.internal.monitor.ui.MenuCommandMonitor;
import org.eclipse.mylyn.internal.monitor.ui.MonitorUiPlugin;
import org.eclipse.mylyn.internal.monitor.ui.PerspectiveChangeMonitor;
import org.eclipse.mylyn.internal.monitor.ui.WindowChangeMonitor;
import org.eclipse.mylyn.monitor.core.IInteractionEventListener;
import org.eclipse.mylyn.monitor.ui.AbstractCommandMonitor;
import org.eclipse.mylyn.monitor.ui.IActionExecutionListener;
import org.eclipse.mylyn.monitor.ui.IMonitorLifecycleListener;
import org.eclipse.mylyn.monitor.ui.MonitorUi;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.events.ShellListener;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IStartup;
import org.eclipse.ui.IWindowListener;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.eclipse.update.internal.ui.security.Authentication;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;

import com.atlassian.connector.eclipse.internal.monitor.usage.operations.UsageDataUploadJob;
import com.atlassian.connector.eclipse.monitor.usage.IMonitorActivator;

/**
 * @author Mik Kersten
 * @author Shawn Minto
 */
public class UiUsageMonitorPlugin extends AbstractUIPlugin {

	private static final long HOUR = 3600 * 1000;

	public static final long DEFAULT_DELAY_BETWEEN_TRANSMITS = 7 * 24 * HOUR;

	public static final String MONITOR_LOG_NAME = "monitor-log";

	public static final String ID_PLUGIN = "com.atlassian.connector.eclipse.monitor.usage"; //$NON-NLS-1$

	private static final long SIX_HOURS_IN_MS = 6 * 60 * 60 * 1000;

	private static final long TEN_MINUTES_IN_MS = 10 * 60 * 1000;

	private InteractionEventLogger interactionLogger;

	private PerspectiveChangeMonitor perspectiveMonitor;

	private ActivityChangeMonitor activityMonitor;

	private MenuCommandMonitor menuMonitor;

	private WindowChangeMonitor windowMonitor;

	private KeybindingCommandMonitor keybindingCommandMonitor;

	private static UiUsageMonitorPlugin plugin;

	private final List<IActionExecutionListener> actionExecutionListeners = new ArrayList<IActionExecutionListener>();

	private final List<AbstractCommandMonitor> commandMonitors = new ArrayList<AbstractCommandMonitor>();

	private ResourceBundle resourceBundle;

	private final Authentication uploadAuthentication = null;

	private static boolean performingUpload = false;

	private boolean questionnaireEnabled = true;

	private boolean backgroundEnabled = false;

	private final StudyParameters studyParameters = new StudyParameters();

	private final ListenerList lifecycleListeners = new ListenerList();

	public static class UiUsageMonitorStartup implements IStartup {

		public void earlyStartup() {
			// everything happens on normal start
		}
	}

	private final IWindowListener WINDOW_LISTENER = new IWindowListener() {
		public void windowActivated(IWorkbenchWindow window) {
		}

		public void windowDeactivated(IWorkbenchWindow window) {
		}

		public void windowClosed(IWorkbenchWindow window) {
			if (window.getShell() != null) {
				window.getShell().removeShellListener(SHELL_LISTENER);
			}
		}

		public void windowOpened(IWorkbenchWindow window) {
			if (window.getShell() != null && !PlatformUI.getWorkbench().isClosing()) {
				window.getShell().addShellListener(SHELL_LISTENER);
			}
		}
	};

	private final ShellListener SHELL_LISTENER = new ShellListener() {

		public void shellDeactivated(ShellEvent arg0) {
			if (!isPerformingUpload() && ContextCorePlugin.getDefault() != null) {
				for (IInteractionEventListener listener : MonitorUiPlugin.getDefault().getInteractionListeners()) {
					listener.stopMonitoring();
				}
			}
		}

		public void shellActivated(ShellEvent arg0) {
//			if (!MonitorUiPlugin.getDefault().suppressConfigurationWizards() && ContextCorePlugin.getDefault() != null) {
//				checkForStatisticsUpload();
//			}
			if (!isPerformingUpload() && ContextCorePlugin.getDefault() != null) {
				for (IInteractionEventListener listener : MonitorUiPlugin.getDefault().getInteractionListeners()) {
					listener.startMonitoring();
				}
			}
		}

		public void shellDeiconified(ShellEvent arg0) {
		}

		public void shellIconified(ShellEvent arg0) {
		}

		public void shellClosed(ShellEvent arg0) {
		}
	};

	private LogMoveUtility logMoveUtility;

	private UsageDataUploadJob scheduledStatisticsUploadJob;

	private final List<IMonitorActivator> monitorActivators = new ArrayList<IMonitorActivator>();

	/**
	 * NOTE: this needs to be a separate class in order to avoid loading ..mylyn.context.core on eager startup
	 */
	private class LogMoveUtility {

//		private final IContextStoreListener DATA_DIR_MOVE_LISTENER = new IContextStoreListener() {
//
//			public void contextStoreMoved(File file) {
//				if (!isPerformingUpload()) {
//					for (IInteractionEventListener listener : MonitorUiPlugin.getDefault().getInteractionListeners()) {
//						listener.stopMonitoring();
//					}
//					interactionLogger.moveOutputFile(getMonitorLogFile().getAbsolutePath());
//					for (IInteractionEventListener listener : MonitorUiPlugin.getDefault().getInteractionListeners()) {
//						listener.startMonitoring();
//					}
//				}
//			}
//		};

		void start() {
//			ContextCore.getContextStore().addListener(DATA_DIR_MOVE_LISTENER);
		}

		void stop() {
//			ContextCore.getContextStore().removeListener(DATA_DIR_MOVE_LISTENER);
		}
	}

	public UiUsageMonitorPlugin() {
		plugin = this;

	}

	@Override
	public void start(BundleContext context) throws Exception {
		super.start(context);
		final IWorkbench workbench = PlatformUI.getWorkbench();
		workbench.getDisplay().asyncExec(new Runnable() {
			public void run() {
				try {
					interactionLogger = new InteractionEventLogger(getMonitorLogFile());
					perspectiveMonitor = new PerspectiveChangeMonitor();
					activityMonitor = new ActivityChangeMonitor();
					windowMonitor = new WindowChangeMonitor();
					menuMonitor = new MenuCommandMonitor();
					keybindingCommandMonitor = new KeybindingCommandMonitor();

					// browserMonitor = new BrowserMonitor();
					// setAcceptedUrlMatchList(studyParameters.getAcceptedUrlList());

					// ------- moved from synch start
					MonitorUsageExtensionPointReader extensionReader = new MonitorUsageExtensionPointReader();

					studyParameters.setUsageCollectors(extensionReader.getUsageCollectors());
					// ------- moved from synch start

					Collection<IMonitorActivator> monitors = extensionReader.getMonitors();
					if (monitors != null) {
						monitorActivators.addAll(monitors);
					}

					if (getPreferenceStore().getBoolean(MonitorPreferenceConstants.PREF_MONITORING_ENABLED)) {
						startMonitoring();
					} else {
						if (!suppressConfigurationWizards() && !CoreUtil.TEST_MODE) {
							askUserForPermissionToMonitor();
						}
					}
				} catch (Throwable t) {
					StatusHandler.log(new Status(IStatus.ERROR, UiUsageMonitorPlugin.ID_PLUGIN,
							Messages.UiUsageMonitorPlugin_failed_to_start, t));
				}
			}
		});
	}

	protected boolean suppressConfigurationWizards() {
		if (MonitorUiPlugin.getDefault().suppressConfigurationWizards()) {
			return true;
		}
		final List<String> commandLineArgs = Arrays.asList(Platform.getCommandLineArgs());
		return commandLineArgs.contains("-testPluginName");
	}

	public void startMonitoring() {
		if (getPreferenceStore().contains(MonitorPreferenceConstants.PREF_MONITORING_STARTED)) {
			return;
		}
		interactionLogger.startMonitoring();
		for (IInteractionEventListener listener : MonitorUiPlugin.getDefault().getInteractionListeners()) {
			listener.startMonitoring();
		}

		IWorkbench workbench = PlatformUI.getWorkbench();
		MonitorUi.addInteractionListener(interactionLogger);
		getCommandMonitors().add(keybindingCommandMonitor);

		getActionExecutionListeners().add(new ActionExecutionMonitor());
		workbench.addWindowListener(WINDOW_LISTENER);
		for (IWorkbenchWindow w : MonitorUiPlugin.getDefault().getMonitoredWindows()) {
			if (w.getShell() != null) {
				w.getShell().addShellListener(SHELL_LISTENER);
			}
		}

		if (logMoveUtility == null) {
			logMoveUtility = new LogMoveUtility();
		}
		logMoveUtility.start();

		MonitorUiPlugin.getDefault().addWindowPerspectiveListener(perspectiveMonitor);
		workbench.getActivitySupport().getActivityManager().addActivityManagerListener(activityMonitor);
		workbench.getDisplay().addFilter(SWT.Selection, menuMonitor);
		workbench.addWindowListener(windowMonitor);

		// installBrowserMonitor(workbench);

		for (Object listener : lifecycleListeners.getListeners()) {
			((IMonitorLifecycleListener) listener).startMonitoring();
		}

		for (IMonitorActivator activator : monitorActivators) {
			activator.start();
		}

		// schedule statistics upload
		startUploadStatisticsJob();

		getPreferenceStore().setValue(MonitorPreferenceConstants.PREF_MONITORING_STARTED, true);
	}

	public boolean isObfuscationEnabled() {
		return UiUsageMonitorPlugin.getPrefs().getBoolean(MonitorPreferenceConstants.PREF_MONITORING_OBFUSCATE);
	}

	public void stopMonitoring() {
		if (!getPreferenceStore().contains(MonitorPreferenceConstants.PREF_MONITORING_STARTED)) {
			return;
		}

		// stop statistics upload
		stopUploadStatisticsJob();

		for (IMonitorActivator activator : monitorActivators) {
			activator.stop();
		}

		for (Object listener : lifecycleListeners.getListeners()) {
			((IMonitorLifecycleListener) listener).stopMonitoring();
		}

		for (IInteractionEventListener listener : MonitorUiPlugin.getDefault().getInteractionListeners()) {
			listener.stopMonitoring();
		}

		IWorkbench workbench = PlatformUI.getWorkbench();
		MonitorUi.removeInteractionListener(interactionLogger);

		getCommandMonitors().remove(keybindingCommandMonitor);
		getActionExecutionListeners().remove(new ActionExecutionMonitor());

		workbench.removeWindowListener(WINDOW_LISTENER);
		for (IWorkbenchWindow w : MonitorUiPlugin.getDefault().getMonitoredWindows()) {
			if (w.getShell() != null) {
				w.getShell().removeShellListener(SHELL_LISTENER);
			}
		}
		logMoveUtility.stop();
		// ContextCore.getPluginPreferences().removePropertyChangeListener(DATA_DIR_MOVE_LISTENER);

		MonitorUiPlugin.getDefault().removeWindowPerspectiveListener(perspectiveMonitor);
		workbench.getActivitySupport().getActivityManager().removeActivityManagerListener(activityMonitor);

		if (!workbench.getDisplay().isDisposed()) {
			workbench.getDisplay().removeFilter(SWT.Selection, menuMonitor);
		}

		workbench.removeWindowListener(windowMonitor);

		// uninstallBrowserMonitor(workbench);
		interactionLogger.stopMonitoring();

		getPreferenceStore().setValue(MonitorPreferenceConstants.PREF_MONITORING_STARTED, false);
	}

	public void addMonitoringLifecycleListener(IMonitorLifecycleListener listener) {
		lifecycleListeners.add(listener);
		if (isMonitoringEnabled()) {
			listener.startMonitoring();
		}
	}

	public void removeMonitoringLifecycleListener(IMonitorLifecycleListener listener) {
		lifecycleListeners.remove(listener);
	}

	@Override
	public void stop(BundleContext context) throws Exception {
		if (getPreferenceStore().getBoolean(MonitorPreferenceConstants.PREF_MONITORING_ENABLED)) {
			stopMonitoring();
		}

		super.stop(context);
		plugin = null;
		resourceBundle = null;
	}

	public void actionObserved(IAction action, String info) {
		for (IActionExecutionListener listener : actionExecutionListeners) {
			listener.actionObserved(action);
		}
	}

	public List<IActionExecutionListener> getActionExecutionListeners() {
		return actionExecutionListeners;
	}

	public List<AbstractCommandMonitor> getCommandMonitors() {
		return commandMonitors;
	}

	public File getLogFilesRootDir() {
		return new File(ResourcesPlugin.getWorkspace().getRoot().getLocation().toString()
				+ "/.metadata/.atlassian-connector-for-eclipse"); //$NON-NLS-1$
	}

	/**
	 * Parallels TasksUiPlugin.getDefaultDataDirectory()
	 */
	public File getMonitorLogFile() {
		File rootDir = getLogFilesRootDir();
		File file = new File(rootDir, MONITOR_LOG_NAME + InteractionContextManager.CONTEXT_FILE_EXTENSION_OLD);
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
	 * Returns the string from the plugin's resource bundle, or 'key' if not found.
	 */
	public static String getResourceString(String key) {
		ResourceBundle bundle = UiUsageMonitorPlugin.getDefault().getResourceBundle();
		try {
			return (bundle != null) ? bundle.getString(key) : key;
		} catch (MissingResourceException e) {
			return key;
		}
	}

	/**
	 * Returns the plugin's resource bundle,
	 */
	public ResourceBundle getResourceBundle() {
		try {
			if (resourceBundle == null) {
				resourceBundle = ResourceBundle.getBundle("org.eclipse.mylyn.monitor.ui.MonitorPluginResources");
			}
		} catch (MissingResourceException x) {
			resourceBundle = null;
		}
		return resourceBundle;
	}

	/**
	 * One time action (after this plugin was installed) to ask user if we can monitor him/her. User can decide later on
	 * to disable or enable this.
	 */
	private void askUserForPermissionToMonitor() {
		if (getStudyParameters().getUsageCollectors().size() == 0) {
			return;
		}

		final IPreferenceStore store = plugin.getPreferenceStore();

		// already configured so don't bother asking
		if (!isFirstTime()) {
			return;
		} else {
			store.setValue(MonitorPreferenceConstants.PREF_MONITORING_FIRST_TIME, "false"); // must not use boolean here, it will not be stored
		}

		boolean agreement = MessageDialog.openQuestion(Display.getDefault().getActiveShell(),
				Messages.UiUsageMonitorPlugin_send_usage_feedback, NLS.bind(
						Messages.UiUsageMonitorPlugin_please_consider_uploading, getUsageCollectorFeatures()));

		store.setValue(MonitorPreferenceConstants.PREF_MONITORING_ENABLE_SUBMISSION, Boolean.toString(agreement));
		store.setValue(MonitorPreferenceConstants.PREF_MONITORING_ENABLED, Boolean.toString(agreement));

		// force saving, in case workbench crashes
		if (store instanceof IPersistentPreferenceStore) {
			try {
				((IPersistentPreferenceStore) store).save();
			} catch (IOException e) {
				// ignore it here
			}
		}

		if (agreement) {
			startMonitoring();
		}
	}

	private String getUsageCollectorFeatures() {
		StringBuilder sb = new StringBuilder();
		for (UsageCollector collector : getStudyParameters().getUsageCollectors()) {
			Bundle bnd = Platform.getBundle(collector.getBundle());
			if (bnd != null) {
				sb.append(bnd.getHeaders().get("Bundle-Name"));
				sb.append('\n');
			}
		}
		return sb.toString();
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

	public boolean isQuestionnaireEnabled() {
		return questionnaireEnabled;
	}

	public void setQuestionnaireEnabled(boolean questionnaireEnabled) {
		this.questionnaireEnabled = questionnaireEnabled;
	}

	public StudyParameters getStudyParameters() {
		return studyParameters;
	}

	public boolean isMonitoringEnabled() {
		return getPreferenceStore().getBoolean(MonitorPreferenceConstants.PREF_MONITORING_ENABLED);
	}

	public boolean isBackgroundEnabled() {
		return backgroundEnabled;
	}

	public void setBackgroundEnabled(boolean backgroundEnabled) {
		this.backgroundEnabled = backgroundEnabled;
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

	public boolean isSubmissionEnabled() {
		return getPreferenceStore().getBoolean(MonitorPreferenceConstants.PREF_MONITORING_ENABLE_SUBMISSION);
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
}
