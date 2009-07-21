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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationTargetException;
import java.net.NoRouteToHostException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.methods.GetMethod;
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
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.mylyn.commons.core.StatusHandler;
import org.eclipse.mylyn.internal.commons.core.ZipFileUtil;
import org.eclipse.mylyn.internal.monitor.usage.InteractionEventLogger;
import org.eclipse.mylyn.internal.monitor.usage.MonitorFileRolloverJob;
import org.eclipse.mylyn.internal.monitor.usage.UiUsageMonitorPlugin;
import org.eclipse.mylyn.monitor.core.InteractionEvent;
import org.eclipse.mylyn.monitor.usage.AbstractStudyBackgroundPage;
import org.eclipse.mylyn.monitor.usage.AbstractStudyQuestionnairePage;
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

	public static final String LOG = "log";

	public static final String STATS = "usage";

	public static final String QUESTIONAIRE = "questionaire";

	public static final String BACKGROUND = "background";

	private static final String ORG_ECLIPSE_PREFIX = "org.eclipse.";

	public static final int HTTP_SERVLET_RESPONSE_SC_OK = 200;

	public static final int SIZE_OF_INT = 8;

	private boolean failed = false;

	private boolean displayBackgroundPage = false;

	private boolean displayFileSelectionPage = false;

	/** The id of the user */
	private int uid;

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
				"icons/wizban/banner-user.gif"));
		super.setWindowTitle("Mylyn Feedback");
	}

	private void init(boolean performUpload) {
		this.performUpload = performUpload;
		setNeedsProgressMonitor(true);
		uid = UiUsageMonitorPlugin.getDefault().getPreferenceStore().getInt(UiUsageMonitorPlugin.PREF_USER_ID);
		if (uid == 0 || uid == -1) {
			uid = this.getNewUid();
			UiUsageMonitorPlugin.getDefault().getPreferenceStore().setValue(UiUsageMonitorPlugin.PREF_USER_ID, uid);
		}
		uploadPage = new UsageUploadWizardPage(this);
		fileSelectionPage = new UsageFileSelectionWizardPage("TODO, change this string");
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

		Job j = new Job("Upload User Statistics") {

			@Override
			protected IStatus run(IProgressMonitor monitor) {
				try {
					monitor.beginTask("Uploading user statistics", 3);
					performUpload(monitor);
					monitor.done();
					// op.run(monitor);
					return Status.OK_STATUS;
				} catch (Exception e) {
					Status status = new Status(IStatus.ERROR, UiUsageMonitorPlugin.ID_PLUGIN, IStatus.ERROR,
							"Error uploading statistics", e);
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
		}
		File zipFile = zipFilesForUpload();
		if (zipFile == null) {
			return;
		}

		upload(zipFile, STATS, monitor);

		if (zipFile.exists()) {
			zipFile.delete();
		}

		if (!failed) {
			PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {
				public void run() {
					// popup a dialog telling the user that the upload was good
					MessageDialog.openInformation(Display.getCurrent().getActiveShell(), "Successful Upload",
							"Your usage statistics have been successfully uploaded.\n Thank you for participating.");
				}
			});
		}

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
	 */
	private void upload(File f, String type, IProgressMonitor monitor) {
		if (failed) {
			return;
		}

		int status = 0;

		try {
			String servletUrl = UiUsageMonitorPlugin.getDefault().getStudyParameters().getServletUrl();
			final PostMethod filePost = new PostMethod(servletUrl);

			Part[] parts = { new FilePart("temp.txt", f) };

			filePost.setRequestEntity(new MultipartRequestEntity(parts, filePost.getParams()));

			final HttpClient client = new HttpClient();

			status = client.executeMethod(filePost);
			filePost.releaseConnection();

		} catch (final Exception e) {
			// there was a problem with the file upload so throw up an error
			// dialog to inform the user and log the exception
			failed = true;
			if (e instanceof NoRouteToHostException || e instanceof UnknownHostException) {
				PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {
					public void run() {
						MessageDialog.openError(null, "Error Uploading", "There was an error uploading the file"
								+ ": \n" + "No network connection.  Please try again later");
					}
				});
			} else {
				PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {
					public void run() {
						MessageDialog.openError(null, "Error Uploading", "There was an error uploading the file"
								+ ": \n" + e.getClass().getCanonicalName());
					}
				});
				StatusHandler.log(new Status(IStatus.ERROR, UiUsageMonitorPlugin.ID_PLUGIN, "Error uploading", e));
			}
		}

		monitor.worked(1);

		final String filedesc = f.getName();

		final int httpResponseStatus = status;

		if (status == 401) {
			// The uid was incorrect so inform the user
			PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {
				public void run() {
					MessageDialog.openError(null, "Error Uploading", "There was an error uploading the " + filedesc
							+ ": \n" + "Your uid was incorrect: " + uid + "\n");
				}
			});
		} else if (status == 407) {
			failed = true;
			PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {
				public void run() {
					MessageDialog.openError(null, "Error Uploading",
							"Could not upload because proxy server authentication failed.  Please check your proxy server settings.");
				}
			});
		} else if (status != 200) {
			failed = true;
			// there was a problem with the file upload so throw up an error
			// dialog to inform the user
			PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {
				public void run() {
					MessageDialog.openError(null, "Error Uploading", "There was an error uploading the " + filedesc
							+ ": \n" + "HTTP Response Code " + httpResponseStatus + "\n" + "Please try again later");
				}
			});
		} else {
			// the file was uploaded successfully
		}

	}

	public String getMonitorFileName() {
		return monitorFile.getAbsolutePath();
	}

	/** The status from the http request */
	private int status;

	/** the response for the http request */
	private String resp;

	public int getExistingUid(String firstName, String lastName, String emailAddress, boolean anonymous) {
		if (failed) {
			return -1;
		}
		try {

			// TODO, do this method properly
			// create a new post method
			String url = UiUsageMonitorPlugin.getDefault().getStudyParameters().getServletUrl()
					+ UiUsageMonitorPlugin.getDefault().getStudyParameters().getServletUrl();
			final GetMethod getUidMethod = new GetMethod(url);

			NameValuePair first = new NameValuePair("firstName", firstName);
			NameValuePair last = new NameValuePair("lastName", lastName);
			NameValuePair email = new NameValuePair("email", emailAddress);
			NameValuePair job = new NameValuePair("jobFunction", "");
			NameValuePair size = new NameValuePair("companySize", "");
			NameValuePair buisness = new NameValuePair("companyBuisness", "");
			NameValuePair contact = new NameValuePair("contact", "");
			NameValuePair anon = null;
			if (anonymous) {
				anon = new NameValuePair("anonymous", "true");
			} else {
				anon = new NameValuePair("anonymous", "false");
			}

			if (UiUsageMonitorPlugin.getDefault().usingContactField()) {
				getUidMethod.setQueryString(new NameValuePair[] { first, last, email, job, size, buisness, anon,
						contact });
			} else {
				getUidMethod.setQueryString(new NameValuePair[] { first, last, email, job, size, buisness, anon });
			}

			// create a new client and upload the file
			final HttpClient client = new HttpClient();
			UiUsageMonitorPlugin.getDefault().configureProxy(client, url);

			ProgressMonitorDialog pmd = new ProgressMonitorDialog(Display.getCurrent().getActiveShell());
			pmd.run(false, false, new IRunnableWithProgress() {
				public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
					monitor.beginTask("Get User Id", 1);

					try {
						status = client.executeMethod(getUidMethod);

						resp = getData(getUidMethod.getResponseBodyAsStream());

						// release the connection to the server
						getUidMethod.releaseConnection();
					} catch (final Exception e) {
						// there was a problem with the file upload so throw up
						// an error
						// dialog to inform the user and log the exception
						failed = true;
						if (e instanceof NoRouteToHostException || e instanceof UnknownHostException) {
							PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {
								public void run() {
									MessageDialog.openError(null, "Error Uploading",
											"There was an error getting a new user id: \n"
													+ "No network connection.  Please try again later");
								}
							});
						} else {
							PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {
								public void run() {
									MessageDialog.openError(null, "Error Uploading",
											"There was an error getting a new user id: \n"
													+ e.getClass().getCanonicalName() + e.getMessage());
								}
							});
							StatusHandler.log(new Status(IStatus.ERROR, UiUsageMonitorPlugin.ID_PLUGIN,
									"Error uploading", e));
						}
					}
					monitor.worked(1);
					monitor.done();
				}
			});

			if (status != 200) {
				// there was a problem with the file upload so throw up an error
				// dialog to inform the user

				failed = true;

				// there was a problem with the file upload so throw up an error
				// dialog to inform the user
				PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {
					public void run() {
						MessageDialog.openError(null, "Error Getting User ID",
								"There was an error getting a user id: \n" + "HTTP Response Code " + status + "\n"
										+ "Please try again later");
					}
				});
			} else {
				resp = resp.substring(resp.indexOf(":") + 1).trim();
				uid = Integer.parseInt(resp);
				UiUsageMonitorPlugin.getDefault().getPreferenceStore().setValue(UiUsageMonitorPlugin.PREF_USER_ID, uid);
				return uid;
			}

		} catch (final Exception e) {
			// there was a problem with the file upload so throw up an error
			// dialog to inform the user and log the exception
			failed = true;
			if (e instanceof NoRouteToHostException || e instanceof UnknownHostException) {
				PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {
					public void run() {
						MessageDialog.openError(null, "Error Uploading", "There was an error getting a new user id: \n"
								+ "No network connection.  Please try again later");
					}
				});
			} else {
				PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {
					public void run() {
						MessageDialog.openError(null, "Error Uploading", "There was an error getting a new user id: \n"
								+ e.getClass().getCanonicalName());
					}
				});
				StatusHandler.log(new Status(IStatus.ERROR, UiUsageMonitorPlugin.ID_PLUGIN, "Error uploading", e));
			}
		}
		return -1;
	}

	public int getNewUid() {
		final PostMethod filePost = new PostMethod(UiUsageMonitorPlugin.DEFAULT_UPLOAD_SERVER
				+ UiUsageMonitorPlugin.DEFAULT_UPLOAD_SERVLET_ID);

		filePost.addParameter(new NameValuePair("MylarUserID", ""));
		final HttpClient client = new HttpClient();
		int status = 0;

		try {
			status = client.executeMethod(filePost);

			if (status == HTTP_SERVLET_RESPONSE_SC_OK) {
				InputStream inputStream = filePost.getResponseBodyAsStream();
				byte[] buffer = new byte[SIZE_OF_INT];
				int numBytesRead = inputStream.read(buffer);
				int uid = new Integer(new String(buffer, 0, numBytesRead)).intValue();
				filePost.releaseConnection();

				return uid;
			} else {
				return -1;
			}

		} catch (final Exception e) {
			// there was a problem with the file upload so throw up an error
			// dialog to inform the user and log the exception
			return -1;

		}
	}

	public int getNewUid(String firstName, String lastName, String emailAddress, boolean anonymous, String jobFunction,
			String companySize, String companyFunction, boolean contactEmail) {
		if (failed) {
			return -1;
		}
		try {
			addBackgroundPage();

			final PostMethod filePost = new PostMethod(UiUsageMonitorPlugin.DEFAULT_UPLOAD_SERVER
					+ UiUsageMonitorPlugin.DEFAULT_UPLOAD_SERVLET_ID);
			filePost.addParameter(new NameValuePair("MylarUserID", ""));
			final HttpClient client = new HttpClient();
			int status = 0;

			try {
				status = client.executeMethod(filePost);

				if (status == 202) {
					InputStream inputStream = filePost.getResponseBodyAsStream();
					byte[] buffer = new byte[8];
					int numBytesRead = inputStream.read(buffer);
					int uid = new Integer(new String(buffer, 0, numBytesRead)).intValue();
					filePost.releaseConnection();

					return uid;
				} else {
					return -1;
				}

			} catch (final Exception e) {
				// there was a problem with the file upload so throw up an error
				// dialog to inform the user and log the exception
			}

			// NameValuePair first = new NameValuePair("firstName", firstName);
			// NameValuePair last = new NameValuePair("lastName", lastName);
			// NameValuePair email = new NameValuePair("email", emailAddress);
			// NameValuePair job = new NameValuePair("jobFunction",
			// jobFunction);
			// NameValuePair size = new NameValuePair("companySize",
			// companySize);
			// NameValuePair buisness = new NameValuePair("companyBuisness",
			// companyFunction);
			// NameValuePair contact = null;
			// if (contactEmail) {
			// contact = new NameValuePair("contact", "true");
			// } else {
			// contact = new NameValuePair("contact", "false");
			// }
			// NameValuePair anon = null;
			// if (anonymous) {
			// anon = new NameValuePair("anonymous", "true");
			// } else {
			// anon = new NameValuePair("anonymous", "false");
			// }

			if (status != 200) {
				// there was a problem with the file upload so throw up an error
				// dialog to inform the user

				failed = true;

				// there was a problem with the file upload so throw up an error
				// dialog to inform the user
				MessageDialog.openError(null, "Error Getting User ID", "There was an error getting a user id: \n"
						+ "HTTP Response Code " + status + "\n" + "Please try again later");
			} else {
				resp = resp.substring(resp.indexOf(":") + 1).trim();
				uid = Integer.parseInt(resp);
				UiUsageMonitorPlugin.getDefault().getPreferenceStore().setValue(UiUsageMonitorPlugin.PREF_USER_ID, uid);
				return uid;
			}

		} catch (Exception e) {
			// there was a problem with the file upload so throw up an error
			// dialog to inform the user and log the exception
			failed = true;
			if (e instanceof NoRouteToHostException || e instanceof UnknownHostException) {
				MessageDialog.openError(null, "Error Uploading", "There was an error getting a new user id: \n"
						+ "No network connection.  Please try again later");
			} else {
				MessageDialog.openError(null, "Error Uploading", "There was an error getting a new user id: \n"
						+ e.getClass().getCanonicalName());
				StatusHandler.log(new Status(IStatus.ERROR, UiUsageMonitorPlugin.ID_PLUGIN, "Error uploading", e));
			}
		}
		return -1;
	}

	private String getData(InputStream i) {
		String s = "";
		String data = "";
		BufferedReader br = new BufferedReader(new InputStreamReader(i));
		try {
			while ((s = br.readLine()) != null) {
				data += s;
			}
		} catch (IOException e) {
			StatusHandler.log(new Status(IStatus.ERROR, UiUsageMonitorPlugin.ID_PLUGIN, "Error uploading", e));
		}
		return data;
	}

	public int getUid() {
		return uid;
	}

	public boolean failed() {
		return failed;
	}

	private File processMonitorFile(File monitorFile) {
		File processedFile = new File("processed-" + UiUsageMonitorPlugin.MONITOR_LOG_NAME + processedFileCount++
				+ ".xml");
		InteractionEventLogger logger = new InteractionEventLogger(processedFile);
		logger.startMonitoring();
		List<InteractionEvent> eventList = logger.getHistoryFromFile(monitorFile);

		if (eventList.size() > 0) {
			for (InteractionEvent event : eventList) {
				if (event.getOriginId().startsWith(ORG_ECLIPSE_PREFIX)) {
					logger.interactionObserved(event);
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
			fileWriter.append(fileName + "\n");
			fileWriter.flush();
			fileWriter.close();
		} catch (IOException e) {
			StatusHandler.log(new Status(IStatus.ERROR, UiUsageMonitorPlugin.ID_PLUGIN,
					"Error unzipping backup monitor log files", e));
		}
	}

	private File zipFilesForUpload() {
		UiUsageMonitorPlugin.setPerformingUpload(true);
		UiUsageMonitorPlugin.getDefault().getInteractionLogger().stopMonitoring();

		List<File> files = new ArrayList<File>();
		File monitorFile = UiUsageMonitorPlugin.getDefault().getMonitorLogFile();
		File fileToUpload = this.processMonitorFile(monitorFile);
		files.add(fileToUpload);

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
								files.add(this.processMonitorFile(f));
								this.addToSubmittedLogFile(currFilePath);
							}
						}
					} catch (IOException e) {
						StatusHandler.log(new Status(IStatus.ERROR, UiUsageMonitorPlugin.ID_PLUGIN,
								"Error unzipping backup monitor log files", e));
					}
				}
			}
		}

		UiUsageMonitorPlugin.getDefault().getInteractionLogger().startMonitoring();
		try {
			File zipFile = File.createTempFile(uid + ".", ".zip");
			ZipFileUtil.createZipFile(zipFile, files);
			return zipFile;
		} catch (Exception e) {
			StatusHandler.log(new Status(IStatus.ERROR, UiUsageMonitorPlugin.ID_PLUGIN, "Error uploading", e));
			return null;
		}
	}
}
