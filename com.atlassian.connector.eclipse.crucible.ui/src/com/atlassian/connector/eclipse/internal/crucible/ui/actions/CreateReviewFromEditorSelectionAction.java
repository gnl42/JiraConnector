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

package com.atlassian.connector.eclipse.internal.crucible.ui.actions;

import com.atlassian.connector.eclipse.internal.crucible.ui.wizards.RepositorySelectionWizard;
import com.atlassian.connector.eclipse.internal.crucible.ui.wizards.ReviewWizard;
import com.atlassian.connector.eclipse.internal.crucible.ui.wizards.SelectCrucibleRepositoryPage;
import com.atlassian.theplugin.commons.crucible.api.UploadItem;
import com.atlassian.theplugin.commons.util.MiscUtil;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.text.TextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.wizard.IWizard;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.mylyn.internal.provisional.commons.ui.WorkbenchUtil;
import org.eclipse.mylyn.internal.tasks.ui.wizards.SelectRepositoryPage;
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.eclipse.ui.IEditorActionDelegate;
import org.eclipse.ui.IEditorPart;

import java.util.Arrays;
import java.util.List;

@SuppressWarnings("restriction")
public class CreateReviewFromEditorSelectionAction implements IEditorActionDelegate {

	private IEditorPart editor;

	private TextSelection selection;

	public CreateReviewFromEditorSelectionAction() {
	}

	public void setActiveEditor(IAction action, IEditorPart targetEditor) {
		this.editor = targetEditor;
		action.setEnabled(false);
	}

	public void run(IAction action) {
		SelectRepositoryPage selectRepositoryPage = new SelectRepositoryPage(
				SelectCrucibleRepositoryPage.ENABLED_CRUCIBLE_REPOSITORY_FILTER) {
			@Override
			protected IWizard createWizard(TaskRepository taskRepository) {
				ReviewWizard wizard = new ReviewWizard(taskRepository,
						MiscUtil.buildHashSet(ReviewWizard.Type.ADD_UPLOAD_ITEMS));
				wizard.setUploadItems(Arrays.asList(new UploadItem("partial_selection_of_"
						+ editor.getEditorInput().getName(), new byte[0], selection.getText().getBytes())));
				return wizard;
			}
		};

		List<TaskRepository> taskRepositories = selectRepositoryPage.getTaskRepositories();
		WizardDialog wd = null;
		if (taskRepositories.size() != 1) {
			wd = new WizardDialog(WorkbenchUtil.getShell(), new RepositorySelectionWizard(selectRepositoryPage));
		} else {
			ReviewWizard reviewWizard = new ReviewWizard(taskRepositories.get(0),
					MiscUtil.buildHashSet(ReviewWizard.Type.ADD_UPLOAD_ITEMS));
			reviewWizard.setUploadItems(Arrays.asList(new UploadItem(editor.getEditorInput().getName(), new byte[0],
					selection.getText().getBytes())));
			wd = new WizardDialog(WorkbenchUtil.getShell(), reviewWizard);
		}
		wd.setBlockOnOpen(true);
		wd.open();
	}

	public void selectionChanged(IAction action, ISelection selection) {
		if (selection instanceof TextSelection) {
			TextSelection sel = (TextSelection) selection;
			action.setEnabled(sel.getLength() > 0);
			this.selection = sel;
		}
	}

}
