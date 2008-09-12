/*******************************************************************************
* Copyright (c) 2004, 2008 Tasktop Technologies and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Tasktop Technologies - initial API and implementation
 *******************************************************************************/

package org.eclipse.mylyn.internal.jira.ui.editor;

import org.eclipse.mylyn.internal.jira.core.JiraCorePlugin;
import org.eclipse.mylyn.internal.provisional.commons.ui.CommonImages;
import org.eclipse.mylyn.tasks.ui.ITasksUiConstants;
import org.eclipse.mylyn.tasks.ui.TasksUiImages;
import org.eclipse.mylyn.tasks.ui.TasksUiUtil;
import org.eclipse.mylyn.tasks.ui.editors.AbstractTaskEditorPageFactory;
import org.eclipse.mylyn.tasks.ui.editors.TaskEditor;
import org.eclipse.mylyn.tasks.ui.editors.TaskEditorInput;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.forms.editor.IFormPage;

/**
 * @author Steffen Pingel
 */
public class JiraTaskEditorPageFactory extends AbstractTaskEditorPageFactory {

	@Override
	public boolean canCreatePageFor(TaskEditorInput input) {
		if (input.getTask().getConnectorKind().equals(JiraCorePlugin.CONNECTOR_KIND)) {
			return true;
		} else if (TasksUiUtil.isOutgoingNewTask(input.getTask(), JiraCorePlugin.CONNECTOR_KIND)) {
			return true;
		}
		return false;
	}

	@Override
	public IFormPage createPage(TaskEditor parentEditor) {
		return new JiraTaskEditorPage(parentEditor);
	}

	@Override
	public String[] getConflictingIds(TaskEditorInput input) {
		if (!input.getTask().getConnectorKind().equals(JiraCorePlugin.CONNECTOR_KIND)) {
			return new String[] { ITasksUiConstants.ID_PAGE_PLANNING };
		}
		return null;
	}

	@Override
	public Image getPageImage() {
		return CommonImages.getImage(TasksUiImages.REPOSITORY_SMALL);
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