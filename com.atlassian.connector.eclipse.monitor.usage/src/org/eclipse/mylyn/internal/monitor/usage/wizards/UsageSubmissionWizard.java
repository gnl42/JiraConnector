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

package org.eclipse.mylyn.internal.monitor.usage.wizards;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.NoRouteToHostException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.UUID;

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
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.mylyn.commons.core.StatusHandler;
import org.eclipse.mylyn.internal.commons.core.ZipFileUtil;
import org.eclipse.mylyn.internal.monitor.usage.InteractionEventLogger;
import org.eclipse.mylyn.internal.monitor.usage.Messages;
import org.eclipse.mylyn.internal.monitor.usage.MonitorFileRolloverJob;
import org.eclipse.mylyn.internal.monitor.usage.UiUsageMonitorPlugin;
import org.eclipse.mylyn.internal.monitor.usage.UsageCollector;
import org.eclipse.mylyn.monitor.core.InteractionEvent;
import org.eclipse.mylyn.monitor.usage.AbstractStudyBackgroundPage;
import org.eclipse.mylyn.monitor.usage.AbstractStudyQuestionnairePage;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.plugin.AbstractUIPlugin;

/**
 * A wizard for uploading the Mylyn statistics to a website
 * 
 * @author Shawn Minto
 */
public class UsageSubmissionWizard extends Wizard implements INewWizard {

	public static final String LOG = "log"; //$NON-NLS-1$

	public static final String STATS = "usage"; //$NON-NLS-1$

	public static final String QUESTIONAIRE = "questionaire"; //$NON-NLS-1$

	public static final String BACKGROUND = "background"; //$NON-NLS-1$

	private static final String ORG_ECLIPSE_PREFIX = "org.eclipse."; //$NON-NLS-1$

	public static final int HTTP_SERVLET_RESPONSE_SC_OK = 200;

	public static final int SIZE_OF_INT = 8;

	private boolean failed = false;

	private boolean displayBackgroundPage = false;

	private boolean displayFileSelectionPage = false;

	private final File monitorFile = UiUsageMonitorPlugin.getDefault().getMonitorLogFile();

	private static int processedFileCount = 1;

	private UsageUploadWizardPage uploadPage;

	private UsageFileSelectionWizardPage fileSelectionPage;

	// private GetNewUserIdPage getUidPage;

	private AbstractStudyQuestionnairePage questionnairePage;

	private AbstractStudyBackgroundPage backgroundPage;

	private boolean performUpload = true;

	private List<String> backupFilesToUpload;

	public UsageSubmissionWizard() {
		super();
		setTitles();
		init(true);
	}

	public UsageSubmissionWizard(boolean performUpload) {
		super();
		setTitles();
		init(performUpload);
	}

	private void setTitles() {
		super.setDefaultPageImageDescriptor(AbstractUIPlugin.imageDescriptorFromPlugin(UiUsageMonitorPlugin.ID_PLUGIN,
				"icons/wizban/banner-user.gif")); //$NON-NLS-1$
		super.setWindowTitle(Messages.UsageSubmissionWizard_0);
	}

	private void init(boolean performUpload) {
		this.performUpload = performUpload;
		setNeedsProgressMonitor(true);
		uploadPage = new UsageUploadWizardPage(this);
		fileSelectionPage = new UsageFileSelectionWizardPage(Messages.UsageSubmissionWizard_7);
		if (UiUsageMonitorPlugin.getDefault().isBackgroundEnabled()) {
			AbstractStudyBackgroundPage page = UiUsageMonitorPlugin.getDefault()
					.getStudyParameters()
					.getBackgroundPage();
			backgroundPage = page;
		}
		if (UiUsageMonitorPlugin.getDefault().isQuestionnaireEnabled() && performUpload) {
			AbstractStudyQuestionnairePage page = UiUsageMonitorPlugin.getDefault()
					.getStudyParameters()
					.getQuestionnairePage();
			questionnairePage = page;
		}
		super.setForcePreviousAndNextButtons(true);

	}

	private File questionnaireFile = null;

	private File backgroundFile = null;

	@Override
	public boolean performFinish() {

		if (!performUpload) {
			return true;
		}
		if (UiUsageMonitorPlugin.getDefault().isQuestionnaireEnabled() && performUpload && questionnairePage != null) {
			questionnaireFile = questionnairePage.createFeedbackFile();
		}
		if (UiUsageMonitorPlugin.getDefault().isBackgroundEnabled() && performUpload && displayBackgroundPage
				&& backgroundPage != null) {
			backgroundFile = backgroundPage.createFeedbackFile();
		}

		if (displayFileSelectionPage) {
			backupFilesToUpload = fileSelectionPage.getZipFilesSelected();
		}

		// final WorkspaceModifyOperation op = new WorkspaceModifyOperation() {
		// protected void execute(final IProgressMonitor monitor) throws
		// CoreException {
		// monitor.beginTask("Uploading user statistics", 3);
		// performUpload(monitor);
		// monitor.done();
		// }
		// };

		Job j = new Job(Messages.UsageSubmissionWizard_upload_user_stats) {

			@Override
			protected IStatus run(IProgressMonitor monitor) {
				try {
					monitor.beginTask(Messages.UsageSubmissionWizard_uploading_usage_stats, 3);
					performUpload(monitor);
					monitor.done();
					// op.run(monitor);
					return Status.OK_STATUS;
				} catch (Exception e) {
					Status status = new Status(IStatus.ERROR, UiUsageMonitorPlugin.ID_PLUGIN, IStatus.ERROR,
							Messages.UsageSubmissionWizard_error_uploading, e);
					StatusHandler.log(status);
					return status;
				}
			}
		};
		// j.setUser(true);
		j.setPriority(Job.DECORATE);
		j.schedule();
		return true;
	}

