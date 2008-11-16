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

package com.atlassian.theplugin.eclipse.ui.action;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.team.internal.ui.actions.TeamAction;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;

import com.atlassian.theplugin.eclipse.core.operation.AbstractNonLockingOperation;
import com.atlassian.theplugin.eclipse.core.operation.IActionOperation;
import com.atlassian.theplugin.eclipse.core.operation.LoggedOperation;
import com.atlassian.theplugin.eclipse.preferences.Activator;
import com.atlassian.theplugin.eclipse.ui.utility.DefaultOperationWrapperFactory;
import com.atlassian.theplugin.eclipse.ui.utility.ICancellableOperationWrapper;
import com.atlassian.theplugin.eclipse.ui.utility.IOperationWrapperFactory;
import com.atlassian.theplugin.eclipse.ui.utility.UIMonitorUtil;
import com.atlassian.theplugin.eclipse.util.ProgressMonitorUtility;

/**
 * This class implements operation running policies
 * 
 * @author Alexander Gurov
 */
@SuppressWarnings("restriction")
public abstract class AbstractAction extends TeamAction {
	// copy paste in order to fix problems with Eclipse 3.0.x->3.1.x->3.2 API
	// changes
	private IWorkbenchWindow window;
	private Shell shell;
	private ISelectionListener selectionListener = new ISelectionListener() {
		public void selectionChanged(IWorkbenchPart part, ISelection selection) {
			if (selection instanceof IStructuredSelection) {
				AbstractAction.this
						.checkSelection((IStructuredSelection) selection);
			}
		}
	};

	public AbstractAction() {
		super();
	}

	public final void run(final IAction action) {
		ProgressMonitorUtility.doTaskExternal(new AbstractNonLockingOperation(
				"Operation.CallMenuAction") {
			protected void runImpl(IProgressMonitor monitor) throws Exception {
				if (AbstractAction.this.isEnabled()) {
					AbstractAction.this.runImpl(action);
				}
			}
		}, new NullProgressMonitor(), null);
	}

	public abstract boolean isEnabled();

	public abstract void runImpl(IAction action);

	protected void execute(IAction action) throws InvocationTargetException,
			InterruptedException {
		// compatibility with 3.3
	}

	protected ICancellableOperationWrapper runBusy(IActionOperation operation) {
		return UIMonitorUtil.doTaskBusy(operation, this
				.getOperationWrapperFactory());
	}

	protected ICancellableOperationWrapper runNow(IActionOperation operation,
			boolean cancellable) {
		return UIMonitorUtil.doTaskNow(this.getShell(), operation, cancellable,
				this.getOperationWrapperFactory());
	}

	protected ICancellableOperationWrapper runScheduled(
			IActionOperation operation) {
		return UIMonitorUtil.doTaskScheduled(this.getTargetPart(), operation,
				this.getOperationWrapperFactory());
	}

	protected IOperationWrapperFactory getOperationWrapperFactory() {
		return new DefaultOperationWrapperFactory();
	}

	protected void handleException(Exception ex) {
		this.handle(ex, Activator.getDefault()
				.getResource("Error.ActionFailed"), Activator.getDefault()
				.getResource("Error.ActionFailed.Message"));
	}

	public void selectionChanged(IAction action, ISelection selection) {
		try {
			if (selection == null || selection.isEmpty()
					|| !(selection instanceof IStructuredSelection)) {
				action.setEnabled(false);
				return;
			}

			this.checkSelection((IStructuredSelection) selection);

			super.selectionChanged(action, selection);
		} catch (Throwable ex) {
			LoggedOperation.reportError(Activator.getDefault().getResource(
					"Error.MenuEnablement"), ex);
		}
	}

	@SuppressWarnings("unchecked")
	public Object[] getSelectedResources(Class c) {
		// This method is created in order to provide fix for Eclipse 3.1.0
		// where identical method is removed from TeamAction
		return TeamAction.getSelectedAdaptables(this.getSelection(), c);
	}

	protected Shell getShell() {
		return this.shell != null ? this.shell : super.getShell();
	}

	public IWorkbenchWindow getWindow() {
		return this.window;
	}

	public void init(IWorkbenchWindow window) {
		this.window = window;
		this.shell = this.window.getShell();
		this.window.getSelectionService().addPostSelectionListener(
				this.selectionListener);
	}

	public void dispose() {
		if (this.window != null) {
			this.window.getSelectionService().removePostSelectionListener(
					this.selectionListener);
		}
		super.dispose();
	}

	protected abstract void checkSelection(IStructuredSelection selection);

	protected abstract IStructuredSelection getSelection();

}
