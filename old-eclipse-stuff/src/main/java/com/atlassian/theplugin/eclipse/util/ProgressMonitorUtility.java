/*******************************************************************************
 * Copyright (c) 2005-2006 Polarion Software.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Alexander Gurov - Initial API and implementation
 *******************************************************************************/

package com.atlassian.theplugin.eclipse.util;

import java.text.MessageFormat;

import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;

import com.atlassian.theplugin.eclipse.core.operation.IActionOperation;
import com.atlassian.theplugin.eclipse.core.operation.IUnprotectedOperation;
import com.atlassian.theplugin.eclipse.core.operation.LoggedOperation;
import com.atlassian.theplugin.eclipse.preferences.Activator;

/**
 * Monitor management utility class
 * 
 * @author Alexander Gurov
 */
public final class ProgressMonitorUtility {
	public static final int TOTAL_WORK = 100;

	public static Job doTaskScheduledDefault(IActionOperation runnable) {
		return ProgressMonitorUtility.doTaskScheduledDefault(runnable, true);
	}

	/*
	 * public static Job doTaskScheduled(IActionOperation runnable) { return
	 * ProgressMonitorUtility.doTaskScheduled(runnable, true); }
	 */

	public static Job doTaskScheduledDefault(IActionOperation runnable,
			boolean system) {
		return ProgressMonitorUtility.doTaskScheduled(runnable,
				ILoggedOperationFactory.DEFAULT, system);
	}

	/*
	 * public static Job doTaskScheduled(IActionOperation runnable, boolean
	 * system) { return ProgressMonitorUtility.doTaskScheduled(runnable,
	 * SVNTeamPlugin.instance().getOptionProvider().getLoggedOperationFactory(),
	 * system); }
	 */

	public static Job doTaskScheduled(IActionOperation runnable,
			ILoggedOperationFactory factory, boolean system) {
		final IActionOperation logged = factory == null ? runnable : factory
				.getLogged(runnable);
		Job job = new Job(logged.getOperationName()) {
			protected IStatus run(IProgressMonitor monitor) {
				try {
					ResourcesPlugin
							.getWorkspace()
							.run(new IWorkspaceRunnable() {
								public void run(IProgressMonitor monitor)
										throws CoreException {
									ProgressMonitorUtility.doTaskExternal(
											logged, monitor, null);
								}
							}, this.getRule(), IWorkspace.AVOID_UPDATE, monitor);
				} catch (CoreException e) {
					LoggedOperation.reportError(Activator.getDefault()
							.getResource("Error.ScheduledTask"), e);
				}
				return Status.OK_STATUS;
			}
		};
		job.setRule(logged.getSchedulingRule());
		job.setSystem(system);
		job.schedule();
		return job;
	}

	public static void doTaskExternalDefault(IActionOperation runnable,
			IProgressMonitor monitor) {
		ProgressMonitorUtility.doTaskExternal(runnable, monitor,
				ILoggedOperationFactory.DEFAULT);
	}

	/*
	 * public static void doTaskExternal(IActionOperation runnable,
	 * IProgressMonitor monitor) {
	 * ProgressMonitorUtility.doTaskExternal(runnable, monitor,
	 * Activator.getDefault().getOptionProvider().getLoggedOperationFactory());
	 * }
	 */

	public static void doTaskExternal(IActionOperation runnable,
			IProgressMonitor monitor, ILoggedOperationFactory factory) {
		if (monitor == null) {
			monitor = new NullProgressMonitor();
		}
		monitor.beginTask(null, ProgressMonitorUtility.TOTAL_WORK);
		try {
			ProgressMonitorUtility.doTask(factory == null ? runnable : factory
					.getLogged(runnable), monitor, 1);
		} finally {
			monitor.done();
		}
	}

	public static void doTask(IActionOperation runnable,
			IProgressMonitor monitor, int subTasksCount) {
		if (subTasksCount > 0) {
			monitor = new SubProgressMonitorWithInfo(monitor,
					ProgressMonitorUtility.TOTAL_WORK / subTasksCount);
		}
		monitor.beginTask(runnable.getOperationName(),
				ProgressMonitorUtility.TOTAL_WORK);
		ProgressMonitorUtility.setTaskInfo(monitor, runnable, Activator
				.getDefault().getResource("Progress.Running"));
		try {
			runnable.run(monitor);
		} finally {
			ProgressMonitorUtility.setTaskInfo(monitor, runnable, Activator
					.getDefault().getResource("Progress.Done"));
			monitor.done();
		}
	}

	public static void doSubTask(IActionOperation runnable,
			IUnprotectedOperation op, IProgressMonitor monitor,
			int subTasksCount) throws Exception {
		if (subTasksCount > 0) {
			monitor = new SubProgressMonitorWithInfo(monitor,
					ProgressMonitorUtility.TOTAL_WORK / subTasksCount);
		}
		monitor.beginTask(runnable.getOperationName(),
				ProgressMonitorUtility.TOTAL_WORK);
		try {
			op.run(monitor);
		} finally {
			monitor.done();
		}
	}

	public static void progress(IProgressMonitor monitor, int current, int total) {
		if (monitor instanceof SubProgressMonitorWithInfo) {
			SubProgressMonitorWithInfo info = (SubProgressMonitorWithInfo) monitor;
			if (total != IProgressMonitor.UNKNOWN) {
				int real = ProgressMonitorUtility.TOTAL_WORK * current / total;
				real -= info.getCurrentProgress();
				info.worked(real);
			} else {
				info.unknownProgress(current);
			}
		} else {
			monitor.worked(1);
		}
	}

	public static void setTaskInfo(IProgressMonitor monitor,
			IActionOperation op, String subTask) {
		String message = Activator.getDefault().getResource("Progress.SubTask");
		monitor.subTask(MessageFormat.format(message,
				new Object[] {
						op.getOperationName(),
						subTask == null || subTask.length() == 0 ? Activator
								.getDefault().getResource("Progress.Running")
								: subTask }));
	}

	private ProgressMonitorUtility() {
	}

}
