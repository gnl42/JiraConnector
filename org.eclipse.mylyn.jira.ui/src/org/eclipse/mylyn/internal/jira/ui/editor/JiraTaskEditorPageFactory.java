/*******************************************************************************
 * Copyright (c) 2004, 2007 Mylyn project committers and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.mylyn.internal.jira.ui.editor;

import org.eclipse.mylyn.internal.jira.core.JiraCorePlugin;
import org.eclipse.mylyn.internal.provisional.commons.ui.CommonImages;
import org.eclipse.mylyn.internal.tasks.ui.TasksUiImages;
import org.eclipse.mylyn.tasks.ui.TasksUi;
import org.eclipse.mylyn.tasks.ui.editors.AbstractTaskEditorPageFactory;
import org.eclipse.mylyn.tasks.ui.editors.TaskEditor;
import org.eclipse.mylyn.tasks.ui.editors.TaskEditorInput;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.forms.editor.FormPage;

/**
 * @author Steffen Pingel
 */
public class JiraTaskEditorPageFactory extends AbstractTaskEditorPageFactory {

	@Override
	public boolean canCreatePageFor(TaskEditorInput input) {
		if (input.getTask().getConnectorKind().equals(JiraCorePlugin.CONNECTOR_KIND)) {
			return true;
		} else if (input.getData() instanceof JiraTaskInitializationData) {
			return true;
		}
		return TasksUi.getTaskDataManager().hasTaskData(input.getTask(), JiraCorePlugin.CONNECTOR_KIND);
	}

	@Override
	public FormPage createPage(TaskEditor parentEditor) {
		return new JiraTaskEditor2(parentEditor);
	}

	@Override
	public String[] getConflictingIds(TaskEditorInput input) {
		if (!input.getTask().getConnectorKind().equals(JiraCorePlugin.CONNECTOR_KIND)) {
			return new String[] { TasksUi.ID_PLANNING_PAGE };
		}
		return null;
	}

	@Override
	public Image getPageImage() {
		return CommonImages.getImage(TasksUiImages.REPOSITORY);
	}

	@Override
	public String getPageText() {
		return "JIRA";
	}

	@Override
	public int getPriority() {
		return PRIORITY_TASK;
	}

}