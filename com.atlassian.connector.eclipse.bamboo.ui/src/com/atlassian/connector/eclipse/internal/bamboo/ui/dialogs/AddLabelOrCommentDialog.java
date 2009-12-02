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

package com.atlassian.connector.eclipse.internal.bamboo.ui.dialogs;

import com.atlassian.connector.eclipse.internal.bamboo.core.BambooCorePlugin;
import com.atlassian.connector.eclipse.internal.bamboo.core.client.BambooClient;
import com.atlassian.connector.eclipse.internal.bamboo.ui.BambooUiPlugin;
import com.atlassian.connector.eclipse.ui.dialogs.ProgressDialog;
import com.atlassian.theplugin.commons.bamboo.BambooBuild;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.window.Window;
import org.eclipse.mylyn.commons.core.StatusHandler;
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.widgets.FormToolkit;

import java.lang.reflect.InvocationTargetException;

/**
 * Dialog for adding a label or a comment to a build.
 * 
 * @author Thomas Ehrnhoefer
 */
public class AddLabelOrCommentDialog extends ProgressDialog {

	public class AddLabelRunnable implements IRunnableWithProgress {
		public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
			monitor.beginTask("Adding label", IProgressMonitor.UNKNOWN);
			if (value.length() > 0) {
				BambooClient client = BambooCorePlugin.getRepositoryConnector().getClientManager().getClient(
						taskRepository);
				try {
					client.addLabelToBuild(monitor, taskRepository, build, value);
				} catch (CoreException e) {
					throw new InvocationTargetException(e);
				}
			}
		}
	}

	public class AddCommentRunnable implements IRunnableWithProgress {
		public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
			monitor.beginTask("Adding comment", IProgressMonitor.UNKNOWN);
			if (value.length() > 0) {
				BambooClient client = BambooCorePlugin.getRepositoryConnector().getClientManager().getClient(
						taskRepository);
				try {
					client.addCommentToBuild(monitor, taskRepository, build, value);
				} catch (CoreException e) {
					throw new InvocationTargetException(e);
				}
			}
		}
	}

	private final BambooBuild build;

	private final String shellTitle;

	private final TaskRepository taskRepository;

	private FormToolkit toolkit;

	private Text text;

	private Button okButton;

	private String value;

	private final Type type;

	public enum Type {
		LABEL, COMMENT;
	}

	public AddLabelOrCommentDialog(Shell parentShell, BambooBuild build, TaskRepository taskRepository, Type type) {
		super(parentShell);
		this.shellTitle = "Bamboo";
		this.build = build;
		this.taskRepository = taskRepository;
		this.type = type;
	}

	@Override
	protected Control createPageControls(Composite parent) {
		//CHECKSTYLE:MAGIC:OFF
		getShell().setText(shellTitle);
		setTitle("Bamboo Build");

		if (type == Type.LABEL) {
			setMessage(NLS.bind("Add a label to build {0}", build.getPlanKey()));
		} else {
			setMessage(NLS.bind("Add a comment to build {0}", build.getPlanKey()));
		}

		//CHECKSTYLE:MAGIC:OFF
		Composite composite = new Composite(parent, SWT.NONE);

		composite.setLayout(new GridLayout(1, false));

		if (toolkit == null) {
			toolkit = new FormToolkit(getShell().getDisplay());
		}
		parent.addDisposeListener(new DisposeListener() {
			public void widgetDisposed(DisposeEvent e) {
				if (toolkit != null) {
					toolkit.dispose();
				}
			}
		});

		GridData textGridData;
		switch (type) {
		case COMMENT:
			new Label(composite, SWT.NONE).setText("Comment:");
			text = new Text(composite, SWT.MULTI | SWT.WRAP | SWT.BORDER | SWT.V_SCROLL);
			textGridData = new GridData(GridData.GRAB_VERTICAL | GridData.VERTICAL_ALIGN_FILL
					| GridData.GRAB_HORIZONTAL | GridData.HORIZONTAL_ALIGN_FILL);
			textGridData.heightHint = 50;
			break;
		default:
			new Label(composite, SWT.NONE).setText("Label:");
			text = new Text(composite, SWT.SINGLE | SWT.BORDER);
			textGridData = new GridData(GridData.GRAB_HORIZONTAL | GridData.HORIZONTAL_ALIGN_FILL);
		}
		text.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				boolean enabled = false;
				if (text != null && text.getText().trim().length() > 0) {
					enabled = true;
				}
				if (okButton != null && !okButton.isDisposed()) {
					okButton.setEnabled(enabled);
				}
			}
		});
		text.setLayoutData(textGridData);
		text.forceFocus();

		//CHECKSTYLE:MAGIC:OFF
		((GridLayout) parent.getLayout()).makeColumnsEqualWidth = false;
		GridDataFactory.fillDefaults().grab(true, true).applyTo(composite);
		return composite;
		//CHECKSTYLE:MAGIC:ON
	}

	public void addLabel() {
		try {
			value = text.getText();
			setMessage("");
			run(true, false, new AddLabelRunnable());
		} catch (InvocationTargetException e) {
			StatusHandler.log(new Status(IStatus.ERROR, BambooUiPlugin.PLUGIN_ID, e.getCause().getMessage(),
					e.getCause()));
			setErrorMessage("Unable to add label to build.");
			return;
		} catch (InterruptedException e) {
			StatusHandler.log(new Status(IStatus.ERROR, BambooUiPlugin.PLUGIN_ID, e.getCause().getMessage(),
					e.getCause()));
			setErrorMessage("Unable to add label to build");
			return;
		}
		setReturnCode(Window.OK);
		close();
	}

	public void addComment() {
		try {
			value = text.getText();
			setMessage("");
			run(true, false, new AddCommentRunnable());
		} catch (InvocationTargetException e) {
			StatusHandler.log(new Status(IStatus.ERROR, BambooUiPlugin.PLUGIN_ID, e.getCause().getMessage(),
					e.getCause()));
			setErrorMessage("Unable to add comment to build.");
			return;
		} catch (InterruptedException e) {
			StatusHandler.log(new Status(IStatus.ERROR, BambooUiPlugin.PLUGIN_ID, e.getCause().getMessage(),
					e.getCause()));
			setErrorMessage("Unable to add comment to build");
			return;
		}
		setReturnCode(Window.OK);
		close();
	}

	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		if (type == Type.LABEL) {
			okButton = createButton(parent, IDialogConstants.CLIENT_ID + 1, "&Add Label", true);
		} else {
			okButton = createButton(parent, IDialogConstants.CLIENT_ID + 1, "&Add Comment", true);
		}
		okButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (type == Type.LABEL) {
					addLabel();
				} else {
					addComment();
				}
			}
		});
		okButton.setEnabled(false);
		createButton(parent, IDialogConstants.CANCEL_ID, IDialogConstants.CANCEL_LABEL, false).addSelectionListener(
				new SelectionAdapter() {
					@Override
					public void widgetSelected(SelectionEvent e) {
						cancelPressed();
					}
				});
	}

	public void cancelAddLabel() {
		setReturnCode(Window.CANCEL);
		close();
	}

	public TaskRepository getTaskRepository() {
		return taskRepository;
	}

	protected boolean isResizable() {
		return true;
	}

}
