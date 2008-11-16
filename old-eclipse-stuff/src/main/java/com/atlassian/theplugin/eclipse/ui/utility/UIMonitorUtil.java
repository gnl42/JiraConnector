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

package com.atlassian.theplugin.eclipse.ui.utility;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;

import com.atlassian.theplugin.eclipse.core.operation.IActionOperation;
import com.atlassian.theplugin.eclipse.preferences.Activator;

/**
 * UI Monitor Utility class
 * 
 * @author Alexander Gurov
 */
public final class UIMonitorUtil {
	public static final IOperationWrapperFactory DEFAULT_FACTORY = new DefaultOperationWrapperFactory();
	public static final IOperationWrapperFactory WORKSPACE_MODIFY_FACTORY = new WorkspaceModifyOperationWrapperFactory();

	public static void parallelSyncExec(Runnable runnable) {
		// requires additional investigation: is it possible to deadlock on UI
		// synch mutex here ?
		UIMonitorUtil.getDisplay().syncExec(runnable);
	}

	public static Display getDisplay() {
		Display retVal = Display.getCurrent();
		return retVal == null ? PlatformUI.getWorkbench().getDisplay() : retVal;
	}

	public static Shell getShell() {
		final Shell[] retVal = new Shell[1];
		final Display display = UIMonitorUtil.getDisplay();
		display.syncExec(new Runnable() {
			public void run() {
				retVal[0] = display.getActiveShell();
				while (retVal[0] != null && retVal[0].getParent() != null
						&& retVal[0].getParent() instanceof Shell) {
					retVal[0] = (Shell) retVal[0].getParent();
				}
				if (retVal[0] == null) {
					retVal[0] = new Shell(display, SWT.DIALOG_TRIM
							| SWT.APPLICATION_MODAL);
				}
			}
		});
		return retVal[0];
	}

	public static ICancellableOperationWrapper doTaskScheduledWorkspaceModify(
			IWorkbenchPart part, IActionOperation op) {
		return UIMonitorUtil.doTaskScheduled(part, op,
				UIMonitorUtil.WORKSPACE_MODIFY_FACTORY);
	}

	public static ICancellableOperationWrapper doTaskScheduledDefault(
			IWorkbenchPart part, IActionOperation op) {
		return UIMonitorUtil.doTaskScheduled(part, op,
				UIMonitorUtil.DEFAULT_FACTORY);
	}

	public static ICancellableOperationWrapper doTaskScheduled(
			IWorkbenchPart part, IActionOperation op,
			IOperationWrapperFactory factory) {
		ICancellableOperationWrapper runnable = factory.getCancellable(factory
				.getLogged(op));

		try {
			new SVNTeamOperationWrapper(part, runnable).run();
		} catch (InterruptedException e) {
			runnable.setCancelled(true);
		} catch (InvocationTargetException e) {
			throw new RuntimeException(e);
		}

		return runnable;
	}

	public static ICancellableOperationWrapper doTaskScheduledWorkspaceModify(
			IActionOperation op) {
		return UIMonitorUtil.doTaskScheduled(op,
				UIMonitorUtil.WORKSPACE_MODIFY_FACTORY);
	}

	public static ICancellableOperationWrapper doTaskScheduledDefault(
			IActionOperation op) {
		return UIMonitorUtil.doTaskScheduled(op, UIMonitorUtil.DEFAULT_FACTORY);
	}

	public static ICancellableOperationWrapper doTaskScheduledActive(
			IActionOperation op) {
		IWorkbenchPart activePart = UIMonitorUtil.getActivePart();
		if (activePart != null) {
			return UIMonitorUtil.doTaskScheduled(activePart, op,
					UIMonitorUtil.DEFAULT_FACTORY);
		}
		return UIMonitorUtil.doTaskScheduledDefault(op);
	}

	public static IWorkbenchPart getActivePart() {
		IWorkbenchPage activePage = UIMonitorUtil.getActivePage();
		return activePage == null ? null : activePage.getActivePart();
	}

	public static IWorkbenchPage getActivePage() {
		IWorkbenchWindow window = Activator.getDefault().getWorkbench()
				.getActiveWorkbenchWindow();
		if (window == null) {
			IWorkbenchWindow[] ws = Activator.getDefault().getWorkbench()
					.getWorkbenchWindows();
			window = ws != null && ws.length > 0 ? ws[0] : null;
		}
		return window == null ? null : window.getActivePage();
	}

