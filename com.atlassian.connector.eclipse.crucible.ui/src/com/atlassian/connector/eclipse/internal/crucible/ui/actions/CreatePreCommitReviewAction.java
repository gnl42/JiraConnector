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
import com.atlassian.theplugin.commons.util.MiscUtil;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.wizard.IWizard;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.mylyn.internal.provisional.commons.ui.WorkbenchUtil;
import org.eclipse.mylyn.internal.tasks.ui.wizards.SelectRepositoryPage;
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.eclipse.team.internal.ui.actions.TeamAction;

import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.List;

/**
 * Action for creating a review from a revision
 * 
 * @author Thomas Ehrnhoefer
 */
@SuppressWarnings("restriction")
public class CreatePreCommitReviewAction extends TeamAction {
	public CreatePreCommitReviewAction() {
		super();
	}

	@Override
	protected void execute(IAction action) throws InvocationTargetException, InterruptedException {
		SelectRepositoryPage selectRepositoryPage = new SelectRepositoryPage(
				SelectCrucibleRepositoryPage.ENABLED_CRUCIBLE_REPOSITORY_FILTER) {
			@Override
			protected IWizard createWizard(TaskRepository taskRepository) {
				ReviewWizard wizard = new ReviewWizard(taskRepository,
						MiscUtil.buildHashSet(ReviewWizard.Type.ADD_WORKSPACE_PATCH));
				wizard.setRoots(Arrays.asList(getSelectedResources()));
				return wizard;
			}
		};

		List<TaskRepository> taskRepositories = selectRepositoryPage.getTaskRepositories();
		WizardDialog wd = null;
		if (taskRepositories.size() != 1) {
			wd = new WizardDialog(WorkbenchUtil.getShell(), new RepositorySelectionWizard(selectRepositoryPage));
		} else {
			ReviewWizard reviewWizard = new ReviewWizard(taskRepositories.get(0),
					MiscUtil.buildHashSet(ReviewWizard.Type.ADD_WORKSPACE_PATCH));
			reviewWizard.setRoots(Arrays.asList(getSelectedResources()));
			wd = new WizardDialog(WorkbenchUtil.getShell(), reviewWizard);
		}
		wd.setBlockOnOpen(true);
		wd.open();
	}
}
