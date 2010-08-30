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

package com.atlassian.connector.eclipse.internal.monitor.core.operations;

import java.io.File;
import java.io.IOException;
import java.net.NoRouteToHostException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.multipart.FilePart;
import org.apache.commons.httpclient.methods.multipart.MultipartRequestEntity;
import org.apache.commons.httpclient.methods.multipart.Part;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.mylyn.commons.core.StatusHandler;
import org.eclipse.mylyn.internal.commons.core.ZipFileUtil;
import org.eclipse.osgi.util.NLS;

import com.atlassian.connector.eclipse.internal.monitor.core.Messages;
import com.atlassian.connector.eclipse.monitor.core.InteractionEvent;
import com.atlassian.connector.eclipse.monitor.core.InteractionEventLogger;
import com.atlassian.connector.eclipse.monitor.core.MonitorCorePlugin;

public final class UsageDataUploadJob extends Job {

	private final boolean ifTimeElapsed;

	private static int processedFileCount = 1;

	public UsageDataUploadJob(boolean ignoreLastTransmit) {
		super(Messages.UsageDataUploadJob_uploading_usage_stats);
		this.ifTimeElapsed = ignoreLastTransmit;
	}

	@Override
	protected IStatus run(IProgressMonitor monitor) {
		try {
			if (ifTimeElapsed) {
				performUpload(monitor);
			} else {
				checkLastTransmitTimeAndRun(monitor);
			}
			return Status.OK_STATUS;
		} catch (Exception e) {
			Status status = new Status(IStatus.ERROR, MonitorCorePlugin.ID_PLUGIN, IStatus.ERROR,
					Messages.UsageDataUploadJob_error_uploading, e);
			StatusHandler.log(status);
			return status;
		}
	}

	private void checkLastTransmitTimeAndRun(IProgressMonitor monitor) {
		final MonitorCorePlugin plugin = MonitorCorePlugin.getDefault();

		Date lastTransmit = plugin.getPreviousTransmitDate();
		if (lastTransmit == null) {
			lastTransmit = new Date();
			plugin.setPreviousTransmitDate(lastTransmit);
		}

		final Date currentTime = new Date();

		if (currentTime.getTime() > lastTransmit.getTime() + MonitorCorePlugin.DELAY_BETWEEN_TRANSMITS) {

			// time must be stored right away into preferences, to prevent
			// other threads
			plugin.setPreviousTransmitDate(currentTime);

			performUpload(monitor);
		}
	}

	private void performUpload(IProgressMonitor monitor) {
		SubMonitor submonitor = SubMonitor.convert(monitor, Messages.UsageDataUploadJob_uploading_usage_stats, 3);

		MonitorCorePlugin.getDefault().getInteractionLogger().stopMonitoring();
		try {
			File zipFile = zipFilesForUpload(submonitor.newChild(1));
			if (zipFile == null) {
				return;
			}
			try {
				upload(MonitorCorePlugin.UPLOAD_URL, zipFile, submonitor.newChild(1));
			} finally {
				if (zipFile.exists()) {
					zipFile.delete();
				}
			}
		} finally {
			// clear the log every time so it doesn't grow, don't care if it was sent
			try {
				MonitorCorePlugin.getDefault().getInteractionLogger().clearInteractionHistory();
			} catch (IOException e) {
				StatusHandler.log(new Status(IStatus.ERROR, MonitorCorePlugin.ID_PLUGIN,
						"Failed to clear the Usage Data log", e));
			}
			MonitorCorePlugin.getDefault().getInteractionLogger().startMonitoring();
		}
		return;
	}

	private File zipFilesForUpload(IProgressMonitor monitor) {
		List<File> files = new ArrayList<File>();
		File monitorFile = MonitorCorePlugin.getDefault().getMonitorLogFile();
		File fileToUpload;
		try {
			fileToUpload = this.processMonitorFile(monitorFile);
		} catch (IOException e1) {
			StatusHandler.log(new Status(IStatus.ERROR, MonitorCorePlugin.ID_PLUGIN,
					Messages.UsageDataUploadJob_error_uploading, e1));
			return null;
		}
		files.add(fileToUpload);

		try {
			File zipFile = File.createTempFile(MonitorCorePlugin.getDefault().getUserId() + ".", ".zip"); //$NON-NLS-1$ //$NON-NLS-2$
			ZipFileUtil.createZipFile(zipFile, files, monitor);
			return zipFile;
		} catch (Exception e) {
			StatusHandler.log(new Status(IStatus.ERROR, MonitorCorePlugin.ID_PLUGIN,
					Messages.UsageDataUploadJob_error_uploading, e));
			return null;
		}
	}

	private File processMonitorFile(File monitorFile) throws IOException {
		File processedFile = File.createTempFile(String.format("processed-%s%d.", MonitorCorePlugin.MONITOR_LOG_NAME,
				processedFileCount++), ".xml");
		InteractionEventLogger logger = new InteractionEventLogger(processedFile);
		logger.startMonitoring();
		List<InteractionEvent> eventList = logger.getHistoryFromFile(monitorFile);

		if (eventList.size() > 0) {
			for (InteractionEvent event : eventList) {
				logger.interactionObserved(event);
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
	private boolean upload(String uploadUrl, File f, IProgressMonitor monitor) {
		int status = 0;

		try {
			final PostMethod filePost = new PostMethod(uploadUrl);
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
				StatusHandler.log(new Status(IStatus.ERROR, MonitorCorePlugin.ID_PLUGIN,
						Messages.UsageDataUploadJob_no_network, e));
			} else {
				StatusHandler.log(new Status(IStatus.ERROR, MonitorCorePlugin.ID_PLUGIN,
						Messages.UsageDataUploadJob_unknown_exception, e));
			}
			return false;
		}

		monitor.worked(1);

		if (status == 401) {
			StatusHandler.log(new Status(IStatus.ERROR, MonitorCorePlugin.ID_PLUGIN, NLS.bind(
					Messages.UsageDataUploadJob_invalid_uid, f.getName(), "")));
		} else if (status == 407) {
			StatusHandler.log(new Status(IStatus.ERROR, MonitorCorePlugin.ID_PLUGIN,
					Messages.UsageDataUploadJob_proxy_authentication));
		} else if (status != 200) {
			// there was a problem with the file upload so throw up an error
			// dialog to inform the user
			StatusHandler.log(new Status(IStatus.ERROR, MonitorCorePlugin.ID_PLUGIN, NLS.bind(
					Messages.UsageDataUploadJob_30, f.getName(), status)));
		} else {
			// the file was uploaded successfully
			return true;
		}
		return false;
	}

}