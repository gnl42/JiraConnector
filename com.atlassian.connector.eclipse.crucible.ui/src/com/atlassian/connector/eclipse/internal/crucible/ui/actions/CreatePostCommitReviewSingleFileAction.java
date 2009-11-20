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

import com.atlassian.connector.eclipse.internal.crucible.ui.CrucibleUiPlugin;
import com.atlassian.connector.eclipse.internal.crucible.ui.wizards.RepositorySelectionWizard;
import com.atlassian.connector.eclipse.internal.crucible.ui.wizards.ReviewWizard;
import com.atlassian.connector.eclipse.internal.crucible.ui.wizards.SelectCrucibleRepositoryPage;
import com.atlassian.connector.eclipse.ui.team.RepositoryInfo;
import com.atlassian.connector.eclipse.ui.team.RevisionInfo;
import com.atlassian.connector.eclipse.ui.team.TeamUiUtils;
import com.atlassian.theplugin.commons.util.MiscUtil;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.wizard.IWizard;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.mylyn.commons.core.StatusHandler;
import org.eclipse.mylyn.internal.provisional.commons.ui.WorkbenchUtil;
import org.eclipse.mylyn.internal.tasks.ui.wizards.SelectRepositoryPage;
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.eclipse.team.internal.ui.actions.TeamAction;

import java.lang.reflect.InvocationTargetException;
import java.util.List;

// TODO jj check if we need to extend TeamAction

@SuppressWarnings("restriction")
public class CreatePostCommitReviewSingleFileAction extends TeamAction {

	public CreatePostCommitReviewSingleFileAction() {
		// ignore
	}

	@Override
	protected void execute(IAction action) throws InvocationTargetException, InterruptedException {
		IResource[] resources = getSelectedResources();

		// handle only single resource
		// TODO jj action should be enabled only for single selection (TeamAction overrides plugin.xml settings)

		if (resources != null && resources.length > 0 && resources[0] != null) {
			try {
				RevisionInfo revisionInfo = TeamUiUtils.getLocalRevision(resources[0]);
				RepositoryInfo repositoryInfo = TeamUiUtils.getApplicableRepository(resources[0]);

				if (revisionInfo != null && repositoryInfo != null) {
					openReviewWizard(revisionInfo, repositoryInfo);
				}

			} catch (CoreException e) {
				StatusHandler.log(new Status(IStatus.ERROR, CrucibleUiPlugin.PLUGIN_ID,
						"Cannot get local revision for selected resource", e));
			}
		}

	}

	private void openReviewWizard(final RevisionInfo revisionInfo, final RepositoryInfo repositoryInfo) {
		SelectRepositoryPage selectRepositoryPage = new SelectRepositoryPage(
				SelectCrucibleRepositoryPage.ENABLED_CRUCIBLE_REPOSITORY_FILTER) {
			@Override
			protected IWizard createWizard(TaskRepository taskRepository) {
				ReviewWizard wizard = new ReviewWizard(taskRepository,
						MiscUtil.buildHashSet(ReviewWizard.Type.ADD_SCM_FILE));
				wizard.setSelectedScmResource(revisionInfo, repositoryInfo);
				return wizard;
			}
		};

		List<TaskRepository> taskRepositories = selectRepositoryPage.getTaskRepositories();
		WizardDialog wd = null;
		if (taskRepositories.size() != 1) {
			wd = new WizardDialog(WorkbenchUtil.getShell(), new RepositorySelectionWizard(selectRepositoryPage));
		} else {
			ReviewWizard reviewWizard = new ReviewWizard(taskRepositories.get(0),
					MiscUtil.buildHashSet(ReviewWizard.Type.ADD_SCM_FILE));
			reviewWizard.setSelectedScmResource(revisionInfo, repositoryInfo);
			wd = new WizardDialog(WorkbenchUtil.getShell(), reviewWizard);
		}
		wd.setBlockOnOpen(true);
		wd.open();
	}

	// TODO jj improve enable/disable state

//	@Override
//	protected void setActionEnablement(IAction action) {
//		boolean enabled = false;
//		for (IResource resource : getSelectedResources()) {
//			if (enabledFor(resource)) {
//				enabled = true;
//			}
//		}
//		action.setEnabled(enabled);
//	}
//
//	private boolean enabledFor(IResource selected) {
//		ITeamResourceConnector connector = AtlassianUiPlugin.getDefault().getTeamResourceManager().getTeamConnector(
//				selected);
//
//		return (connector != null);
//	}

}
