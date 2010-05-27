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

import org.eclipse.mylyn.internal.tasks.core.data.ITaskDataManagerListener;
import org.eclipse.mylyn.internal.tasks.core.data.TaskDataManagerEvent;
import org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin;

import com.atlassian.connector.eclipse.internal.jira.ui.JiraImages;
import com.atlassian.connector.eclipse.internal.jira.ui.editor.JiraTaskEditorPage;

/**
 * @author Jacek Jaroczynski
 */
@SuppressWarnings("restriction")
public class StartWorkEditorToolbarAction extends StartWorkAction {

	private static final String ID = "com.atlassian.connector.eclipse.internal.jira.ui.actions.StartWorkAction"; //$NON-NLS-1$

	private final JiraTaskEditorPage editorPage;

	public StartWorkEditorToolbarAction(JiraTaskEditorPage editorPage) {
		super();
		this.editorPage = editorPage;
		setImageDescriptor(JiraImages.START_PROGRESS);
		setId(ID);

		update();

		TasksUiPlugin.getTaskDataManager().addListener(new ITaskDataManagerListener() {

			public void taskDataUpdated(TaskDataManagerEvent event) {
				update();
			}

			public void editsDiscarded(TaskDataManagerEvent event) {
				update();
			}
		});
	}

	@Override
	public void run() {
		update();
		doActionInsideEditor(editorPage, editorPage.getModel().getTaskData(), editorPage.getModel().getTask());
	}

	protected void update() {
		if (isTaskInProgress(editorPage.getModel().getTaskData(), editorPage.getModel().getTask())) {
			setChecked(true);
			setToolTipText(Messages.StartWorkAction_stop);
		} else if (isTaskInStop(editorPage.getModel().getTaskData(), editorPage.getModel().getTask())) {
			setChecked(false);
			setToolTipText(Messages.StartWorkAction_start);
		} else {
			setChecked(false);
			setEnabled(false);
			setToolTipText(Messages.StartWorkAction_start);
		}
	}
}
