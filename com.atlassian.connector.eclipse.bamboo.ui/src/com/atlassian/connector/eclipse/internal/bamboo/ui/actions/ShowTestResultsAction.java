/*******************************************************************************
 * Copyright (c) 2009 Atlassian and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Atlassian - initial API and implementation
 ******************************************************************************/

package com.atlassian.connector.eclipse.internal.bamboo.ui.actions;

import com.atlassian.connector.eclipse.internal.bamboo.core.BambooConstants;
import com.atlassian.connector.eclipse.internal.bamboo.ui.BambooImages;
import com.atlassian.connector.eclipse.internal.bamboo.ui.BambooUiPlugin;
import com.atlassian.connector.eclipse.internal.bamboo.ui.EclipseBambooBuild;
import com.atlassian.connector.eclipse.internal.bamboo.ui.operations.RetrieveTestResultsJob;
import com.atlassian.connector.eclipse.internal.bamboo.ui.views.TestResultsView;
import com.atlassian.theplugin.commons.bamboo.BambooBuild;
import com.atlassian.theplugin.commons.bamboo.BuildDetails;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;
import org.eclipse.jdt.internal.junit.ui.JUnitPlugin;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.mylyn.commons.core.StatusHandler;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;

/**
 * Action to open the Test Results
 * 
 * @author Thomas Ehrnhoefer
 * @author Wojciech Seliga
 */
@SuppressWarnings("restriction")
public class ShowTestResultsAction extends EclipseBambooBuildSelectionListenerAction {

	private static boolean isJUnitAvailable = false;

	static {
		try {
			if (JUnitPlugin.getDefault() != null) {
				isJUnitAvailable = true;
			}

		} catch (Throwable e) {
			//ignore - swallow exception
		}
	}

	public ShowTestResultsAction() {
		super(null);
		initialize();
	}

	private void initialize() {
		setText(BambooConstants.SHOW_TEST_RESULTS_ACTION_LABEL);
		setImageDescriptor(BambooImages.JUNIT);
	}

	@Override
	void onRun(EclipseBambooBuild eclipseBambooBuild) {
		downloadAndShowTestResults(eclipseBambooBuild);
	}

	private void downloadAndShowTestResults(final EclipseBambooBuild eclipseBambooBuild) {
		final BambooBuild build = eclipseBambooBuild.getBuild();
		RetrieveTestResultsJob job = new RetrieveTestResultsJob(build, eclipseBambooBuild.getTaskRepository());
		job.addJobChangeListener(new JobChangeAdapter() {
			@Override
			public void done(IJobChangeEvent event) {
				if (event.getResult() == Status.OK_STATUS) {
					BuildDetails testResults = ((RetrieveTestResultsJob) event.getJob()).getBuildDetails();
					if (testResults != null) {
						showJUnitView(testResults, build.getPlanKey() + "-" + build.getNumber());
					} else {
						Display.getDefault().syncExec(new Runnable() {
							public void run() {
								MessageDialog.openError(Display.getDefault().getActiveShell(), getText(),
										"Retrieving test result for " + build.getPlanKey() + "-" + build.getNumber()
												+ " failed. See Error Log for details.");
							}
						});
					}
				}
			}
		});
		job.schedule();
	}

	@Override
	boolean onUpdateSelection(EclipseBambooBuild eclipseBambooBuild) {
		if (!isJUnitAvailable) {
			return false;
		}
		final BambooBuild build = eclipseBambooBuild.getBuild();
		return (build.getTestsFailed() + build.getTestsPassed()) > 0;
	}

	private void showJUnitView(final BuildDetails testResults, final String buildKey) {
		Display.getDefault().asyncExec(new Runnable() {
			public void run() {
				new ShowTestResultsExecution().execute(testResults, buildKey);
			}
		});
	}

	/**
	 * Execution of the ShowTestResultsAction. Seperate class since there are optional dependencies, which should get
	 * loaded if and only if the dependencies are met.
	 * 
	 * @author Thomas Ehrnhoefer
	 */
	private class ShowTestResultsExecution {

		public void execute(final BuildDetails testResults, final String buildKey) {
			IWorkbenchWindow activeWorkbenchWindow = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
			if (activeWorkbenchWindow == null) {
				StatusHandler.log(new Status(IStatus.ERROR, BambooUiPlugin.PLUGIN_ID,
						"Error opening JUnit View. No active workbench window."));
				return;
			}
			try {
				IViewPart testsView = activeWorkbenchWindow.getActivePage().showView(TestResultsView.ID);
				if (testsView != null && testsView instanceof TestResultsView) {
					((TestResultsView) testsView).setTestsResult(buildKey, testResults);
				}
			} catch (PartInitException e) {
				StatusHandler.log(new Status(IStatus.ERROR, BambooUiPlugin.PLUGIN_ID, "Error opening JUnit View", e));
				return;
			}
		}

	}

}
