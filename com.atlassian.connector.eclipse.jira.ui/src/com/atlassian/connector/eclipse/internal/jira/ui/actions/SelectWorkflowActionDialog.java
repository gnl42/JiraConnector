/*******************************************************************************
 * Copyright (c) 2012 Atlassian and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Atlassian - initial API and implementation
 ******************************************************************************/

package com.atlassian.connector.eclipse.internal.jira.ui.actions;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.mylyn.tasks.core.ITask;
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.eclipse.mylyn.tasks.core.data.TaskAttribute;
import org.eclipse.mylyn.tasks.core.data.TaskData;
import org.eclipse.mylyn.tasks.core.data.TaskOperation;
import org.eclipse.mylyn.tasks.ui.TasksUi;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

public class SelectWorkflowActionDialog extends MessageDialog {

	private final static String[] buttons = new String[] { IDialogConstants.OK_LABEL, IDialogConstants.CANCEL_LABEL };

	private final TaskRepository repository;

	private final TaskData taskData;

	private final boolean startWork;

	private final List<TaskOperation> operations;

	private String selectedAction = "-1";

	private Combo actionsComboBox;

	public SelectWorkflowActionDialog(Shell parentShell, TaskData taskData, ITask iTask, boolean startWork) {
		super(parentShell, NLS.bind((startWork ? Messages.SelectWorkflowAction_Start_Title
				: Messages.SelectWorkflowAction_Stop_Title), iTask.getTaskKey()), null, null, SWT.NONE, buttons, 0);
		this.taskData = taskData;
		this.startWork = startWork;
		repository = TasksUi.getRepositoryManager().getRepository(iTask.getConnectorKind(), iTask.getRepositoryUrl());
		TaskAttribute selectedOperationAttribute = taskData.getRoot().getMappedAttribute(TaskAttribute.OPERATION);
		operations = taskData.getAttributeMapper().getTaskOperations(selectedOperationAttribute);
	}

	@Override
	protected Point getInitialSize() {
		return new Point(350, 150);
	}

	@Override
	protected Control createMessageArea(Composite composite) {
		super.createMessageArea(composite);

		final Composite c1 = new Composite(composite, SWT.NONE);

		GridLayout gl = new GridLayout();
		gl.numColumns = 1;
		gl.horizontalSpacing = 10;
		gl.verticalSpacing = 10;
		gl.marginWidth = 0;
		c1.setLayout(gl);

		actionsComboBox = new Combo(c1, SWT.DROP_DOWN | SWT.READ_ONLY);
		List<String> items = new ArrayList<String>();
		for (TaskOperation operation : operations) {
			try {
				Integer.parseInt(operation.getOperationId());
				items.add(operation.getLabel());
			} catch (NumberFormatException e) {
				// there are some built-in Mylyn ops that don't have numeric IDs (like "leave unchanged" or whatever)
			}
		}
		actionsComboBox.setItems(items.toArray(new String[items.size()]));
		if (items.size() > 0) {
			actionsComboBox.select(0);
		}

		new Label(c1, SWT.NONE).setText(Messages.SelectWorkflowAction_Label);

		return composite;
	}

	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		super.createButtonsForButtonBar(parent);

		getButton(0).setEnabled(true);
	}

	@Override
	protected void buttonPressed(int buttonId) {
		if (buttonId == 0) {
			updateSelectedAction();
		}
		super.buttonPressed(buttonId);
	}

	private void updateSelectedAction() {
		int sel = actionsComboBox.getSelectionIndex();
		String val = actionsComboBox.getItem(sel);
		for (TaskOperation operation : operations) {
			if (val.equals(operation.getLabel())) {
				selectedAction = operation.getOperationId();
				break;
			}
		}
	}

	public String getSelectedAction() {
		return selectedAction;
	}
}
