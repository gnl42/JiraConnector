/*******************************************************************************
 * Copyright (c) 2004, 2008 Tasktop Technologies and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Meghan Allen - initial API and implementation
 *******************************************************************************/

package org.eclipse.mylyn.internal.monitor.usage;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.IJobChangeListener;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.mylyn.commons.core.StatusHandler;
import org.eclipse.mylyn.internal.monitor.core.collection.IUsageCollector;
import org.eclipse.mylyn.internal.monitor.usage.editors.UsageStatsEditorInput;
import org.eclipse.mylyn.internal.monitor.usage.editors.UsageSummaryReportEditorPart;
import org.eclipse.mylyn.monitor.core.InteractionEvent;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;

/**
 * Job that performs the rollover of the monitor interaction history log file (modelled after
 * org.eclipse.mylyn.internal.tasks.ui.util.TaskDataExportJob). Overwrites destination if exists!
 * 
 * @author Meghan Allen
 * 
 */
public class MonitorFileRolloverJob extends Job implements IJobChangeListener {

	private static final String JOB_LABEL = "Mylyn Monitor Log Rollover";

	// XXX: needs to be the same as NAME_DATA_DIR in org.eclipse.mylyn.tasks.ui.TasksUIPlugin
	private static final String NAME_DATA_DIR = ".mylyn";

	private static final String DIRECTORY_MONITOR_BACKUP = "monitor";

	private static final String ZIP_EXTENSION = ".zip";

	private List<IUsageCollector> collectors = null;

	private ReportGenerator generator = null;

	private IEditorInput input = null;

	private boolean forceSyncForTesting = false;

	public static final String BACKUP_FILE_SUFFIX = "monitor-log";

	public MonitorFileRolloverJob(List<IUsageCollector> collectors) {
		super(JOB_LABEL);
		this.collectors = collectors;
	}

	@SuppressWarnings("deprecation")
	private String getYear(InteractionEvent event) {
		return "" + (event.getDate().getYear() + 1900);
	}

	public void forceSyncForTesting(boolean forceSync) {
		this.forceSyncForTesting = forceSync;
	}

	private String getMonth(int month) {
		switch (month) {
		case 0:
			return "01";
		case 1:
			return "02";
		case 2:
			return "03";
		case 3:
			return "04";
		case 4:
			return "05";
		case 5:
			return "06";
		case 6:
			return "07";
		case 7:
			return "08";
		case 8:
			return "09";
		case 9:
			return "10";
		case 10:
			return "11";
		case 11:
			return "12";
		default:
			return "";

		}
	}

	public static String getZippedMonitorFileDirPath() {
		return ResourcesPlugin.getWorkspace().getRoot().getLocation().toString() + File.separatorChar + NAME_DATA_DIR
				+ File.separatorChar + DIRECTORY_MONITOR_BACKUP;
	}

