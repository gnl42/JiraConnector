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

import com.atlassian.connector.eclipse.internal.bamboo.core.BambooCorePlugin;
import com.atlassian.connector.eclipse.internal.bamboo.ui.BambooBuildAdapter;
import com.atlassian.connector.eclipse.internal.bamboo.ui.BambooUiUtil;
import com.atlassian.connector.eclipse.internal.bamboo.ui.operations.RetrieveFullBuildInfoJob;
import com.atlassian.theplugin.commons.BambooFileInfo;
import com.atlassian.theplugin.commons.bamboo.BambooBuild;
import com.atlassian.theplugin.commons.bamboo.BambooChangeSet;
import com.atlassian.theplugin.commons.bamboo.BuildDetails;
import com.atlassian.theplugin.commons.bamboo.BuildStatus;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.mylyn.commons.core.StatusHandler;
import org.eclipse.mylyn.tasks.core.TaskMapping;
import org.eclipse.mylyn.tasks.ui.TasksUi;
import org.eclipse.mylyn.tasks.ui.TasksUiImages;
import org.eclipse.mylyn.tasks.ui.TasksUiUtil;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.BaseSelectionListenerAction;

import java.util.Formatter;

/**
 * Create a task from failed build.
 * 
 * @author Pawel Niewiadomski
 */
public class NewTaskFromFailedBuildAction extends BaseSelectionListenerAction {

	public NewTaskFromFailedBuildAction() {
		super(null);
		initialize();
	}

	private void initialize() {
		setText("New Task From Failed Build...");
		setToolTipText("New Task From Failed Build...");
		setImageDescriptor(TasksUiImages.TASK_NEW);
	}

	@Override
	public void run() {
		ISelection s = super.getStructuredSelection();
		if (s instanceof IStructuredSelection) {
			IStructuredSelection selection = (IStructuredSelection) s;
			Object selected = selection.iterator().next();
			if (selected instanceof BambooBuildAdapter) {
				final BambooBuildAdapter build = (BambooBuildAdapter) selected;
				if (build != null) {
					downloadAndCreateNewTask(build.getBuild());
				}
			}
		}
	}

	@Override
	protected boolean updateSelection(IStructuredSelection selection) {
		if (selection.size() == 1) {
			try {
				BambooBuild build = ((BambooBuildAdapter) selection.getFirstElement()).getBuild();
				build.getNumber(); // check if this is a valid build, it'll throw exc otherwise
				return build.getStatus().equals(BuildStatus.FAILURE);
			} catch (UnsupportedOperationException e) {
				// ignore
			}
		}
		return false;
	}

	private String createBuildDescription(BambooBuild build, String buildLog, BuildDetails buildDetails) {
		// TODO NLS externalize strings
		final StringBuilder sb = new StringBuilder();
		Formatter fmt = new Formatter(sb);
		fmt.format("\n-- Build %s-%d failed, please investigate and fix...\n", build.getPlanKey(), build.getNumber());
		fmt.format("\n-- Build result: %s\n", build.getResultUrl());

		if (buildDetails != null) {
			if (buildDetails.getCommitInfo() != null) {
				sb.append("\n-- Code changes:\n\n");
				for (BambooChangeSet changeSet : buildDetails.getCommitInfo()) {
					fmt.format("[%s] %s\n", changeSet.getAuthor(),
							BambooUiUtil.getCommentSnippet(changeSet.getComment()));
					if (changeSet.getFiles() != null) {
						for (BambooFileInfo file : changeSet.getFiles()) {
							sb.append('\t');
							sb.append(file.getFileDescriptor().getAbsoluteUrl());
							sb.append('\n');
						}
					}
				}
			}

			sb.append("\n-- Tests:\n");
			fmt.format("\nTests in total: %d, failed tests: %d\n", build.getTestsFailed() + build.getTestsPassed(),
					build.getTestsFailed());

			if (buildDetails.getFailedTestDetails() != null && buildDetails.getFailedTestDetails().size() > 0) {
				sb.append('\n');
				sb.append(BambooUiUtil.getFailedTestsDescription(buildDetails));
				sb.append('\n');
			}
		}

		int errorLines = 0;
		String[] buildLogLines = buildLog == null ? new String[0] : buildLog.split("[\r\n]");
		StringBuilder errors = new StringBuilder();
		for (String buildLogLine : buildLogLines) {
			if (buildLogLine.startsWith(BambooUiUtil.LOG_STR_ERROR)) {
				String[] lineElements = buildLogLine.split("\t");
				if (errorLines > 0) {
					errors.append("\n");
				}
				//remove first 3 tokens (type, date, time)
				for (int i = 2; i < lineElements.length; i++) {
					errors.append(lineElements[i]);
				}
				errorLines++;
			}
		}

		fmt.format("\n-- Error lines:\n", errorLines);
		if (errorLines > 0) {
			sb.append('\n');
			sb.append(errors);
		} else {
			sb.append("\nBuild didn't generate any error lines.\n");
		}

		return sb.toString();
	}

	private void downloadAndCreateNewTask(final BambooBuild build) {
		final RetrieveFullBuildInfoJob job = new RetrieveFullBuildInfoJob(build, TasksUi.getRepositoryManager()
				.getRepository(BambooCorePlugin.CONNECTOR_KIND, build.getServerUrl()));
		job.setUser(true);
		job.setPriority(Job.INTERACTIVE);
		job.addJobChangeListener(new JobChangeAdapter() {
			@Override
			public void done(final IJobChangeEvent event) {
				IStatus status = job.getStatus();
				RetrieveFullBuildInfoJob infoJob = (RetrieveFullBuildInfoJob) event.getJob();

				if (infoJob.getBuildDetails() != null || infoJob.getBuildLog() != null) {
					final String description = createBuildDescription(build, infoJob.getBuildLog(),
							infoJob.getBuildDetails());

					Display.getDefault().asyncExec(new Runnable() {
						public void run() {
							Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
							TaskMapping taskMapping = new TaskMapping() {
								@Override
								public String getDescription() {
									return description;
								}

								@Override
								public String getSummary() {
									final StringBuilder sb = new StringBuilder();
									Formatter fmt = new Formatter(sb);
									fmt.format("Build %s-%d failed, please investigate and fix...", build.getPlanKey(),
											build.getNumber());
									return sb.toString();
								}

								@Override
								public String getTaskUrl() {
									return build.getBuildUrl();
								}
							};
							TasksUiUtil.openNewTaskEditor(shell, taskMapping, null);
						}
					});
				} else {
					StatusHandler.log(status);

					Display.getDefault().asyncExec(new Runnable() {
						public void run() {
							MessageDialog.openError(null, getText(), "Retrieving build details for "
									+ build.getPlanKey() + "-" + build.getNumber()
									+ " failed. See error log for details.");
						}
					});
				}
			}
		});
		job.schedule();
	}
}