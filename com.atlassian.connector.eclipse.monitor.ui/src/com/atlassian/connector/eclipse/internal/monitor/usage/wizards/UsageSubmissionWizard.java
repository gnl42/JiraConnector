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

package com.atlassian.connector.eclipse.internal.monitor.usage.wizards;

import java.io.File;

import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.mylyn.internal.provisional.commons.ui.WorkbenchUtil;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.plugin.AbstractUIPlugin;

import com.atlassian.connector.eclipse.internal.monitor.core.Messages;
import com.atlassian.connector.eclipse.internal.monitor.core.operations.UsageDataUploadJob;
import com.atlassian.connector.eclipse.internal.monitor.usage.MonitorUiPlugin;
import com.atlassian.connector.eclipse.monitor.core.MonitorCorePlugin;

/**
 * A wizard for uploading the Mylyn statistics to a website
 * 
 * @author Shawn Minto
 */
public class UsageSubmissionWizard extends Wizard implements INewWizard {

	public static final String LOG = "log"; //$NON-NLS-1$

	public static final String QUESTIONAIRE = "questionaire"; //$NON-NLS-1$

	public static final String BACKGROUND = "background"; //$NON-NLS-1$

	public static final int HTTP_SERVLET_RESPONSE_SC_OK = 200;

	public static final int SIZE_OF_INT = 8;

	private final boolean failed = false;

	private final File monitorFile = MonitorCorePlugin.getDefault().getMonitorLogFile();

	private UsageUploadWizardPage uploadPage;

	public UsageSubmissionWizard() {
		super();
		setTitles();
		init();
	}

	private void setTitles() {
		super.setDefaultPageImageDescriptor(AbstractUIPlugin.imageDescriptorFromPlugin(MonitorUiPlugin.ID_PLUGIN,
				"icons/wizban/banner-user.gif")); //$NON-NLS-1$
		super.setWindowTitle(Messages.UsageSubmissionWizard_title);
	}

	private void init() {
		setNeedsProgressMonitor(true);
		uploadPage = new UsageUploadWizardPage(this);

		super.setForcePreviousAndNextButtons(true);
	}

	//private File questionnaireFile = null;

	//private File backgroundFile = null;

	@Override
	public boolean performFinish() {
		Job j = new UsageDataUploadJob(true);
		j.setPriority(Job.DECORATE);
		j.addJobChangeListener(new JobChangeAdapter() {
			@Override
			public void done(IJobChangeEvent event) {
				if (!event.getResult().isOK()) {
					return;
				}

				PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {
					public void run() {
						MessageDialog.openInformation(WorkbenchUtil.getShell(), "Usage Data Submission",
								"Usage Data was uploaded. Thank you for participating!");
					}
				});
			}
		});
		j.schedule();
		return true;
	}

	@Override
	public boolean canFinish() {
		return this.getContainer().getCurrentPage() == uploadPage;
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
		addPage(uploadPage);
	}

	public String getMonitorFileName() {
		return monitorFile.getAbsolutePath();
	}

	public boolean failed() {
		return failed;
	}
}
