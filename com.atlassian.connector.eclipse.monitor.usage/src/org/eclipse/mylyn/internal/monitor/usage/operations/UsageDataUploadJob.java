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

package org.eclipse.mylyn.internal.monitor.usage.operations;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.net.NoRouteToHostException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.multipart.FilePart;
import org.apache.commons.httpclient.methods.multipart.MultipartRequestEntity;
import org.apache.commons.httpclient.methods.multipart.Part;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.mylyn.commons.core.StatusHandler;
import org.eclipse.mylyn.internal.commons.core.ZipFileUtil;
import org.eclipse.mylyn.internal.monitor.usage.InteractionEventLogger;
import org.eclipse.mylyn.internal.monitor.usage.Messages;
import org.eclipse.mylyn.internal.monitor.usage.MonitorFileRolloverJob;
import org.eclipse.mylyn.internal.monitor.usage.MonitorPreferenceConstants;
import org.eclipse.mylyn.internal.monitor.usage.UiUsageMonitorPlugin;
import org.eclipse.mylyn.internal.monitor.usage.UsageCollector;
import org.eclipse.mylyn.monitor.core.InteractionEvent;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.PlatformUI;

public final class UsageDataUploadJob extends Job {
	public static final String STATS = "usage"; //$NON-NLS-1$

	public static final String SUBMISSION_LOG_FILE_NAME = "submittedUsageLogs.txt"; //$NON-NLS-1$

	private boolean failed = false;

	private final boolean ifTimeElapsed;

	private boolean failedAgain;

	private static int processedFileCount = 1;

	public UsageDataUploadJob(boolean ignoreLastTransmit) {
		super(Messages.UsageSubmissionWizard_upload_user_stats);
		this.ifTimeElapsed = ignoreLastTransmit;
	}

	@Override
	protected IStatus run(IProgressMonitor monitor) {
		try {
			monitor.beginTask(Messages.UsageSubmissionWizard_uploading_usage_stats, 3);
			if (ifTimeElapsed) {
				performUpload(monitor);
			} else {
				checkLastTransmitTimeAndRun(monitor);
			}
			monitor.done();
			return Status.OK_STATUS;
		} catch (Exception e) {
			Status status = new Status(IStatus.ERROR, UiUsageMonitorPlugin.ID_PLUGIN, IStatus.ERROR,
					Messages.UsageSubmissionWizard_error_uploading, e);
			StatusHandler.log(status);
			return status;
		}
	}

	private void checkLastTransmitTimeAndRun(IProgressMonitor monitor) {
		final UiUsageMonitorPlugin plugin = UiUsageMonitorPlugin.getDefault();

		Date lastTransmit;
		if (plugin.getPreferenceStore().contains(MonitorPreferenceConstants.PREF_PREVIOUS_TRANSMIT_DATE)) {
			lastTransmit = new Date(plugin.getPreferenceStore().getLong(
					MonitorPreferenceConstants.PREF_PREVIOUS_TRANSMIT_DATE));
		} else {
			lastTransmit = new Date();
			plugin.getPreferenceStore().setValue(MonitorPreferenceConstants.PREF_PREVIOUS_TRANSMIT_DATE,
					lastTransmit.getTime());
		}

		final Date currentTime = new Date();

		if (currentTime.getTime() > lastTransmit.getTime() + plugin.getTransmitPromptPeriod()
				&& plugin.isSubmissionEnabled() && !plugin.isFirstTime()) {

			// time must be stored right away into preferences, to prevent
			// other threads
			lastTransmit.setTime(new Date().getTime());
			plugin.getPreferenceStore().setValue(MonitorPreferenceConstants.PREF_PREVIOUS_TRANSMIT_DATE,
					currentTime.getTime());

			performUpload(monitor);
		}
	}

