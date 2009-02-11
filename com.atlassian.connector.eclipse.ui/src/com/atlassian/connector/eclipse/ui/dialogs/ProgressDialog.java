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

package com.atlassian.connector.eclipse.ui.dialogs;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.operation.ModalContext;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.wizard.ProgressMonitorPart;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.HashMap;

/**
 * Dialog that can display progress
 * 
 * @author Shawn Minto
 */
public abstract class ProgressDialog extends TitleAreaDialog {

	private boolean lockedUI = false;

	private Composite pageContainer;

	private Cursor waitCursor;

	private ProgressMonitorPart progressMonitorPart;

	private final SelectionAdapter cancelListener;

	private long activeRunningOperations = 0;

	private final HashMap<Integer, Button> buttons = new HashMap<Integer, Button>();

	public ProgressDialog(Shell parentShell) {
		super(parentShell);
		setDialogHelpAvailable(false);
		setHelpAvailable(false);
		cancelListener = new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				cancelPressed();
			}
		};

	}

	/*
	 * (non-Javadoc) Method declared on Dialog.
	 */
	@Override
	protected Control createDialogArea(Composite parent) {
		Composite composite = (Composite) super.createDialogArea(parent);
		// Build the Page container
		pageContainer = new Composite(composite, SWT.NULL);
		pageContainer.setLayout(new GridLayout());
		GridData gd = new GridData(GridData.FILL_BOTH | GridData.GRAB_HORIZONTAL | GridData.GRAB_VERTICAL);
		pageContainer.setLayoutData(gd);
		pageContainer.setFont(parent.getFont());
		createPageControls(pageContainer);

		// Insert a progress monitor
		GridLayout pmlayout = new GridLayout();
		pmlayout.numColumns = 1;
		progressMonitorPart = createProgressMonitorPart(composite, pmlayout);
		GridData gridData = new GridData(GridData.FILL_HORIZONTAL);
		progressMonitorPart.setLayoutData(gridData);
		progressMonitorPart.setVisible(true);
		// Build the separator line
		Label separator = new Label(parent, SWT.HORIZONTAL | SWT.SEPARATOR);
		separator.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		applyDialogFont(progressMonitorPart);
		return composite;
	}

	protected abstract Control createPageControls(Composite parent);

	protected Collection<? extends Control> getDisableableControls() {
		return buttons.values();
	}

	/**
	 * About to start a long running operation triggered through the wizard. Shows the progress monitor and disables the
	 * wizard's buttons and controls.
	 * 
	 * @param enableCancelButton
	 *            <code>true</code> if the Cancel button should be enabled, and <code>false</code> if it should be
	 *            disabled
	 * @return the saved UI state
	 */
	private void aboutToStart(boolean enableCancelButton) {
		if (getShell() != null) {
			// Save focus control
			Control focusControl = getShell().getDisplay().getFocusControl();
			if (focusControl != null && focusControl.getShell() != getShell()) {
				focusControl = null;
			}
			// Set the busy cursor to all shells.
			Display d = getShell().getDisplay();
			waitCursor = new Cursor(d, SWT.CURSOR_WAIT);
			setDisplayCursor(waitCursor);

			for (Control button : getDisableableControls()) {
				button.setEnabled(false);
			}

			progressMonitorPart.setVisible(true);
		}
	}

	/**
	 * A long running operation triggered through the wizard was stopped either by user input or by normal end. Hides
	 * the progress monitor and restores the enable state wizard's buttons and controls.
	 * 
	 * @param savedState
	 *            the saved UI state as returned by <code>aboutToStart</code>
	 * @see #aboutToStart
	 */
	private void stopped(Object savedState) {
		if (getShell() != null) {
			progressMonitorPart.setVisible(false);
			setDisplayCursor(null);
			waitCursor.dispose();
			waitCursor = null;

			for (Control button : getDisableableControls()) {
				button.setEnabled(true);
			}
		}
	}

	@Override
	protected void createButtonsForButtonBar(Composite parent) {
	}

	/**
	 * Create the progress monitor part in the receiver.
	 * 
	 * @param composite
	 * @param pmlayout
	 * @return ProgressMonitorPart
	 */
	protected ProgressMonitorPart createProgressMonitorPart(Composite composite, GridLayout pmlayout) {
		return new ProgressMonitorPart(composite, pmlayout, SWT.DEFAULT) {
			private String currentTask = null;

			@Override
			public void setBlocked(IStatus reason) {
				super.setBlocked(reason);
				if (!lockedUI) {
					getBlockedHandler().showBlocked(getShell(), this, reason, currentTask);
				}
			}

			@Override
			public void clearBlocked() {
				super.clearBlocked();
				if (!lockedUI) {
					getBlockedHandler().clearBlocked();
				}
			}

			@Override
			public void beginTask(String name, int totalWork) {
				super.beginTask(name, totalWork);
				currentTask = name;
			}

			@Override
			public void setTaskName(String name) {
				super.setTaskName(name);
				currentTask = name;
			}

			@Override
			public void subTask(String name) {
				super.subTask(name);
				if (currentTask == null) {
					currentTask = name;
				}
			}
		};
	}

	/**
	 * This implementation of IRunnableContext#run(boolean, boolean, IRunnableWithProgress) blocks until the runnable
	 * has been run, regardless of the value of <code>fork</code>. It is recommended that <code>fork</code> is set
	 * to true in most cases. If <code>fork</code> is set to <code>false</code>, the runnable will run in the UI
	 * thread and it is the runnable's responsibility to call <code>Display.readAndDispatch()</code> to ensure UI
	 * responsiveness.
	 * 
	 * UI state is saved prior to executing the long-running operation and is restored after the long-running operation
	 * completes executing. Any attempt to change the UI state of the wizard in the long-running operation will be
	 * nullified when original UI state is restored.
	 * 
	 */
	public void run(boolean fork, boolean cancelable, IRunnableWithProgress runnable) throws InvocationTargetException,
			InterruptedException {
		// The operation can only be canceled if it is executed in a separate
		// thread.
		// Otherwise the UI is blocked anyway.
		Object state = null;
		if (activeRunningOperations == 0) {
			aboutToStart(fork && cancelable);
		}
		activeRunningOperations++;
		try {
			if (!fork) {
				lockedUI = true;
			}
			ModalContext.run(runnable, fork, getProgressMonitor(), getShell().getDisplay());
			lockedUI = false;
		} finally {
			activeRunningOperations--;
			// Stop if this is the last one
			if (activeRunningOperations <= 0) {
				stopped(state);
			}
		}
	}

	/**
	 * Returns the progress monitor for this wizard dialog (if it has one).
	 * 
	 * @return the progress monitor, or <code>null</code> if this wizard dialog does not have one
	 */
	protected IProgressMonitor getProgressMonitor() {
		return progressMonitorPart;
	}

	/**
	 * Sets the given cursor for all shells currently active for this window's display.
	 * 
	 * @param c
	 *            the cursor
	 */
	private void setDisplayCursor(Cursor c) {
		Shell[] shells = getShell().getDisplay().getShells();
		for (Shell element : shells) {
			element.setCursor(c);
		}
	}

	@Override
	protected Button createButton(Composite parent, int id, String label, boolean defaultButton) {
		// increment the number of columns in the button bar
		((GridLayout) parent.getLayout()).numColumns++;
		Button button = new Button(parent, SWT.PUSH);
		button.setText(label);
		button.setFont(JFaceResources.getDialogFont());
		button.setData(new Integer(id));
		button.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent event) {
				buttonPressed(((Integer) event.widget.getData()).intValue());
			}
		});
		if (defaultButton) {
			Shell shell = parent.getShell();
			if (shell != null) {
				shell.setDefaultButton(button);
			}
		}
		buttons.put(new Integer(id), button);
		setButtonLayoutData(button);
		return button;
	}
}
