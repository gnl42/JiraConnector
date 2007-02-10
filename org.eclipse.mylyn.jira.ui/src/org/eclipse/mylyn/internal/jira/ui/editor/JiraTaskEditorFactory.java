/*******************************************************************************
 * Copyright (c) 2004 - 2006 Mylar committers and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.mylar.internal.jira.ui.editor;

import org.eclipse.mylar.core.MylarStatusHandler;
import org.eclipse.mylar.internal.jira.ui.JiraTask;
import org.eclipse.mylar.internal.jira.ui.JiraUiPlugin;
import org.eclipse.mylar.tasks.core.ITask;
import org.eclipse.mylar.tasks.core.TaskRepository;
import org.eclipse.mylar.tasks.ui.TasksUiPlugin;
import org.eclipse.mylar.tasks.ui.editors.ITaskEditorFactory;
import org.eclipse.mylar.tasks.ui.editors.RepositoryTaskEditorInput;
import org.eclipse.mylar.tasks.ui.editors.TaskEditor;
import org.eclipse.mylar.tasks.ui.editors.TaskEditorInput;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;

/**
 * @author Mik Kersten
 */
public class JiraTaskEditorFactory implements ITaskEditorFactory {

	public boolean canCreateEditorFor(ITask task) {
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
		if (editorInput instanceof RepositoryTaskEditorInput  || editorInput instanceof TaskEditorInput) {
			return new JiraTaskEditor(parentEditor);
		} 
		return null;
	}

	public IEditorInput createEditorInput(ITask task) {
		JiraTask jiraTask = (JiraTask) task;
		TaskRepository repository = TasksUiPlugin.getRepositoryManager().getRepository(JiraUiPlugin.REPOSITORY_KIND,
				jiraTask.getRepositoryUrl());
		try {
			return new RepositoryTaskEditorInput(repository, jiraTask.getHandleIdentifier(), jiraTask.getTaskUrl());
		} catch (Exception e) {
			MylarStatusHandler.fail(e, "Could not create Trac editor input", true);
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