	private void performUpload(IProgressMonitor monitor) {
		failed = false;
		/*
		FIXME: decide if we want to enable it?
		 
		if (UiUsageMonitorPlugin.getDefault().isBackgroundEnabled() && performUpload && backgroundFile != null) {
			upload(backgroundFile, BACKGROUND, monitor);

			if (failed) {
				failed = false;
			}

			if (backgroundFile.exists()) {
				backgroundFile.delete();
			}
		}

		if (UiUsageMonitorPlugin.getDefault().isQuestionnaireEnabled() && performUpload && questionnaireFile != null) {
			upload(questionnaireFile, QUESTIONAIRE, monitor);

			if (failed) {
				failed = false;
			}

			if (questionnaireFile.exists()) {
				questionnaireFile.delete();
			}
		}*/

		for (UsageCollector collector : UiUsageMonitorPlugin.getDefault().getStudyParameters().getUsageCollectors()) {
			File zipFile = zipFilesForUpload(collector);
			if (zipFile == null) {
				return;
			}

			if (!upload(collector, zipFile, STATS, monitor)) {
				failed = true;
			}

			if (zipFile.exists()) {
				zipFile.delete();
			}
		}

		if (failed) {
			/*
			 * If this job was re-scheduled (check UiUsageMonitorPlugin#scheduledStatisticsUploadJob)
			 * we use the same instance again and again. We don't want to annoy people every time upload
			 * fails so let's show an error dialog box for the first time and then just shut up. 
			 */
			if (!failedAgain) {
				failedAgain = true;
				PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {
					public void run() {
						// popup a dialog telling the user that the upload failed
						MessageDialog.openError(Display.getCurrent().getActiveShell(),
								Messages.UsageSubmissionWizard_error_uploading,
								Messages.UsageSubmissionWizard_some_uploads_failed);
					}
				});
			}
		} else {
			failedAgain = false;
		}

