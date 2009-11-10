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

package com.atlassian.connector.eclipse.internal.monitor.usage.operations;

import java.io.File;
import java.io.IOException;
import java.net.NoRouteToHostException;
import java.net.UnknownHostException;
import java.util.Arrays;

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
import org.eclipse.mylyn.monitor.core.InteractionEvent;

import com.atlassian.connector.eclipse.internal.monitor.usage.InteractionEventLogger;
import com.atlassian.connector.eclipse.internal.monitor.usage.Messages;
import com.atlassian.connector.eclipse.internal.monitor.usage.StudyParameters;
import com.atlassian.connector.eclipse.internal.monitor.usage.UiUsageMonitorPlugin;

public final class UploadMonitoringStatusJob extends Job {

	private final boolean enabled;

	public UploadMonitoringStatusJob(boolean enabled) {
		super("Reporting Usage Data Monitoring Status");
		setPriority(Job.SHORT);
		setUser(false);
		setSystem(true);
		this.enabled = enabled;
	}

	@Override
	protected IStatus run(IProgressMonitor monitor) {
		final SubMonitor submonitor = SubMonitor.convert(monitor);
		final File monitoringDisabledLog;
		try {
			monitoringDisabledLog = createMonitoringDisabledLog(submonitor.newChild(1));
		} catch (IOException e) {
			return Status.OK_STATUS;
		}

		try {
			upload(UiUsageMonitorPlugin.getDefault().getStudyParameters(), monitoringDisabledLog, monitor);
		} finally {
			monitoringDisabledLog.delete();
		}

		return Status.OK_STATUS;
	}

	private File createMonitoringDisabledLog(IProgressMonitor monitor) throws IOException {
		SubMonitor submonitor = SubMonitor.convert(monitor);
		File temp = File.createTempFile("temp.", ".xml");
		try {
			InteractionEventLogger iel = new InteractionEventLogger(temp);
			iel.startMonitoring();
			iel.interactionObserved(InteractionEvent.makePreference(UiUsageMonitorPlugin.ID_PLUGIN,
					enabled ? "enabled usage data monitor" : "disabled usage data monitor")); //$NON-NLS-1$ //$NON-NLS-2$
			iel.stopMonitoring();

			File zipFile = File.createTempFile(UiUsageMonitorPlugin.getDefault().getUserId() + ".", ".zip"); //$NON-NLS-1$ //$NON-NLS-2$
			ZipFileUtil.createZipFile(zipFile, Arrays.asList(new File[] { temp }), submonitor.newChild(1));
			return zipFile;
		} finally {
			temp.delete();
		}
	}

	/**
	 * Method to upload a file to a cgi script
	 * 
	 * @param f
	 *            The file to upload
	 * @return true on success
	 */
	private boolean upload(StudyParameters params, File log, IProgressMonitor monitor) {
		int status = 0;

		try {
			final PostMethod filePost = new PostMethod(params.getUploadUrl());
			try {
				Part[] parts = { new FilePart("temp.txt", log, "application/zip", FilePart.DEFAULT_CHARSET) }; //$NON-NLS-1$

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

		return status == 200;
	}
}