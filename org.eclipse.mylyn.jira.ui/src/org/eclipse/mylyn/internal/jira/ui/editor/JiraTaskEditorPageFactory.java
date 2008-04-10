/*******************************************************************************
 * Copyright (c) 2004, 2007 Mylyn project committers and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.mylyn.internal.jira.ui.editor;

import org.eclipse.mylyn.internal.jira.core.JiraCorePlugin;
import org.eclipse.mylyn.internal.tasks.ui.TasksUiImages;
import org.eclipse.mylyn.tasks.core.RepositoryTaskData;
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
		if (input.getTaskRepository().getConnectorKind().equals(JiraCorePlugin.REPOSITORY_KIND)) {
			return true;
		}

		RepositoryTaskData taskData = input.getTaskData();
		return taskData != null && taskData.getConnectorKind().equals(JiraCorePlugin.REPOSITORY_KIND);
	}

	@Override
	public FormPage createPage(TaskEditor parentEditor) {
		return new JiraTaskEditor2(parentEditor);
	}

	@Override
	public Image getPageImage() {
		return TasksUiImages.getImage(TasksUiImages.REPOSITORY);
	}

	@Override
	public String getPageText() {
		return "JIRA";
	}

}