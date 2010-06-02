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

package com.atlassian.connector.eclipse.internal.jira.ui.actions;

import org.eclipse.mylyn.tasks.core.ITask;
import org.eclipse.mylyn.tasks.core.data.TaskData;

import com.atlassian.connector.eclipse.internal.jira.ui.JiraImages;
import com.atlassian.connector.eclipse.internal.jira.ui.editor.JiraTaskEditorPage;

/**
 * @author Jacek Jaroczynski
 */
public class StartWorkEditorToolbarAction extends StartWorkAction {

	private static final String ID = "com.atlassian.connector.eclipse.internal.jira.ui.actions.StartWorkAction"; //$NON-NLS-1$

	private final JiraTaskEditorPage editorPage;

	public StartWorkEditorToolbarAction(final JiraTaskEditorPage editorPage) {
		super();
		this.editorPage = editorPage;
		setImageDescriptor(JiraImages.START_PROGRESS);
		setId(ID);

		update();
	}

	@Override
	public void run() {
		update();
		run(editorPage, editorPage.getModel().getTaskData(), editorPage.getModel().getTask());
	}

	private void update() {
		update(editorPage.getModel().getTaskData(), editorPage.getModel().getTask());
	}

	public void update(TaskData taskData, ITask task) {
		if (isTaskInProgress(taskData, task)) {
			setChecked(true);
			setToolTipText(Messages.StartWorkAction_stop);
		} else if (isTaskInStop(taskData, task)) {
			setChecked(false);
			setToolTipText(Messages.StartWorkAction_start);
		} else {
			setChecked(false);
			setEnabled(false);
			setToolTipText(Messages.StartWorkAction_start);
		}
	}
}