	@Override
	@SuppressWarnings("deprecation")
	public IStatus run(final IProgressMonitor progressMonitor) {

		progressMonitor.beginTask(JOB_LABEL, IProgressMonitor.UNKNOWN);

		final File monitorFile = UiUsageMonitorPlugin.getDefault().getMonitorLogFile();
		InteractionEventLogger logger = UiUsageMonitorPlugin.getDefault().getInteractionLogger();

		logger.stopMonitoring();

		List<InteractionEvent> events = logger.getHistoryFromFile(monitorFile);
		progressMonitor.worked(1);

		int nowMonth = Calendar.getInstance().get(Calendar.MONTH);
		if (events.size() > 0 && events.get(0).getDate().getMonth() != nowMonth) {
			int currMonth = events.get(0).getDate().getMonth();

			String fileName = getYear(events.get(0)) + "-" + getMonth(currMonth) + "-" + BACKUP_FILE_SUFFIX;

			File dir = new File(getZippedMonitorFileDirPath());

			if (!dir.exists()) {
				dir.mkdirs();
			}
			try {
				File currBackupZipFile = new File(dir, fileName + ZIP_EXTENSION);
				if (!currBackupZipFile.exists()) {
					currBackupZipFile.createNewFile();
				}
				ZipOutputStream zipFileStream;

				zipFileStream = new ZipOutputStream(new FileOutputStream(currBackupZipFile));
				zipFileStream.putNextEntry(new ZipEntry(UiUsageMonitorPlugin.getDefault().getMonitorLogFile().getName()));

				for (InteractionEvent event : events) {
					int monthOfCurrEvent = event.getDate().getMonth();
					if (monthOfCurrEvent == currMonth) {
						// put in curr zip
						String xml = logger.writeLegacyEvent(event);

						zipFileStream.write(xml.getBytes());

					} else if (monthOfCurrEvent != nowMonth) {
						// we are finished backing up currMonth, but now need to
						// start backing up monthOfCurrEvent
						progressMonitor.worked(1);
						zipFileStream.closeEntry();
						zipFileStream.close();

						fileName = getYear(event) + "-" + getMonth(monthOfCurrEvent) + "-" + BACKUP_FILE_SUFFIX;
						currBackupZipFile = new File(dir, fileName + ZIP_EXTENSION);
						if (!currBackupZipFile.exists()) {

							currBackupZipFile.createNewFile();

						}
						zipFileStream = new ZipOutputStream(new FileOutputStream(currBackupZipFile));
						zipFileStream.putNextEntry(new ZipEntry(UiUsageMonitorPlugin.getDefault()
								.getMonitorLogFile()
								.getName()));
						currMonth = monthOfCurrEvent;
						String xml = logger.writeLegacyEvent(event);
						zipFileStream.write(xml.getBytes());
					} else if (monthOfCurrEvent == nowMonth) {
						// if these events are from the current event, just put
						// them back in the current log (first clear the log,
						// since we are putting them all back)

						logger.clearInteractionHistory(false);
						logger.interactionObserved(event);
					}
				}
				zipFileStream.closeEntry();
				zipFileStream.close();
			} catch (IOException e) {
				StatusHandler.fail(new Status(IStatus.ERROR, UiUsageMonitorPlugin.ID_PLUGIN,
						"Mylyn monitor log rollover failed", e));
			}

		}
		progressMonitor.worked(1);
		logger.startMonitoring();

		generator = new ReportGenerator(UiUsageMonitorPlugin.getDefault().getInteractionLogger(), collectors, this,
				forceSyncForTesting);

		progressMonitor.worked(1);
		final List<File> files = new ArrayList<File>();

		files.add(monitorFile);
		input = new UsageStatsEditorInput(files, generator);

		progressMonitor.done();

		if (forceSyncForTesting) {
			try {
				final IEditorInput input = this.input;

				IWorkbenchPage page = UiUsageMonitorPlugin.getDefault()
						.getWorkbench()
						.getActiveWorkbenchWindow()
						.getActivePage();
				if (page == null) {
					return new Status(IStatus.ERROR, UiUsageMonitorPlugin.ID_PLUGIN, IStatus.OK, "Mylyn Usage Summary",
							null);
				}
				if (input != null) {
					page.openEditor(input, UsageSummaryReportEditorPart.ID);
				}

			} catch (PartInitException e) {
				StatusHandler.fail(new Status(IStatus.ERROR, UiUsageMonitorPlugin.ID_PLUGIN,
						"Could not show usage summary", e));
			}

		}

		return Status.OK_STATUS;
	}

	public void aboutToRun(IJobChangeEvent event) {
		// ignore

	}

	public void awake(IJobChangeEvent event) {
		// ignore

	}

	public void done(IJobChangeEvent event) {
		PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {
			public void run() {
				try {
					final IWorkbenchPage page = UiUsageMonitorPlugin.getDefault()
							.getWorkbench()
							.getActiveWorkbenchWindow()
							.getActivePage();
					if (page == null) {
						return;
					}

					if (input != null) {
						page.openEditor(input, UsageSummaryReportEditorPart.ID);
					}

				} catch (PartInitException e) {
					StatusHandler.fail(new Status(IStatus.ERROR, UiUsageMonitorPlugin.ID_PLUGIN,
							"Could not show usage summary", e));
				}
			}
		});

	}

	public void running(IJobChangeEvent event) {
		// ignore

	}

	public void scheduled(IJobChangeEvent event) {
		// ignore

	}

	public void sleeping(IJobChangeEvent event) {
		// ignore

	}

}