	public void performUpload(IProgressMonitor monitor) {
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

		PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {
			public void run() {
				if (failed) {
					// popup a dialog telling the user that the upload was good
					MessageDialog.openError(Display.getCurrent().getActiveShell(),
							Messages.UsageSubmissionWizard_error_uploading,
							Messages.UsageSubmissionWizard_some_uploads_failed);
				} else {
					// popup a dialog telling the user that the upload was good
					MessageDialog.openInformation(Display.getCurrent().getActiveShell(),
							Messages.UsageSubmissionWizard_successful_upload, Messages.UsageSubmissionWizard_thank_you);
				}
			}
		});

		UiUsageMonitorPlugin.getDefault().getInteractionLogger().startMonitoring();
		UiUsageMonitorPlugin.setPerformingUpload(false);
		return;
	}

	@Override
	public boolean performCancel() {
		UiUsageMonitorPlugin.getDefault().userCancelSubmitFeedback(new Date(), true);
		return true;
	}

	@Override
	public boolean canFinish() {
		if (!performUpload) {
			return true;// getUidPage.isPageComplete();
		} else {
			return this.getContainer().getCurrentPage() == uploadPage || !performUpload;
		}
	}

	public UsageUploadWizardPage getUploadPage() {
		return uploadPage;
	}

	/**
	 * @see org.eclipse.ui.IWorkbenchWizard#init(org.eclipse.ui.IWorkbench,
	 *      org.eclipse.jface.viewers.IStructuredSelection)
	 */
	public void init(IWorkbench workbench, IStructuredSelection selection) {
		// no initialization needed
	}

	@Override
	public void addPages() {
		if (UiUsageMonitorPlugin.getDefault().isQuestionnaireEnabled() && performUpload && questionnairePage != null) {
			addPage(questionnairePage);
		}
		if (performUpload) {
			if (UsageFileSelectionWizardPage.unsubmittedLogsExist()) {
				addPage(fileSelectionPage);
				displayFileSelectionPage = true;
			}
			addPage(uploadPage);
		}
	}

	public void addBackgroundPage() {
		if (UiUsageMonitorPlugin.getDefault().isBackgroundEnabled() && backgroundPage != null) {
			addPage(backgroundPage);
			displayBackgroundPage = true;
		}
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
				Part[] parts = { new FilePart("temp.txt", f) }; //$NON-NLS-1$

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
						Messages.UsageSubmissionWizard_19, e));
			}
			return false;
		}

		monitor.worked(1);

		if (status == 401) {
			StatusHandler.log(new Status(IStatus.ERROR, UiUsageMonitorPlugin.ID_PLUGIN, NLS.bind(
					Messages.UsageSubmissionWizard_invalid_uid, f.getName(), "")));
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

	public String getMonitorFileName() {
		return monitorFile.getAbsolutePath();
	}

	/** The status from the http request */
	//private int status;

	/** the response for the http request */
	//private String resp;

	/*
	private String getData(InputStream i) {
		String s = ""; //$NON-NLS-1$
		String data = ""; //$NON-NLS-1$
		BufferedReader br = new BufferedReader(new InputStreamReader(i));
		try {
			while ((s = br.readLine()) != null) {
				data += s;
			}
		} catch (IOException e) {
			StatusHandler.log(new Status(IStatus.ERROR, UiUsageMonitorPlugin.ID_PLUGIN,
					Messages.UsageSubmissionWizard_error_uploading, e));
		}
		return data;
	}
	*/

	public boolean failed() {
		return failed;
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

	private void addToSubmittedLogFile(String fileName) {
		File submissionLogFile = new File(MonitorFileRolloverJob.getZippedMonitorFileDirPath(),
				UsageFileSelectionWizardPage.SUBMISSION_LOG_FILE_NAME);
		try {
			FileWriter fileWriter = new FileWriter(submissionLogFile, true);
			fileWriter.append(fileName + Messages.UsageSubmissionWizard_90);
			fileWriter.flush();
			fileWriter.close();
		} catch (IOException e) {
			StatusHandler.log(new Status(IStatus.ERROR, UiUsageMonitorPlugin.ID_PLUGIN,
					Messages.UsageSubmissionWizard_91, e));
		}
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
			if (displayFileSelectionPage && backupFilesToUpload.size() > 0) {
				for (String currFilePath : backupFilesToUpload) {
					File file = new File(MonitorFileRolloverJob.getZippedMonitorFileDirPath(), currFilePath);
					if (file.exists()) {
						List<File> unzippedFiles;
						try {
							unzippedFiles = ZipFileUtil.unzipFiles(file, System.getProperty("java.io.tmpdir"),
									new NullProgressMonitor());

							if (unzippedFiles.size() > 0) {
								for (File f : unzippedFiles) {
									files.add(this.processMonitorFile(f, collector.getEventFilters()));
									this.addToSubmittedLogFile(currFilePath);
								}
							}
						} catch (IOException e) {
							StatusHandler.log(new Status(IStatus.ERROR, UiUsageMonitorPlugin.ID_PLUGIN,
									Messages.UsageSubmissionWizard_error_uploading, e));
						}
					}
				}
			}

			try {
				File zipFile = File.createTempFile(UUID.randomUUID() + ".", ".zip"); //$NON-NLS-1$ //$NON-NLS-2$
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
}
