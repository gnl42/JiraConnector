/*******************************************************************************
 * Copyright (c) 2009 Tasktop Technologies and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Tasktop Technologies - initial API and implementation
 *******************************************************************************/

package com.atlassian.connector.eclipse.internal.jira.ui.editor;

import org.eclipse.mylyn.internal.tasks.ui.editors.SingleSelectionAttributeEditor;
import org.eclipse.mylyn.internal.tasks.ui.editors.TaskEditorNewCommentPart;
import org.eclipse.mylyn.tasks.core.data.TaskAttribute;
import org.eclipse.mylyn.tasks.core.data.TaskDataModel;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.widgets.FormToolkit;

import com.atlassian.connector.eclipse.internal.jira.core.JiraAttribute;

@SuppressWarnings("restriction")
/**
 * @author Jacek Jaroczynski
 */
public class JiraNewCommentPart extends TaskEditorNewCommentPart {
	private final TaskDataModel taskDataModel;

	public JiraNewCommentPart(TaskDataModel taskDataModel) {
		this.taskDataModel = taskDataModel;
	}

	@Override
	public void createControl(Composite parent, FormToolkit toolkit) {
		super.createControl(parent, toolkit);

		TaskAttribute projectRoles = taskDataModel.getTaskData().getRoot().getAttribute(
				JiraAttribute.PROJECT_ROLES.id());

		if (projectRoles != null) {

			Composite composite = toolkit.createComposite(super.getComposite());
			GridLayout layout = new GridLayout(2, false);
			layout.marginWidth = 1;
			layout.horizontalSpacing = 10;
			composite.setLayout(layout);

			SingleSelectionAttributeEditor editor = new SingleSelectionAttributeEditor(taskDataModel, projectRoles);
			editor.createLabelControl(composite, toolkit);
			editor.createControl(composite, toolkit);

			toolkit.paintBordersFor(composite);
		}

	}

}