	public static ICancellableOperationWrapper doTaskScheduled(
			IActionOperation op, IOperationWrapperFactory factory) {
		ICancellableOperationWrapper runnable = factory.getCancellable(factory
				.getLogged(op));

		new ScheduledOperationWrapper(runnable).schedule();

		return runnable;
	}

	public static ICancellableOperationWrapper doTaskNowWorkspaceModify(
			IActionOperation op, boolean cancellable) {
		return UIMonitorUtil.doTaskNowWorkspaceModify(UIMonitorUtil.getShell(),
				op, cancellable);
	}

	public static ICancellableOperationWrapper doTaskNowWorkspaceModify(
			Shell shell, IActionOperation op, boolean cancellable) {
		return UIMonitorUtil.doTaskNow(shell, op, cancellable,
				UIMonitorUtil.WORKSPACE_MODIFY_FACTORY);
	}

	public static ICancellableOperationWrapper doTaskNowDefault(
			IActionOperation op, boolean cancellable) {
		return UIMonitorUtil.doTaskNowDefault(UIMonitorUtil.getShell(), op,
				cancellable);
	}

	public static ICancellableOperationWrapper doTaskNowDefault(Shell shell,
			IActionOperation op, boolean cancellable) {
		return UIMonitorUtil.doTaskNow(shell, op, cancellable,
				UIMonitorUtil.DEFAULT_FACTORY);
	}

	public static ICancellableOperationWrapper doTaskNow(Shell shell,
			IActionOperation op, boolean cancellable,
			IOperationWrapperFactory factory) {
		ICancellableOperationWrapper runnable = factory.getCancellable(factory
				.getLogged(op));
		try {
			new ProgressMonitorDialog(shell).run(true, cancellable, runnable);
		} catch (RuntimeException ex) {
			throw ex;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		return runnable;
	}

	public static ICancellableOperationWrapper doTaskBusyDefault(
			IActionOperation op) {
		return UIMonitorUtil.doTaskBusy(op, UIMonitorUtil.DEFAULT_FACTORY);
	}

	public static ICancellableOperationWrapper doTaskBusyWorkspaceModify(
			IActionOperation op) {
		return UIMonitorUtil.doTaskBusy(op,
				UIMonitorUtil.WORKSPACE_MODIFY_FACTORY);
	}

	public static ICancellableOperationWrapper doTaskBusy(IActionOperation op,
			IOperationWrapperFactory factory) {
		final ICancellableOperationWrapper runnable = factory
				.getCancellable(factory.getLogged(op));
		BusyIndicator.showWhile(null, new Runnable() {
			public void run() {
				try {
					runnable.run(new NullProgressMonitor());
				} catch (InterruptedException e) {
					runnable.setCancelled(true);
				} catch (InvocationTargetException e) {
					throw new RuntimeException(e);
				}
			}
		});
		return runnable;
	}

	public static ICancellableOperationWrapper doTaskExternalDefault(
			IActionOperation op, IProgressMonitor monitor) {
		return UIMonitorUtil.doTaskExternal(op, monitor,
				UIMonitorUtil.DEFAULT_FACTORY);
	}

	public static ICancellableOperationWrapper doTaskExternalWorkspaceModify(
			IActionOperation op, IProgressMonitor monitor) {
		return UIMonitorUtil.doTaskExternal(op, monitor,
				UIMonitorUtil.WORKSPACE_MODIFY_FACTORY);
	}

	public static ICancellableOperationWrapper doTaskExternal(
			IActionOperation op, IProgressMonitor monitor,
			IOperationWrapperFactory factory) {
		if (monitor == null) {
			monitor = new NullProgressMonitor();
		}
		ICancellableOperationWrapper runnable = factory.getCancellable(factory
				.getLogged(op));
		try {
			runnable.run(monitor);
		} catch (InterruptedException e) {
			runnable.setCancelled(true);
		} catch (InvocationTargetException e) {
			throw new RuntimeException(e);
		}
		return runnable;
	}

	private UIMonitorUtil() {
	}

}
