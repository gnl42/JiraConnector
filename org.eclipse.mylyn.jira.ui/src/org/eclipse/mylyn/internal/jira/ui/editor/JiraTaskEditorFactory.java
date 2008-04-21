/*******************************************************************************
 * Copyright (c) 2004, 2007 Mylyn project committers and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.mylyn.internal.jira.ui.editor;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.mylyn.internal.jira.core.JiraCorePlugin;
import org.eclipse.mylyn.internal.jira.core.JiraTask;
import org.eclipse.mylyn.internal.jira.ui.JiraUiPlugin;
import org.eclipse.mylyn.monitor.core.StatusHandler;
import org.eclipse.mylyn.tasks.core.AbstractTask;
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.eclipse.mylyn.tasks.ui.TasksUi;
import org.eclipse.mylyn.tasks.ui.editors.AbstractTaskEditorFactory;
import org.eclipse.mylyn.tasks.ui.editors.RepositoryTaskEditorInput;
import org.eclipse.mylyn.tasks.ui.editors.TaskEditor;
import org.eclipse.mylyn.tasks.ui.editors.TaskEditorInput;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;

/**
 * @author Mik Kersten
 * @author Steffen Pingel
 */
public class JiraTaskEditorFactory extends AbstractTaskEditorFactory {

	@Override
	public boolean canCreateEditorFor(AbstractTask task) {
		return task instanceof JiraTask;
	}

	@Override
	public boolean canCreateEditorFor(IEditorInput input) {
		if (input instanceof RepositoryTaskEditorInput) {
			RepositoryTaskEditorInput existingInput = (RepositoryTaskEditorInput) input;
			return existingInput.getTaskData() != null
					&& JiraCorePlugin.CONNECTOR_KIND.equals(existingInput.getRepository().getConnectorKind());
		}
		return false;
	}

	@Override
	public IEditorPart createEditor(TaskEditor parentEditor, IEditorInput editorInput) {
		if (editorInput instanceof RepositoryTaskEditorInput) {
			RepositoryTaskEditorInput taskInput = (RepositoryTaskEditorInput) editorInput;
			if (taskInput.getTaskData().isNew()) {
				return new NewJiraTaskEditor(parentEditor);
			} else {
				return new JiraTaskEditor(parentEditor);
			}
		} else if (editorInput instanceof TaskEditorInput) {
			return new JiraTaskEditor(parentEditor);
		}
		return null;
	}

	@Override
	public IEditorInput createEditorInput(AbstractTask task) {
		JiraTask jiraTask = (JiraTask) task;
		TaskRepository repository = TasksUi.getRepositoryManager().getRepository(JiraCorePlugin.CONNECTOR_KIND,
				jiraTask.getRepositoryUrl());
		try {
			return new RepositoryTaskEditorInput(repository, jiraTask.getTaskId(), jiraTask.getUrl());
		} catch (Exception e) {
			StatusHandler.fail(new Status(IStatus.ERROR, JiraUiPlugin.ID_PLUGIN, "Could not create JIRA editor input",
					e));
		}
		return null;
	}

	@Override
	public String getTitle() {
		return "JIRA";
	}

	@Override
	public boolean providesOutline() {
		return true;
	}
}