		UiUsageMonitorPlugin.getDefault().getInteractionLogger().startMonitoring();
		UiUsageMonitorPlugin.setPerformingUpload(false);
		return;
	}

	private File zipFilesForUpload(UsageCollector collector) {
		UiUsageMonitorPlugin.setPerformingUpload(true);
		UiUsageMonitorPlugin.getDefault().getInteractionLogger().stopMonitoring();
		try {
			List<File> files = new ArrayList<File>();
			File monitorFile = UiUsageMonitorPlugin.getDefault().getMonitorLogFile();
			File fileToUpload = this.processMonitorFile(monitorFile, collector.getEventFilters());
			files.add(fileToUpload);

			// check if backup/archive files were also selected and add them
			List<File> backupFilesToUpload = getBackupFiles();
			if (backupFilesToUpload.size() > 0) {
				for (File backupFile : backupFilesToUpload) {
					if (backupFile.exists()) {
						List<File> unzippedFiles;
						try {
							unzippedFiles = ZipFileUtil.unzipFiles(backupFile, System.getProperty("java.io.tmpdir"), //$NON-NLS-1$
									new NullProgressMonitor());

							if (unzippedFiles.size() > 0) {
								for (File unzippedFile : unzippedFiles) {
									files.add(this.processMonitorFile(unzippedFile, collector.getEventFilters()));
									this.addToSubmittedLogFile(backupFile);
								}
							}
						} catch (IOException e) {
							StatusHandler.log(new Status(IStatus.ERROR, UiUsageMonitorPlugin.ID_PLUGIN,
									Messages.UsageSubmissionWizard_error_unzipping, e));
						}
					}
				}
			}

			try {
				File zipFile = File.createTempFile(UiUsageMonitorPlugin.getDefault().getUserId() + ".", ".zip"); //$NON-NLS-1$ //$NON-NLS-2$
				ZipFileUtil.createZipFile(zipFile, files);
				return zipFile;
			} catch (Exception e) {
				StatusHandler.log(new Status(IStatus.ERROR, UiUsageMonitorPlugin.ID_PLUGIN,
						Messages.UsageSubmissionWizard_error_uploading, e));
				return null;
			}
		} finally {
			UiUsageMonitorPlugin.getDefault().getInteractionLogger().startMonitoring();
		}
	}

	private void addToSubmittedLogFile(File file) {
		File submissionLogFile = new File(MonitorFileRolloverJob.getZippedMonitorFileDirPath(),
				SUBMISSION_LOG_FILE_NAME);
		try {
			FileWriter fileWriter = new FileWriter(submissionLogFile, true);
			fileWriter.append(file.getAbsolutePath() + "\n"); //$NON-NLS-1$
			fileWriter.flush();
			fileWriter.close();
		} catch (IOException e) {
			StatusHandler.log(new Status(IStatus.ERROR, UiUsageMonitorPlugin.ID_PLUGIN,
					Messages.UsageSubmissionWizard_error_uploading, e));
		}
	}

	private File processMonitorFile(File monitorFile, Collection<String> eventFilters) {
		File processedFile = new File(String.format(
				"processed-%s%d.xml", UiUsageMonitorPlugin.MONITOR_LOG_NAME, processedFileCount++)); //$NON-NLS-1$
		InteractionEventLogger logger = new InteractionEventLogger(processedFile);
		logger.startMonitoring();
		List<InteractionEvent> eventList = logger.getHistoryFromFile(monitorFile);

		boolean filtersEmpty = eventFilters.size() == 0;
		if (eventList.size() > 0) {
			for (InteractionEvent event : eventList) {
				if (filtersEmpty) {
					logger.interactionObserved(event);
				} else {
					for (String prefix : eventFilters) {
						if (event.getOriginId().startsWith(prefix)) {
							logger.interactionObserved(event);
						}
					}
				}
			}
		}

		return processedFile;
	}

	/**
	 * Method to upload a file to a cgi script
	 * 
	 * @param f
	 *            The file to upload
	 * @return true on success
	 */
	private boolean upload(UsageCollector collector, File f, String type, IProgressMonitor monitor) {
		int status = 0;

		try {
			final PostMethod filePost = new PostMethod(collector.getUploadUrl());
			try {
				Part[] parts = { new FilePart("temp.txt", f, "application/zip", FilePart.DEFAULT_CHARSET) }; //$NON-NLS-1$

				filePost.setRequestEntity(new MultipartRequestEntity(parts, filePost.getParams()));

				final HttpClient client = new HttpClient();

				status = client.executeMethod(filePost);
			} finally {
				filePost.releaseConnection();
			}
		} catch (final Exception e) {
			// there was a problem with the file upload so throw up an error
			// dialog to inform the user and log the exception
			if (e instanceof NoRouteToHostException || e instanceof UnknownHostException) {
				StatusHandler.log(new Status(IStatus.ERROR, UiUsageMonitorPlugin.ID_PLUGIN,
						Messages.UsageSubmissionWizard_no_network, e));
			} else {
				StatusHandler.log(new Status(IStatus.ERROR, UiUsageMonitorPlugin.ID_PLUGIN,
						Messages.UsageSubmissionWizard_unknown_exception, e));
			}
			return false;
		}

		monitor.worked(1);

		if (status == 401) {
			StatusHandler.log(new Status(IStatus.ERROR, UiUsageMonitorPlugin.ID_PLUGIN, NLS.bind(
					Messages.UsageSubmissionWizard_invalid_uid, f.getName(), ""))); //$NON-NLS-1$
		} else if (status == 407) {
			StatusHandler.log(new Status(IStatus.ERROR, UiUsageMonitorPlugin.ID_PLUGIN,
					Messages.UsageSubmissionWizard_proxy_authentication));
		} else if (status != 200) {
			// there was a problem with the file upload so throw up an error
			// dialog to inform the user
			StatusHandler.log(new Status(IStatus.ERROR, UiUsageMonitorPlugin.ID_PLUGIN, NLS.bind(
					Messages.UsageSubmissionWizard_30, f.getName(), status)));
		} else {
			// the file was uploaded successfully
			return true;
		}
		return false;
	}

	public static List<File> getBackupFiles() {
		ArrayList<File> backupFiles = new ArrayList<File>();
		try {
			final File backupFolder = MonitorFileRolloverJob.getZippedMonitorFileDirPath();

			if (backupFolder.exists()) {
				File[] files = backupFolder.listFiles();
				File submissionLogFile = new File(backupFolder, SUBMISSION_LOG_FILE_NAME);

				if (!submissionLogFile.exists()) {
					submissionLogFile.createNewFile();
				}

				FileInputStream inputStream = new FileInputStream(submissionLogFile);

				int bytesRead = 0;
				byte[] buffer = new byte[1000];

				String fileContents = ""; //$NON-NLS-1$

				if (submissionLogFile.exists()) {
					while ((bytesRead = inputStream.read(buffer)) != -1) {
						fileContents += new String(buffer, 0, bytesRead);
					}
				}
				for (File file : files) {
					if (file.getName().contains(MonitorFileRolloverJob.BACKUP_FILE_SUFFIX)
							&& !fileContents.contains(file.getName())) {
						backupFiles.add(file);
					}
				}
			}
		} catch (FileNotFoundException e) {
			StatusHandler.log(new Status(IStatus.ERROR, UiUsageMonitorPlugin.ID_PLUGIN,
					Messages.UsageDataUploadJob_cant_read_files, e));
		} catch (IOException e) {
			StatusHandler.log(new Status(IStatus.ERROR, UiUsageMonitorPlugin.ID_PLUGIN,
					Messages.UsageDataUploadJob_cant_read_files, e));
		}
		return backupFiles;
	}
}