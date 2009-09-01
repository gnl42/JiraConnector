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

import com.atlassian.connector.eclipse.internal.crucible.ui.wizards.CrucibleReviewWizard;
import com.atlassian.connector.eclipse.internal.crucible.ui.wizards.RepositorySelectionWizard;
import com.atlassian.connector.eclipse.internal.crucible.ui.wizards.SelectCrucibleRepositoryPage;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.IWizard;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.mylyn.internal.provisional.commons.ui.WorkbenchUtil;
import org.eclipse.mylyn.internal.tasks.ui.wizards.SelectRepositoryPage;
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.eclipse.ui.IActionDelegate;
import org.eclipse.ui.actions.BaseSelectionListenerAction;

/**
 * Action for creating a review from a revision
 * 
 * @author Thomas Ehrnhoefer
 */
@SuppressWarnings("restriction")
public class CreatePreCommitReviewAction extends BaseSelectionListenerAction implements IActionDelegate {

	private ISelection selection;

	public CreatePreCommitReviewAction() {
		super("New Crucible Review...");
	}

	public void run(IAction action) {
		if (selection == null || selection.isEmpty() || !(selection instanceof IStructuredSelection)) {
			return;
		}

		WizardDialog wd = new WizardDialog(WorkbenchUtil.getShell(), new RepositorySelectionWizard(
				new SelectRepositoryPage(SelectCrucibleRepositoryPage.CRUCIBLE_REPOSITORY_FILTER) {
					@Override
					protected IWizard createWizard(TaskRepository taskRepository) {
						return new CrucibleReviewWizard(taskRepository);
					}
				}));
		wd.setBlockOnOpen(true);
		wd.open();
	}

	public void selectionChanged(IAction action, ISelection selected) {
		this.selection = selected;
	}

}
