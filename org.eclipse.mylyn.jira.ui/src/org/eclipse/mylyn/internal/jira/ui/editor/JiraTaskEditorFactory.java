/*******************************************************************************
 * Copyright (c) 2004 - 2006 Mylar committers and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.mylyn.internal.jira.ui.editor;

import org.eclipse.mylyn.internal.jira.ui.JiraTask;
import org.eclipse.mylyn.internal.jira.ui.JiraUiPlugin;
import org.eclipse.mylyn.internal.monitor.core.util.StatusManager;
import org.eclipse.mylyn.tasks.core.AbstractTask;
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.eclipse.mylyn.tasks.ui.TasksUiPlugin;
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

	public boolean canCreateEditorFor(AbstractTask task) {
		return task instanceof JiraTask;
	}

	public boolean canCreateEditorFor(IEditorInput input) {
		if (input instanceof RepositoryTaskEditorInput) {
			RepositoryTaskEditorInput existingInput = (RepositoryTaskEditorInput) input;
			return existingInput.getTaskData() != null
					&& JiraUiPlugin.REPOSITORY_KIND.equals(existingInput.getRepository().getKind());
		} 
		return false;
	}

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

	public IEditorInput createEditorInput(AbstractTask task) {
		JiraTask jiraTask = (JiraTask) task;
		TaskRepository repository = TasksUiPlugin.getRepositoryManager().getRepository(JiraUiPlugin.REPOSITORY_KIND,
				jiraTask.getRepositoryUrl());
		try {
			return new RepositoryTaskEditorInput(repository, jiraTask.getTaskId(), jiraTask.getTaskUrl());
		} catch (Exception e) {
			StatusManager.fail(e, "Could not create JIRA editor input", true);
		}
		return null;
	}

	public String getTitle() {
		return "JIRA";
	}

	public boolean providesOutline() {
		return true;
	}
}
