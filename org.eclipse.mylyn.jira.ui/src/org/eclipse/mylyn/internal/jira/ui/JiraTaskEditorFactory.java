/*******************************************************************************
 * Copyright (c) 2006 - 2006 Mylar eclipse.org project and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Mylar project committers - initial API and implementation
 *******************************************************************************/

package org.eclipse.mylar.internal.jira.ui;

/**
 * @author Mik Kersten
 */
import org.eclipse.mylar.internal.jira.JiraTask;
import org.eclipse.mylar.internal.tasks.ui.ITaskEditorFactory;
import org.eclipse.mylar.internal.tasks.ui.editors.MylarTaskEditor;
import org.eclipse.mylar.tasks.core.ITask;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;

public class JiraTaskEditorFactory implements ITaskEditorFactory {

	public JiraTaskEditorFactory() {
	}

	public boolean canCreateEditorFor(ITask task) {
		return false;
		// return task instanceof JiraTask;
	}

	public IEditorPart createEditor(MylarTaskEditor parentEditor, IEditorInput editorInput) {
		return new JiraTaskEditor();
	}

	public IEditorInput createEditorInput(ITask task) {
		return new JiraIssueEditorInput((JiraTask) task);
	}

	public String getTitle() {
		return "Jira";
	}

	public void notifyEditorActivationChange(IEditorPart editor) {
	}

	public boolean providesOutline() {
		return true;
	}

	public boolean canCreateEditorFor(IEditorInput input) {
		return false;
	}
}
